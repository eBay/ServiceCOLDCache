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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class WildcardMatcherTest {

    @Test
    public void testWildcardMatchTrailingStar() {
        String pattern = "http://ebay.com/*";
        String text = "http://ebay.com/pathtodir";
        boolean matcherResult = WildcardMatcher.wildcardMatched(pattern, text);
        assertThat(matcherResult, is(true));
    }

    @Test
    public void testWildcardNotMatchTrailingStar() {
        String pattern = "http://ebay.com/*";
        String text = "http://google.com/pathtodir";
        boolean matcherResult = WildcardMatcher.wildcardMatched(pattern, text);
        assertThat(matcherResult, is(false));
    }

    @Test
    public void testWildcardMatchMiddleStar() {
        String pattern = "http://ebay.com/*/subpath";
        String text = "http://ebay.com/parentpath/subpath";
        boolean matcherResult = WildcardMatcher.wildcardMatched(pattern, text);
        assertThat(matcherResult, is(true));
    }

    @Test
    public void testWildcardNotMatchMiddleStar() {
        String pattern = "http://ebay.com/*/subpath";
        String text = "http://ebay.com/parentpath/anothersubpath";
        boolean matcherResult = WildcardMatcher.wildcardMatched(pattern, text);
        assertThat(matcherResult, is(false));
    }

    @Test
    public void testWildcardOnlyStar() {
        String pattern = "*";
        String text = "any";
        boolean matcherResult = WildcardMatcher.wildcardMatched(pattern, text);
        assertThat(matcherResult, is(true));
    }

    @Test
    public void testMatchingStartWith() {
        String pattern = "^abcd";
        String text = "abcd";
        boolean matcherResult = WildcardMatcher.wildcardMatched(pattern, text);
        assertThat(matcherResult, is(true));

        text = "xxxabcdxxx";
        matcherResult = WildcardMatcher.wildcardMatched(pattern, text);
        assertThat(matcherResult, is(false));

        pattern = "abcd";
        text = "xxxabcdxxx";
        matcherResult = WildcardMatcher.wildcardMatched(pattern, text);
        assertThat(matcherResult, is(true));
    }

    @Test
    public void testMatchingEndWith() {
        String pattern = "abcd$";
        String text = "abcd$";
        boolean matcherResult = WildcardMatcher.wildcardMatched(pattern, text);
        assertThat(matcherResult, is(true));

        text = "xxxabcdxxx";
        matcherResult = WildcardMatcher.wildcardMatched(pattern, text);
        assertThat(matcherResult, is(false));

        pattern = "abcd";
        text = "xxxabcdxxx";
        matcherResult = WildcardMatcher.wildcardMatched(pattern, text);
        assertThat(matcherResult, is(true));
    }

    @Test(expected = NullPointerException.class)
    public void testWildcardWithNullPattern() {
        String pattern = null;
        String text = "http://ebay.com/parentpath/anothersubpath";
        WildcardMatcher.wildcardMatched(pattern, text);
    }

    @Test
    public void testWildcardWithNullText() {
        String pattern = "http://ebay.com/*/subpath";
        String text = null;
        boolean matcherResult = WildcardMatcher.wildcardMatched(pattern, text);
        assertThat(matcherResult, is(false));
    }
}
