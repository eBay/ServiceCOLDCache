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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;

import org.ebayopensource.scc.config.AppConfiguration;
import org.ebayopensource.scc.config.ConfigLoader;
import org.junit.BeforeClass;
import org.junit.Test;

public class RequestKeyGeneratorTest {

	private static AppConfiguration s_appConfig;

	@BeforeClass
	public static void before() throws IOException {
		s_appConfig = new AppConfiguration(new ConfigLoader(), null);
		s_appConfig.init();
	}

	@Test
	public void test() {
		RequestKeyGenerator keyGen = new RequestKeyGenerator(
				s_appConfig);
		DefaultFullHttpRequest req1 = new DefaultFullHttpRequest(
				HttpVersion.HTTP_1_0, HttpMethod.GET, "http://s.ebay.com/v1/s1");
		DefaultFullHttpRequest req2 = new DefaultFullHttpRequest(
				HttpVersion.HTTP_1_0, HttpMethod.GET, "http://s.ebay.com/v1/s1");
		assertEquals(keyGen.generateKey(req1), keyGen.generateKey(req2));

		req2 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0,
				HttpMethod.POST, "http://s.ebay.com/v1/s1");
		assertNotEquals(keyGen.generateKey(req1), keyGen.generateKey(req2));

		req2 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
				"http://s.ebay.com/v1/s1");
		assertEquals(keyGen.generateKey(req1), keyGen.generateKey(req2));

		req2 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET,
				"http://s.ebay.com/v1/s2");
		assertNotEquals(keyGen.generateKey(req1), keyGen.generateKey(req2));

		req2 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET,
				"http://s.ebay.com/v1/s1");
		req1.headers().add("header1", "value1");
		req2.headers().add("header1", "value1");
		assertEquals(keyGen.generateKey(req1), keyGen.generateKey(req2));

		req2 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET,
				"http://s.ebay.com/v1/s1");
		req2.headers().add("header1", "value2");
		assertNotEquals(keyGen.generateKey(req1), keyGen.generateKey(req2));

		req2 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET,
				"http://s.ebay.com/v1/s1");
		req2.headers().add("header1", "value1");
		req2.trailingHeaders().add("header1", "value1");
		assertEquals(keyGen.generateKey(req1), keyGen.generateKey(req2));

		req2.headers().add("Date", "idvalue");
		assertEquals(keyGen.generateKey(req1), keyGen.generateKey(req2));
	}

	@Test
	public void testHttpsReq() {
		RequestKeyGenerator keyGen = new RequestKeyGenerator(
				s_appConfig);
		DefaultFullHttpRequest req1 = new DefaultFullHttpRequest(
				HttpVersion.HTTP_1_0, HttpMethod.GET, "/v1/s1");

		assertTrue(keyGen.generateKey(req1).startsWith("/v1/s1"));

		HttpHeaders.setHost(req1, "s.ebay.com");
		assertTrue(keyGen.generateKey(req1).startsWith(
				"https://s.ebay.com/v1/s1"));
	}

	@Test
	public void testURIMatchOnly() throws IOException {
		AppConfiguration appConfig = new AppConfiguration(new ConfigLoader(),
				"./src/test/resources/testuserconfig.json");
		appConfig.init();
		RequestKeyGenerator keyGen = new RequestKeyGenerator(appConfig);

		ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
		buffer.writeBytes("{\"fromDate\":1464251112185,\"toDate\":1464337512185}"
				.getBytes());
		DefaultFullHttpRequest req1 = new DefaultFullHttpRequest(
				HttpVersion.HTTP_1_0, HttpMethod.GET,
				"http://test.ebay.com/v1/s1", buffer);

		String key1 = keyGen.generateKey(req1);

		buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
		buffer.writeBytes("{\"fromDate\":1464251113750,\"toDate\":1464337513750}"
				.getBytes());
		DefaultFullHttpRequest req2 = new DefaultFullHttpRequest(
				HttpVersion.HTTP_1_0, HttpMethod.GET,
				"http://test.ebay.com/v1/s1", buffer);

		String key2 = keyGen.generateKey(req2);
		assertEquals(key1, key2);

		HttpHeaders.setContentLength(req2, 758);
		key2 = keyGen.generateKey(req2);
		assertEquals(key1, key2);

		appConfig.put("uriMatchOnly", null);
		keyGen = new RequestKeyGenerator(appConfig);
		key1 = keyGen.generateKey(req1);
		key2 = keyGen.generateKey(req2);

		assertNotEquals(key1, key2);
	}
}
