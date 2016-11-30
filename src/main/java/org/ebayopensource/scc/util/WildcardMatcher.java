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
package org.ebayopensource.scc.util;

import java.util.List;
import java.util.Map;

import org.ebayopensource.scc.cache.policy.CacheDecisionObject;

/**
 *
 * A wildcard(*) matcher, This implementation was adapted from book: <i>Beautiful Code</i> chapter one,
 * @see <a href="http://www.cs.princeton.edu/courses/archive/spr09/cos333/beautiful.html">online book</a>
 *
 */
public class WildcardMatcher {

    public static boolean wildcardMatched(String pattern, String text) {
        if(pattern == null) {
            throw new NullPointerException("pattern can not be null!");
        }
        if(text == null) {
            return false;
        }
        if (pattern.charAt(0) == '^')
            return matchHere(1, pattern, 0, text);
        int textIndex = 0, patternIndex = 0;
        do {
            if(matchHere(patternIndex, pattern, textIndex, text))
                return true;
        }while(textIndex++ < text.length());
        return false;
    }

    private static boolean matchHere(int patternIndex, String pattern, int textIndex, String text) {
        if (pattern.length() == patternIndex)
            return true;
        if (pattern.charAt(patternIndex) == '*')
            return matchStar(patternIndex + 1, pattern, textIndex, text);
        if (pattern.charAt(patternIndex) == '$' && patternIndex == pattern.length())
            return textIndex == text.length();
        if(textIndex < text.length() && pattern.charAt(patternIndex) == text.charAt(textIndex))
            return matchHere(patternIndex + 1, pattern, textIndex + 1, text);
        return false;
    }

    private static boolean matchStar(int patternIndex, String pattern, int textIndex, String text) {
        do {
            if (matchHere(patternIndex, pattern, textIndex, text))
                return true;
        } while (textIndex++ < text.length());
        return false;
    }

	public static boolean isPatternCanBeMatchedIn(
			List<Map<String, Object>> patternProps,
			CacheDecisionObject decisionObj) {
		for (Map<String, Object> patternProp : patternProps) {
			String uriPattern = (String) patternProp.get("uri");
			String httpMethodValue = (String) patternProp.get("method");
			if (bothURIAndHttpMethodMatched(uriPattern, httpMethodValue,
					decisionObj)) {
				return true;
			}
		}
		return false;
	}

	public static boolean bothURIAndHttpMethodMatched(String uriPattern,
			String methodConfigValue, CacheDecisionObject decisionObj) {
		return wildcardMatched(uriPattern, decisionObj.getUri())
				&& httpMethodMatched(methodConfigValue,
						decisionObj.getHttpMethod());
	}

	public static boolean httpMethodMatched(String configValue, String method) {
		return configValue == null || "*".equals(configValue)
				|| configValue.toUpperCase().equals(method.toUpperCase());
	}

}
