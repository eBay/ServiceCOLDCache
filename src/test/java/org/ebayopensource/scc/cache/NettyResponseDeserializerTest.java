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
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.nio.charset.Charset;
import java.util.ArrayList;

import org.junit.Test;

import org.ebayopensource.scc.cache.CacheResponse.CacheEntry;

public class NettyResponseDeserializerTest {

	@Test
	public void testDeserialize() {
		NettyResponseDeserializer deserializer = new NettyResponseDeserializer();

		CacheResponse cacheResponse = new CacheResponse(
				HttpVersion.HTTP_1_1.toString(), 404,
				HttpResponseStatus.NOT_FOUND.reasonPhrase(),
				new ArrayList<CacheEntry<String, String>>(),
				new ArrayList<CacheEntry<String, String>>());

		FullHttpResponse response = deserializer.deserialize(cacheResponse);

		assertNotNull(response);
		assertEquals(HttpVersion.HTTP_1_1, response.getProtocolVersion());
		assertEquals(HttpResponseStatus.NOT_FOUND, response.getStatus());
		assertEquals(0, response.content().readableBytes());
		assertTrue(response.headers().isEmpty());
		assertTrue(response.trailingHeaders().isEmpty());

		cacheResponse.setContent("Hello, world!".getBytes());
		cacheResponse.getHeaders().add(new CacheEntry<>("header1", "value1"));
		cacheResponse.getTrailingHeaders().add(
				new CacheEntry<>("tHeader1", "value2"));

		response = deserializer.deserialize(cacheResponse);

		assertEquals("Hello, world!",
				response.content().toString(Charset.forName("UTF-8")));
		assertEquals("value1", response.headers().get("header1"));
		assertEquals("value2", response.trailingHeaders().get("tHeader1"));
	}
}
