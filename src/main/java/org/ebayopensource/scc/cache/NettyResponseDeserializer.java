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

import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.util.List;
import java.util.Map.Entry;

import org.ebayopensource.scc.cache.CacheResponse.CacheEntry;

public class NettyResponseDeserializer implements
		IDeserializer<FullHttpResponse, CacheResponse> {

	@Override
	public FullHttpResponse deserialize(CacheResponse cacheResp) {
		CompositeByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT
				.compositeBuffer();
		if (cacheResp.getContent() != null) {
			byteBuf.capacity(cacheResp.getContent().length);
			byteBuf.setBytes(0, cacheResp.getContent());
			byteBuf.writerIndex(cacheResp.getContent().length);
		}
		DefaultFullHttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.valueOf(cacheResp.getProtocalVersion()),
				new HttpResponseStatus(cacheResp.getCode(),
						cacheResp.getReasonPhrase()), byteBuf, true);
		HttpHeaders headers = response.headers();
		List<CacheEntry<String, String>> cacheHeaders = cacheResp.getHeaders();
		for (Entry<String, String> entry : cacheHeaders) {
			headers.add(entry.getKey(), entry.getValue());
		}

		HttpHeaders trailingHeaders = response.trailingHeaders();
		List<CacheEntry<String, String>> cacheTrailingHeaders = cacheResp
				.getTrailingHeaders();
		for (Entry<String, String> entry : cacheTrailingHeaders) {
			trailingHeaders.add(entry.getKey(), entry.getValue());
		}

		return response;
	}

}
