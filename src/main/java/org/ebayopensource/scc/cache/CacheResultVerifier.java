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
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CacheResultVerifier {
	private static boolean s_vaStrict = Boolean.valueOf(System
			.getProperty("coldCache.verifyCacheResult.strict"));
	private static Set<String> s_tolerantHeaders = new HashSet<>(
			Arrays.asList(new String[] { "DATE" }));
	protected FullHttpRequest m_req;
	protected FullHttpResponse m_cacheResp;
	protected String m_key;

	public CacheResultVerifier(String key, FullHttpRequest cr,
			FullHttpResponse cacheResp) {
		this.m_key = key;
		this.m_req = cr;
		this.m_cacheResp = cacheResp;
	}

	public CacheVerifiedResult fetchResult(FullHttpResponse actualResp) {
		CacheVerifiedResult result = new CacheVerifiedResult();
		String cause = match(actualResp);
		result.cause = cause;
		result.key = m_key;
		result.result = (cause == null);
		return result;
	}

	protected String match(FullHttpResponse actualResp) {
		String eTag = HttpHeaders.getHeader(m_cacheResp, "ETag");
		String actualETag = HttpHeaders.getHeader(actualResp, "ETag");
		if (actualETag != null && actualETag.equals(eTag)) {
			return null;
		} else if (actualETag != null && !actualETag.equals(eTag)) {
			return String.format("ETag mismatch: cache - %s; actual - %s",
					eTag, actualETag);
		} else if (actualETag == null && eTag == null) {
			// match status code
			if (actualResp.getStatus().compareTo(m_cacheResp.getStatus()) != 0) {
				return String.format(
						"Status code mismatch: cache - %s; actual - %s",
						m_cacheResp.getStatus().code(), actualResp.getStatus()
								.code());
			}

			// match headers
			HttpHeaders actualHeaders = actualResp.headers();
			HttpHeaders headers = m_cacheResp.headers();
			List<String> mh = new ArrayList<>();// mismatched headers
			checkHeaderDiffer(actualHeaders, headers, mh);
			if (!mh.isEmpty()) {
				return String.format("Mismatched headers: %s", mh.toString());
			}
			checkHeaderDiffer(headers, actualHeaders, mh);
			if (!mh.isEmpty()) {
				return String.format("Mismatched headers: %s", mh.toString());
			}

			// match entity
			ByteBuf actualContent = actualResp.content();
			ByteBuf content = m_cacheResp.content();
			int len = actualContent.readableBytes();
			if (len != content.readableBytes()) {
				return "Mismatched body input stream.";
			}

			for (int i = 0; i < len; i++) {
				if (actualContent.getByte(i) != content.getByte(i)) {
					return "Mismatched body input stream.";
				}
			}
		}
		return null;
	}

	private void checkHeaderDiffer(HttpHeaders headers1, HttpHeaders headers2,
			List<String> mh) {
		for (String headerName : headers1.names()) {
			if (!s_vaStrict
					&& s_tolerantHeaders.contains(headerName.toUpperCase())) {
				continue;
			}
			String v1 = headers1.get(headerName);
			String v2 = headers2.get(headerName);
			if (!v1.equals(v2)) {
				mh.add(headerName);
			}
		}
	}

}
