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
package org.ebayopensource.scc.track;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.Executors;

import org.apache.http.HttpHost;
import org.junit.Test;

import org.ebayopensource.scc.config.AppConfiguration;

public class TrackerClientTest {

	@Test
	public void test() throws InterruptedException {
		AppConfiguration config = mock(AppConfiguration.class);
		when(config.getString("trackerClient.endpoint.host")).thenReturn(
				"127.0.0.1");
		when(config.getInt("trackerClient.endpoint.port")).thenReturn(8080);
		when(config.getString("trackerClient.endpoint.path")).thenReturn(
				"/build-service/webapi/serviceCache/tracker");
		when(config.getBoolean("trackerClient.endpoint.isSecure")).thenReturn(
				false);
		when(config.getInt("trackerClient.request.interval")).thenReturn(10000);

		TrackerClient tc = new TrackerClient(config,
				Executors.newScheduledThreadPool(1));
		tc.start();
		Thread.sleep(1000);
	}

	@Test
	public void testInit() {
		AppConfiguration config = mock(AppConfiguration.class);
		when(config.getString("trackerClient.endpoint.host")).thenReturn(
				"127.0.0.1");
		when(config.getInt("trackerClient.endpoint.port")).thenReturn(8080);
		when(config.getString("trackerClient.endpoint.path")).thenReturn(
				"/build-service/webapi/serviceCache/tracker");
		when(config.getBoolean("trackerClient.endpoint.isSecure")).thenReturn(
				false);

		TrackerClient tc = new TrackerClient(config,
				Executors.newScheduledThreadPool(1));
		HttpHost httpHost = tc.createHttpHost();
		assertEquals(8080, httpHost.getPort());
		assertEquals("http", httpHost.getSchemeName());

		when(config.getInt("trackerClient.endpoint.port")).thenReturn(null);
		tc = new TrackerClient(config, Executors.newScheduledThreadPool(1));
		httpHost = tc.createHttpHost();
		assertEquals(80, httpHost.getPort());

		when(config.getBoolean("trackerClient.endpoint.isSecure")).thenReturn(
				true);
		tc = new TrackerClient(config, Executors.newScheduledThreadPool(1));
		httpHost = tc.createHttpHost();
		assertEquals(443, httpHost.getPort());

		when(config.getBoolean("trackerClient.endpoint.isSecure")).thenReturn(
				true);
		when(config.getInt("trackerClient.endpoint.port")).thenReturn(96542);
		tc = new TrackerClient(config, Executors.newScheduledThreadPool(1));
		httpHost = tc.createHttpHost();
		assertEquals(96542, httpHost.getPort());
	}

	@Test
	public void testGet() {
		AppConfiguration config = mock(AppConfiguration.class);
		when(config.getString("trackerClient.endpoint.host")).thenReturn(
				"127.0.0.1");
		when(config.getInt("trackerClient.endpoint.port")).thenReturn(8080);
		when(config.getString("trackerClient.endpoint.path")).thenReturn(
				"/build-service/webapi/serviceCache/tracker");
		when(config.getBoolean("trackerClient.endpoint.isSecure")).thenReturn(
				false);

		TrackerClient tc = new TrackerClient(config,
				Executors.newScheduledThreadPool(1));

		assertNotNull(tc.getIp());
	}
}
