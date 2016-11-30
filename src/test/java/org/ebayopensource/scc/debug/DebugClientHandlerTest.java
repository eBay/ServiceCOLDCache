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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(PowerMockRunner.class)
public class DebugClientHandlerTest {
	@Mock
	private HttpResponse response;
	@Mock
	private HttpHeaders headers;
	@Mock
	private Logger LOGGER;
	private DebugClientHandler handler;
	@Mock
	private ChannelHandlerContext ctx;
	@Mock
	private LastHttpContent lastHttpContent;
	@Mock
	private ByteBuf contentBuf;
	@Mock
	private Throwable error;

	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		try {
			setFinalStatic(DebugClientHandler.class.getDeclaredField("LOGGER"), LOGGER);
		} catch (Exception e) {
			Assert.fail();
		}
		handler = new DebugClientHandler();
		Mockito.when(response.headers()).thenReturn(headers);
	}

	@Test
	public void testChannelRead0_debugOnResponse() {
		Mockito.when(LOGGER.isDebugEnabled()).thenReturn(true);
		Mockito.when(response.getStatus()).thenReturn(HttpResponseStatus.OK);
		Mockito.when(response.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
		Mockito.when(headers.isEmpty()).thenReturn(false);
		Set<String> headersSet = new HashSet<>();
		headersSet.add("no-cache");
		Mockito.when(headers.names()).thenReturn(headersSet);
		List<String> noCacheValues = new ArrayList<>();
		noCacheValues.add("private");
		Mockito.when(headers.getAll("no-cache")).thenReturn(noCacheValues);
		PowerMockito.mockStatic(HttpHeaders.class);
		BDDMockito.given(HttpHeaders.isTransferEncodingChunked(response)).willReturn(true);
		handler.channelRead0(ctx, response);
		Mockito.verify(LOGGER, Mockito.times(1)).debug("STATUS: " + HttpResponseStatus.OK);
		Mockito.verify(LOGGER, Mockito.times(1)).debug("VERSION: " + HttpVersion.HTTP_1_1);
		Mockito.verify(LOGGER, Mockito.times(1)).debug("HEADER: no-cache = private");
		Mockito.verify(LOGGER, Mockito.times(1)).debug("CHUNKED CONTENT {");


		BDDMockito.given(HttpHeaders.isTransferEncodingChunked(response)).willReturn(false);
		handler.channelRead0(ctx, response);
		Mockito.verify(LOGGER, Mockito.times(1)).debug("CONTENT {");
	}

	@Test
	public void testChannelRead0_debugOffContent(){
		Mockito.when(LOGGER.isDebugEnabled()).thenReturn(false);
		String result = "{result:true}";
		Mockito.when(contentBuf.toString(CharsetUtil.UTF_8)).thenReturn(result);
		Mockito.when(lastHttpContent.content()).thenReturn(contentBuf);
		handler.channelRead0(ctx, lastHttpContent);
		Mockito.verify(LOGGER, Mockito.times(0)).debug(result);
	}

	@Test
	public void testChannelRead0_debugOnContent(){
		Mockito.when(LOGGER.isDebugEnabled()).thenReturn(true);
		String result = "{result:true}";
		Mockito.when(contentBuf.toString(CharsetUtil.UTF_8)).thenReturn(result);
		Mockito.when(lastHttpContent.content()).thenReturn(contentBuf);
		handler.channelRead0(ctx, lastHttpContent);
		Mockito.verify(LOGGER, Mockito.times(1)).debug(result);
		Mockito.verify(LOGGER, Mockito.times(1)).debug("} END OF CONTENT");
	}

	@Test
	public void testExceptionCaught(){
		Mockito.when(error.getMessage()).thenReturn("socket is closed");
		try {
			handler.exceptionCaught(ctx, error);
			Mockito.verify(LOGGER, Mockito.times(1)).error("socket is closed");
			Mockito.verify(ctx, Mockito.times(1)).close();
		} catch (Exception e) {
			Assert.fail();
		}
	}


	static void setFinalStatic(Field field, Object newValue) throws Exception {
		field.setAccessible(true);
		// remove final modifier from field
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		field.set(null, newValue);
	}
}
