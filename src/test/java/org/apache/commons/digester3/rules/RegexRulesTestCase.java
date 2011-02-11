/* $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.digester3.rules;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.commons.digester3.OrderRule;
import org.apache.commons.digester3.Rule;
import org.junit.Test;

/**
 * Test case for RegexRules
 *
 * @author Robert Burrell Donkin
 * @version $Revision$ $Date$
 */
public class RegexRulesTestCase {

    /** Test regex that matches everything */
    @Test
    public void testMatchAll() {
        // set up which should match every rule
        RegexRules rules = new RegexRules(
            new RegexMatcher() {

                public boolean match(String pathPattern, String rulePattern) {
                    return true;
                }

            });

        rules.add("/a/b/b", new OrderRule("alpha"));
        rules.add("/a/d", new OrderRule("beta"));
        rules.add("/b", new OrderRule("gamma"));

        // now test a few patterns
        // check that all are return in the order which they were added
        List<Rule> matches = rules.match("", "x/g/e");
        assertEquals("Wrong number of rules returned (1)", 3, matches.size());
        assertEquals("Rule Out Of Order (1)", "alpha", ((OrderRule) matches.get(0)).getIdentifier());
        assertEquals("Rule Out Of Order (2)", "beta", ((OrderRule) matches.get(1)).getIdentifier());
        assertEquals("Rule Out Of Order (3)", "gamma", ((OrderRule) matches.get(2)).getIdentifier());

        matches = rules.match("", "/a");
        assertEquals("Wrong number of rules returned (2)", 3, matches.size());
        assertEquals("Rule Out Of Order (4)", "alpha", ((OrderRule) matches.get(0)).getIdentifier());
        assertEquals("Rule Out Of Order (5)", "beta", ((OrderRule) matches.get(1)).getIdentifier());
        assertEquals("Rule Out Of Order (6)", "gamma", ((OrderRule) matches.get(2)).getIdentifier());
    }

    /** Test regex matcher that matches nothing */
    @Test
    public void testMatchNothing() {
        // set up which should match every rule
        RegexRules rules = new RegexRules(new RegexMatcher() {

            public boolean match(String pathPattern, String rulePattern) {
                return false;
            }

        });

        rules.add("/b/c/f", new OrderRule("alpha"));
        rules.add("/c/f", new OrderRule("beta"));
        rules.add("/b", new OrderRule("gamma"));

        // now test a few patterns
        // check that all are return in the order which they were added
        List<Rule> matches = rules.match("", "/b/c");
        assertEquals("Wrong number of rules returned (1)", 0, matches.size());

        matches = rules.match("", "/b/c/f");
        assertEquals("Wrong number of rules returned (2)", 0, matches.size());
    }

    /** Test a mixed regex - in other words, one that sometimes returns true and sometimes false */
    @Test
    public void testMatchMixed() {
        // set up which should match every rule
        RegexRules rules = new RegexRules(new RegexMatcher() {

            public boolean match(String pathPattern, String rulePattern) {
                return (rulePattern.equals("/match/me"));
            }

        });

        rules.add("/match", new OrderRule("alpha"));
        rules.add("/match/me", new OrderRule("beta"));
        rules.add("/match", new OrderRule("gamma"));

        // now test a few patterns
        // check that all are return in the order which they were added
        List<Rule> matches = rules.match("", "/match");
        assertEquals("Wrong number of rules returned (1)", 1, matches.size());
        assertEquals("Wrong Rule (1)", "beta", ((OrderRule) matches.get(0)).getIdentifier());

        matches = rules.match("", "/a/match");
        assertEquals("Wrong Rule (2)", "beta", ((OrderRule) matches.get(0)).getIdentifier());
    }

    /** Test rules and clear methods */
    @Test
    public void testClear() {
        // set up which should match every rule
        RegexRules rules = new RegexRules(new RegexMatcher() {

            public boolean match(String pathPattern, String rulePattern) {
                return true;
            }

        });

        rules.add("/abba", new OrderRule("alpha"));
        rules.add("/ad/ma", new OrderRule("beta"));
        rules.add("/gamma", new OrderRule("gamma"));

        // check that rules returns all rules in the order which they were added
        List<Rule> matches = rules.rules();
        assertEquals("Wrong number of rules returned (1)", 3, matches.size());
        assertEquals("Rule Out Of Order (1)", "alpha", ((OrderRule) matches.get(0)).getIdentifier());
        assertEquals("Rule Out Of Order (2)", "beta", ((OrderRule) matches.get(1)).getIdentifier());
        assertEquals("Rule Out Of Order (3)", "gamma", ((OrderRule) matches.get(2)).getIdentifier());

        matches = rules.match("", "/eggs");
        assertEquals("Wrong number of rules returned (2)", 3, matches.size());
        assertEquals("Rule Out Of Order (4)", "alpha", ((OrderRule) matches.get(0)).getIdentifier());
        assertEquals("Rule Out Of Order (5)", "beta", ((OrderRule) matches.get(1)).getIdentifier());
        assertEquals("Rule Out Of Order (6)", "gamma", ((OrderRule) matches.get(2)).getIdentifier());

        rules.clear();
        matches = rules.rules();
        assertEquals("Wrong number of rules returned (3)", 0, matches.size());

        matches = rules.match("", "/eggs");
        assertEquals("Wrong number of rules returned (4)", 0, matches.size());
    }

    @Test
    public void testSimpleRegexMatch() {
        SimpleRegexMatcher matcher = new SimpleRegexMatcher();

        assertEquals(
                "Simple Regex Match '/alpha/beta/gamma' to '/alpha/beta/gamma' ",
                true,
                matcher.match("/alpha/beta/gamma", "/alpha/beta/gamma"));
        assertEquals(
                "Simple Regex Match '/alpha/beta/gamma' to '/alpha/beta/gamma/epsilon' ",
                false,
                matcher.match("/alpha/beta/gamma", "/alpha/beta/gamma/epsilon"));
        assertEquals(
                "Simple Regex Match '/alpha/beta/gamma' to '/alpha/*' ",
                true,
                matcher.match("/alpha/beta/gamma", "/alpha/*"));
        assertEquals(
                "Simple Regex Match '/alpha/beta/gamma' to '/alpha/*/gamma' ",
                true,
                matcher.match("/alpha/beta/gamma", "/alpha/*/gamma"));
        assertEquals(
                "Simple Regex Match '/alpha/beta/gamma' to '/alpha/*me' ",
                false,
                matcher.match("/alpha/beta/gamma", "/alpha/*me"));
        assertEquals(
                "Simple Regex Match '/alpha/beta/gamma' to '*/beta/gamma' ",
                true,
                matcher.match("/alpha/beta/gamma", "*/beta/gamma"));
        assertEquals(
                "Simple Regex Match '/alpha/beta/gamma' to '*/alpha/beta/gamma' ",
                true,
                matcher.match("/alpha/beta/gamma", "*/alpha/beta/gamma"));
        assertEquals(
                "Simple Regex Match '/alpha/beta/gamma' to '*/bet/gamma' ",
                false,
                matcher.match("/alpha/beta/gamma", "*/bet/gamma"));
        assertEquals(
                "Simple Regex Match '/alpha/beta/gamma' to 'alph?/beta/gamma' ",
                true,
                matcher.match("/alpha/beta/gamma", "/alph?/beta/gamma"));
        assertEquals(
                "Simple Regex Match '/alpha/beta/gamma' to '/?lpha/beta/gamma' ",
                true,
                matcher.match("/alpha/beta/gamma", "/?lpha/beta/gamma"));
        assertEquals(
                "Simple Regex Match '/alpha/beta/gamma' to '/alpha/?beta/gamma' ",
                false,
                matcher.match("/alpha/beta/gamma", "/alpha/?beta/gamma"));
        assertEquals(
                "Simple Regex Match '/alpha/beta/gamma' to '/alpha/?eta/*' ",
                true,
                matcher.match("/alpha/beta/gamma", "/alpha/?eta/*"));
        assertEquals(
                "Simple Regex Match '/alpha/beta/gamma' to '/alpha/?eta/*e' ",
                false,
                matcher.match("/alpha/beta/gamma", "/alpha/?eta/*e"));
        assertEquals(
                "Simple Regex Match '/alpha/beta/gamma' to '*/?et?/?amma' ",
                true,
                matcher.match("/alpha/beta/gamma", "*/?et?/?amma"));
        assertEquals(
                "Simple Regex Match '/alpha/beta/gamma/beta/epsilon/beta/gamma/epsilon' to "
                + " '*/beta/gamma/?p*n' ",
                true,
                matcher.match("/alpha/beta/gamma/beta/epsilon/beta/gamma/epsilon", "*/beta/gamma/?p*n"));
        assertEquals(
                "Simple Regex Match '/alpha/beta/gamma/beta/epsilon/beta/gamma/epsilon' to "
                + " '*/beta/gamma/?p*no' ",
                false,
                matcher.match("/alpha/beta/gamma", "*/beta/gamma/?p*no"));
    }

}