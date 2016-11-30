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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringEncoder;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ebayopensource.scc.AppCtxInitializer;
import org.ebayopensource.scc.config.AppConfiguration;

/**
 * 
 */
public class HttpSnoopClient {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(HttpSnoopClient.class);

	private final URI uri;

	public HttpSnoopClient(URI uri) {
		this.uri = uri;
	}

	public boolean run() throws Exception {
		String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
		String host = uri.getHost() == null ? "localhost" : uri.getHost();
		int port = uri.getPort();
		if (port == -1) {
			if ("http".equalsIgnoreCase(scheme)) {
				port = 80;
			} else if ("https".equalsIgnoreCase(scheme)) {
				port = 443;
			}
		}

		if (!"http".equalsIgnoreCase(scheme)
				&& !"https".equalsIgnoreCase(scheme)) {
			LOGGER.error("Only HTTP(S) is supported.");
			return false;
		}

		boolean ssl = "https".equalsIgnoreCase(scheme);

		// Configure the client.
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
					.handler(new HttpSnoopClientInitializer(ssl));

			// Make the connection attempt.
			Channel ch = b.connect(host, port).sync().channel();

			// Prepare the HTTP request.
			QueryStringEncoder encoder = new QueryStringEncoder(uri.toString());

			URI uriGet = null;
			try {
				uriGet = new URI(encoder.toString());
			} catch (URISyntaxException e) {
				LOGGER.error(e.getMessage());
			}

			FullHttpRequest request = new DefaultFullHttpRequest(
					HttpVersion.HTTP_1_1, HttpMethod.GET,
					uriGet.toASCIIString());

			request.headers().set(HttpHeaders.Names.HOST, host);
			request.headers().set(HttpHeaders.Names.CONNECTION,
					HttpHeaders.Values.CLOSE);
			request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING,
					HttpHeaders.Values.GZIP);
			request.headers().set(HttpHeaders.Names.CONNECTION,
					HttpHeaders.Values.CLOSE);

			// Send the HTTP request.
			ch.writeAndFlush(request);

			// Wait for the server to close the connection.
			ch.closeFuture().sync();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return false;
		} finally {
			// Shut down executor threads to exit.
			group.shutdownGracefully();
		}
		return true;
	}

	private static String appendParameters(String[] args, StringBuilder urlStr) {
		for (int idx = 1; idx < args.length; idx++) {
			if (idx % 2 == 0) {
				urlStr.append("=" + args[idx]);
			} else {
				if (idx == 1) {
					urlStr.append(args[idx]);
				} else {
					urlStr.append("&" + args[idx]);
				}
			}
		}
		return urlStr.toString();
	}

	public static boolean removeCacheDirectory(String cacheDirPath) {
		try {
			LOGGER.info("Cache directory is: " + cacheDirPath);
			FileUtils.deleteDirectory(new File(cacheDirPath));
			LOGGER.info("Cache directory " + cacheDirPath + " is deleted");
			return true;
		} catch (Exception e) {
			LOGGER.warn("Failed to delete the cache directory " + cacheDirPath);
		}
		return false;
	}

	public static void main(String[] args) throws Exception {
		Options options = buildCmdOptions();
		CommandLine cmdLine = new DefaultParser().parse(options, args, false);
		String[] cmdArgs = cmdLine.getArgs();
		if (cmdArgs.length > 0) {
			boolean result = false;
			String action = cmdArgs[0];

			AppConfiguration appConfig = AppCtxInitializer
					.initAppConfig(cmdLine);

			StringBuilder urlStr = new StringBuilder("http://127.0.0.1:")
					.append(appConfig.getAdminPort()).append("/cmd/")
					.append(action).append("?");
			URI uri = new URI(appendParameters(cmdArgs, urlStr));
			result |= new HttpSnoopClient(uri).run();

			// per the design, remove the cache directory even if the server is
			// down.
			if (!result
					&& action
							.equalsIgnoreCase(HttpSnoopServerHandler.CacheCommand.cleanupCache
									.toString())) {
				result |= removeCacheDirectory(appConfig.getCacheDir());
			}

			String msg = result ? "%s command is executed successfully."
					: "%s command failed.";
			LOGGER.info(String.format(msg, action));
		} else {
			System.out.println("Usage: "
					+ HttpSnoopClient.class.getSimpleName() + " <cmdName>");
		}
	}

	private static Options buildCmdOptions() {
		Options options = new Options();

		// user settings
		Option opt = new Option("s", "settings", true,
				"User settings for Service COLD Cache.");
		opt.setArgs(1);
		options.addOption(opt);

		return options;
	}
}
