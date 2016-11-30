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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.lang.reflect.Field;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.ebayopensource.scc.ProxyServerMitmManager;
import org.ebayopensource.scc.cache.CacheManager;
import org.ebayopensource.scc.cache.CacheResponse;
import org.ebayopensource.scc.cache.CacheResultVerifier;
import org.ebayopensource.scc.cache.CacheVerifiedResult;
import org.ebayopensource.scc.cache.policy.PolicyManager;
import org.ebayopensource.scc.config.AppConfiguration;
import org.littleshoot.proxy.impl.ClientToProxyConnection;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugManager {

	private static final String KEY_MAX_DEBUG_THREADS = "debugManager.maxDebugThreads";
	private Executor executor;
	private PolicyManager<FullHttpRequest, FullHttpResponse, CacheResponse> m_policyManager;
	private CacheManager<FullHttpRequest, FullHttpResponse, CacheResponse> m_cacheManager;
	private AppConfiguration m_appConfig;
	public static final AttributeKey<CacheResultVerifier> DEBUG_RESULT = AttributeKey.valueOf("debugResult");
	private static final Logger LOGGER = LoggerFactory.getLogger(DebugManager.class);
	private EventLoopGroup group = new NioEventLoopGroup();
	private Channel debugChannel;
	private Channel debugSslChannel;

	public DebugManager(AppConfiguration appConfig, PolicyManager<FullHttpRequest, FullHttpResponse, CacheResponse> policyManager) {
		Integer poolSize = appConfig.getInt(KEY_MAX_DEBUG_THREADS);
		executor = Executors.newFixedThreadPool(poolSize != null ? poolSize : 10);
		m_policyManager = policyManager;
		m_appConfig = appConfig;
		m_cacheManager = policyManager.getCacheManager();
	}

	public DefaultHttpProxyServer setupDebugChannel(int port) {
		try {
			LOGGER.info("Proxy Debug Server start up, listening on port: " + port);
			DebugHttpFilterSource debugFilterSource = new DebugHttpFilterSource();
			DefaultHttpProxyServer debugServer = (DefaultHttpProxyServer) DefaultHttpProxyServer.bootstrap().withPort(port)
					.withManInTheMiddle(new ProxyServerMitmManager())
					.withFiltersSource(debugFilterSource).start();
			debugServer.setIdleConnectionTimeout(0);

			Bootstrap clientBootstrap = new Bootstrap();
			clientBootstrap.group(group)
					.channel(NioSocketChannel.class)
					.handler(new DebugNettyClientInitializer());
			debugChannel = clientBootstrap.connect("127.0.0.1", port).sync().channel();
			debugSslChannel = clientBootstrap.connect("127.0.0.1", port).sync().channel();
			return debugServer;
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage());
		}
		return null;
	}

	public void shutdown() {
		group.shutdownGracefully();
	}

	private void forwardDebugRequest(FullHttpRequest request) {
		if (!ssl(request)) {
			if (debugChannel != null) {
				try {
					debugChannel.writeAndFlush(request);
				} catch (Exception e) {
					LOGGER.error(e.getMessage());
				}
			}
		} else {
			if (debugSslChannel != null) {
				try {
					debugSslChannel.writeAndFlush(request);
				} catch (Exception e) {
					LOGGER.error(e.getMessage());
				}
			}
		}
	}

	private boolean ssl(FullHttpRequest request) {
		return m_policyManager.generateCacheKey(request).startsWith("https:");
	}


	/**
	 * Issue a debug request when debugging is enabled.
	 *
	 * @param httpObject      Http request of client
	 * @param m_ctx           Netty context
	 * @param fromDebugFilter Indicator shows if the request require to be forwarded
	 * @return An indicator showing if debug manager consumes the request. If true, the caller needs to stop handling request.
	 */
	public void issueDebugRequest(FullHttpRequest httpObject, final ChannelHandlerContext m_ctx, boolean fromDebugFilter) {
		if (debugEnabled()) {
			final FullHttpRequest request = httpObject.copy();
			if (fromDebugFilter) {
				try {
					if (ssl(request)) {
						Field field = ClientToProxyConnection.class.getDeclaredField("mitming");
						field.setAccessible(true);
						field.set(m_ctx.handler(), true);
					}
				} catch (Exception e) {
					LOGGER.error(e.getMessage());
				}
				String key = m_policyManager.generateCacheKey(request);
				FullHttpResponse cacheResponse = m_policyManager.getCacheManager().get(key);
				CacheResultVerifier verifier = new CacheResultVerifier(key, request, cacheResponse);
				Attribute<CacheResultVerifier> debugging = m_ctx.attr(DEBUG_RESULT);
				debugging.set(verifier);
			} else {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						forwardDebugRequest(request);
					}
				});
			}
		}
	}


	/**
	 * @return Return true if debug is enabled
	 */
	public boolean debugEnabled() {
		return m_appConfig.getBoolean("debugManager.debugEnabled");
	}

	/**
	 * Process response debug
	 *
	 * @param response Response Object
	 * @param m_ctx    Netty Context
	 * @return Return indicator shows if the response requires handling for debugging. If true, the caller needs to stop handling response
	 */
	public boolean debugResponse(FullHttpResponse response, ChannelHandlerContext m_ctx) {
		boolean consume = false;
		if (debugEnabled()) {
			Attribute<CacheResultVerifier> debugResult = m_ctx.attr(DEBUG_RESULT);
			if (debugResult.get() != null) {
				try {
					CacheVerifiedResult result = debugResult.get().fetchResult(response);
					m_cacheManager.getStats().addCacheVerifiedResult(result);
				} catch (Exception e) {
					LOGGER.error(e.getMessage());
				} finally {
					consume = true;
				}
			}
		}
		return consume;
	}

}
