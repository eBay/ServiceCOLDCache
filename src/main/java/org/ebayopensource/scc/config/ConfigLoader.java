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
package org.ebayopensource.scc.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class ConfigLoader {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ConfigLoader.class);

	public static Config load(InputStream is) {
		GsonBuilder gb = new GsonBuilder();
		Gson gson = gb.create();
		@SuppressWarnings("unchecked")
		Map<String, Object> fromJson = gson.fromJson(new InputStreamReader(is),
				Map.class);

		return load(fromJson);
	}

	public static Config load(Map<String, Object> configs) {
		return new Config(configs);
	}

	public Config load(String globalConfigFile, String userConfigFile)
			throws IOException {
		InputStream is = null;
		Config globalConfig = null;
		try {
			is = getClass().getResourceAsStream(globalConfigFile);
			globalConfig = load(is);
		} finally {
			if (is != null) {
				is.close();
			}
		}

		File ucf = new File(userConfigFile);
		if (ucf.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(ucf);
				Config userConfig = load(fis);
				globalConfig = merge(globalConfig, userConfig);
			} catch (FileNotFoundException e) {
				LOGGER.error(e.getMessage());
			} catch (JsonSyntaxException e) {
				LOGGER.error("Please correct the malformed config.json:"
						+ userConfigFile, e);
			} finally {
				if (fis != null) {
					fis.close();
				}
			}
		}

		return globalConfig;
	}

	private Config merge(Config globalConfig, Config userConfig) {
		globalConfig.m_configs.putAll(userConfig.m_configs);
		return globalConfig;
	}

	public String loadVersion() throws IOException {
		InputStream is = null;
		try {
			is = getClass().getResourceAsStream("/version.txt");
			byte[] buf = new byte[32];
			int len = is.read(buf);
			return new String(buf, 0, len);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}
}
