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
import java.util.Map;

public class Policy {

    private List<Map<String, Object>> m_patternProperties;
    private Map<String, Object> m_config;

    protected Policy() {}

    @SuppressWarnings("unchecked")
    public static Policy fromConfigMap(Map<String, Object> configMap) {
        notNull("configMap cannot be null!", configMap);
        Policy policy = new Policy();
        policy.m_config = configMap;
        if(isNotGlobal(configMap)) {
            policy.m_patternProperties = (List<Map<String, Object>>)configMap.get("pattern");
        }
        return policy;
    }

    private static boolean isNotGlobal(Map<String, Object> configs) {
        return configs.containsKey("pattern");
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getExcludes() {
        return (List<Map<String, Object>>) m_config.get("excludes");
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getIncludes() {
        return (List<Map<String, Object>>) m_config.get("includes");
    }

    public Map<String, Object> getConfig() {
        return m_config;
    }

    public Boolean isEnableCache() {
        return (Boolean) m_config.get("enableCache");
    }
    /**
     * @return the pattern properties, key properties of a policy, used to identify a policy, e.g. <br>
     * <code>
     * declaredPolicies: [{pattern: [ {uri: 'http://xxx', method: 'post' } <-<i> this is a pattern properties object</i>]}]
     * </code>
     *
     */
    public List<Map<String, Object>> getPatternProperties() {
        return m_patternProperties;
    }

}
