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
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("rawtypes")
public class ChunkResponsesSerializer extends BaseResponseSerializer<List> {

	@Override
	public Class<? extends List> getType() {
		return List.class;
	}

	@Override
	protected HttpResponse getHttpResponse(Object obj) {
		List chunkes = (List) obj;
		return (HttpResponse) chunkes.get(0);
	}

	@Override
	protected List<Entry<String, String>> getTrailingHeaders(Object obj) {
		List chunkes = (List) obj;
		Map<String, String> trailingHeaders = new LinkedHashMap<>();
		for (Object chunk : chunkes) {
			if (chunk instanceof LastHttpContent) {
				Iterator<Entry<String, String>> it = ((LastHttpContent) chunk)
						.trailingHeaders().iterator();
				while (it.hasNext()) {
					Entry<String, String> next = it.next();
					trailingHeaders.put(next.getKey(), next.getValue());
				}
			}
		}
		return new ArrayList<>(trailingHeaders.entrySet());
	}

	@Override
	protected ByteBuf[] getContent(Object obj) {
		List chunkes = (List) obj;
		final int len = chunkes.size() - 1;
		List<ByteBuf> bufs = new ArrayList<>();
		for (int i = 0; i < len; i++) {
			Object chunk = chunkes.get(i + 1);
			if (chunk instanceof HttpContent) {
				bufs.add(((HttpContent) chunk).content());
			}
		}
		return bufs.toArray(new ByteBuf[0]);
	}

}
