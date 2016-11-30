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
package org.ebayopensource.scc.filter;

import static org.junit.Assert.*;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import org.ebayopensource.scc.AppCtx;
import org.ebayopensource.scc.AppCtxInitializer;
import org.ebayopensource.scc.ProxyServerException;
import org.ebayopensource.scc.cache.JCSCache;

public class CacheHttpFilterSourceTest {

	@Test
	public void test() throws ParseException, ProxyServerException {
		AppCtxInitializer initializer = new AppCtxInitializer();
		AppCtx ctx = initializer.init(null);
		CacheHttpFilterSource filter = new CacheHttpFilterSource(ctx);

		assertTrue(filter.getMaximumRequestBufferSizeInBytes() > 0);
		assertEquals(0, filter.getMaximumResponseBufferSizeInBytes());

		assertNotNull(filter.filterRequest(null, null));
		
		JCSCache.getInstance().shutdownCache();
	}

}
