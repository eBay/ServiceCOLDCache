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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class CacheResultVerifierTest {

	private CacheResultVerifier verifier;
	@Mock
	private FullHttpRequest request;
	@Mock
	private FullHttpResponse cacheResponse;
	@Mock
	private HttpHeaders cacheHeaders;
	@Mock
	private ByteBuf cacheBuf;
	@Mock
	private FullHttpResponse actualResponse;
	@Mock
	private HttpHeaders actHeaders;
	@Mock
	private ByteBuf actBuf;

	@Before
	public void setup() {
		Mockito.when(cacheResponse.headers()).thenReturn(cacheHeaders);
		Mockito.when(actualResponse.headers()).thenReturn(actHeaders);
		verifier = new CacheResultVerifier("serverHostAndPort=www.ebay.com:443",
				request, cacheResponse);
		Mockito.when(cacheHeaders.get("ETag")).thenReturn(null);
		Mockito.when(actHeaders.get("ETag")).thenReturn(null);
		Mockito.when(cacheResponse.content()).thenReturn(cacheBuf);
		Mockito.when(actualResponse.content()).thenReturn(actBuf);
	}

	@Test
	public void testFetchResult_invalidETag() {
		Mockito.when(cacheHeaders.get("ETag")).thenReturn("1");
		Mockito.when(actHeaders.get("ETag")).thenReturn("1");
		CacheVerifiedResult result = verifier.fetchResult(actualResponse);
		Assert.assertNull(result.cause);
	}

	@Test
	public void testFetchResult_differentStatus() {
		Mockito.when(cacheResponse.getStatus()).thenReturn(HttpResponseStatus.OK);
		Mockito.when(actualResponse.getStatus()).thenReturn(HttpResponseStatus.CREATED);
		CacheVerifiedResult result = verifier.fetchResult(actualResponse);
		Assert.assertEquals("Status code mismatch: cache - 200; actual - 201", result.cause);
	}

	@Test
	public void testFetchResult_sameContent() {
		Mockito.when(cacheResponse.getStatus()).thenReturn(HttpResponseStatus.OK);
		Mockito.when(actualResponse.getStatus()).thenReturn(HttpResponseStatus.OK);
		Mockito.when(cacheBuf.readableBytes()).thenReturn(1);
		Mockito.when(actBuf.readableBytes()).thenReturn(1);
		Mockito.when(cacheBuf.getByte(0)).thenReturn((byte)1);
		Mockito.when(actBuf.getByte(0)).thenReturn((byte)1);
		CacheVerifiedResult result = verifier.fetchResult(actualResponse);
		Assert.assertEquals(null, result.cause);
	}

	@Test
	public void testFetchResult_differentContentLength() {
		Mockito.when(cacheResponse.getStatus()).thenReturn(HttpResponseStatus.OK);
		Mockito.when(actualResponse.getStatus()).thenReturn(HttpResponseStatus.OK);
		Mockito.when(cacheBuf.readableBytes()).thenReturn(1);
		Mockito.when(actBuf.readableBytes()).thenReturn(2);
		CacheVerifiedResult result = verifier.fetchResult(actualResponse);
		Assert.assertEquals("Mismatched body input stream.", result.cause);
	}

	@Test
	public void testFetchResult_differentContent() {
		Mockito.when(cacheResponse.getStatus()).thenReturn(HttpResponseStatus.OK);
		Mockito.when(actualResponse.getStatus()).thenReturn(HttpResponseStatus.OK);
		Mockito.when(cacheBuf.readableBytes()).thenReturn(1);
		Mockito.when(actBuf.readableBytes()).thenReturn(1);
		Mockito.when(cacheBuf.getByte(0)).thenReturn((byte)1);
		Mockito.when(actBuf.getByte(0)).thenReturn((byte)0);
		CacheVerifiedResult result = verifier.fetchResult(actualResponse);
		Assert.assertEquals("Mismatched body input stream.", result.cause);
	}

	@Test
	public void testFetchResult_sameHeader() {
		Set<String> headerSet = new HashSet<>();
		headerSet.add("no-cache");
		Mockito.when(cacheHeaders.names()).thenReturn(headerSet);
		Mockito.when(actHeaders.names()).thenReturn(headerSet);
		Mockito.when(actHeaders.get("no-cache")).thenReturn("1");
		Mockito.when(cacheHeaders.get("no-cache")).thenReturn("1");
		Mockito.when(cacheResponse.getStatus()).thenReturn(HttpResponseStatus.OK);
		Mockito.when(actualResponse.getStatus()).thenReturn(HttpResponseStatus.OK);
		Mockito.when(cacheBuf.readableBytes()).thenReturn(1);
		Mockito.when(actBuf.readableBytes()).thenReturn(1);
		Mockito.when(cacheBuf.getByte(0)).thenReturn((byte)1);
		Mockito.when(actBuf.getByte(0)).thenReturn((byte)1);
		CacheVerifiedResult result = verifier.fetchResult(actualResponse);
		Assert.assertNull(result.cause);
	}

	@Test
	public void testFetchResult_diffHeaderValue() {
		Set<String> headerSet = new HashSet<>();
		headerSet.add("no-cache");
		Mockito.when(cacheHeaders.names()).thenReturn(headerSet);
		Mockito.when(actHeaders.names()).thenReturn(headerSet);
		Mockito.when(actHeaders.get("no-cache")).thenReturn("1");
		Mockito.when(cacheHeaders.get("no-cache")).thenReturn("0");
		Mockito.when(cacheResponse.getStatus()).thenReturn(HttpResponseStatus.OK);
		Mockito.when(actualResponse.getStatus()).thenReturn(HttpResponseStatus.OK);
		Mockito.when(cacheBuf.readableBytes()).thenReturn(1);
		Mockito.when(actBuf.readableBytes()).thenReturn(1);
		Mockito.when(cacheBuf.getByte(0)).thenReturn((byte)1);
		Mockito.when(actBuf.getByte(0)).thenReturn((byte)1);
		CacheVerifiedResult result = verifier.fetchResult(actualResponse);
		Assert.assertEquals("Mismatched headers: [no-cache]", result.cause);
	}

	@Test
	public void testFetchResult_missHeader() {
		Set<String> headerSet = new HashSet<>();
		headerSet.add("no-cache");
		Mockito.when(cacheHeaders.names()).thenReturn(headerSet);
		Mockito.when(cacheHeaders.get("no-cache")).thenReturn("1");
		Mockito.when(cacheResponse.getStatus()).thenReturn(HttpResponseStatus.OK);
		Mockito.when(actualResponse.getStatus()).thenReturn(HttpResponseStatus.OK);
		Mockito.when(cacheBuf.readableBytes()).thenReturn(1);
		Mockito.when(actBuf.readableBytes()).thenReturn(1);
		Mockito.when(cacheBuf.getByte(0)).thenReturn((byte)1);
		Mockito.when(actBuf.getByte(0)).thenReturn((byte)1);
		CacheVerifiedResult result = verifier.fetchResult(actualResponse);
		Assert.assertEquals("Mismatched headers: [no-cache]", result.cause);

		Mockito.when(cacheHeaders.names()).thenReturn(new HashSet<String>());
		Mockito.when(actHeaders.names()).thenReturn(headerSet);
		Mockito.when(actHeaders.get("no-cache")).thenReturn("1");
		Mockito.when(cacheHeaders.get("no-cache")).thenReturn("");
		result = verifier.fetchResult(actualResponse);
		Assert.assertEquals("Mismatched headers: [no-cache]", result.cause);
	}

	@Test
	public void testFetchResult_vaStrictr() {
		System.setProperty("coldCache.verifyCacheResult.strict", "false");
		Set<String> headerSet = new HashSet<>();
		Mockito.when(cacheHeaders.names()).thenReturn(headerSet);
		Mockito.when(actHeaders.names()).thenReturn(headerSet);
		Mockito.when(cacheResponse.getStatus()).thenReturn(HttpResponseStatus.OK);
		Mockito.when(actualResponse.getStatus()).thenReturn(HttpResponseStatus.OK);
		Mockito.when(cacheBuf.readableBytes()).thenReturn(1);
		Mockito.when(actBuf.readableBytes()).thenReturn(1);
		Mockito.when(cacheBuf.getByte(0)).thenReturn((byte)1);
		Mockito.when(actBuf.getByte(0)).thenReturn((byte)1);
		CacheVerifiedResult result = verifier.fetchResult(actualResponse);
		Assert.assertNull(result.cause);
	}

	@Test
	public void testFetchResult() {
		FullHttpResponse cache = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
				ByteBufAllocator.DEFAULT.buffer());
		FullHttpResponse actual = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
				ByteBufAllocator.DEFAULT.buffer());
		CacheResultVerifier crv = new CacheResultVerifier("key", null, cache);

		HttpHeaders.setHeader(cache, "ETag", "etagvalue");
		HttpHeaders.setHeader(actual, "ETag", "etagvalue");

		CacheVerifiedResult result = crv.fetchResult(actual);
		Assert.assertTrue(result.result);

		HttpHeaders.setHeader(actual, "ETag", "etagvalue2");
		result = crv.fetchResult(actual);
		Assert.assertFalse(result.result);

		cache = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK, ByteBufAllocator.DEFAULT.buffer());
		actual = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK, ByteBufAllocator.DEFAULT.buffer());
		crv = new CacheResultVerifier("key", null, cache);

		actual.setStatus(HttpResponseStatus.SERVICE_UNAVAILABLE);
		result = crv.fetchResult(actual);
		Assert.assertFalse(result.result);
		actual.setStatus(HttpResponseStatus.OK);

		HttpHeaders.setHeader(cache, "h1", "value");
		HttpHeaders.setHeader(actual, "h1", "value");
		result = crv.fetchResult(actual);
		Assert.assertTrue(result.result);

		HttpHeaders.setHeader(cache, "h2", "value");
		HttpHeaders.setHeader(actual, "h2", "value2");
		result = crv.fetchResult(actual);
		Assert.assertFalse(result.result);
		HttpHeaders.setHeader(actual, "h2", "value");

		ByteBuf cc = cache.content();
		ByteBuf ac = actual.content();

		cc.writeBytes("abc".getBytes());
		result = crv.fetchResult(actual);
		Assert.assertFalse(result.result);

		ac.writeBytes("abc".getBytes());
		result = crv.fetchResult(actual);
		Assert.assertTrue(result.result);

		cc.writeBytes("eft".getBytes());
		ac.writeBytes("xyz".getBytes());
		result = crv.fetchResult(actual);
		Assert.assertFalse(result.result);

	}
}