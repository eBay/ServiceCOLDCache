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
package org.ebayopensource.scc.track;

import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AutoRetryHttpClient;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ebayopensource.scc.config.AppConfiguration;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;

public class TrackerClient {

	private static final String ERROR_FAILED_TO_SEND = "Failed to send tracking data to service.";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TrackerClient.class);

	private String m_host;
	private int m_port;
	private String m_path;
	private boolean m_isSecure;
	private int m_interval;
	private ScheduledExecutorService m_scheduledService;


	public TrackerClient(AppConfiguration appConfig, ScheduledExecutorService scheduledService) {
		m_scheduledService = scheduledService;
		init(appConfig);
	}

	private void init(AppConfiguration appConfig) {
		m_host = appConfig.getString("trackerClient.endpoint.host");
		Integer value = appConfig.getInt("trackerClient.endpoint.port");
		m_port = value != null ? value : -1;
		m_path = appConfig.getString("trackerClient.endpoint.path");
		m_isSecure = appConfig.getBoolean("trackerClient.endpoint.isSecure");
		value = appConfig.getInt("trackerClient.request.interval");
		m_interval = value != null ? value : 86400;
	}

	public void start() {
		Runnable thread = new Runnable() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run() {
				Tracker tracker = new Tracker(getUser(), getHostName(),
						getIp(), getOs(), System.currentTimeMillis(), getInitiator());

				AutoRetryHttpClient client = new AutoRetryHttpClient(
						new DefaultServiceUnavailableRetryStrategy(3, 3000));
				HttpPost request = createRequest(tracker);
				HttpHost httpHost = createHttpHost();

				try {
					HttpResponse resp = client.execute(httpHost, request);
					int code = resp.getStatusLine().getStatusCode();
					if (HttpResponseStatus.OK.code() == code) {
						InputStream is = null;
						try {
							is = resp.getEntity().getContent();
							is = new BufferedInputStream(is);
							InputStreamReader reader = new InputStreamReader(is);
							JsonStreamParser parser = new JsonStreamParser(
									reader);
							while (parser.hasNext()) {
								JsonElement json = parser.next();
								LOGGER.debug("Tracking data sent: " + json);
							}
						} finally {
							if (is != null) {
								is.close();
							}
						}

					} else {
						LOGGER.error(ERROR_FAILED_TO_SEND);
						LOGGER.debug("Response code: " + code);
					}
				} catch (IOException e) {
					LOGGER.warn(ERROR_FAILED_TO_SEND);
				}
			}

		};

		m_scheduledService.scheduleAtFixedRate(thread, 0, m_interval,
				TimeUnit.SECONDS);
	}

	protected String getInitiator() {
		String initiator = System.getProperty("org.ebayopensource.ssc.initiator");
		return (initiator != null && !initiator.isEmpty()) ? initiator
				: Tracker.UNKNOWN;
	}

	protected HttpEntity createEntity(Object pojo) {
		Gson gson = new GsonBuilder().serializeNulls()
				.setFieldNamingStrategy(new FieldNamingStrategy() {

					@Override
					public String translateName(Field field) {
						String name = field.getName();
						if (name.startsWith("m_")) {
							name = name.substring(2);
						}
						return name;
					}
				}).create();
		return new StringEntity(gson.toJson(pojo), ContentType.APPLICATION_JSON);
	}

	public String getHostName() {
		InetAddress ia = getInetAddress();
		return ia != null ? ia.getCanonicalHostName() : Tracker.UNKNOWN;
	}

	private InetAddress getInetAddress() {
		Enumeration<NetworkInterface> eni = null;
		try {
			eni = NetworkInterface.getNetworkInterfaces();
			while (eni.hasMoreElements()) {
				NetworkInterface n = eni.nextElement();
				Enumeration<InetAddress> eia = n.getInetAddresses();
				while (eia.hasMoreElements()) {
					InetAddress current = eia.nextElement();
					if (current.isSiteLocalAddress()) {
						return current;
					}
				}
			}
			return InetAddress.getLocalHost();
		} catch (SocketException | UnknownHostException ex) {
			LOGGER.warn("Failed to get IP address.", ex);
			return null;
		}
	}

	public String getIp() {
		InetAddress ia = getInetAddress();
		return ia != null ? ia.getHostAddress() : Tracker.UNKNOWN;
	}

	public String getOs() {
		return System.getProperty("os.name");
	}

	public String getUser() {
		return System.getProperty("user.name");
	}

	protected HttpHost createHttpHost() {
		int port = m_isSecure ? 443 : 80;
		if (m_port != -1) {
			port = m_port;
		}
		String schema = m_isSecure ? "https" : "http";
		HttpHost httpHost = new HttpHost(m_host, port, schema);
		return httpHost;
	}

	protected HttpPost createRequest(Object pojo) {
		HttpPost request = new HttpPost(m_path);
		request.setEntity(createEntity(pojo));
		return request;
	}

}
