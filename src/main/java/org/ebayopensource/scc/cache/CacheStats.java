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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CacheStats {
	private AtomicInteger missedCount = new AtomicInteger(0);
	private AtomicInteger hitCount = new AtomicInteger(0);
	private List<CacheVerifiedResult> results = new ArrayList<>(100);

	public void incrementMissedCount() {
		missedCount.incrementAndGet();
	}

	public void incrementHitCount() {
		hitCount.incrementAndGet();
	}

	public int getMissedCount() {
		return missedCount.get();
	}

	public int getHitCount() {
		return hitCount.get();
	}

	public void addCacheVerifiedResult(CacheVerifiedResult cvr) {
		int threshold = AppCtx.getInstance().getAppConfig().getInt("cacheStats.verifiedResultThreshold");
		synchronized (results) {
			if (results.size() == threshold) {
				results.remove(0);
			}
			results.add(cvr);
		}
	}

	public List<CacheVerifiedResult> getCacheVerifiedResult() {
		return results;
	}

	public String getFalsedCacheVerifiedResultStats() {
		int count = 0;
		StringBuilder sb = new StringBuilder();
		for (CacheVerifiedResult r : results) {
			if (!r.result) {
				count++;
				sb.append("Result: ").append(r.result).append("\tCache: ")
						.append(r.key).append("\tCause: ").append(r.cause)
						.append("\n");
			}
		}
		sb.append("Total ").append(count).append(" inconsistent cache records");
		return sb.toString();
	}

	public String getHitMissedStats() {
		return "Hit: " + hitCount.get() + "\nmissed:" + missedCount.get()
				+ "\n";
	}
}