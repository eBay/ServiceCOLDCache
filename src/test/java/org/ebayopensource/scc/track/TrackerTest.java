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
package org.ebayopensource.scc.track;

import static org.junit.Assert.*;

import org.junit.Test;

public class TrackerTest {

	@Test
	public void test() {
		Tracker t = new Tracker("testUser", "abc.corp.ebay.com",
				"10.249.22.42", "Windows 7", System.currentTimeMillis(), "CMD");

		assertNotNull(t.getHost());
		assertNotNull(t.getIp());
		assertNotNull(t.getOs());
		assertNotNull(t.getStartTime());
		assertNotNull(t.getUser());

		t.setHost("");
		t.setIp("");
		t.setOs("");
		t.setStartTime(0);
		t.setUser("");
	}

}
