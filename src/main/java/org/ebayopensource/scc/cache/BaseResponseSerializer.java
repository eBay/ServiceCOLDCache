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
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.ebayopensource.scc.cache.CacheResponse.CacheEntry;

public abstract class BaseResponseSerializer<T> implements
		ISerializer<CacheResponse, T> {

	public BaseResponseSerializer() {
		super();
	}

	protected abstract HttpResponse getHttpResponse(Object obj);

	protected abstract List<Entry<String, String>> getTrailingHeaders(Object obj);

	protected abstract ByteBuf[] getContent(Object obj);

	public CacheResponse serialize(Object obj) {
		if (!getType().isAssignableFrom(obj.getClass())) {
			return null;
		}
		CacheResponse cacheResp = createCacheResponse(getHttpResponse(obj));
		copyTrailingHeaders(cacheResp, getTrailingHeaders(obj));
		copyBody(cacheResp, getContent(obj));
		return cacheResp;
	}

	protected void copyBody(CacheResponse cacheResp, ByteBuf... chunks) {
		int totalSize = 0;
		for (ByteBuf c : chunks) {
			if (c.isReadable()) {
				totalSize += c.readableBytes();
			}
		}

		ByteBuffer nioBuffer = ByteBuffer.allocate(totalSize);
		for (ByteBuf c : chunks) {
			if (c.isReadable()) {
				c.getBytes(c.readerIndex(), nioBuffer);
			}
		}

		if (nioBuffer.hasArray()) {
			cacheResp.setContent(nioBuffer.array());
		}

		// reset Content-Length
		List<CacheEntry<String, String>> headers = cacheResp.getHeaders();
		for (Iterator<CacheEntry<String, String>> it = headers.iterator(); it
				.hasNext();) {
			CacheEntry<String, String> header = it.next();
			if (HttpHeaders.Names.CONTENT_LENGTH.equals(header.getKey())) {
				it.remove();
			}
		}
		headers.add(new CacheEntry<String, String>(
				HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(totalSize)));
	}

	protected void copyTrailingHeaders(CacheResponse cacheResp,
			List<Entry<String, String>> trailingHeaders) {
		List<CacheEntry<String, String>> cacheTrailingHeaders = cacheResp
				.getTrailingHeaders();
		for (Entry<String, String> entry : trailingHeaders) {
			cacheTrailingHeaders.add(new CacheEntry<String, String>(entry
					.getKey(), entry.getValue()));
		}
	}

	protected CacheResponse createCacheResponse(HttpResponse resp) {
		String protocolVersion = resp.getProtocolVersion().text();
		HttpResponseStatus status = resp.getStatus();
		int code = status.code();
		String reasonPhrase = status.reasonPhrase();

		List<Entry<String, String>> headers = resp.headers().entries();
		List<CacheEntry<String, String>> cacheHeaders = new ArrayList<>(
				headers.size());
		for (Entry<String, String> entry : headers) {
			cacheHeaders.add(new CacheEntry<String, String>(entry.getKey(),
					entry.getValue()));
		}

		return new CacheResponse(protocolVersion, code, reasonPhrase,
				cacheHeaders, new ArrayList<CacheEntry<String, String>>());
	}

}