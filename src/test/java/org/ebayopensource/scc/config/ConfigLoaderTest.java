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
package org.ebayopensource.scc.config;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class ConfigLoaderTest {

	@Test
	public void testLoadStringString() throws IOException {
		ConfigLoader configLoader = new ConfigLoader();
		Config config = configLoader.load(AppConfiguration.DEFAULT_GLOBAL_CONFIG_PATH,
				AppConfiguration.DEFAULT_USER_CONFIG_PATH);
		assertEquals(5000, config.getInt("maxCount").intValue());
		assertEquals(259200, config.getInt("timeToLive").intValue());
		
		config = configLoader.load(AppConfiguration.DEFAULT_GLOBAL_CONFIG_PATH,
				"./notexisteduserconfig.json");
		assertEquals(5000, config.getInt("maxCount").intValue());
		assertEquals(259200, config.getInt("timeToLive").intValue());

		config = configLoader.load(AppConfiguration.DEFAULT_GLOBAL_CONFIG_PATH,
				"./src/test/resources/testuserconfig.json");
		assertEquals(100, config.getInt("timeToLive").intValue());

		assertEquals("./testcache", config.getString("cacheDir"));
		assertEquals(5000, config.getDouble("maxCount").doubleValue(), 0.1);

		Config excludeConfig = config.getSubConfig("excludes");
		assertNull(excludeConfig);

		List<Object> excludes = config.getList("excludes");
		assertEquals(3, excludes.size());

		Map<String, Object> object = (Map<String, Object>) excludes.get(0);
		Config firstPatternConfig = ConfigLoader.load(object);
		assertEquals("POST", firstPatternConfig.getString("method"));
		assertEquals("http://test1.ebay.com/v1/s1", firstPatternConfig.getString("pattern"));
		
		
		config = configLoader.load(AppConfiguration.DEFAULT_GLOBAL_CONFIG_PATH,
				"./src/test/resources/wrongformmatuserconfig.json");
		assertEquals(259200, config.getInt("timeToLive").intValue());
		
		String version = configLoader.loadVersion();
		assertNotNull(version);
		System.out.println(version);
		
	}

}
