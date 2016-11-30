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

import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.ebayopensource.scc.cache.CacheStats;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ebayopensource.scc.AppCtx;

public class HttpSnoopServerHandler extends SimpleChannelInboundHandler<Object> {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(HttpSnoopServerHandler.class);

    private HttpRequest request;
    /** Buffer that stores the response content */
    private final StringBuilder buf = new StringBuilder();
    private HttpHeaders respHttpHeaders = new DefaultHttpHeaders();

	public enum CacheCommand {
		enableCache, disableCache, refreshCache, cleanupCache, cacheStatus
	}
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;

            if (is100ContinueExpected(request)) {
                send100Continue(ctx);
            }

            buf.setLength(0);
//            buf.append("WELCOME TO THE WILD WILD WEB SERVER\r\n");
//            buf.append("===================================\r\n");
//
//            buf.append("VERSION: ").append(request.getProtocolVersion()).append("\r\n");
//            buf.append("HOSTNAME: ").append(getHost(request, "unknown")).append("\r\n");
//            buf.append("REQUEST_URI: ").append(request.getUri()).append("\r\n\r\n");

//            List<Map.Entry<String, String>> headers = request.headers().entries();
//            if (!headers.isEmpty()) {
//                for (Map.Entry<String, String> h: request.headers().entries()) {
//                    String key = h.getKey();
//                    String value = h.getValue();
//                    buf.append("HEADER: ").append(key).append(" = ").append(value).append("\r\n");
//                }
//                buf.append("\r\n");
//            }

            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
            Map<String, List<String>> params = queryStringDecoder.parameters();


            if (!params.isEmpty()) {
                for (Entry<String, List<String>> p: params.entrySet()) {
                    String key = p.getKey();
                    List<String> vals = p.getValue();
                    for (String val : vals) {
                        buf.append("PARAM: ").append(key).append(" = ").append(val).append("\r\n");
                    }
                }
                buf.append("\r\n");
            }

            String cmd = request.getUri();
            handleCommand(cmd, buf, ctx);
            appendDecoderResult(buf, request);
        }

        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;

            ByteBuf content = httpContent.content();
            if (content.isReadable()) {
//                buf.append("CONTENT: ");
                buf.append(content.toString(CharsetUtil.UTF_8));
                buf.append("\r\n");
                appendDecoderResult(buf, request);
            }

            if (msg instanceof LastHttpContent) {
                //buf.append("END OF CONTENT\r\n");

                LastHttpContent trailer = (LastHttpContent) msg;
//                if (!trailer.trailingHeaders().isEmpty()) {
//                    buf.append("\r\n");
//                    for (String name: trailer.trailingHeaders().names()) {
//                        for (String value: trailer.trailingHeaders().getAll(name)) {
//                            buf.append("TRAILING HEADER: ");
//                            buf.append(name).append(" = ").append(value).append("\r\n");
//                        }
//                    }
//                    buf.append("\r\n");
//                }
//
                writeResponse(trailer, ctx);
            }
        }
    }

	private void handleCommand(String cmd, StringBuilder buf, ChannelHandlerContext ctx) {
		if (cmd.contains(CacheCommand.enableCache.toString())) {
			LOGGER.info("enable cache......");
			// enable cache
			AppCtx.getInstance().enableCache(true);
			LOGGER.info("enable cache done......");
		} else if (cmd.contains(CacheCommand.disableCache.toString())) {
			LOGGER.info("disable cache......");
			// disable cache
			AppCtx.getInstance().enableCache(false);
			LOGGER.info("disable cache done......");
		} else if (cmd.contains(CacheCommand.cleanupCache.toString())) {
			LOGGER.info("cleanup cache......");
			// cleanup cache
			AppCtx.getInstance().getCacheManager().cleanupCache();
			LOGGER.info("cleanup cache done......");
		} else if (cmd.contains(CacheCommand.refreshCache.toString())) {
			LOGGER.info("refresh cache......");
			// per Patrick, right now the logic for refreshCache is same with cleanupCache.
			// so refreshCache command is not supported in scripts to avoid getting user confused.
			AppCtx.getInstance().getCacheManager().cleanupCache();
			LOGGER.info("refresh cache done......");
		} else if (cmd.contains(CacheCommand.cacheStatus.toString())) {
            CacheStats cacheStats = AppCtx.getInstance().getCacheManager().getStats();
            buf.append("{\"hitCount\": ")
                    .append(cacheStats.getHitCount())
                    .append(", \"missedCount\":")
                    .append(cacheStats.getMissedCount())
                    .append("}");
            respHttpHeaders.set(CONTENT_TYPE, "application/json; charset=UTF-8");
        }
	}

    private static void appendDecoderResult(StringBuilder buf, HttpObject o) {
        DecoderResult result = o.getDecoderResult();
        if (result.isSuccess()) {
            return;
        }

        buf.append(".. WITH DECODER FAILURE: ");
        buf.append(result.cause());
        buf.append("\r\n");
    }

    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
        // Decide whether to close the connection or not.
        boolean keepAlive = isKeepAlive(request);
        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, currentObj.getDecoderResult().isSuccess()? OK : BAD_REQUEST,
                Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        // set request specified headers, may overwrite default header
        for(String name: respHttpHeaders.names()) {
            response.headers().set(name, respHttpHeaders.get(name));
        }

        // Encode the cookie.
//        String cookieString = request.headers().get(COOKIE);
//        if (cookieString != null) {
//            Set<Cookie> cookies = CookieDecoder.decode(cookieString);
//            if (!cookies.isEmpty()) {
//                // Reset the cookies if necessary.
//                for (Cookie cookie: cookies) {
//                    response.headers().add(SET_COOKIE, ServerCookieEncoder.encode(cookie));
//                }
//            }
//        } else {
//            // Browser sent no cookie.  Add some.
//            response.headers().add(SET_COOKIE, ServerCookieEncoder.encode("key1", "value1"));
//            response.headers().add(SET_COOKIE, ServerCookieEncoder.encode("key2", "value2"));
//        }

        // Write the response.
        ctx.write(response).addListener(ChannelFutureListener.CLOSE);;

        return keepAlive;
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
