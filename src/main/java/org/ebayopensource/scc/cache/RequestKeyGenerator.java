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

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ebayopensource.scc.cache.policy.CacheDecisionObject;
import org.ebayopensource.scc.config.AppConfiguration;
import org.ebayopensource.scc.util.Checksum;
import org.ebayopensource.scc.util.WildcardMatcher;

public class RequestKeyGenerator implements IKeyGenerator<FullHttpRequest> {

	private Set<String> m_skipHeaders;
	private AppConfiguration m_appConfig;
	private boolean m_uriMatchEnabled;
	private List<Map<String, Object>> m_uriMatchOnly;

	@SuppressWarnings("unchecked")
	public RequestKeyGenerator(AppConfiguration appConfig) {
		m_appConfig = appConfig;
		List<Object> list = appConfig
				.getList("requestKeyGenerator.skipHeaders");
		m_skipHeaders = new HashSet<>(list.size());
		for (Object obj : list) {
			if (obj != null) {
				m_skipHeaders.add(obj.toString());
			}
		}

		Object subConfig = m_appConfig.getConfig().get("uriMatchOnly");
		if (subConfig != null && subConfig instanceof List) {
			m_uriMatchOnly = (List<Map<String, Object>>) subConfig;
			m_uriMatchEnabled = !m_uriMatchOnly.isEmpty();
		}
	}

	@Override
	public String generateKey(FullHttpRequest req) {
		String rHash = getRequestHash(req);
		if (rHash == null) {
			return null;
		}

		String key = getRequestURI(req) + '|' + rHash;
		return key;
	}

	private String getRequestURI(FullHttpRequest request) {
		String uri = request.getUri();
		if (uri.startsWith("/")) {
			String host = HttpHeaders.getHost(request);
			if (host != null && !host.isEmpty()) {
				uri = "https://" + host + uri;
			}
		}
		return uri;
	}

	private String getRequestHash(FullHttpRequest request) {
		HttpHeaders headers = request.headers();
		String requestURI = getRequestURI(request);
		HttpMethod requestMethod = request.getMethod();
		Set<String> skipHeaders = m_skipHeaders;
		boolean skipRequestContent = m_uriMatchEnabled
				&& WildcardMatcher.isPatternCanBeMatchedIn(
						m_uriMatchOnly,
						new CacheDecisionObject(requestURI, requestMethod
								.name()));
		if(skipRequestContent){
			skipHeaders = new HashSet<>(m_skipHeaders);
			skipHeaders.add(HttpHeaders.Names.CONTENT_LENGTH.toUpperCase());
		}

		int uriHashcode = requestURI.hashCode();
		int methodHashCode = requestMethod.hashCode();
		List<Entry<String, String>> entries = headers.entries();
		List<String> hashList = new ArrayList<>();
		for (Iterator<Entry<String, String>> it = entries.iterator(); it
				.hasNext();) {
			Entry<String, String> entry = it.next();
			if (skipHeaders.contains(entry.getKey().toUpperCase())) {
				continue;
			}
			hashList.add(entry.getKey());
			hashList.add(entry.getValue());
		}

		int headersHashcode = hashList.hashCode();

		StringBuilder sb = new StringBuilder(4);
		sb.append(uriHashcode).append(methodHashCode).append(headersHashcode);

		if (!skipRequestContent) {
			ByteBuf content = request.content();
			sb.append(content.hashCode());
		}

		return Checksum.checksum(sb.toString());
	}

}
