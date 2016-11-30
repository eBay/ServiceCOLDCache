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

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LoggingHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class DebugNettyClientInitializerTest {

	private DebugNettyClientInitializer initializer;
	@Mock
	private SocketChannel channel;
	@Mock
	private ChannelPipeline pl;

	@Before
	public void setup() {
		initializer = new DebugNettyClientInitializer();
		Mockito.when(channel.pipeline()).thenReturn(pl);
	}

	@Test
	public void testInitChannel() throws Exception {
		initializer.initChannel(channel);
		Mockito.verify(pl, Mockito.times(1)).addLast(Mockito.eq("log"), Mockito.any(LoggingHandler.class));
		Mockito.verify(pl, Mockito.times(1)).addLast(Mockito.eq("codec"), Mockito.any(HttpClientCodec.class));
		Mockito.verify(pl, Mockito.times(1)).addLast(Mockito.eq("aggregator"), Mockito.any(HttpObjectAggregator.class));
		Mockito.verify(pl, Mockito.times(1)).addLast(Mockito.eq("handler"), Mockito.any(DebugClientHandler.class));
	}
}
