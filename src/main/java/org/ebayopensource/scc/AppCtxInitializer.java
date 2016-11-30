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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.ebayopensource.scc.config.AppConfiguration;
import org.ebayopensource.scc.config.ConfigLoader;

public class AppCtxInitializer {

	private static final String DEFAULT_CACHE_FOLDER = ".proxycache";

	public AppCtx init(String[] args) throws ParseException,
			ProxyServerException {
		Options options = buildCmdOptions();
		CommandLine cmdLine = new DefaultParser().parse(options, args, false);

		AppConfiguration appConfig = initAppConfig(cmdLine);
		Registry registry = initRegistry(appConfig);

		AppCtx appCtx = AppCtx.getInstance();
		appCtx.init(appConfig, registry);
		return appCtx;
	}

	private Registry initRegistry(AppConfiguration appConfig) {
		Registry registry = new Registry(appConfig);
		registry.init();
		return registry;
	}

	public static AppConfiguration initAppConfig(CommandLine cmdLine)
			throws ProxyServerException {
		AppConfiguration appConfig = new AppConfiguration(new ConfigLoader(),
				cmdLine.getOptionValue('s'));
		try {
			appConfig.init();
		} catch (IOException e) {
			throw new ProxyServerException(e.getMessage(),
					ErrorCode.INVALID_CONFIG_FILE, e);
		}

		try {
			char opt = 'p';
			int proxyPort = getInt(cmdLine, opt);
			if (proxyPort != -1) {
				appConfig.setProxyPort(proxyPort);
			}

			int adminPort = getInt(cmdLine, 'a');
			if (adminPort != -1) {
				appConfig.setAdminPort(adminPort);
			}
			String cacheDirPath = cmdLine.getOptionValue('c');
			if (cacheDirPath != null && !cacheDirPath.isEmpty()) {
				appConfig.setCacheDir(cacheDirPath);
			}
		} catch (Exception e) {
			throw new ProxyServerException(e.getMessage(),
					ErrorCode.INVALID_CMD_ARGS, e);
		}

		if (appConfig.getCacheDir() == null) {
			appConfig.setCacheDir("./" + DEFAULT_CACHE_FOLDER);
		}

		return appConfig;
	}

	public static int getInt(CommandLine cmdLine, char opt) {
		String value = cmdLine.getOptionValue(opt);
		int num = -1;
		if (value != null) {
			num = Integer.parseInt(value);
		}
		return num;
	}

	private Options buildCmdOptions() {
		Options options = new Options();

		// user settings
		Option opt = new Option("s", "settings", true,
				"User settings for Service COLD Cache.");
		opt.setArgs(1);
		options.addOption(opt);

		opt = new Option("p", "proxyPort", true,
				"Specify proxy port, default is 32876.");
		opt.setArgs(1);
		options.addOption(opt);

		opt = new Option("a", "adminPort", true,
				"Specify admin port, default is 55321.");
		opt.setArgs(1);
		options.addOption(opt);

		opt = new Option("c", "cacheDir", true,
				"Specify cache directory, default is ./cache directory.");
		opt.setArgs(1);
		options.addOption(opt);

		return options;
	}

}
