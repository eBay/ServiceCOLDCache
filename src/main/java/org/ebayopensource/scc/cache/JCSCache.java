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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.access.exception.CacheException;
import org.apache.commons.jcs.auxiliary.AuxiliaryCache;
import org.apache.commons.jcs.auxiliary.disk.indexed.IndexedDiskCache;
import org.apache.commons.jcs.engine.control.CompositeCache;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;
import org.codehaus.plexus.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ebayopensource.scc.config.AppConfiguration;

public class JCSCache implements ICache<CacheResponse> {

	private static final String KEY_PREFIX = "jcsCache.";
	protected static final String KEY_SAVE_CHECK_INTERVAL = KEY_PREFIX
			+ "saveCheck.interval";
	public static final String KEY_CONFIG_FILE = KEY_PREFIX + "configFile";
	private static final Logger LOGGER = LoggerFactory
			.getLogger(JCSCache.class);

	private static CompositeCacheManager s_ccm;
	private static IndexedDiskCache<String, CacheResponse> s_idc;
	private static CompositeCache<String, CacheResponse> s_cache;
	private static CacheAccess<String, CacheResponse> s_cacheAccess;

	private static JCSCache s_instance = new JCSCache();

	public static JCSCache getInstance() {
		return s_instance;
	}

	private static IndexedDiskCache<String, CacheResponse> getIndexDiskCache(
			CompositeCache<String, CacheResponse> cache) {
		try {
			Object fieldValue = ReflectionUtils.getValueIncludingSuperclasses(
					"auxCaches", cache);
			if (fieldValue == null || !(fieldValue instanceof AuxiliaryCache[])) {
				return null;
			}
			@SuppressWarnings("unchecked")
			AuxiliaryCache<String, CacheResponse>[] auxCaches = (AuxiliaryCache[]) fieldValue;
			if (auxCaches.length == 0
					|| !(auxCaches[0] instanceof IndexedDiskCache)) {
				return null;
			}
			return (IndexedDiskCache<String, CacheResponse>) auxCaches[0];
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// do nothing
		}
		return null;
	}

	private static void saveKeys() throws NoSuchMethodException,
			SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		if (s_idc == null) {
			return;
		}
		Method method = s_idc.getClass().getDeclaredMethod("saveKeys");
		if (method == null) {
			return;
		}
		method.setAccessible(true);
		method.invoke(s_idc);
	}

	private AtomicLong m_lastAccess = new AtomicLong(0);
	private AppConfiguration m_appConfig;
	private ScheduledExecutorService m_scheduledService;
	private volatile boolean m_isInitialized = false;
	private volatile int m_lastCacheSize = 0;

	private JCSCache() {
	}

	public boolean isInitialized() {
		return m_isInitialized;
	}

	public void init(AppConfiguration appConfig, Properties cacheProps,
			ScheduledExecutorService scheduledService) {
		m_appConfig = appConfig;
		synchronized (JCSCache.class) {
			s_ccm = CompositeCacheManager.getUnconfiguredInstance();
			s_ccm.configure(cacheProps);
			s_cache = s_ccm.getCache("default");
			s_idc = getIndexDiskCache(s_cache);
			s_cacheAccess = new CacheAccess<>(s_cache);
		}
		m_scheduledService = scheduledService;
		launchSaveCacheThread();
		m_isInitialized = true;
	}

	private void launchSaveCacheThread() {
		final int saveCheckInterval = m_appConfig
				.getInt(KEY_SAVE_CHECK_INTERVAL);
		Runnable runnable = new Runnable() {

			public void run() {
				try {
					doSave();
				} catch (Exception e) {
					LOGGER.warn(e.getMessage(), e);
				}

			}

			private void doSave() throws NoSuchMethodException,
					SecurityException, IllegalAccessException,
					IllegalArgumentException, InvocationTargetException {
				long last = JCSCache.this.m_lastAccess.get();
				if (last != 0
						&& (System.currentTimeMillis() - last > saveCheckInterval)) {
					int size = getMemoryCacheSize();
					if (size > m_lastCacheSize) {
						synchronized (s_cacheAccess) {
							s_cache.save();
							saveKeys();
							LOGGER.info("All cache flushs to disk.");
						}
						m_lastCacheSize = size;
					} else if (size < m_lastCacheSize) {
						m_lastCacheSize = size;
					}
				}
			}

			private int getMemoryCacheSize() {
				return s_cache.getMemoryCache().getSize();
			}
		};
		m_scheduledService.scheduleAtFixedRate(runnable, 0, saveCheckInterval,
				TimeUnit.SECONDS);
	}

	@Override
	public CacheResponse get(String key) {
		CacheResponse response = null;
		if (m_isInitialized) {
			m_lastAccess.set(System.currentTimeMillis());
			synchronized (s_cacheAccess) {
				response = (CacheResponse) s_cacheAccess.get(key);
			}
		}
		return response;
	}

	@Override
	public void put(String key, CacheResponse resp) {
		if (m_isInitialized && resp != null) {
			try {
				synchronized (s_cacheAccess) {
					s_cacheAccess.put(key, resp);
				}
			} catch (CacheException e) {
				LOGGER.warn(e.getMessage(), e);
			}
		}
	}

	public void shutdownCache() {
		if (s_ccm != null) {
			s_cache.save();
			s_ccm.shutDown();
		}
		synchronized (JCSCache.class) {
			s_cache = null;
			s_idc = null;
			s_ccm = null;
			s_cacheAccess = null;
		}
		m_isInitialized = false;
	}

	public static CompositeCacheManager getCompositeCacheManager() {
		return s_ccm;
	}

	@Override
	public void cleanup() {
		s_cacheAccess.clear();
	}
}
