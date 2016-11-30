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
package org.ebayopensource.scc.cache;

import static org.junit.Assert.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.util.ArrayList;

import org.junit.Test;

import org.ebayopensource.scc.cache.CacheResponse.CacheEntry;

public class CacheResponseTest {

	@Test
	public void test() {
		CacheResponse cacheResponse = new CacheResponse(HttpVersion.HTTP_1_1.text(),
				HttpResponseStatus.OK.code(),
				HttpResponseStatus.OK.reasonPhrase(), new ArrayList<CacheEntry<String, String>>(),
				new ArrayList<CacheEntry<String, String>>());
		
		cacheResponse.setCode(404);
		assertEquals(404, cacheResponse.getCode());
		
		cacheResponse.setReasonPhrase("anyPhase");
		assertEquals("anyPhase", cacheResponse.getReasonPhrase());

		cacheResponse.setProtocalVersion(HttpVersion.HTTP_1_0.text());
		assertEquals(HttpVersion.HTTP_1_0.text(), cacheResponse.getProtocalVersion());
		
		ArrayList<CacheEntry<String, String>> newHeaders = new ArrayList<CacheEntry<String, String>>();
		cacheResponse.setHeaders(newHeaders);
		newHeaders.add(new CacheResponse.CacheEntry<String, String>("k1", "v1"));
		assertEquals(newHeaders, cacheResponse.getHeaders());
		
		ArrayList<CacheEntry<String, String>> newTHeaders = new ArrayList<CacheEntry<String, String>>();
		cacheResponse.setTrailingHeaders(newTHeaders);
		newHeaders.add(new CacheResponse.CacheEntry<String, String>("k2", "v2"));
		assertEquals(newTHeaders, cacheResponse.getTrailingHeaders());
	}

}
