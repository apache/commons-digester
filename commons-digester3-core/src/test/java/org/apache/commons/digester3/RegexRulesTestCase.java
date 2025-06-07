/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.digester3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test case for RegexRules
 */
public class RegexRulesTestCase
{

    /** Test rules and clear methods */
    @Test
    void testClear()
    {
        // set up which should match every rule
        final RegexRules rules = new RegexRules( new RegexMatcher()
        {
            @Override
            public boolean match( final String pathPattern, final String rulePattern )
            {
                return true;
            }
        } );

        rules.add( "/abba", new TestRule( "alpha" ) );
        rules.add( "/ad/ma", new TestRule( "beta" ) );
        rules.add( "/gamma", new TestRule( "gamma" ) );

        // check that rules returns all rules in the order which they were added
        List<Rule> matches = rules.rules();
        assertEquals( 3, matches.size(), "Wrong number of rules returned (1)" );
        assertEquals( "alpha", ( ( TestRule ) matches.get( 0 ) ).getIdentifier(), "Rule Out Of Order (1)" );
        assertEquals( "beta", ( ( TestRule ) matches.get( 1 ) ).getIdentifier(), "Rule Out Of Order (2)" );
        assertEquals( "gamma", ( ( TestRule ) matches.get( 2 ) ).getIdentifier(), "Rule Out Of Order (3)" );

        matches = rules.match( "", "/eggs", null, null );
        assertEquals( 3, matches.size(), "Wrong number of rules returned (2)" );
        assertEquals( "alpha", ( ( TestRule ) matches.get( 0 ) ).getIdentifier(), "Rule Out Of Order (4)" );
        assertEquals( "beta", ( ( TestRule ) matches.get( 1 ) ).getIdentifier(), "Rule Out Of Order (5)" );
        assertEquals( "gamma", ( ( TestRule ) matches.get( 2 ) ).getIdentifier(), "Rule Out Of Order (6)" );

        rules.clear();
        matches = rules.rules();
        assertEquals( 0, matches.size(), "Wrong number of rules returned (3)" );

        matches = rules.match( "", "/eggs", null, null );
        assertEquals( 0, matches.size(), "Wrong number of rules returned (4)" );
    }

    /** Test regex that matches everything */
    @Test
    void testMatchAll()
    {
        // set up which should match every rule
        final RegexRules rules = new RegexRules( new RegexMatcher()
        {
            @Override
            public boolean match( final String pathPattern, final String rulePattern )
            {
                return true;
            }
        } );

        rules.add( "/a/b/b", new TestRule( "alpha" ) );
        rules.add( "/a/d", new TestRule( "beta" ) );
        rules.add( "/b", new TestRule( "gamma" ) );

        // now test a few patterns
        // check that all are return in the order which they were added
        List<Rule> matches = rules.match( "", "x/g/e", null, null );
        assertEquals( 3, matches.size(), "Wrong number of rules returned (1)" );
        assertEquals( "alpha", ( ( TestRule ) matches.get( 0 ) ).getIdentifier(), "Rule Out Of Order (1)" );
        assertEquals( "beta", ( ( TestRule ) matches.get( 1 ) ).getIdentifier(), "Rule Out Of Order (2)" );
        assertEquals( "gamma", ( ( TestRule ) matches.get( 2 ) ).getIdentifier(), "Rule Out Of Order (3)" );

        matches = rules.match( "", "/a", null, null );
        assertEquals( 3, matches.size(), "Wrong number of rules returned (2)" );
        assertEquals( "alpha", ( ( TestRule ) matches.get( 0 ) ).getIdentifier(), "Rule Out Of Order (4)" );
        assertEquals( "beta", ( ( TestRule ) matches.get( 1 ) ).getIdentifier(), "Rule Out Of Order (5)" );
        assertEquals( "gamma", ( ( TestRule ) matches.get( 2 ) ).getIdentifier(), "Rule Out Of Order (6)" );
    }

    /** Test a mixed regex - in other words, one that sometimes returns true and sometimes false */
    @Test
    void testMatchMixed()
    {
        // set up which should match every rule
        final RegexRules rules = new RegexRules( new RegexMatcher()
        {
            @Override
            public boolean match( final String pathPattern, final String rulePattern )
            {
                return rulePattern.equals( "/match/me" );
            }
        } );

        rules.add( "/match", new TestRule( "alpha" ) );
        rules.add( "/match/me", new TestRule( "beta" ) );
        rules.add( "/match", new TestRule( "gamma" ) );

        // now test a few patterns
        // check that all are return in the order which they were added
        List<Rule> matches = rules.match( "", "/match", null, null );
        assertEquals( 1, matches.size(), "Wrong number of rules returned (1)" );
        assertEquals( "beta", ( ( TestRule ) matches.get( 0 ) ).getIdentifier(), "Wrong Rule (1)" );

        matches = rules.match( "", "/a/match", null, null );
        assertEquals( "beta", ( ( TestRule ) matches.get( 0 ) ).getIdentifier(), "Wrong Rule (2)" );
    }

    /** Test regex matcher that matches nothing */
    @Test
    void testMatchNothing()
    {
        // set up which should match every rule
        final RegexRules rules = new RegexRules( new RegexMatcher()
        {
            @Override
            public boolean match( final String pathPattern, final String rulePattern )
            {
                return false;
            }
        } );

        rules.add( "/b/c/f", new TestRule( "alpha" ) );
        rules.add( "/c/f", new TestRule( "beta" ) );
        rules.add( "/b", new TestRule( "gamma" ) );

        // now test a few patterns
        // check that all are return in the order which they were added
        List<Rule> matches = rules.match( "", "/b/c", null, null );
        assertEquals( 0, matches.size(), "Wrong number of rules returned (1)" );

        matches = rules.match( "", "/b/c/f", null, null );
        assertEquals( 0, matches.size(), "Wrong number of rules returned (2)" );
    }

    @Test
    void testSimpleRegexMatch()
    {

        final SimpleRegexMatcher matcher = new SimpleRegexMatcher();

        // SimpleLog log = new SimpleLog("{testSimpleRegexMatch:SimpleRegexMatcher]");
        // log.setLevel(SimpleLog.LOG_LEVEL_TRACE);

        assertTrue( matcher.match( "/alpha/beta/gamma", "/alpha/beta/gamma" ), "Simple Regex Match '/alpha/beta/gamma' to '/alpha/beta/gamma' " );
        assertFalse( matcher.match( "/alpha/beta/gamma", "/alpha/beta/gamma/epsilon" ), "Simple Regex Match '/alpha/beta/gamma' to '/alpha/beta/gamma/epsilon' " );
        assertTrue( matcher.match( "/alpha/beta/gamma", "/alpha/*" ), "Simple Regex Match '/alpha/beta/gamma' to '/alpha/*' " );
        assertTrue( matcher.match( "/alpha/beta/gamma", "/alpha/*/gamma" ), "Simple Regex Match '/alpha/beta/gamma' to '/alpha/*/gamma' " );
        assertFalse( matcher.match( "/alpha/beta/gamma", "/alpha/*me" ), "Simple Regex Match '/alpha/beta/gamma' to '/alpha/*me' " );
        assertTrue( matcher.match( "/alpha/beta/gamma", "*/beta/gamma" ), "Simple Regex Match '/alpha/beta/gamma' to '*/beta/gamma' " );
        assertTrue( matcher.match( "/alpha/beta/gamma", "*/alpha/beta/gamma" ), "Simple Regex Match '/alpha/beta/gamma' to '*/alpha/beta/gamma' " );
        assertFalse( matcher.match( "/alpha/beta/gamma", "*/bet/gamma" ), "Simple Regex Match '/alpha/beta/gamma' to '*/bet/gamma' " );
        assertTrue( matcher.match( "/alpha/beta/gamma", "/alph?/beta/gamma" ), "Simple Regex Match '/alpha/beta/gamma' to 'alph?/beta/gamma' " );
        assertTrue( matcher.match( "/alpha/beta/gamma", "/?lpha/beta/gamma" ), "Simple Regex Match '/alpha/beta/gamma' to '/?lpha/beta/gamma' " );
        assertFalse( matcher.match( "/alpha/beta/gamma", "/alpha/?beta/gamma" ), "Simple Regex Match '/alpha/beta/gamma' to '/alpha/?beta/gamma' " );
        assertTrue( matcher.match( "/alpha/beta/gamma", "/alpha/?eta/*" ), "Simple Regex Match '/alpha/beta/gamma' to '/alpha/?eta/*' " );
        assertFalse( matcher.match( "/alpha/beta/gamma", "/alpha/?eta/*e" ), "Simple Regex Match '/alpha/beta/gamma' to '/alpha/?eta/*e' " );
        assertTrue( matcher.match( "/alpha/beta/gamma", "*/?et?/?amma" ), "Simple Regex Match '/alpha/beta/gamma' to '*/?et?/?amma' " );
        assertTrue( matcher.match( "/alpha/beta/gamma/beta/epsilon/beta/gamma/epsilon", "*/beta/gamma/?p*n" ), "Simple Regex Match '/alpha/beta/gamma/beta/epsilon/beta/gamma/epsilon' to "
                + " '*/beta/gamma/?p*n' " );
        assertFalse( matcher.match( "/alpha/beta/gamma", "*/beta/gamma/?p*no" ), "Simple Regex Match '/alpha/beta/gamma/beta/epsilon/beta/gamma/epsilon' to "
                + " '*/beta/gamma/?p*no' " );
    }
}
