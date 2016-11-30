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

import io.netty.handler.codec.http.HttpResponse;

import java.util.List;

/**
 * 
 * @param <T>
 *            for request object
 * @param <R>
 *            for response object
 * @param <C>
 *            for context object
 */
public class FilterManager<T, R, C> {

	private List<IHttpRequestProxyFilter<T, C>> m_requestFilters;
	private List<IHttpResponseProxyFilter<R, C>> m_responseFilters;

	public FilterManager(List<IHttpRequestProxyFilter<T, C>> requestFilters,
			List<IHttpResponseProxyFilter<R, C>> responseFilters) {
		m_requestFilters = requestFilters;
		m_responseFilters = responseFilters;
	}

	public HttpResponse filterRequest(T req, C ctx) {
		for (IHttpRequestProxyFilter<T, C> filter : m_requestFilters) {
			HttpResponse resp = filter.filterRequest(req, ctx);
			if (resp != null) {
				return resp;
			}
		}
		return null;
	}

	public R filterResponse(R resp, C ctx) {
		R response = resp;
		for (IHttpResponseProxyFilter<R, C> filter : m_responseFilters) {
			response = filter.filterResponse(resp, ctx);
		}
		return response;
	}

}
