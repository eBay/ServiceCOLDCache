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

import org.ebayopensource.scc.AppCtx;
import org.ebayopensource.scc.Registry;
import org.ebayopensource.scc.config.AppConfiguration;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.FieldSetter;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;

@RunWith(PowerMockRunner.class)
public class DebugHttpFilterTest {
	@Mock
	private ChannelHandlerContext m_ctx;
	@Mock
	private AppConfiguration m_appConfig;
	@Mock
	private DebugManager m_debugManager;
	@Mock
	private HttpRequest originalRequest;
	@Mock
	private Registry m_registry;
	private DebugHttpFilter debugHttpFilter;


	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		writeField(AppCtx.getInstance(), "m_appConfig", m_appConfig);
		writeField(AppCtx.getInstance(), "m_registry", m_registry);
		Mockito.when(m_registry.getDebugManager()).thenReturn(m_debugManager);
		debugHttpFilter = new DebugHttpFilter(originalRequest, m_ctx);
	}

	@Test
	public void testClientToProxyRequest() {
		FullHttpRequest request = Mockito.mock(FullHttpRequest.class);
		HttpResponse response = debugHttpFilter.clientToProxyRequest(request);
		Assert.assertNull(response);
		Mockito.verify(m_debugManager, Mockito.times(1)).issueDebugRequest(request, m_ctx, true);
	}

	@Test
	public void testServerToProxyResponse() {
		FullHttpResponse response = Mockito.mock(FullHttpResponse.class);
		HttpObject obj = debugHttpFilter.serverToProxyResponse(response);
		Assert.assertEquals(obj, response);
		Mockito.verify(m_debugManager, Mockito.times(1)).debugResponse(response, m_ctx);
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
}
