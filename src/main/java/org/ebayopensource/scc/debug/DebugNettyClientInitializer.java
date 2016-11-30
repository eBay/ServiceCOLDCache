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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class DebugNettyClientInitializer extends ChannelInitializer<SocketChannel> {

	public DebugNettyClientInitializer() {
	}

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		// Create a default pipeline implementation.
		ChannelPipeline p = ch.pipeline();
		p.addLast("log", new LoggingHandler(LogLevel.INFO));
		p.addLast("codec", new HttpClientCodec());
		p.addLast("aggregator", new HttpObjectAggregator(1048576));
		p.addLast("handler", new DebugClientHandler());
	}
}