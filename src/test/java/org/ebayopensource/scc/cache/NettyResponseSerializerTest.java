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

import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import org.junit.Test;

public class NettyResponseSerializerTest {

	@Test
	public void testSerialize() {
		NettyResponseSerializer serializer = new NettyResponseSerializer();

		DefaultFullHttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
		CacheResponse cacheRes = serializer.serialize(response);
		assertNotNull(cacheRes);
		assertEquals(HttpVersion.HTTP_1_0.toString(),
				cacheRes.getProtocalVersion());
		assertEquals(HttpResponseStatus.OK.code(), cacheRes.getCode());
		assertEquals(0, cacheRes.getContent().length);
		assertTrue(cacheRes.getHeaders().get(0).getKey()
				.equals("Content-Length"));
		assertTrue(cacheRes.getTrailingHeaders().isEmpty());

		ByteBuf content = new EmptyByteBuf(ByteBufAllocator.DEFAULT);
		response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0,
				HttpResponseStatus.OK, content);
		cacheRes = serializer.serialize(response);
		assertEquals(0, cacheRes.getContent().length);

		content = UnpooledByteBufAllocator.DEFAULT.buffer();
		content.writeBytes("Hello, World!".getBytes());
		response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0,
				HttpResponseStatus.OK, content);
		cacheRes = serializer.serialize(response);
		assertEquals("Hello, World!", new String(cacheRes.getContent()));

		HttpHeaders headers = response.headers();
		headers.add("header1", "value1");

		HttpHeaders trailingHeaders = response.trailingHeaders();
		trailingHeaders.add("tHeader2", "value2");

		cacheRes = serializer.serialize(response);
		Entry<String, String> header = cacheRes.getHeaders().get(0);
		assertEquals("header1", header.getKey());
		assertEquals("value1", header.getValue());

		Entry<String, String> tHeader = cacheRes.getTrailingHeaders().get(0);
		assertEquals("tHeader2", tHeader.getKey());
		assertEquals("value2", tHeader.getValue());
	}

	@Test
	public void testGetType(){
		assertEquals(FullHttpResponse.class, new NettyResponseSerializer().getType());
	}
}
