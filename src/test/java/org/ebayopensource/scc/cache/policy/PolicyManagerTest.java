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

import org.ebayopensource.scc.cache.CacheManager;
import org.ebayopensource.scc.cache.CacheResponse;
import org.ebayopensource.scc.cache.IKeyGenerator;
import org.ebayopensource.scc.cache.policy.CacheDecisionObject;
import org.ebayopensource.scc.cache.policy.PolicyManager;
import org.ebayopensource.scc.config.AppConfiguration;
import org.ebayopensource.scc.config.Config;
import com.google.gson.Gson;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PolicyManagerTest {
    private Map<String, Object> m_configMap = new HashMap<>();
    private Config m_config;
    private AppConfiguration m_appConfiguration;
    private IKeyGenerator<FullHttpRequest> m_keyGen;

    private PolicyManager<FullHttpRequest, FullHttpResponse, CacheResponse> policyManager;

    @Before
    public void setup() {
        m_config = Mockito.mock(Config.class);
        Mockito.when(m_config.asMap()).thenReturn(m_configMap);

        m_appConfiguration = Mockito.mock(AppConfiguration.class);
        Mockito.when(m_appConfiguration.getConfig()).thenReturn(m_config);

        m_keyGen = Mockito.mock(IKeyGenerator.class);
        policyManager = new PolicyManager<FullHttpRequest, FullHttpResponse, CacheResponse>(m_appConfiguration, m_keyGen, null) {
            @Override
            protected CacheManager<FullHttpRequest, FullHttpResponse, CacheResponse> initCacheManager() {
                return Mockito.mock(CacheManager.class);
            }
        };
    }

    @Test
    public void testCacheDisabledInGlobalPolicy() {
        String configJson = "{" +
                "\"enableCache\": false," +
                "\"includes\": [" +
                "{\"uri\": \"http://ebay.com/*\", \"method\": \"post\"}," +
                "]" +
                "}";
        m_configMap.clear();
        m_configMap.putAll(new Gson().fromJson(configJson, Map.class));
        policyManager.policyChanged();

        String uri = "http://ebay.com/path";
        String method = "get";

        boolean decision = policyManager.cacheIsNeededFor(new CacheDecisionObject(uri, method));
        assertThat(decision, is(false));
    }

    @Test
    public void testOnlyIncludesInGlobalPolicy() {
        String configJson = "{" +
                "\"includes\": [" +
                "{\"uri\": \"http://ebay.com/*\", \"method\": \"post\"}," +
                "{\"uri\": \"http://google.com*\", \"method\": \"*\"}," +
                "{\"uri\": \"http://ebay.com/*/subpath\", \"method\": \"*\"}," +
                "{\"uri\": \"*.html\"}" +
                "]" +
                "}";
        m_configMap.clear();
        m_configMap.putAll(new Gson().fromJson(configJson, Map.class));
        policyManager.policyChanged();

        String uri = "http://ebay.com/anysubpath";
        String method = "post";

        boolean decision = policyManager.cacheIsNeededFor(new CacheDecisionObject(uri, method));
        assertThat(decision, is(true));

        uri = "http://google.com";
        method = "get";
        decision = policyManager.cacheIsNeededFor(new CacheDecisionObject(uri, method));
        assertThat(decision, is(true));

        uri = "http://anyother.com/something.html";
        method = "get";
        decision = policyManager.cacheIsNeededFor(new CacheDecisionObject(uri, method));
        assertThat(decision, is(true));

        uri = "http://ebay.com/parentpath/subpath";
        method = "get";
        decision = policyManager.cacheIsNeededFor(new CacheDecisionObject(uri, method));
        assertThat(decision, is(true));

        uri = "http://ebay.com/";
        method = "get";
        decision = policyManager.cacheIsNeededFor(new CacheDecisionObject(uri, method));
        assertThat(decision, is(false));

        uri = "http://anyother.com/something.jsp";
        method = "get";
        decision = policyManager.cacheIsNeededFor(new CacheDecisionObject(uri, method));
        assertThat(decision, is(false));

        uri = "http://anyother.com/";
        method = "post";
        decision = policyManager.cacheIsNeededFor(new CacheDecisionObject(uri, method));
        assertThat(decision, is(false));
    }

    @Test
    public void testOnlyExcludesInGlobalPolicy() {
        String configJson = "{" +
                "\"excludes\": [" +
                "{\"uri\": \"*.jpg\"}," +
                "{\"uri\": \"http://ebay.com/download/*\"}" +
                "]" +
                "}";
        m_configMap.clear();
        m_configMap.putAll(new Gson().fromJson(configJson, Map.class));
        policyManager.policyChanged();

        String uri = "http://ebay.com/anysubpath";
        String method = "post";

        boolean decision = policyManager.cacheIsNeededFor(new CacheDecisionObject(uri, method));
        assertThat(decision, is(true));

        uri = "http://ebay.com/download/something.blob";
        method = "get";
        decision = policyManager.cacheIsNeededFor(new CacheDecisionObject(uri, method));
        assertThat(decision, is(false));

        uri = "http://ebay.com/static/pic.jpg";
        method = "get";
        decision = policyManager.cacheIsNeededFor(new CacheDecisionObject(uri, method));
        assertThat(decision, is(false));
    }

    @Test
    public void testNeitherIncludeNorExcludeSetInGlobalPolicy() {
        String configJson = "{}";
        m_configMap.clear();
        m_configMap.putAll(new Gson().fromJson(configJson, Map.class));
        policyManager.policyChanged();

        boolean decision = policyManager.cacheIsNeededFor(new CacheDecisionObject("anyURI", "anyMethod"));
        assertThat(decision, is(PolicyManager.DEFAULT_CACHEABLE));

    }
}
