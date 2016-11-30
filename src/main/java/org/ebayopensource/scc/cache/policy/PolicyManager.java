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
package org.ebayopensource.scc.cache.policy;

import static org.ebayopensource.scc.util.AssertUtils.*;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ebayopensource.scc.cache.CacheManager;
import org.ebayopensource.scc.cache.IKeyGenerator;
import org.ebayopensource.scc.config.AppConfiguration;
import org.ebayopensource.scc.config.Config;
import org.ebayopensource.scc.util.WildcardMatcher;

public abstract class PolicyManager<T, R, C> {
	public static boolean DEFAULT_CACHEABLE = true;

	private static final Logger LOG = LoggerFactory
			.getLogger(PolicyManager.class);

	protected AppConfiguration m_appConfig;
	protected ScheduledExecutorService m_scheduledService;
	protected List<Policy> m_declaredPolicies;
	protected Policy m_globalPolicy;
	protected IKeyGenerator<T> m_keyGen;
	protected CacheManager<T, R, C> m_cacheManager;

	public PolicyManager(AppConfiguration appConfig, IKeyGenerator<T> keyGen, ScheduledExecutorService scheduledService) {
		m_appConfig = appConfig;
		m_keyGen = keyGen;
		m_scheduledService = scheduledService;
		updatePolicies();

		/*implementation may access policy, so initCacheManager should be called after policy init*/
		m_cacheManager = initCacheManager();
	}

	/**
	 * 
	 * @param decisionObj
	 *            the object used to make caching decision (by policy)
	 * @return whether cache is needed
	 * 
	 */
	public boolean cacheIsNeededFor(CacheDecisionObject decisionObj) {
		notNull("decisionObj can not be null!", decisionObj);

		try {
			Policy matchedPolicy = matchPolicies(decisionObj);
			if (!policyCacheEnabled(matchedPolicy)) {
				return false;
			}
			return makeDecisionByPolicyIncludesAndExcludes(matchedPolicy, decisionObj);
		}catch (Exception e) {
			LOG.error("Exception threw while trying to decide cache for: " + decisionObj + " cache skipped", e);
			return false;
		}
	}

	/**
	 * should be called after any policy changed, when overwriting this method, the subclass should <strong>call
	 * super.policyChanged() first</strong>
	 */
	public void policyChanged() {
		updatePolicies();
	}

	public CacheManager<T, R, C> getCacheManager() {
		return m_cacheManager;
	}

	abstract protected CacheManager<T, R, C> initCacheManager();

	public String generateCacheKey(T t) {
		return m_keyGen.generateKey(t);
	}

	private void updatePolicies() {
		Config appConfig = m_appConfig.getConfig();
		m_globalPolicy = Policy.fromConfigMap(appConfig.asMap());

		updateDeclaredPolices();
	}

	private void updateDeclaredPolices() {
		// TODO for supporting declared m_declaredPolicies
	}

	private Policy matchPolicies(CacheDecisionObject decisionObj) {
		Policy matchedDeclaredPolicy = matchPolicyInDeclaredPolicies(decisionObj);
		if (matchedDeclaredPolicy == null) {
			return m_globalPolicy;
		} else {
			return matchedDeclaredPolicy;
		}
	}

	private Policy matchPolicyInDeclaredPolicies(
			CacheDecisionObject decisionObject) {
		// TODO for supporting declared m_declaredPolicies
		return null;
	}

	private boolean policyCacheEnabled(Policy policy) {
		return policy == null || policy.isEnableCache() == null
				|| policy.isEnableCache();
	}

	private boolean makeDecisionByPolicyIncludesAndExcludes(Policy policy,
			CacheDecisionObject decisionObj) {
		// TODO for supporting more complex includes/excludes policy
		if (policy == null)
			return true;
		if (policy.getIncludes() != null) {
			return WildcardMatcher.isPatternCanBeMatchedIn(policy.getIncludes(), decisionObj);
		} else if(policy.getExcludes() != null) {
			return !WildcardMatcher.isPatternCanBeMatchedIn(policy.getExcludes(), decisionObj);
		}
		return DEFAULT_CACHEABLE;
	}

}
