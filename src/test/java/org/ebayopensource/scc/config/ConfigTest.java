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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class ConfigTest {

	@Test
	public void testHashCode() {
		assertNotNull(new Config(new HashMap<String, Object>()).hashCode());
	}

	@Test
	public void test() {
		HashMap<String, Object> configs = new HashMap<String, Object>();
		Config config = new Config(configs);
		assertTrue(config.getNames().isEmpty());

		config.setConfig("key", "v");

		Map<String, Object> asMap = config.asMap();
		assertNotNull(asMap);
		assertEquals("v", asMap.get("key"));

		Set<String> names = config.getNames();
		assertEquals("key", names.toArray()[0]);
	}

	@Test
	public void testEqualsObject() {
		Config config = new Config(new HashMap<String, Object>());
		assertFalse(config.equals(new Object()));
		
		Config config2 = new Config(new HashMap<String, Object>());
		assertTrue(config.equals(config2));
		
		config.setConfig("k", "v");
		assertFalse(config.equals(config2));
		assertFalse(config.equals(config2.asMap()));
		
		config2.setConfig("k", "v");
		assertTrue(config.equals(config2));
		assertTrue(config.equals(config2.asMap()));
		
		config2.setConfig("k", 'v');
		assertFalse(config.equals(config2));
		assertFalse(config.equals(config2.asMap()));
		
		
	}

}
