/*******************************************************************************
 * Copyright (c) 2016 eBay Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.ebayopensource.scc.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CacheManager<T, R, C> {

	private ICache<C> m_cache;
	private Map<Class<?>, ISerializer<C, ?>> m_serializers;
	private IDeserializer<R, C> m_deserializer;
	private CacheStats m_cacheStats;

	public CacheManager(ICache<C> cache, ISerializer<C, ?>[] serializers,
			IDeserializer<R, C> de) {
		m_cache = cache;
		m_deserializer = de;

		m_serializers = new HashMap<>();
		for (ISerializer<C, ?> se : serializers) {
			m_serializers.put(se.getType(), se);
		}

		m_cacheStats = new CacheStats();
	}

	public void put(String key, Object resp) {
		Class<? extends Object> targetClz = resp.getClass();
		ISerializer<C, ?> serializer = m_serializers.get(targetClz);
		if(serializer == null){
			Set<Entry<Class<?>, ISerializer<C, ?>>> entrySet = m_serializers.entrySet();
			for (Entry<Class<?>, ISerializer<C, ?>> entry : entrySet) {
				if(entry.getKey().isAssignableFrom(resp.getClass())){
					serializer = entry.getValue();
					break;
				}
			}
			
		}
		if (serializer != null) {
			C cacheObj = serializer.serialize(resp);
			m_cache.put(key, cacheObj);
		}
	}

	public R get(String key) {
		C cacheObj = m_cache.get(key);
		if (cacheObj != null) {
			m_cacheStats.incrementHitCount();
			R deserializedResult = m_deserializer.deserialize(cacheObj);

			return deserializedResult;
		}
		m_cacheStats.incrementMissedCount();
		return null;
	}

	public CacheStats getStats() {
		return m_cacheStats;
	}

	public ICache<C> getCache() {
		return m_cache;
	}

	public void cleanupCache() {
		m_cache.cleanup();
	}
}
