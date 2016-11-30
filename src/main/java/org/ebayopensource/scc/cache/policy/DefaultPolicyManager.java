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
package org.ebayopensource.scc.cache.policy;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ebayopensource.scc.cache.CacheManager;
import org.ebayopensource.scc.cache.CacheResponse;
import org.ebayopensource.scc.cache.ChunkResponsesSerializer;
import org.ebayopensource.scc.cache.IKeyGenerator;
import org.ebayopensource.scc.cache.ISerializer;
import org.ebayopensource.scc.cache.JCSCache;
import org.ebayopensource.scc.cache.NettyResponseDeserializer;
import org.ebayopensource.scc.cache.NettyResponseSerializer;
import org.ebayopensource.scc.config.AppConfiguration;

public class DefaultPolicyManager extends
		PolicyManager<FullHttpRequest, FullHttpResponse, CacheResponse> {
	private static final Logger LOG = LoggerFactory
			.getLogger(PolicyManager.class);

	private final static Map<String, String> CACHE_CLASS_NAME_MAPPING;
	static {
		Map<String, String> mapping = new HashMap<>();
		mapping.put("LRU",
				"org.apache.commons.jcs.engine.memory.lru.LRUMemoryCache");
		CACHE_CLASS_NAME_MAPPING = Collections.unmodifiableMap(mapping);
	}

	public DefaultPolicyManager(AppConfiguration appConfig,
			IKeyGenerator<FullHttpRequest> keyGen,
			ScheduledExecutorService scheduledService) {
		super(appConfig, keyGen, scheduledService);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected CacheManager<FullHttpRequest, FullHttpResponse, CacheResponse> initCacheManager() {
		ISerializer<CacheResponse, ?>[] serializers = new ISerializer[] {
				new NettyResponseSerializer(), new ChunkResponsesSerializer() };
		NettyResponseDeserializer nettyResponseDeserializer = new NettyResponseDeserializer();
		configureJCSCache();
		return new CacheManager<FullHttpRequest, FullHttpResponse, CacheResponse>(
				JCSCache.getInstance(), serializers, nettyResponseDeserializer);
	}

	private void configureJCSCache() {
		Properties builtInJCSProps = loadDefaultJCSConfig();

		mergeGlobalPolicyIntoBuiltinJCSProps(builtInJCSProps);
		mergeDeclaredPoliciesIntoBuiltinJCSProps(builtInJCSProps);

		JCSCache cache = JCSCache.getInstance();
		cache.init(m_appConfig, builtInJCSProps, m_scheduledService);
	}

	private void mergeGlobalPolicyIntoBuiltinJCSProps(Properties props) {
		mergeJsonPropsIntoJCSProps("default", m_globalPolicy.getConfig(), props);
	}

	private void mergeDeclaredPoliciesIntoBuiltinJCSProps(Properties props) {
		// TODO for supporting declared declaredPolicies
	}

	private Properties loadDefaultJCSConfig() {
		Properties ccfFileProps = new Properties();
		try {
			InputStream ins = Thread
					.currentThread()
					.getContextClassLoader()
					.getResourceAsStream(
							m_appConfig.getString(JCSCache.KEY_CONFIG_FILE));
			if (ins != null) {
				ccfFileProps.load(ins);
			} else {
				LOG.info("Can not found default cache.ccf properties, just return empty properties");
			}
			return ccfFileProps;
		} catch (IOException e) {
			String msg = "Exception threw while trying to load builtin JCS Config File! "
					+ e.getMessage();
			LOG.error(msg, e);
			throw new RuntimeException(msg, e);
		}

	}

	private void mergeJsonPropsIntoJCSProps(String regionName,
			Map<String, Object> jsonConfig, Properties builtinJCSProps) {
		if (!"default".equals(regionName)) {
			regionName = "region." + regionName;
		}

		Object maxCount = jsonConfig.get("maxCount");
		if (maxCount != null) {
			// MaxObjects for items in memory
			builtinJCSProps.put("jcs." + regionName
					+ ".cacheattributes.MaxObjects", intStringValue(maxCount));
			// MaxKeySize for items in disk
			builtinJCSProps.put("jcs.auxiliary.DC.attributes.MaxKeySize",
					intStringValue(maxCount));
		}
		Object timeToLive = jsonConfig.get("timeToLive");
		if (timeToLive != null) {
			builtinJCSProps.put("jcs." + regionName
					+ ".elementattributes.MaxLife", intStringValue(timeToLive));
		}
		Object update = jsonConfig.get("update");
		if (update != null) {
			if (CACHE_CLASS_NAME_MAPPING.containsKey(update.toString()
					.toUpperCase())) {
				String cacheNameClassName = CACHE_CLASS_NAME_MAPPING.get(update
						.toString().toUpperCase());
				builtinJCSProps.put("jcs." + regionName
						+ ".cacheattributes.MemoryCacheName",
						cacheNameClassName);
			} else {
				String msg = "Property [update] has an invalid value: \""
						+ update + "\", the value must be one of: "
						+ CACHE_CLASS_NAME_MAPPING.keySet();
				LOG.error(msg);
				throw new IllegalArgumentException(msg);
			}
		}
		Object cacheDir = jsonConfig.get("cacheDir");
		if (cacheDir != null) {
			builtinJCSProps.put("jcs.auxiliary.DC.attributes.DiskPath",
					cacheDir);
		}

	}

	private String intStringValue(Object val) {
		if (val instanceof Number) {
			return String.valueOf(((Number) val).intValue());
		} else if (val == null) {
			return "";
		}
		return val.toString();
	}
}
