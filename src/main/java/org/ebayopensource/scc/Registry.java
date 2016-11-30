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
package org.ebayopensource.scc;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.ebayopensource.scc.cache.CacheManager;
import org.ebayopensource.scc.cache.CacheResponse;
import org.ebayopensource.scc.cache.RequestKeyGenerator;
import org.ebayopensource.scc.cache.policy.DefaultPolicyManager;
import org.ebayopensource.scc.cache.policy.PolicyManager;
import org.ebayopensource.scc.config.AppConfiguration;
import org.ebayopensource.scc.debug.DebugManager;
import org.ebayopensource.scc.filter.FilterManager;
import org.ebayopensource.scc.filter.IHttpRequestProxyFilter;
import org.ebayopensource.scc.filter.IHttpResponseProxyFilter;
import org.ebayopensource.scc.filter.NettyRequestProxyFilter;
import org.ebayopensource.scc.filter.NettyResponseProxyFilter;

public class Registry {
	protected FilterManager<HttpObject, HttpObject, ChannelHandlerContext> m_filterManager;
	protected CacheManager<FullHttpRequest, FullHttpResponse, CacheResponse> m_cacheManager;
	protected PolicyManager<FullHttpRequest, FullHttpResponse, CacheResponse> m_policyManager;
	protected DebugManager m_debugManager;
	protected AppConfiguration m_appConfig;

	protected ExecutorService m_execService;

	protected ScheduledExecutorService m_scheduledService;

	public Registry(AppConfiguration appConfig) {
		m_appConfig = appConfig;
	}

	public void init() {
		m_execService = Executors.newCachedThreadPool();
		m_scheduledService = Executors.newScheduledThreadPool(1);

		RequestKeyGenerator keyGenerator = new RequestKeyGenerator(
				m_appConfig);

		/*
		 * m_policyManager and keyGenerator are used in the initialization of
		 * NettyRequestProxyFilter, NettyResponseProxyFilter, please make sure
		 * init this two first
		 */
		m_policyManager = new DefaultPolicyManager(m_appConfig, keyGenerator,
				m_scheduledService);

		m_cacheManager = m_policyManager.getCacheManager();

		m_debugManager = new DebugManager(m_appConfig, m_policyManager);

		List<IHttpRequestProxyFilter<HttpObject, ChannelHandlerContext>> requestFilters = new ArrayList<>();
		requestFilters.add(new NettyRequestProxyFilter(m_policyManager,
				m_appConfig));
		List<IHttpResponseProxyFilter<HttpObject, ChannelHandlerContext>> responseFilters = new ArrayList<>();
		responseFilters.add(new NettyResponseProxyFilter(m_policyManager,
				m_execService));

		m_filterManager = new FilterManager<>(requestFilters, responseFilters);

	}

	public DebugManager getDebugManager() {
		return m_debugManager;
	}

	public FilterManager<HttpObject, HttpObject, ChannelHandlerContext> getFilterManager() {
		return m_filterManager;
	}

	public CacheManager<FullHttpRequest, FullHttpResponse, CacheResponse> getCacheManager() {
		return m_cacheManager;
	}

	public PolicyManager<FullHttpRequest, FullHttpResponse, CacheResponse> getPolicyManager() {
		return m_policyManager;
	}

	public ExecutorService getExecutorService() {
		return m_execService;
	}

	public ScheduledExecutorService getScheduledService() {
		return m_scheduledService;
	}
}
