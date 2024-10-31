/*
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

package org.apache.commons.digester3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * <p>
 * Runs standard tests for RulesBase as well as tests of extensions.
 */
public class ExtendedBaseRulesTestCase
    extends RulesBaseTestCase
{

    /**
     * <p>
     * This should be overridden by subclasses.
     *
     * @return the matching rules to be tested.
     */
    @Override
    protected Rules createMatchingRulesForTest()
    {

        return new ExtendedBaseRules();
    }

    @Test
    public void testAncestorMatch()
    {
        // test fixed root ancestor
        digester.getRules().clear();

        digester.addRule( "!a/b/*", new TestRule( "uni-a-b-star" ) );
        digester.addRule( "a/b/*", new TestRule( "a-b-star" ) );
        digester.addRule( "a/b/c", new TestRule( "a-b-c" ) );
        digester.addRule( "a/b/?", new TestRule( "a-b-child" ) );

        List<Rule> list = digester.getRules().match( null, "a/b/c", null, null );

        assertEquals( 2, list.size(), "Simple ancestor matches (1)" );
        assertEquals( "uni-a-b-star", ( ( TestRule ) list.get( 0 ) ).getIdentifier(), "Universal ancestor mismatch (1)" );
        assertEquals( "a-b-c", ( ( TestRule ) list.get( 1 ) ).getIdentifier(), "Parent precedence failure" );

        list = digester.getRules().match( null, "a/b/b", null, null );
        assertEquals( 2, list.size(), "Simple ancestor matches (2)" );
        assertEquals( "uni-a-b-star", ( ( TestRule ) list.get( 0 ) ).getIdentifier(), "Universal ancestor mismatch (2)" );
        assertEquals( "a-b-child", ( ( TestRule ) list.get( 1 ) ).getIdentifier(), "Child precedence failure" );

        list = digester.getRules().match( null, "a/b/d", null, null );
        assertEquals( 2, list.size(), "Simple ancestor matches (3)" );
        assertEquals( "uni-a-b-star", ( ( TestRule ) list.get( 0 ) ).getIdentifier(), "Universal ancestor mismatch (3)" );
        assertEquals( "a-b-child", ( ( TestRule ) list.get( 1 ) ).getIdentifier(), "Ancestor mismatch (1)" );

        list = digester.getRules().match( null, "a/b/d/e/f", null, null );
        assertEquals( 2, list.size(), "Simple ancestor matches (4)" );
        assertEquals( "uni-a-b-star", ( ( TestRule ) list.get( 0 ) ).getIdentifier(), "Universal ancestor mismatch (4)" );
        assertEquals( "a-b-star", ( ( TestRule ) list.get( 1 ) ).getIdentifier(), "Ancestor mismatch (2)" );

        // test wildcard root ancestor
        digester.getRules().clear();

        digester.addRule( "!*/a/b/*", new TestRule( "uni-star-a-b-star" ) );
        digester.addRule( "*/b/c/*", new TestRule( "star-b-c-star" ) );
        digester.addRule( "*/b/c/d", new TestRule( "star-b-c-d" ) );
        digester.addRule( "a/b/c", new TestRule( "a-b-c" ) );

        list = digester.getRules().match( null, "a/b/c", null, null );
        assertEquals( 2, list.size(), "Wildcard ancestor match (1)" );
        assertEquals( "uni-star-a-b-star", ( ( TestRule ) list.get( 0 ) ).getIdentifier(), "Universal ancestor mismatch (5)" );
        assertEquals( "a-b-c", ( ( TestRule ) list.get( 1 ) ).getIdentifier(), "Match missed (1)" );

        list = digester.getRules().match( null, "b/c", null, null );
        assertEquals( 1, list.size(), "Wildcard ancestor match (2)" );
        assertEquals( "star-b-c-star", ( ( TestRule ) list.get( 0 ) ).getIdentifier(), "Match missed (2)" );

        list = digester.getRules().match( null, "a/b/c/d", null, null );
        assertEquals( 2, list.size(), "Wildcard ancestor match (3)" );
        assertEquals( "uni-star-a-b-star", ( ( TestRule ) list.get( 0 ) ).getIdentifier(), "Match missed (3)" );
        assertEquals( "star-b-c-d", ( ( TestRule ) list.get( 1 ) ).getIdentifier(), "Match missed (4)" );

        list = digester.getRules().match( null, "b/b/c/e/d", null, null );
        assertEquals( 1, list.size(), "Wildcard ancestor match (2)" );
        assertEquals( "star-b-c-star", ( ( TestRule ) list.get( 0 ) ).getIdentifier(), "Match missed (5)" );
    }

    /**
     * Basic test of parent matching rules. A parent match matches any child of a particular kind of parent. A wild
     * parent has a wildcard prefix. This method tests non-universal wildcards.
     */
    @Test
    public void testBasicParentMatch()
    {

        // clear any existing rules
        digester.getRules().clear();

        assertEquals( 0, digester.getRules().rules().size(), "Initial rules list is empty" );

        // Set up rules
        // since these are all NON-UNIVERSAL matches
        // only expect one match at each stage
        digester.addRule( "alpha/beta/gamma/delta", new TestRule( "exact" ) );
        digester.addRule( "*/beta/gamma/epsilon", new TestRule( "wildcard_child" ) );
        digester.addRule( "alpha/beta/gamma/?", new TestRule( "exact_parent" ) );
        digester.addRule( "*/beta/gamma/?", new TestRule( "wildcard_parent" ) );

        // this should match just the exact since this takes precedence
        List<Rule> list = digester.getRules().match( null, "alpha/beta/gamma/delta", null, null );

        // all three rules should match
        assertEquals( 1, list.size(), "Testing basic parent mismatch (A)" );

        Iterator<Rule> it = list.iterator();
        assertEquals( "exact", ( ( TestRule ) it.next() ).getIdentifier(), "Testing basic parent mismatch (B)" );

        // we don't have an exact match for this child so we should get the exact parent
        list = digester.getRules().match( null, "alpha/beta/gamma/epsilon", null, null );

        // all three rules should match
        assertEquals( 1, list.size(), "Testing basic parent mismatch (C)" );

        it = list.iterator();
        assertEquals( "exact_parent", ( ( TestRule ) it.next() ).getIdentifier(), "Testing basic parent mismatch (D)" );

        // wildcard child overrides wildcard parent
        list = digester.getRules().match( null, "alpha/omega/beta/gamma/epsilon", null, null );

        // all three rules should match
        assertEquals( 1, list.size(), "Testing basic parent mismatch (E)" );

        it = list.iterator();
        assertEquals( "wildcard_child", ( ( TestRule ) it.next() ).getIdentifier(), "Testing basic parent mismatch (F)" );

        // nothing else matches so return wildcard parent
        list = digester.getRules().match( null, "alpha/omega/beta/gamma/zeta", null, null );

        // all three rules should match
        assertEquals( 1, list.size(), "Testing basic parent mismatch (G)" );

        it = list.iterator();
        assertEquals( "wildcard_parent", ( ( TestRule ) it.next() ).getIdentifier(), "Testing basic parent mismatch (H)" );

        // clean up
        digester.getRules().clear();

    }

    /**
     * Basic test of universal matching rules. Universal rules act independent.
     */
    @Test
    public void testBasicUniversal()
    {

        // clear any existing rules
        digester.getRules().clear();

        assertEquals( 0, digester.getRules().rules().size(), "Initial rules list is empty" );

        // Set up rules
        // set up universal matches against non-universal ones
        digester.addRule( "alpha/beta/gamma", new TestRule( "exact" ) );
        digester.addRule( "*/beta/gamma", new TestRule( "non_wildcard_head" ) );
        digester.addRule( "!*/beta/gamma", new TestRule( "universal_wildcard_head" ) );
        digester.addRule( "!alpha/beta/gamma/?", new TestRule( "universal_wildcard_child" ) );
        digester.addRule( "alpha/beta/gamma/?", new TestRule( "non_wildcard_child" ) );
        digester.addRule( "alpha/beta/gamma/epsilon", new TestRule( "exact2" ) );
        digester.addRule( "alpha/epsilon/beta/gamma/zeta", new TestRule( "exact3" ) );
        digester.addRule( "*/gamma/?", new TestRule( "non_wildhead_child" ) );
        digester.addRule( "!*/epsilon/beta/gamma/?", new TestRule( "universal_wildhead_child" ) );

        List<Rule> list;
        Iterator<Rule> it;

        // test universal wildcard head
        list = digester.getRules().match( null, "alpha/beta/gamma", null, null );

        assertEquals( 2, list.size(), "Testing universal wildcard mismatch (A)" );

        it = list.iterator();
        assertEquals( "exact", ( ( TestRule ) it.next() ).getIdentifier(), "Testing universal wildcard mismatch (B)" );
        assertEquals( "universal_wildcard_head", ( ( TestRule ) it.next() ).getIdentifier(), "Testing universal wildcard mismatch (C)" );

        // test universal parent
        list = digester.getRules().match( null, "alpha/beta/gamma/epsilon", null, null );

        assertEquals( 2, list.size(), "Testing universal wildcard mismatch (D)" );

        it = list.iterator();
        assertEquals( "universal_wildcard_child", ( ( TestRule ) it.next() ).getIdentifier(), "Testing universal wildcard mismatch (E)" );
        assertEquals( "exact2", ( ( TestRule ) it.next() ).getIdentifier(), "Testing universal wildcard mismatch (F)" );

        // test universal parent
        list = digester.getRules().match( null, "alpha/beta/gamma/zeta", null, null );

        assertEquals( 2, list.size(), "Testing universal wildcard mismatch (G)" );

        it = list.iterator();
        assertEquals( "universal_wildcard_child", ( ( TestRule ) it.next() ).getIdentifier(), "Testing universal wildcard mismatch (H)" );
        assertEquals( "non_wildcard_child", ( ( TestRule ) it.next() ).getIdentifier(), "Testing universal wildcard mismatch (I)" );

        // test wildcard universal parent
        list = digester.getRules().match( null, "alpha/epsilon/beta/gamma/alpha", null, null );

        assertEquals( 2, list.size(), "Testing universal wildcard mismatch (J)" );

        it = list.iterator();
        assertEquals( "non_wildhead_child", ( ( TestRule ) it.next() ).getIdentifier(), "Testing universal wildcard mismatch (K)" );
        assertEquals( "universal_wildhead_child", ( ( TestRule ) it.next() ).getIdentifier(), "Testing universal wildcard mismatch (L)" );

        // test wildcard universal parent
        list = digester.getRules().match( null, "alpha/epsilon/beta/gamma/zeta", null, null );

        assertEquals( 2, list.size(), "Testing universal wildcard mismatch (M)" );

        it = list.iterator();
        assertEquals( "exact3", ( ( TestRule ) it.next() ).getIdentifier(), "Testing universal wildcard mismatch (M)" );
        assertEquals( "universal_wildhead_child", ( ( TestRule ) it.next() ).getIdentifier(), "Testing universal wildcard mismatch (O)" );

        // clean up
        digester.getRules().clear();

    }

    @Test
    public void testInstructors()
    {
        digester.getRules().clear();

        digester.addRule( "!instructors/*", new TestRule( "instructors" ) );
        digester.addRule( "!instructor/*", new TestRule( "instructor" ) );

        final List<Rule> list = digester.getRules().match( null, "instructors", null, null );
        assertEquals( 1, list.size(), "Only expect to match instructors" );
        assertEquals( "instructors", ( ( TestRule ) list.get( 0 ) ).getIdentifier(), "Instructors expected" );

    }

    @Test
    public void testLongMatch()
    {

        digester.getRules().clear();

        digester.addRule( "a/b/c/d/*", new TestRule( "a-b-c-d-star" ) );

        List<Rule> list = digester.getRules().match( null, "a/b/c/d/e", null, null );
        assertEquals( 1, list.size(), "Long match (1)" );
        assertEquals( "a-b-c-d-star", ( ( TestRule ) list.get( 0 ) ).getIdentifier(), "Match missed (1)" );

        list = digester.getRules().match( null, "a/b/c/d/e/f", null, null );
        assertEquals( 1, list.size(), "Long match (2)" );
        assertEquals( "a-b-c-d-star", ( ( TestRule ) list.get( 0 ) ).getIdentifier(), "Match missed (2)" );

        list = digester.getRules().match( null, "a/b/c/d/e/f/g", null, null );
        assertEquals( 1, list.size(), "Long match (3)" );
        assertEquals( "a-b-c-d-star", ( ( TestRule ) list.get( 0 ) ).getIdentifier(), "Match missed (3)" );

        list = digester.getRules().match( null, "a/b/c/d", null, null );
        assertEquals( 0, list.size(), "Long match (4)" );
    }

    @Test
    public void testMiddleInstructors()
    {
        digester.getRules().clear();

        digester.addRule( "!instructors/*", new TestRule( "instructors" ) );

        final List<Rule> list = digester.getRules().match( null, "/tosh/instructors/fiddlesticks", null, null );
        assertEquals( 0, list.size(), "No matches expected" );

    }

    /**
     * Basic test of wildcard matches. A universal will match matches anything! A non-universal will match matches anything
     * not matched by something else. This method tests non-universal and universal wildcard matches.
     */
    @Test
    public void testRootTailMatch()
    {

        // clear any existing rules
        digester.getRules().clear();

        assertEquals( 0, digester.getRules().rules().size(), "Initial rules list is empty" );

        // Set up rules
        // The combinations a little large to test everything but we'll pick a couple and try them.
        digester.addRule( "*/a", new TestRule( "a_tail" ) );

        List<Rule> list;

        list = digester.getRules().match( null, "a", null, null );

        assertEquals( 1, list.size(), "Testing tail wrong size (A)" );
        assertEquals( "a_tail", ( ( TestRule ) list.get( 0 ) ).getIdentifier(), "Testing tail mismatch (B)" );

        list = digester.getRules().match( null, "beta/a", null, null );

        assertEquals( 1, list.size(), "Testing tail wrong size (C)" );
        assertEquals( "a_tail", ( ( TestRule ) list.get( 0 ) ).getIdentifier(), "Testing tail mismatch (D)" );

        list = digester.getRules().match( null, "be/aaa", null, null );

        assertEquals( 0, list.size(), "Testing tail no matches (E)" );

        list = digester.getRules().match( null, "aaa", null, null );

        assertEquals( 0, list.size(), "Testing tail no matches (F)" );

        list = digester.getRules().match( null, "a/beta", null, null );

        assertEquals( 0, list.size(), "Testing tail no matches (G)" );

        // clean up
        digester.getRules().clear();

    }

    /**
     * Basic test of wildcard matches. A universal will match matches anything! A non-universal wildcard matches anything
     * not matched by something else. This method tests non-universal and universal wildcard matches.
     */
    @Test
    public void testWildCardMatch()
    {

        // clear any existing rules
        digester.getRules().clear();

        assertEquals( 0, digester.getRules().rules().size(), "Initial rules list is empty" );

        // Set up rules
        // The combinations a little large to test everything but we'll pick a couple and try them.
        digester.addRule( "*", new TestRule( "basic_wildcard" ) );
        digester.addRule( "!*", new TestRule( "universal_wildcard" ) );
        digester.addRule( "alpha/beta/gamma/delta", new TestRule( "exact" ) );
        digester.addRule( "*/beta/gamma/?", new TestRule( "wildcard_parent" ) );

        List<Rule> list;
        Iterator<Rule> it;

        // The universal wildcard will always match whatever else does
        list = digester.getRules().match( null, "alpha/beta/gamma/delta", null, null );

        // all three rules should match
        assertEquals( 2, list.size(), "Testing wildcard mismatch (A)" );

        it = list.iterator();
        assertEquals( "universal_wildcard", ( ( TestRule ) it.next() ).getIdentifier(), "Testing wildcard mismatch (B)" );
        assertEquals( "exact", ( ( TestRule ) it.next() ).getIdentifier(), "Testing wildcard mismatch (C)" );

        // The universal wildcard will always match whatever else does
        list = digester.getRules().match( null, "alpha/beta/gamma/epsilon", null, null );

        assertEquals( 2, list.size(), "Testing wildcard mismatch (D)" );

        it = list.iterator();
        assertEquals( "universal_wildcard", ( ( TestRule ) it.next() ).getIdentifier(), "Testing wildcard mismatch (E)" );
        assertEquals( "wildcard_parent", ( ( TestRule ) it.next() ).getIdentifier(), "Testing wildcard mismatch (F)" );

        // The universal wildcard will always match whatever else does
        // we have no other non-universal matching so this will match the non-universal wildcard as well
        list = digester.getRules().match( null, "alpha/gamma", null, null );

        assertEquals( 2, list.size(), "Testing wildcard mismatch (G)" );

        it = list.iterator();
        assertEquals( "basic_wildcard", ( ( TestRule ) it.next() ).getIdentifier(), "Testing wildcard mismatch (H)" );
        assertEquals( "universal_wildcard", ( ( TestRule ) it.next() ).getIdentifier(), "Testing wildcard mismatch (I)" );

        // clean up
        digester.getRules().clear();

    }
}
