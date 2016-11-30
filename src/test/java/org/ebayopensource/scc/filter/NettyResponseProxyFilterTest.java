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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import org.junit.Test;

import static org.mockito.Mockito.*;

import org.ebayopensource.scc.cache.CacheManager;
import org.ebayopensource.scc.cache.policy.PolicyManager;
import org.ebayopensource.scc.config.AppConfiguration;

@SuppressWarnings({"rawtypes","unchecked"})
public class NettyResponseProxyFilterTest {

	@Test
	public void testFilterFullHttpResponse() {
		PolicyManager policyManager = mock(PolicyManager.class);
		AppConfiguration appConfig = mock(AppConfiguration.class);
		when(appConfig.getBoolean(AppConfiguration.KEY_DEBUG_INFO)).thenReturn(true);
		
		NettyResponseProxyFilter filter = new NettyResponseProxyFilter(
				policyManager, Executors.newCachedThreadPool());

		ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
		when(ctx.attr(any(AttributeKey.class))).thenReturn(
				mock(Attribute.class));
		Attribute cachable = mock(Attribute.class);
		when(ctx.attr(NettyRequestProxyFilter.IS_CACHABLE))
				.thenReturn(cachable);
		when(cachable.get()).thenReturn(Boolean.FALSE);
		assertNull(filter.filterResponse(null, ctx));
		
		when(cachable.get()).thenReturn(Boolean.TRUE);
		assertNull(filter.filterResponse(null, ctx));
		
		HttpResponse resp = mock(HttpResponse.class);
		when(resp.getStatus()).thenReturn(HttpResponseStatus.OK);
		assertEquals(resp, filter.filterResponse(resp, ctx));
		
		resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
		assertEquals(resp, filter.filterResponse(resp, ctx));
		
		resp.setStatus(HttpResponseStatus.OK);
		assertEquals(resp, filter.filterResponse(resp, ctx));
		
		Attribute cacheKey = mock(Attribute.class);
		when(ctx.attr(NettyRequestProxyFilter.CACHE_KEY)).thenReturn(cacheKey);
		when(cacheKey.getAndRemove()).thenReturn("key");
		when(policyManager.getCacheManager()).thenReturn(mock(CacheManager.class));
		assertEquals(resp, filter.filterResponse(resp, ctx));
		
		when(cacheKey.get()).thenReturn("key");
		assertEquals(resp, filter.filterResponse(resp, ctx));
	}
	
	@Test
	public void testFilterChunkeResponses() throws InterruptedException{
		PolicyManager policyManager = mock(PolicyManager.class);
		AppConfiguration appConfig = mock(AppConfiguration.class);
		when(appConfig.getBoolean(AppConfiguration.KEY_DEBUG_INFO)).thenReturn(true);
		
		ExecutorService tp = Executors.newCachedThreadPool();
		NettyResponseProxyFilter filter = new NettyResponseProxyFilter(
				policyManager, tp);
		
		ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
		when(ctx.attr(any(AttributeKey.class))).thenReturn(
				mock(Attribute.class));
		Attribute cachable = mock(Attribute.class);
		when(ctx.attr(NettyRequestProxyFilter.IS_CACHABLE))
				.thenReturn(cachable);
		when(cachable.get()).thenReturn(Boolean.TRUE);
		
		Attribute cacheKey = mock(Attribute.class);
		when(ctx.attr(NettyRequestProxyFilter.CACHE_KEY)).thenReturn(cacheKey);
		when(cacheKey.get()).thenReturn("key");
		
		HttpResponse resp = mock(HttpResponse.class);
		when(resp.getStatus()).thenReturn(HttpResponseStatus.OK);
		filter.filterResponse(resp, ctx);
		HttpContent httpContent = mock(HttpContent.class);
		when(httpContent.duplicate()).thenReturn(mock(HttpContent.class));
		filter.filterResponse(httpContent, ctx);
		LastHttpContent lastHttpContent = mock(LastHttpContent.class);
		when(lastHttpContent.duplicate()).thenReturn(mock(LastHttpContent.class));
		filter.filterResponse(lastHttpContent, ctx);
		
		filter.filterResponse(resp, ctx);
		filter.filterResponse(lastHttpContent, ctx);
		
		tp.awaitTermination(10, TimeUnit.SECONDS);
	}

}
