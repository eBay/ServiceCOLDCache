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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;

public class ChunkResponsesSerializerTest {

	@Test
	public void testGetHttpResponse() {
		List<HttpObject> list = new ArrayList<>();
		list.add(mock(HttpResponse.class));
		
		assertNotNull(new ChunkResponsesSerializer().getHttpResponse(list));
	}
	
	@Test
	public void testGetTrailingHeaders(){
		List<HttpObject> list = new ArrayList<>();
		list.add(mock(HttpResponse.class));
		list.add(LastHttpContent.EMPTY_LAST_CONTENT);
		
		LastHttpContent lhc = new DefaultLastHttpContent();
		HttpHeaders th = lhc.trailingHeaders();
		th.add("Host", "127.0.0.1");
		th.add("Content-Length1", "1203");
		list.add(lhc);
		
		lhc = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		th = lhc.trailingHeaders();
		th.add("Date", "2016.05.26");
		th.add("Content-Length1", "2000");
		list.add(lhc);
		
		List<Entry<String, String>> trailingHeaders = new ChunkResponsesSerializer().getTrailingHeaders(list);
		assertEquals(3, trailingHeaders.size());
		for (Entry<String, String> entry : trailingHeaders) {
			if("Content-Length".equals(entry.getKey())){
				assertEquals(2000, entry.getValue());
			}
		}
	}
	
	@Test
	public void getContent(){
		List<HttpObject> list = new ArrayList<>();
		list.add(mock(HttpResponse.class));
		list.add(mock(HttpContent.class));
		list.add(mock(LastHttpContent.class));
		list.add(mock(FullHttpResponse.class));
		
		ByteBuf[] content = new ChunkResponsesSerializer().getContent(list);
		assertEquals(3, content.length);
	}

	@Test
	public void testGetType() {
		assertEquals(List.class, new ChunkResponsesSerializer().getType());
	}
}
