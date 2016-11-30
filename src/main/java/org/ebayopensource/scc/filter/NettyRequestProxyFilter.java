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
package org.ebayopensource.scc.filter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ebayopensource.scc.cache.CacheResponse;
import org.ebayopensource.scc.cache.policy.CacheDecisionObject;
import org.ebayopensource.scc.cache.policy.PolicyManager;
import org.ebayopensource.scc.config.AppConfiguration;
import com.google.gson.JsonObject;

/**
 * for LittleProxy request filter
 * 
 */
public class NettyRequestProxyFilter implements
		IHttpRequestProxyFilter<HttpObject, ChannelHandlerContext> {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(NettyRequestProxyFilter.class);

	private static final String KEY_MAX_SEND_BUFFER_SIZE = "nettyRequestProxyFilter.maxSendBufferSize";

	public static final AttributeKey<String> CACHE_KEY = AttributeKey
			.<String> valueOf("cacheKey");
	public static final AttributeKey<String> REQUEST_URI = AttributeKey
			.<String> valueOf("requestUri");
	public static final AttributeKey<Boolean> IS_CACHABLE = AttributeKey
			.<Boolean> valueOf("isCachable");

	private int m_maxSendBufferSize;

	private PolicyManager<FullHttpRequest, FullHttpResponse, CacheResponse> m_policyManager;

	private boolean m_debugInfo;

	private AppConfiguration m_appConfig;

	private static final DefaultFullHttpResponse RESPONSE_404;
	
	static {
		RESPONSE_404 = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
		HttpHeaders.setContentLength(RESPONSE_404, 0);
		HttpHeaders.setKeepAlive(RESPONSE_404, false);
	}

	public NettyRequestProxyFilter(
			PolicyManager<FullHttpRequest, FullHttpResponse, CacheResponse> policyManager,
			AppConfiguration appConfig) {
		m_policyManager = policyManager;
		m_maxSendBufferSize = appConfig.getInt(KEY_MAX_SEND_BUFFER_SIZE);
		m_debugInfo = appConfig.getBoolean(AppConfiguration.KEY_DEBUG_INFO);
		m_appConfig = appConfig;
	}

	@Override
	public HttpResponse filterRequest(HttpObject reqObject,
			ChannelHandlerContext reqCtx) {
		if (reqObject instanceof HttpRequest) {
			String uri = ((HttpRequest) reqObject).getUri();
			LOGGER.info("Received a request: " + uri);
			reqCtx.attr(REQUEST_URI).setIfAbsent(uri);
		}

		if (reqObject instanceof FullHttpRequest) {
			FullHttpRequest req = (FullHttpRequest) reqObject;
			if (!isProxyRequest(req)) {
				return handleNonProxyRequest(req);
			}
			boolean isCachable = m_policyManager
					.cacheIsNeededFor(new CacheDecisionObject(req.getUri(), req
							.getMethod().name()));
			reqCtx.attr(IS_CACHABLE).setIfAbsent(isCachable);
			if (isCachable) {
				String key = m_policyManager.generateCacheKey(req);
				FullHttpResponse response = m_policyManager.getCacheManager()
						.get(key);
				Attribute<String> attr = reqCtx.attr(CACHE_KEY);
				attr.set(key);
				debugRequestInfo(reqObject, key);
				if (response != null) {
					long bSize = 0;
					if (response.headers().contains(
							HttpHeaders.Names.CONTENT_LENGTH)) {
						bSize = HttpHeaders.getContentLength(response);
					} else {
						bSize = response.content().readableBytes();
					}

					if (bSize != 0) {
						setBufferSizeIfConfigIsSocketChannelConfig(reqCtx
								.channel().config(), bSize);
					}
					LOGGER.info("HIT CACHE: " + key);
				}
				return response;
			}
		}
		debugRequestInfo(reqObject, null);
		return null;
	}

	protected HttpResponse handleNonProxyRequest(FullHttpRequest req) {
		String uri = req.getUri();
		if ("/version".equals(uri)) {
			if (HttpMethod.GET.equals(req.getMethod())) {
				JsonObject jsonObj = new JsonObject();
				jsonObj.addProperty("name", m_appConfig.getAppName());
				jsonObj.addProperty("version", m_appConfig.getAppVersion());
				byte[] content = jsonObj.toString().getBytes(CharsetUtil.UTF_8);
				DefaultFullHttpResponse resp = new DefaultFullHttpResponse(
						HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
						Unpooled.copiedBuffer(content));
				HttpHeaders.setKeepAlive(resp, false);
				HttpHeaders.setHeader(resp, HttpHeaders.Names.CONTENT_TYPE,
						"application/json");
				HttpHeaders.setContentLength(resp, content.length);
				return resp;
			}
		}
		
		return RESPONSE_404;
	}

	protected boolean isProxyRequest(FullHttpRequest req) {
		String host = HttpHeaders.getHost(req);
		if (host == null || host.isEmpty()) {
			return true;
		}
		String[] values = host.split(":");
		if (values.length < 2) {
			return true;
		}
		String hostName = values[0];
		String port = values[1];
		if (!port.equals(String.valueOf(m_appConfig.getProxyPort()))) {
			return true;
		}
		try {
			InetAddress target = InetAddress.getByName(hostName);
			Enumeration<NetworkInterface> eni = null;
			eni = NetworkInterface.getNetworkInterfaces();
			while (eni.hasMoreElements()) {
				NetworkInterface n = eni.nextElement();
				Enumeration<InetAddress> eia = n.getInetAddresses();
				while (eia.hasMoreElements()) {
					InetAddress current = eia.nextElement();
					if (current.equals(target)) {
						return false;
					}
				}
			}
		} catch (UnknownHostException | SocketException e) {
			LOGGER.warn(e.getMessage());
		}
		return true;
	}

	private void setBufferSizeIfConfigIsSocketChannelConfig(
			ChannelConfig config, long contentLength) {
		if (config instanceof SocketChannelConfig) {
			int sendBufferSize = contentLength < m_maxSendBufferSize ? (int) contentLength
					: m_maxSendBufferSize;
			((SocketChannelConfig) config).setSendBufferSize(sendBufferSize);
		}
	}

	private void debugRequestInfo(HttpObject httpObject, String key) {
		if (m_debugInfo && httpObject instanceof HttpRequest) {
			if (key != null) {
				LOGGER.debug("Cache Key: " + key);
			}
			if (httpObject instanceof FullHttpRequest) {
				FullHttpRequest req = (FullHttpRequest) httpObject;
				HttpHeaders headers = req.headers();
				LOGGER.debug("Headers:");
				for (Iterator<Entry<String, String>> it = headers.iterator(); it
						.hasNext();) {
					Entry<String, String> entry = it.next();
					LOGGER.debug("\t" + entry.getKey() + ":\t"
							+ entry.getValue());
				}
				ByteBuf content = req.content();
				int length = content.readableBytes();
				LOGGER.debug("Content Length: " + length);
				if (length != 0) {
					LOGGER.debug("Content: "
							+ content.toString(Charset.forName("UTF-8")));
				}
			}
		}
	}

}
