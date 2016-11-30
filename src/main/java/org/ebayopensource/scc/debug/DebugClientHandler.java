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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugClientHandler extends SimpleChannelInboundHandler<HttpObject> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DebugClientHandler.class);

	public DebugClientHandler() {
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
		if (LOGGER.isDebugEnabled()) {
			if (msg instanceof HttpResponse) {
				HttpResponse response = (HttpResponse) msg;
				LOGGER.debug("STATUS: " + response.getStatus());
				LOGGER.debug("VERSION: " + response.getProtocolVersion());
				if (!response.headers().isEmpty()) {
					for (String name : response.headers().names()) {
						for (String value : response.headers().getAll(name)) {
							LOGGER.debug("HEADER: " + name + " = " + value);
						}
					}
				}
				if (HttpHeaders.isTransferEncodingChunked(response)) {
					LOGGER.debug("CHUNKED CONTENT {");
				} else {
					LOGGER.debug("CONTENT {");
				}
			}
			if (msg instanceof HttpContent) {
				HttpContent content = (HttpContent) msg;
				LOGGER.debug(content.content().toString(CharsetUtil.UTF_8));
				if (content instanceof LastHttpContent) {
					LOGGER.debug("} END OF CONTENT");
				}
			}
		}
	}

	@Override
	public void exceptionCaught(
			ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOGGER.error(cause.getMessage());
		ctx.close();
	}

}