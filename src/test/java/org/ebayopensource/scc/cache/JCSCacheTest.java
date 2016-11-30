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

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.ebayopensource.scc.config.AppConfiguration;
import org.ebayopensource.scc.config.ConfigLoader;

public class JCSCacheTest {

	private static final String TEST_USER_CONFIG="./src/test/resources/testuserconfig.json";

	private Properties m_cacheProps;
	@Before
	public void setUp() throws IOException {
		m_cacheProps = loadTestCacheConfigFile();
	}

	@Test
	public void testInit() throws IOException {
		AppConfiguration appConfig = new AppConfiguration(new ConfigLoader(),
				TEST_USER_CONFIG);
		appConfig.init();
		JCSCache cache = JCSCache.getInstance();
		assertNull(JCSCache.getCompositeCacheManager());
		assertFalse(cache.isInitialized());
		cache.init(appConfig, m_cacheProps, Executors.newScheduledThreadPool(1));
		assertTrue(cache.isInitialized());
		assertNotNull(JCSCache.getCompositeCacheManager());
		//init again
		cache.init(appConfig, m_cacheProps, Executors.newScheduledThreadPool(1));

		cache.shutdownCache();
	}

	@Test
	public void test() throws IOException, InterruptedException{
		JCSCache cache = JCSCache.getInstance();
		cache.put("anykey", Mockito.mock(CacheResponse.class));
		assertNull(cache.get("anykey"));

		AppConfiguration appConfig = new AppConfiguration(new ConfigLoader(),
				TEST_USER_CONFIG);
		appConfig.init();
		appConfig.put(JCSCache.KEY_SAVE_CHECK_INTERVAL, 100);
		cache.init(appConfig, m_cacheProps, Executors.newScheduledThreadPool(1));

		CacheResponse cacheResp = Mockito.mock(CacheResponse.class);
		cache.put("key", cacheResp);
		assertEquals(cacheResp, cache.get("key"));
		Thread.sleep(400);

		cache.cleanup();
		assertNull(cache.get("key"));

		cache.shutdownCache();
	}

	private Properties loadTestCacheConfigFile() throws IOException {
		Properties properties = new Properties();
		properties.load(getClass().getClassLoader().getResourceAsStream("cache.ccf"));
		return properties;
	}

}
