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
package org.ebayopensource.scc.debug;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import org.ebayopensource.scc.AppCtx;
import org.littleshoot.proxy.HttpFiltersAdapter;

public class DebugHttpFilter extends HttpFiltersAdapter {

	private ChannelHandlerContext m_ctx;
	private AppCtx m_appCtx;
	private DebugManager m_debugManager;

	public DebugHttpFilter(HttpRequest originalRequest, ChannelHandlerContext ctx) {
		super(originalRequest);
		this.m_ctx = ctx;
		this.m_appCtx = AppCtx.getInstance();
		m_debugManager = m_appCtx.getRegistry().getDebugManager();
	}


	@Override
	public HttpResponse clientToProxyRequest(final HttpObject httpObject) {
		m_debugManager.issueDebugRequest((FullHttpRequest) httpObject, this.m_ctx, true);
		return null;
	}

	@Override
	public HttpObject serverToProxyResponse(HttpObject httpObject) {
		if (httpObject instanceof FullHttpResponse) {
			m_debugManager.debugResponse((FullHttpResponse) httpObject, this.m_ctx);
		}
		return httpObject;
	}

}
