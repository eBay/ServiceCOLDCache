/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.ebayopensource.io.netty.http.snoop;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.CharsetUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpSnoopClientHandler extends
		SimpleChannelInboundHandler<HttpObject> {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(HttpSnoopClientHandler.class);
	@Override
	public void channelRead0(ChannelHandlerContext ctx, HttpObject msg)
			throws Exception {
		if (msg instanceof HttpResponse) {
			HttpResponse response = (HttpResponse) msg;

			LOGGER.info("Response status: " + response.getStatus());
			if (response.getStatus().equals(OK)) {
				LOGGER.info("Operation is successful");
			} else {
				LOGGER.error("Operation is failed");
			}
		}

		if (msg instanceof HttpContent) {
			HttpContent content = (HttpContent) msg;

			System.out.print(content.content().toString(CharsetUtil.UTF_8));
			System.out.flush();

		}

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
