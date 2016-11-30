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

import org.junit.AfterClass;
import org.junit.Test;

import org.ebayopensource.scc.cache.JCSCache;
import org.ebayopensource.scc.config.AppConfiguration;
import org.ebayopensource.scc.config.ConfigLoader;

public class RegistryTest {

	@Test
	public void test() throws IOException {
		AppConfiguration appConfig = new AppConfiguration(new ConfigLoader(), null);
		appConfig.init();
		Registry registry = new Registry(appConfig);
		assertNull(registry.getCacheManager());
		assertNull(registry.getDebugManager());
		assertNull(registry.getFilterManager());
		assertNull(registry.getPolicyManager());
		registry.init();
		assertNotNull(registry.getCacheManager());
		assertNotNull(registry.getDebugManager());
		assertNotNull(registry.getFilterManager());
		assertNotNull(registry.getPolicyManager());
		
	}

	@AfterClass
	public static void afterClass(){
		JCSCache.getInstance().shutdownCache();
	}
}
