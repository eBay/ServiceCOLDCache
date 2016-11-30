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

import org.junit.Test;

import org.ebayopensource.scc.cache.CacheResponse.CacheEntry;

public class CacheEntryTest {

	@Test
	public void test() {
		CacheEntry<String, String> e1 = new CacheResponse.CacheEntry<>("k1", "v1");
		CacheEntry<String, String> e2 = new CacheResponse.CacheEntry<>("k1", "v1");
		
		assertTrue(e1.equals(e2));
		assertFalse(e1.equals(new Object()));
		e2.setValue("v2");
		assertFalse(e1.equals(e2));
		
		assertNotNull(e1.hashCode());
		assertNotNull(e1.toString());
	}

}
