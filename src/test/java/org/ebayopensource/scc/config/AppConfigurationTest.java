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

import org.junit.Before;
import org.junit.Test;

public class AppConfigurationTest {
	
	private AppConfiguration m_appConfig;

	@Before
	public void before() throws IOException{
		m_appConfig = new AppConfiguration(new ConfigLoader(), "./src/test/resources/testuserconfig.json");
		m_appConfig.init();
	}

	@Test
	public void testGetConfig() {
		assertNotNull(m_appConfig.getConfig());
	}

	@Test
	public void testGetDouble() {
		assertEquals(5000, m_appConfig.getDouble("maxCount"), 0.1);
	}

	@Test
	public void testGetBoolean() {
		assertTrue(m_appConfig.getBoolean("enableCache"));
	}

	@Test
	public void testIsCacheEnabled() {
		assertTrue(m_appConfig.isCacheEnabled());
	}

	@Test
	public void testGetProxyPort() {
		assertEquals(32876, m_appConfig.getProxyPort());
	}

	@Test
	public void testGetDebugPort() {
		assertEquals(32877, m_appConfig.getDebugPort());
	}

	@Test
	public void testGetAdminPort() {
		assertEquals(55321, m_appConfig.getAdminPort());
	}

	@Test
	public void testGetAppInfo() {
		assertTrue(m_appConfig.getAppName().length() > 0);
		assertTrue(m_appConfig.getAppVersion().length() > 0);
	}
}
