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
package org.ebayopensource.scc;

import java.io.IOException;
import java.net.ServerSocket;

import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.impl.ThreadPoolConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ebayopensource.scc.config.AppConfiguration;
import org.ebayopensource.scc.debug.DebugManager;
import org.ebayopensource.scc.filter.CacheHttpFilterSource;
import org.ebayopensource.scc.track.TrackerClient;
import org.ebayopensource.io.netty.http.snoop.HttpSnoopServer;

public class Launcher {

	private static final int MIN_PORT_NUMBER = 1;
	private static final int MAX_PORT_NUMBER = 65535;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(Launcher.class);

	public static void main(String[] args) throws Exception {
		// Application config init
		AppCtxInitializer appCtxInit = new AppCtxInitializer();
		AppCtx ctx = null;
		try {
			ctx = appCtxInit.init(args);
		} catch (ProxyServerException e) {
			LOGGER.error(e.getMessage(), e);
			System.exit(e.getType());
		}
		final AppConfiguration config = ctx.getAppConfig();

		// check port available
		if (!available(config.getProxyPort())) {
			LOGGER.error("Proxy port is already in use: "
					+ config.getProxyPort());
			System.exit(ErrorCode.PROXY_PORT_ALREADY_IN_USE);
		} else if (!available(config.getAdminPort())) {
			LOGGER.error("Admin port is already in use: "
					+ config.getAdminPort());
			System.exit(ErrorCode.AMDIN_PORT_ALREADY_IN_USE);
		}

		// launch a thread for proxy server
		try {
			ThreadPoolConfiguration tpc = new ThreadPoolConfiguration()
					.withAcceptorThreads(
							config.getInt("threadPool.acceptorThreads"))
					.withClientToProxyWorkerThreads(
							config.getInt("threadPool.clientToProxyThreads"))
					.withProxyToServerWorkerThreads(
							config.getInt("threadPool.proxyToServerThreads"));
			DefaultHttpProxyServer.bootstrap().withPort(config.getProxyPort())
					.withManInTheMiddle(new ProxyServerMitmManager())
					.withFiltersSource(new CacheHttpFilterSource(ctx))
					.withThreadPoolConfiguration(tpc).start();
		} catch (Exception e) {
			System.exit(ErrorCode.DEFAULT_ERROR_CODE);
		}
		LOGGER.info("Service COLD Cache start up, listening on port: "
				+ config.getProxyPort());

		// launch a thread for admin console
		Thread adminThread = new Thread() {
			public void run() {
				try {
					int adminServerPort = config.getAdminPort();
					LOGGER.info("Starting Admin Server, listening on port: "
							+ adminServerPort);
					new HttpSnoopServer(adminServerPort).run();
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		};
		adminThread.setDaemon(true);
		adminThread.start();

		// launch a thread for debugger when enabled
		DebugManager debugManager = ctx.getRegistry().getDebugManager();
		if (debugManager.debugEnabled()) {
			debugManager.setupDebugChannel(config.getDebugPort());
		}

		// launch a tracker client to send tracking info
		if (config.getBoolean("trackerClient.enabled")) {
			new TrackerClient(config, ctx.getRegistry().getScheduledService())
					.start();
		}
	}

	public static boolean available(int port) {
		if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
			return false;
		}
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			return true;
		} catch (IOException e) {
			// do nothing
		} finally {
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}

		return false;
	}
}
