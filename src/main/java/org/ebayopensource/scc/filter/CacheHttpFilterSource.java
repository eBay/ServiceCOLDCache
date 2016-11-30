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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import org.ebayopensource.scc.AppCtx;
import org.ebayopensource.scc.debug.DebugManager;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;

public final class CacheHttpFilterSource extends
		HttpFiltersSourceAdapter {
	public static final class CacheHttpFilter extends HttpFiltersAdapter {
		ChannelHandlerContext m_ctx = null;
		private FilterManager<HttpObject, HttpObject, ChannelHandlerContext> m_filterManager;
		private DebugManager m_debugManager;

		public CacheHttpFilter(HttpRequest originalRequest,
				ChannelHandlerContext ctx, FilterManager<HttpObject, HttpObject, ChannelHandlerContext> filterManager, DebugManager debugManager) {
			super(originalRequest);
			this.m_ctx = ctx;
			m_filterManager = filterManager;
			m_debugManager = debugManager;
		}

		@Override
		public HttpResponse clientToProxyRequest(HttpObject httpObject) {
			HttpResponse response = m_filterManager.filterRequest(
					httpObject, this.m_ctx);
			if (response != null) {
				m_debugManager.issueDebugRequest(
						(FullHttpRequest) httpObject, this.m_ctx, false);
			}
			return response;
		}

		@Override
		public HttpObject serverToProxyResponse(HttpObject httpObject) {
			return m_filterManager.filterResponse(httpObject, this.m_ctx);
		}
	}

	private AppCtx m_appCtx;
	
	public CacheHttpFilterSource(AppCtx appCtx){
		m_appCtx = appCtx;
	}

	@Override
	public int getMaximumRequestBufferSizeInBytes() {
		return 1024 * 1024 * 1024;// 1G
	}

	@Override
	public int getMaximumResponseBufferSizeInBytes() {
		return 0;// 1G
	}

	public HttpFilters filterRequest(HttpRequest originalRequest,
			ChannelHandlerContext ctx) {
		return new CacheHttpFilter(originalRequest, ctx, m_appCtx.getRegistry().getFilterManager(), m_appCtx.getRegistry().getDebugManager());
	}

}