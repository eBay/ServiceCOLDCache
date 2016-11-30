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

import com.google.common.base.MoreObjects;

public class CacheDecisionObject {
    private String m_uri;
    private String m_httpMethod;

    public CacheDecisionObject() {}
    public CacheDecisionObject(String uri, String httpMethod) {
        this.m_uri = uri;
        this.m_httpMethod = httpMethod;
    }

    public String getUri() {
        return m_uri;
    }

    public void setUri(String uri) {
        this.m_uri = uri;
    }

    public String getHttpMethod() {
        return m_httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.m_httpMethod = httpMethod;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uri", m_uri)
                .add("httpMethod", m_httpMethod)
                .toString();
    }
}
