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
package org.ebayopensource.scc.filter;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.io.IOException;

import org.junit.Test;

import org.ebayopensource.scc.cache.CacheManager;
import org.ebayopensource.scc.cache.policy.CacheDecisionObject;
import org.ebayopensource.scc.cache.policy.PolicyManager;
import org.ebayopensource.scc.config.AppConfiguration;
import org.ebayopensource.scc.config.ConfigLoader;

public class NettyRequestProxyFilterTest {

	@Test
	public void testFilterRequest() throws IOException {
		AppConfiguration appConfig = new AppConfiguration(new ConfigLoader(),
				null);
		appConfig.init();

		PolicyManager policyManager = mock(PolicyManager.class);
		NettyRequestProxyFilter filter = new NettyRequestProxyFilter(
				policyManager, appConfig);

		ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
		when(ctx.attr(any(AttributeKey.class))).thenReturn(
				mock(Attribute.class));
		assertNull(filter.filterRequest(mock(HttpRequest.class), ctx));

		DefaultFullHttpRequest req = new DefaultFullHttpRequest(
				HttpVersion.HTTP_1_1, HttpMethod.GET, "http://test.ebay.com/s/");
		when(policyManager.cacheIsNeededFor(any(CacheDecisionObject.class)))
				.thenReturn(false);
		assertNull(filter.filterRequest(req, ctx));

		when(policyManager.cacheIsNeededFor(any(CacheDecisionObject.class)))
				.thenReturn(true);
		CacheManager cm = mock(CacheManager.class);
		when(policyManager.getCacheManager()).thenReturn(cm);
		assertNull(filter.filterRequest(req, ctx));

		FullHttpResponse resp = mock(FullHttpResponse.class);
		HttpHeaders respHeaders = mock(HttpHeaders.class);
		when(resp.headers()).thenReturn(respHeaders);
		when(respHeaders.get(any(CharSequence.class))).thenReturn("100");
		when(cm.get(anyString())).thenReturn(resp);
		Channel channel = mock(Channel.class);
		SocketChannelConfig config = mock(SocketChannelConfig.class);
		when(channel.config()).thenReturn(config);
		when(ctx.channel()).thenReturn(channel);
		req.headers().add("h1", "v1");

		when(resp.content()).thenReturn(
				new EmptyByteBuf(new PooledByteBufAllocator())).thenReturn(
				Unpooled.copiedBuffer("Hello".getBytes()));
		assertEquals(resp, filter.filterRequest(req, ctx));
		assertEquals(resp, filter.filterRequest(req, ctx));
	}

	@Test
	public void testHandleNonProxyRequest() throws IOException {
		AppConfiguration appConfig = new AppConfiguration(new ConfigLoader(),
				null);
		appConfig.init();
		PolicyManager policyManager = mock(PolicyManager.class);
		NettyRequestProxyFilter filter = new NettyRequestProxyFilter(
				policyManager, appConfig);

		ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
		when(ctx.attr(any(AttributeKey.class))).thenReturn(
				mock(Attribute.class));
		assertNull(filter.filterRequest(mock(HttpRequest.class), ctx));

		DefaultFullHttpRequest req = new DefaultFullHttpRequest(
				HttpVersion.HTTP_1_1, HttpMethod.GET, "/version");
		HttpHeaders.setHost(req, "localhost:32876");
		
		HttpResponse resp = filter.filterRequest(req, ctx);
		assertTrue(resp instanceof FullHttpResponse);
		FullHttpResponse response = (FullHttpResponse) resp;
		assertEquals("application/json",
				response.headers().get(HttpHeaders.Names.CONTENT_TYPE));
		assertTrue(HttpHeaders.getContentLength(response) > 0);
		assertEquals(HttpResponseStatus.OK, response.getStatus());

		req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST,
				"/version");
		HttpHeaders.setHost(req, "127.0.0.1:32876");
		
		resp = filter.filterRequest(req, ctx);
		assertTrue(resp instanceof FullHttpResponse);
		response = (FullHttpResponse) resp;
		assertEquals(HttpResponseStatus.NOT_FOUND, response.getStatus());
		assertEquals(0, HttpHeaders.getContentLength(response));
		
		
		req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
				"/NOTFOUND");
		HttpHeaders.setHost(req, "127.0.0.1:32876");
		
		resp = filter.filterRequest(req, ctx);
		assertTrue(resp instanceof FullHttpResponse);
		response = (FullHttpResponse) resp;
		assertEquals(HttpResponseStatus.NOT_FOUND, response.getStatus());
		assertEquals(0, HttpHeaders.getContentLength(response));
	}
	
	@Test
	public void testIsProxyRequest() throws IOException{
		AppConfiguration appConfig = new AppConfiguration(new ConfigLoader(),
				null);
		appConfig.init();
		PolicyManager policyManager = mock(PolicyManager.class);
		NettyRequestProxyFilter filter = new NettyRequestProxyFilter(
				policyManager, appConfig);
		
		DefaultFullHttpRequest req = new DefaultFullHttpRequest(
				HttpVersion.HTTP_1_1, HttpMethod.GET, "/version");
		assertTrue(filter.isProxyRequest(req));
		assertRequest(filter, req, false, "localhost:32876");
		assertRequest(filter, req, false, "127.0.0.1:32876");
		assertRequest(filter, req, true, "localhost:32877");
		assertRequest(filter, req, true, "localhost");
		assertRequest(filter, req, true, "127.0.0.1");
		assertRequest(filter, req, true, "trace.vip.ebay.com");
		assertRequest(filter, req, true, "");
	}

	protected void assertRequest(NettyRequestProxyFilter filter,
			DefaultFullHttpRequest req, boolean expected, String host) {
		HttpHeaders.setHost(req, host);
		assertEquals(expected, filter.isProxyRequest(req));
	}
}
