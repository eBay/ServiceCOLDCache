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

import static org.junit.Assert.*;

import org.junit.Test;

import static org.mockito.Mockito.*;

public class CacheManagerTest {

	@Test
	public void testPut() {
		ICache cache = mock(ICache.class);
		ISerializer serializer = mock(ISerializer.class);
		when(serializer.getType()).thenReturn(Object.class);
		IDeserializer deserializer = mock(IDeserializer.class);

		CacheManager<Object, Object, Object> cm = new CacheManager<>(cache,
				new ISerializer[] {serializer}, deserializer);

		Object serializedResult = new Object();
		Object response = new Object();
		when(serializer.serialize(response)).thenReturn(serializedResult);

		cm.put("ck1", response);
	}

	@Test
	public void testGet() {
		ICache cache = mock(ICache.class);
		ISerializer serializer = mock(ISerializer.class);
		IDeserializer deserializer = mock(IDeserializer.class);

		CacheManager<Object, Object, Object> cm = new CacheManager<>(cache,
				new ISerializer[] {serializer}, deserializer);

		assertNull(cm.get("anything"));
		assertEquals(1, cm.getStats().getMissedCount());

		Object cacheObj = new Object();
		when(cache.get("key1")).thenReturn(cacheObj);
		Object deserializedObj = new Object();
		when(deserializer.deserialize(cacheObj)).thenReturn(deserializedObj);
		
		assertEquals(deserializedObj, cm.get("key1"));
		assertEquals(1, cm.getStats().getHitCount());
	}
	
	@Test
	public void test(){
		ICache cache = mock(ICache.class);
		ISerializer serializer = mock(ISerializer.class);
		IDeserializer deserializer = mock(IDeserializer.class);
		CacheManager<Object, Object, Object> cm = new CacheManager<>(cache,
				new ISerializer[] {serializer}, deserializer);
		
		assertEquals(cache, cm.getCache());
		cm.cleanupCache();
	}
	
}
