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
package org.ebayopensource.scc.cache;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CacheResponse implements Serializable {

	private static final long serialVersionUID = -4568586131869328195L;

	private int code;
	private byte[] content;
	private List<CacheEntry<String, String>> headers;
	private String protocalVersion;
	private String reasonPhrase;

	private List<CacheEntry<String, String>> trailingHeaders;

	public CacheResponse(String protocolVersion, int code, String reasonPhrase,
			List<CacheEntry<String, String>> headers,
			List<CacheEntry<String, String>> trailingHeaders) {
		this.protocalVersion = protocolVersion;
		this.code = code;
		this.reasonPhrase = reasonPhrase;
		this.headers = headers;
		this.trailingHeaders = trailingHeaders;
	}

	public int getCode() {
		return code;
	}

	public byte[] getContent() {
		return content == null ? null : Arrays.copyOf(content, content.length);
	}

	public List<CacheEntry<String, String>> getHeaders() {
		return headers;
	}

	public String getProtocalVersion() {
		return protocalVersion;
	}

	public String getReasonPhrase() {
		return reasonPhrase;
	}

	public List<CacheEntry<String, String>> getTrailingHeaders() {
		return trailingHeaders;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public void setContent(byte[] content) {
		this.content = content;

	}

	public void setHeaders(List<CacheEntry<String, String>> headers) {
		this.headers = headers;
	}

	public void setProtocalVersion(String protocalVersion) {
		this.protocalVersion = protocalVersion;
	}

	public void setReasonPhrase(String reasonPhrase) {
		this.reasonPhrase = reasonPhrase;
	}

	public void setTrailingHeaders(
			List<CacheEntry<String, String>> trailingHeaders) {
		this.trailingHeaders = trailingHeaders;
	}

	static class CacheEntry<K extends Serializable, V extends Serializable>
			implements Map.Entry<K, V>, Serializable {
		private static final long serialVersionUID = 7023407117446047150L;
		final K key;
		V value;

		/**
		 * Creates new entry.
		 */
		CacheEntry(K k, V v) {
			value = v;
			key = k;
		}

		public final K getKey() {
			return key;
		}

		public final V getValue() {
			return value;
		}

		public final V setValue(V newValue) {
			V oldValue = value;
			value = newValue;
			return oldValue;
		}

		public final boolean equals(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry e = (Map.Entry) o;
			Object k1 = getKey();
			Object k2 = e.getKey();
			if (k1 == k2 || (k1 != null && k1.equals(k2))) {
				Object v1 = getValue();
				Object v2 = e.getValue();
				if (v1 == v2 || (v1 != null && v1.equals(v2)))
					return true;
			}
			return false;
		}

		public final int hashCode() {
			return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
		}

		public final String toString() {
			return getKey() + "=" + getValue();
		}
	}
}
