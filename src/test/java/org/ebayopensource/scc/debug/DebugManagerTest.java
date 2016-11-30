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

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.Attribute;

import java.lang.reflect.Field;
import java.util.concurrent.Executor;

import org.ebayopensource.scc.cache.CacheManager;
import org.ebayopensource.scc.cache.CacheResponse;
import org.ebayopensource.scc.cache.CacheResultVerifier;
import org.ebayopensource.scc.cache.CacheStats;
import org.ebayopensource.scc.cache.CacheVerifiedResult;
import org.ebayopensource.scc.cache.policy.PolicyManager;
import org.ebayopensource.scc.config.AppConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.littleshoot.proxy.impl.ClientToProxyConnection;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.FieldReader;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class DebugManagerTest {
	@Mock
	private CacheManager<FullHttpRequest, FullHttpResponse, CacheResponse> m_cacheManager;
	@Mock
	private PolicyManager<FullHttpRequest, FullHttpResponse, CacheResponse> m_policyManager;
	private DebugManager debugManager;
	private AppConfiguration m_appConfiguration;
	@Mock
	private Executor executor;
	@Mock
	private Channel debugChannel;
	@Mock
	private Channel debugSslChannel;
	@Mock
	private ClientToProxyConnection clientToProxyConnection;


	@Before
	public void setup() {
		Mockito.when(m_policyManager.getCacheManager()).thenReturn(m_cacheManager);
		m_appConfiguration = Mockito.mock(AppConfiguration.class);
		Mockito.when(m_appConfiguration.getInt("debugManager.maxDebugThreads")).thenReturn(10);
		debugManager = new DebugManager(m_appConfiguration, m_policyManager);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testIssueDebugRequest_fromDebugFilter() {
		Mockito.when(m_appConfiguration.getBoolean("debugManager.debugEnabled")).thenReturn(true);
		FullHttpRequest request = Mockito.mock(FullHttpRequest.class);
		FullHttpResponse cacheResponse = Mockito.mock(FullHttpResponse.class);
		ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);
		Mockito.when(request.copy()).thenReturn(request);
		Mockito.when(m_cacheManager.get("test_req")).thenReturn(cacheResponse);
		Mockito.when(m_policyManager.generateCacheKey(request)).thenReturn("test_req");

		Attribute<CacheResultVerifier> debugging = Mockito.mock(Attribute.class);
		Mockito.when(ctx.attr(DebugManager.DEBUG_RESULT)).thenReturn(debugging);
		debugManager.issueDebugRequest(request, ctx, true);
		CacheResultVerifier verifier = new CacheResultVerifier("test_req", request, cacheResponse);
		Mockito.verify(debugging, Mockito.times(1)).set(Mockito.refEq(verifier));
	}

	@Test
	public void testDebugEnabled_on() {
		Mockito.when(m_appConfiguration.getBoolean("debugManager.debugEnabled")).thenReturn(true);
		Assert.assertTrue(debugManager.debugEnabled());
	}

	@Test
	public void testDebugEnabled_off() {
		Mockito.when(m_appConfiguration.getBoolean("debugManager.debugEnabled")).thenReturn(false);
		Assert.assertTrue(!debugManager.debugEnabled());
	}

	@Test
	public void testShutdown() {
		debugManager.shutdown();
	}

	@Test
	public void testSetupDebugChannel() {
		DefaultHttpProxyServer debugServer = null;
		try {
			debugServer = debugManager.setupDebugChannel(7099);
			Assert.assertNotNull(debugServer);
			Assert.assertNotNull(readField(debugManager, "debugChannel"));
			Assert.assertNotNull(readField(debugManager, "debugSslChannel"));
		} catch (Exception ex) {
			Assert.fail(ex.getMessage());
		} finally {
			debugManager.shutdown();
			if (debugServer != null) {
				debugServer.stop();
			}
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testIssueDebugRequest_sslFromDebugFilter() {
		Mockito.when(m_appConfiguration.getBoolean("debugManager.debugEnabled")).thenReturn(true);
		FullHttpRequest request = Mockito.mock(FullHttpRequest.class);
		FullHttpResponse cacheResponse = Mockito.mock(FullHttpResponse.class);
		ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);
		Mockito.when(ctx.handler()).thenReturn(clientToProxyConnection);
		Mockito.when(request.copy()).thenReturn(request);
		String key = "https://serverHostAndPort=www.ebay.com:443";
		Mockito.when(m_cacheManager.get(key)).thenReturn(cacheResponse);
		Mockito.when(m_policyManager.generateCacheKey(request)).thenReturn(key);

		Attribute<CacheResultVerifier> debugging = Mockito.mock(Attribute.class);
		Mockito.when(ctx.attr(DebugManager.DEBUG_RESULT)).thenReturn(debugging);
		debugManager.issueDebugRequest(request, ctx, true);
		Assert.assertTrue((Boolean) readField(clientToProxyConnection, "mitming"));
		CacheResultVerifier verifier = new CacheResultVerifier(key, request, cacheResponse);
		Mockito.verify(debugging, Mockito.times(1)).set(Mockito.refEq(verifier));
	}

	@Test
	public void testIssueDebugRequest_sslNotFromDebugFilter() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(m_appConfiguration.getBoolean("debugManager.debugEnabled")).thenReturn(true);
		final FullHttpRequest request = Mockito.mock(FullHttpRequest.class);
		final ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);
		Mockito.when(request.copy()).thenReturn(request);
		writeField(debugManager, "executor", executor);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation)
					throws Exception {
				writeField(debugManager, "m_policyManager", m_policyManager);
				Mockito.when(m_policyManager.generateCacheKey(Mockito.any(FullHttpRequest.class))).thenReturn(
						"https://serverHostAndPort=www.ebay.com:443");
				ChannelFuture future = Mockito.mock(ChannelFuture.class);
				Mockito.when(debugSslChannel.closeFuture()).thenReturn(future);
				writeField(debugManager, "debugSslChannel", debugSslChannel);
				writeField(debugManager, "debugChannel", debugChannel);
				Object[] args = invocation.getArguments();
				Runnable runnable = (Runnable) args[0];
				runnable.run();
				Mockito.verify(debugSslChannel, Mockito.times(1)).writeAndFlush(request);
				Mockito.verify(debugChannel, Mockito.times(0)).writeAndFlush(request);
				return null;
			}
		}).when(executor).execute(Mockito.any(Runnable.class));
		debugManager.issueDebugRequest(request, ctx, false);
	}

	@Test
	public void testIssueDebugRequest_notFromDebugFilter() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(m_appConfiguration.getBoolean("debugManager.debugEnabled")).thenReturn(true);
		final FullHttpRequest request = Mockito.mock(FullHttpRequest.class);
		final ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);
		Mockito.when(m_policyManager.generateCacheKey(request)).thenReturn("test_req");
		Mockito.when(request.copy()).thenReturn(request);
		writeField(debugManager, "executor", executor);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation)
					throws Exception {
				writeField(debugManager, "m_policyManager", m_policyManager);
				Object[] args = invocation.getArguments();
				Runnable runnable = (Runnable) args[0];
				writeField(debugManager, "debugSslChannel", debugSslChannel);
				writeField(debugManager, "debugChannel", debugChannel);
				ChannelFuture future = Mockito.mock(ChannelFuture.class);
				Mockito.when(debugChannel.closeFuture()).thenReturn(future);
				runnable.run();
				Mockito.verify(debugChannel, Mockito.times(1)).writeAndFlush(request);
				Mockito.verify(debugSslChannel, Mockito.times(0)).writeAndFlush(request);
				return null;
			}
		}).when(executor).execute(Mockito.any(Runnable.class));
		debugManager.issueDebugRequest(request, ctx, false);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDebugResponse_consume() {
		Mockito.when(m_appConfiguration.getBoolean("debugManager.debugEnabled")).thenReturn(true);
		FullHttpResponse actualResponse = Mockito.mock(FullHttpResponse.class);

		ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);
		Attribute<CacheResultVerifier> debugging = Mockito.mock(Attribute.class);
		Mockito.when(ctx.attr(DebugManager.DEBUG_RESULT)).thenReturn(debugging);
		CacheResultVerifier verifier = Mockito.mock(CacheResultVerifier.class);
		CacheVerifiedResult result = new CacheVerifiedResult();
		result.key = "test_req";
		Mockito.when(verifier.fetchResult(actualResponse)).thenReturn(result);
		Mockito.when(debugging.get()).thenReturn(verifier);

		CacheStats cacheStats = Mockito.mock(CacheStats.class);
		Mockito.when(m_cacheManager.getStats()).thenReturn(cacheStats);
		debugManager.debugResponse(actualResponse, ctx);
		Mockito.verify(cacheStats, Mockito.times(1)).addCacheVerifiedResult(verifier.fetchResult(actualResponse));
	}

	@Test
	public void testDebugResponse_debugOff() {
		Mockito.when(m_appConfiguration.getBoolean("debugManager.debugEnabled")).thenReturn(false);
		FullHttpResponse actualResponse = Mockito.mock(FullHttpResponse.class);

		ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);
		debugManager.debugResponse(actualResponse, ctx);
		Mockito.verify(m_cacheManager, Mockito.times(0)).getStats();
	}

	private void writeField(Object obj, String field, Object v) {
		try {
			Field executorField = obj.getClass().getDeclaredField(field);
			FieldSetter fieldSetter = new FieldSetter(obj, executorField);
			fieldSetter.set(v);
		} catch (NoSuchFieldException e) {
			Assert.fail(e.getMessage());
		}
	}

	private Object readField(Object obj, String field) {
		try {
			Class clazz = obj.getClass();
			if (clazz.getName().contains("$")) {
				clazz = clazz.getSuperclass();
			}
			Field executorField = clazz.getDeclaredField(field);
			FieldReader fieldReader = new FieldReader(obj, executorField);
			return fieldReader.read();
		} catch (NoSuchFieldException e) {
			Assert.fail(e.getMessage());
		}
		return null;
	}

}
