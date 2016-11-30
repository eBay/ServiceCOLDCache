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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Config {

	protected Map<String, Object> m_configs = new HashMap<>();

	public Config(Map<String, Object> configs) {
		m_configs = configs;
	}

	public String getString(String name) {
		Object value = m_configs.get(name);
		return value != null ? value.toString() : null;
	}

	public Integer getInt(String name) {
		Object value = m_configs.get(name);
		if (value instanceof Double) {
			return ((Double) value).intValue();
		}
		return value != null ? Integer.valueOf(value.toString()) : null;
	}

	public Double getDouble(String name) {
		Object value = m_configs.get(name);
		if (value instanceof Double) {
			return ((Double) value);
		}
		return value != null ? Double.valueOf(value.toString()) : null;
	}

	public Boolean getBoolean(String name) {
		Object value = m_configs.get(name);
		if (value instanceof Boolean) {
			return ((Boolean) value);
		}
		return value != null ? Boolean.valueOf(value.toString()) : null;
	}

	@SuppressWarnings("unchecked")
	public List<Object> getList(String name) {
		Object value = m_configs.get(name);
		if (value instanceof List) {
			return ((List<Object>) value);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getMap(String name) {
		Object value = m_configs.get(name);
		if (value instanceof Map) {
			return ((Map<String, Object>) value);
		}
		return null;
	}

	public Config getSubConfig(String name) {
		Object value = m_configs.get(name);
		if (value instanceof Config) {
			return (Config) value;
		}
		Map<String, Object> subConfigs = getMap(name);
		return subConfigs != null ? ConfigLoader.load(subConfigs) : null;
	}

	public Object get(String name) {
		return m_configs.get(name);
	}

	public Map<String, Object> asMap() {
		return Collections.unmodifiableMap(m_configs);
	}

	public void setConfig(String name, Object value) {
		m_configs.put(name, value);
	}

	public Set<String> getNames() {
		return Collections.unmodifiableSet(m_configs.keySet());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_configs == null) ? 0 : m_configs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof Config) {
			Config other = (Config) obj;
			if (m_configs == null) {
				if (other.m_configs != null)
					return false;
			} else if (!m_configs.equals(other.m_configs)) {
				return false;
			} else if (m_configs.equals(other.m_configs)) {
				return true;
			}
		} else if (obj instanceof Map) {
			if (m_configs == null) {
				if (obj != null)
					return false;
			} else if (!m_configs.equals(obj)) {
				return false;
			} else if (m_configs.equals(obj)) {
				return true;
			}
		}
		return false;
	}
}
