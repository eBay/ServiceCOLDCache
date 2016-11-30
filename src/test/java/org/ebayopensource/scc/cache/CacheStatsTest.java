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

import org.ebayopensource.scc.AppCtx;
import org.ebayopensource.scc.config.AppConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.FieldSetter;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;

@RunWith(PowerMockRunner.class)
public class CacheStatsTest {

	@Mock
	private AppConfiguration appConfiguration;
	private CacheStats cacheStats;

	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		cacheStats = new CacheStats();
		AppCtx appCtx = AppCtx.getInstance();
		writeField(appCtx, "m_appConfig", appConfiguration);
	}

	@Test
	public void testAddCacheVerifiedResult() {
		Mockito.when(appConfiguration.getInt("cacheStats.verifiedResultThreshold")).thenReturn(2);
		CacheVerifiedResult res = new CacheVerifiedResult();
		res.key = "1";
		cacheStats.addCacheVerifiedResult(res);
		Assert.assertEquals(1,cacheStats.getCacheVerifiedResult().size());

		res = new CacheVerifiedResult();
		res.key = "2";
		cacheStats.addCacheVerifiedResult(res);
		Assert.assertEquals(2,cacheStats.getCacheVerifiedResult().size());

		res = new CacheVerifiedResult();
		res.key = "3";
		cacheStats.addCacheVerifiedResult(res);
		Assert.assertEquals(2,cacheStats.getCacheVerifiedResult().size());
		Assert.assertEquals("2",cacheStats.getCacheVerifiedResult().get(0).key);

		res = new CacheVerifiedResult();
		cacheStats.addCacheVerifiedResult(res);
		res.key = "4";
		Assert.assertEquals(2,cacheStats.getCacheVerifiedResult().size());
		Assert.assertEquals("3",cacheStats.getCacheVerifiedResult().get(0).key);
	}

	@Test
	public void testHitMissedStats(){
		Assert.assertNotNull(cacheStats.getHitMissedStats());
	}

	@Test
	public void testGetFalsedCacheVerifiedResultStats(){
		Mockito.when(appConfiguration.getInt("cacheStats.verifiedResultThreshold")).thenReturn(100);
		CacheVerifiedResult cvr = new CacheVerifiedResult();
		cacheStats.addCacheVerifiedResult(cvr);
		cvr = new CacheVerifiedResult();
		cvr.result=true;
		cacheStats.addCacheVerifiedResult(cvr);

		Assert.assertNotNull(cacheStats.getFalsedCacheVerifiedResultStats());
	}

	private void writeField(Object obj, String field, Object v) {
		try {
			Field executorField = obj.getClass().getDeclaredField(field);
			FieldSetter fieldSetter = new FieldSetter(obj, executorField);
			fieldSetter.set(v);
		} catch (NoSuchFieldException e) {
			Assert.fail(e.getMessage());
		}
	}
}
