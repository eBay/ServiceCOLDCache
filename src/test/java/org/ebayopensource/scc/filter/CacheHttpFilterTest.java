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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

import org.ebayopensource.scc.debug.DebugManager;
import org.ebayopensource.scc.filter.CacheHttpFilterSource.CacheHttpFilter;

public class CacheHttpFilterTest {
	
	private CacheHttpFilter m_filter;
	private FilterManager<HttpObject, HttpObject, ChannelHandlerContext> m_filterManager;
	private ChannelHandlerContext m_ctx;

	@SuppressWarnings("unchecked")
	@Before
	public void setup(){
		m_filterManager = mock(FilterManager.class);
		DebugManager debugManager = mock(DebugManager.class);
		HttpRequest request = mock(HttpRequest.class);
		m_ctx = mock(ChannelHandlerContext.class);
		m_filter = new CacheHttpFilter(request, m_ctx, m_filterManager, debugManager);
	}

	@Test
	public void testClientToProxyRequestHttpObject() {
		HttpObject httpObject = mock(FullHttpRequest.class);
		when(m_filterManager.filterRequest(httpObject, m_ctx)).thenReturn(null);
		assertNull(m_filter.clientToProxyRequest(httpObject));
		
		when(m_filterManager.filterRequest(httpObject, m_ctx)).thenReturn(mock(HttpResponse.class));
		assertNotNull(m_filter.clientToProxyRequest(httpObject));
	}

	@Test
	public void testServerToProxyResponseHttpObject() {
		assertNull(m_filter.serverToProxyResponse(null));
		HttpObject httpObject = mock(HttpObject.class);
//		assertEquals(httpObject, m_filter.serverToProxyResponse(httpObject));

		FullHttpResponse fullHttpResponse = mock(FullHttpResponse.class);
		when(m_filterManager.filterResponse(fullHttpResponse, m_ctx))
				.thenReturn(mock(HttpResponse.class));
		assertTrue(m_filter.serverToProxyResponse(fullHttpResponse) instanceof HttpResponse);
	}

}
