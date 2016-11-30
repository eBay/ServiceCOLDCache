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

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import org.ebayopensource.scc.cache.CacheManager;
import org.ebayopensource.scc.cache.CacheResponse;
import org.ebayopensource.scc.config.AppConfiguration;

public class AppCtx {

	private static AppCtx s_instance = new AppCtx();

	public static AppCtx getInstance() {
		return s_instance;
	}

	private Registry m_registry;
	private AppConfiguration m_appConfig;

	protected AppCtx() {
	}

	public Registry getRegistry() {
		return m_registry;
	}

	public void init(AppConfiguration appConfig, Registry registry) {
		m_appConfig = appConfig;
		m_registry = registry;
	}

	public CacheManager<FullHttpRequest, FullHttpResponse, CacheResponse> getCacheManager() {
		return m_registry.getCacheManager();
	}

	public AppConfiguration getAppConfig() {
		return m_appConfig;
	}

	public void enableCache(boolean enable) {
		m_appConfig.put(AppConfiguration.KEY_ENABLE_CACHE, enable);
	}
}
