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
package org.ebayopensource.io.netty.http.snoop;

import java.net.URI;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class HttpSnoopClientTest {

	private static Thread server;
	private static Thread client;

	@BeforeClass
	public static void setUp() throws Exception {
		server = new Thread() {
			public void run() {
				try {
					new HttpSnoopServer(55321).run();
				} catch (Exception e1) {
					Assert.fail("Failed to start server:" + e1.getMessage());
					e1.printStackTrace();

				}
			}
		};

		server.start();
		System.out.println("server is started");
		Thread.sleep(5000);
	}

	@Test
	public void testProxyClient() {
		client = new Thread() {
			public void run() {
				String action = "disableCache";
				URI uri;

				try {
					uri = new URI("http://127.0.0.1:55321/cmd/" + action + "?"
							+ "param1=value1");
					new HttpSnoopClient(uri).run();
				} catch (Exception e) {
					Assert.fail("Failed to send http request, root cause is:"
							+ e.getMessage());
					e.printStackTrace();
				}
			}
		};
		client.interrupt();
	}

	@Test 
	public void testRemoveCacheDirectory(){
		HttpSnoopClient.removeCacheDirectory("./cache");
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		Thread.sleep(1000);

		server.interrupt();
		/*
		 * while (server.isAlive()) { Thread.sleep(250); }
		 */
	}
}
