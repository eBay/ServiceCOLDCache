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
package org.ebayopensource.scc;

import static org.junit.Assert.*;

import org.junit.Test;

import static org.mockito.Mockito.*;

import org.ebayopensource.scc.cache.CacheManager;
import org.ebayopensource.scc.config.AppConfiguration;

public class AppCtxTest {

	@Test
	public void test() {
		AppCtx ctx = new AppCtx();
		Registry registry = mock(Registry.class);
		when(registry.getCacheManager()).thenReturn(mock(CacheManager.class));
		ctx.init(mock(AppConfiguration.class), registry);
		
		assertNotNull(ctx.getAppConfig());
		assertNotNull(ctx.getCacheManager());
		
		ctx.enableCache(false);
	}

}
