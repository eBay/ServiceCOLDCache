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
package org.ebayopensource.scc.impl;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.ebayopensource.scc.cache.IKeyGenerator;
import org.ebayopensource.scc.cache.JCSCache;
import org.ebayopensource.scc.cache.policy.DefaultPolicyManager;
import org.ebayopensource.scc.config.AppConfiguration;
import org.ebayopensource.scc.config.Config;
import com.google.gson.Gson;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JCSCache.class)
public class DefaultPolicyManagerTest {
	private Map<String, Object> m_configMap = new HashMap<>();
	private AppConfiguration m_appConfigMock;
	private IKeyGenerator<FullHttpRequest> m_keyGenMock;

	private JCSCache m_jcsCacheMock;

	@Before
	public void setUp() throws Exception {
		Config config = Mockito.mock(Config.class);
		Mockito.when(config.asMap()).thenReturn(m_configMap);

		m_appConfigMock = Mockito.mock(AppConfiguration.class);
		Mockito.when(m_appConfigMock.getConfig()).thenReturn(config);
		Mockito.when(m_appConfigMock.getString(JCSCache.KEY_CONFIG_FILE))
				.thenReturn("./cache.ccf");

		m_keyGenMock = Mockito.mock(IKeyGenerator.class);

		m_jcsCacheMock = Mockito.mock(JCSCache.class);
		PowerMockito.mockStatic(JCSCache.class);
		PowerMockito.when(JCSCache.getInstance()).thenReturn(m_jcsCacheMock);
	}

	@Test
	public void testMergeGlobalPolicyJsonConfigIntoJCSProps() {
		String jsonConfig = "{" + "\"maxCount\": \"123\","
				+ "\"timeToLive\": 123456," + "\"update\" : \"LRU\","
				+ "\"cacheDir\" : \"./cache\"" + "}";
		m_configMap.clear();
		m_configMap.putAll(new Gson().fromJson(jsonConfig, Map.class));

		new DefaultPolicyManager(m_appConfigMock, m_keyGenMock,
				Executors.newScheduledThreadPool(1));

		ArgumentCaptor<AppConfiguration> appConfigArgCaptor = ArgumentCaptor
				.forClass(AppConfiguration.class);
		ArgumentCaptor<Properties> mergedPropsArg = ArgumentCaptor
				.forClass(Properties.class);
		ArgumentCaptor<ScheduledExecutorService> scheduledServiceCaptor = ArgumentCaptor
				.forClass(ScheduledExecutorService.class);
		Mockito.verify(m_jcsCacheMock).init(appConfigArgCaptor.capture(),
				mergedPropsArg.capture(), scheduledServiceCaptor.capture());

		Properties mergedProps = mergedPropsArg.getValue();
		assertThat(mergedProps, is(notNullValue()));
		mergedProps.getProperty("jcs.default.cacheattributes.MaxObjects");
		assertThat(
				mergedProps
						.getProperty("jcs.default.cacheattributes.MaxObjects"),
				is("123"));
		assertThat(
				mergedProps
						.getProperty("jcs.default.elementattributes.MaxLife"),
				is("123456"));
		assertThat(
				mergedProps
						.getProperty("jcs.default.cacheattributes.MemoryCacheName"),
				is("org.apache.commons.jcs.engine.memory.lru.LRUMemoryCache"));
		assertThat(
				mergedProps.getProperty("jcs.auxiliary.DC.attributes.DiskPath"),
				is("./cache"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testJsonConfigWithInvalidJCSClassName() {
		String jsonConfig = "{" + "\"update\" : \"INVALID_ALG\"" + "}";
		m_configMap.clear();
		m_configMap.putAll(new Gson().fromJson(jsonConfig, Map.class));

		new DefaultPolicyManager(m_appConfigMock, m_keyGenMock,
				Executors.newScheduledThreadPool(1));
	}

}
