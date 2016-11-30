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
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.mockito.Mockito.*;

import org.ebayopensource.scc.cache.CacheResponse;

public class FilterManagerTest {

	@Test
	public void testFilterRequest() {
		List<IHttpRequestProxyFilter<FullHttpRequest, CacheResponse>> reqFilters = new ArrayList<>();
		FilterManager<FullHttpRequest, FullHttpResponse, CacheResponse> fm = new FilterManager<>(
				reqFilters, null);

		assertNull(fm.filterRequest(null, null));

		IHttpRequestProxyFilter f1 = mock(IHttpRequestProxyFilter.class);
		reqFilters.add(f1);
		assertNull(fm.filterRequest(null, null));
		
		IHttpRequestProxyFilter f2 = mock(IHttpRequestProxyFilter.class);
		reqFilters.add(f2);
		FullHttpResponse resp = mock(FullHttpResponse.class);
		when(f2.filterRequest(any(), any())).thenReturn(resp);
		
		assertEquals(resp, fm.filterRequest(null, null));
	}

	@Test
	public void testFilterResponse() {
		List<IHttpResponseProxyFilter<FullHttpResponse, CacheResponse>> respFilters = new ArrayList<>();
		FilterManager<FullHttpRequest, FullHttpResponse, CacheResponse> fm = new FilterManager<>(
				null, respFilters);
		assertNull(fm.filterResponse(null, null));
		
		FullHttpResponse resp = mock(FullHttpResponse.class);
		assertNotNull(fm.filterResponse(resp, null));
		
		IHttpResponseProxyFilter f1 = mock(IHttpResponseProxyFilter.class);
		respFilters.add(f1);
		FullHttpResponse changedResp = mock(FullHttpResponse.class);
		when(f1.filterResponse(resp, null)).thenReturn(changedResp);
		assertEquals(changedResp, fm.filterResponse(resp, null));
		
	}

}
