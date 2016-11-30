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
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ebayopensource.scc.cache.CacheResponse;
import org.ebayopensource.scc.cache.policy.PolicyManager;

/**
 * for LittleProxy response filter
 * 
 */
public class NettyResponseProxyFilter implements
		IHttpResponseProxyFilter<HttpObject, ChannelHandlerContext> {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(NettyResponseProxyFilter.class);

	private PolicyManager<FullHttpRequest, FullHttpResponse, CacheResponse> m_policyManager;
	private ExecutorService m_execService;

	private ThreadLocal<List<HttpObject>> m_chunkedResponses = new ThreadLocal<List<HttpObject>>() {

		@Override
		protected List<HttpObject> initialValue() {
			return new ArrayList<>();
		}

	};

	public NettyResponseProxyFilter(
			PolicyManager<FullHttpRequest, FullHttpResponse, CacheResponse> policyManager,
			ExecutorService execService) {
		m_policyManager = policyManager;
		m_execService = execService;
	}

	@Override
	public HttpObject filterResponse(HttpObject orignal,
			ChannelHandlerContext ctx) {
		HttpObject httpObject = orignal;
		if (httpObject instanceof HttpResponse) {
			String uri = ctx.attr(NettyRequestProxyFilter.REQUEST_URI).get();
			LOGGER.info("Received a response from: " + uri);
			if (((HttpResponse) httpObject).getStatus().code() >= 300) {
				ctx.attr(NettyRequestProxyFilter.IS_CACHABLE).set(false);
			}
		}
		if (!ctx.attr(NettyRequestProxyFilter.IS_CACHABLE).get()) {
			return httpObject;
		}
		final String key = ctx.attr(NettyRequestProxyFilter.CACHE_KEY).get();
		if (key == null) {
			return httpObject;
		}

		if (httpObject instanceof HttpResponse) {
			LOGGER.debug("Response code: "
					+ ((HttpResponse) httpObject).getStatus().code());
		}
		if (httpObject instanceof FullHttpResponse) {
			m_policyManager.getCacheManager().put(key, httpObject);
		} else {
			final List<HttpObject> chunkedResponses = m_chunkedResponses.get();
			if (httpObject instanceof HttpContent) {
				HttpContent httpContent = ((HttpContent) httpObject)
						.duplicate();
				httpContent.retain();
				httpObject = httpContent;
			}
			chunkedResponses.add(httpObject);
			if (httpObject instanceof LastHttpContent) {
				Runnable runnable = new Runnable() {

					@Override
					public void run() {
						try {
							m_policyManager.getCacheManager().put(key,
									chunkedResponses);
						} catch (Throwable t) {
							LOGGER.error(t.getMessage(), t);
							return;
						}
						for (HttpObject obj : chunkedResponses) {
							if (obj instanceof HttpContent) {
								HttpContent httpContent = (HttpContent) obj;
								httpContent.release();
							}
						}
						chunkedResponses.clear();
						LOGGER.debug("Cache response to: " + key);
					}

				};
				m_execService.submit(runnable);
			}
		}
		return orignal;
	}

}
