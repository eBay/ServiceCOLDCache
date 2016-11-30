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

import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.junit.AfterClass;
import org.junit.Test;

import org.ebayopensource.scc.cache.JCSCache;
import org.ebayopensource.scc.config.AppConfiguration;

public class AppCtxInitializerTest {

	@Test
	public void test() throws ParseException, IOException, ProxyServerException {
		AppCtxInitializer initializer = new AppCtxInitializer();
		initializer.init(null);
		assertNull(AppCtx.getInstance().getAppConfig().getString("description"));

		initializer.init(new String[] { "-s",
				"./src/test/resources/testuserconfig.json" });
		assertEquals("testConfig", AppCtx.getInstance().getAppConfig()
				.getString("description"));

		initializer.init(new String[] { "--settings",
				"./src/test/resources/testuserconfig.json" });
		assertEquals("testConfig", AppCtx.getInstance().getAppConfig()
				.getString("description"));
		
		AppCtx ctx = initializer.init(new String[] { "-p", "19823", "-a", "29384", "-c", "/temp/proxycache" });
		AppConfiguration config = ctx.getAppConfig();
		assertEquals(19823, config.getProxyPort());
		assertEquals(29384, config.getAdminPort());
		assertEquals("/temp/proxycache", config.getCacheDir());
	}

	@AfterClass
	public static void afterClass() {
		JCSCache.getInstance().shutdownCache();
	}
}
