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

import java.io.IOException;
import java.util.List;

public class AppConfiguration {

	private static final String APP_NAME = "Service COLD Cache";
	/**
	 * default global config file is embedded in jar.
	 */
	public static final String DEFAULT_GLOBAL_CONFIG_PATH = "/config.json";
	/**
	 * default user config file is in app working dir.
	 */
	public static final String DEFAULT_USER_CONFIG_PATH = "./config.json";

	public static final String KEY_ENABLE_CACHE = "enableCache";

	private Config m_config;
	private ConfigLoader m_configLoader;
	private boolean m_isInit = false;
	private String userConfigPath;
	public static final String KEY_DEBUG_INFO = "debugInfo";
	private String m_version;

	public AppConfiguration(ConfigLoader configLoader, String userConfigPath) {
		m_configLoader = configLoader;
		this.userConfigPath = userConfigPath;
	}

	public void init() throws IOException {
		if (isInitialized()) {
			return;
		}
		if (userConfigPath == null) {
			userConfigPath = System
					.getProperty("org.ebayopensource.scc.userConfigPath");
		}
		if (userConfigPath == null) {
			userConfigPath = DEFAULT_USER_CONFIG_PATH;
		}
		m_config = m_configLoader.load(DEFAULT_GLOBAL_CONFIG_PATH,
				userConfigPath);

		m_version = m_configLoader.loadVersion();

		m_isInit = true;
	}

	public boolean isInitialized() {
		return m_isInit;
	}

	public Config getConfig() {
		return m_config;
	}

	public String getString(String name) {
		return m_config.getString(name);
	}

	public Integer getInt(String name) {
		return m_config.getInt(name);
	}

	public Double getDouble(String name) {
		return m_config.getDouble(name);
	}

	public Boolean getBoolean(String name) {
		return m_config.getBoolean(name);
	}

	public List<Object> getList(String name) {
		return m_config.getList(name);
	}

	public void put(String name, Object value) {
		Object oldValue = m_config.get(name);
		if ((value == null && oldValue != null)
				|| (value != null && !value.equals(oldValue))) {
			synchronized (m_config) {
				m_config.setConfig(name, value);
			}
		}
	}

	public boolean isCacheEnabled() {
		return m_config.getBoolean(KEY_ENABLE_CACHE);
	}

	public String getCacheDir() {
		return getString("cacheDir");
	}

	public int getProxyPort() {
		return getInt("proxyPort");
	}

	public int getDebugPort() {
		return getInt("debugPort");
	}

	public int getAdminPort() {
		return getInt("adminPort");
	}

	public void setProxyPort(int port) {
		put("proxyPort", port);
	}

	public void setAdminPort(int port) {
		put("adminPort", port);
	}

	public void setDebugPort(int port) {
		put("debugPort", port);
	}

	public void setCacheDir(String cacheDir) {
		put("cacheDir", cacheDir);
	}

	public String getAppVersion() {
		return m_version;
	}

	public String getAppName() {
		return APP_NAME;
	}
}
