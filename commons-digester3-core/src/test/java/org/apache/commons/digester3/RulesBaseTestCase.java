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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Test Case for the RulesBase matching rules. Most of this material was original contained in the digester test case
 * but was moved into this class so that extensions of the basic matching rules behavior can extend this test case.
 * </p>
 */
public class RulesBaseTestCase
{

    /**
     * The digester instance we will be processing.
     */
    protected Digester digester;

    /**
     * <p>
     * This should be overridden by subclasses.
     *
     * @return the matching rules to be tested.
     */
    protected Rules createMatchingRulesForTest()
    {
        return new RulesBase();
    }

    /**
     * Sets up instance variables required by this test case.
     */
    @BeforeEach
    public void setUp()
    {

        digester = new Digester();
        digester.setRules( createMatchingRulesForTest() );

    }

    /**
     * Tear down instance variables required by this test case.
     */
    @AfterEach
    public void tearDown()
    {

        digester = null;

    }

    /**
     * Test basic matching involving namespaces.
     */
    @Test
    public void testBasicNamespaceMatching()
    {

        // clear any existing rules
        digester.getRules().clear();

        assertEquals( 0, digester.getRules().rules().size(), "Initial rules list is empty" );

        // Set up rules
        digester.addRule( "alpha/beta/gamma", new TestRule( "No-Namespace" ) );
        digester.addRule( "alpha/beta/gamma", new TestRule( "Euclidean-Namespace", "euclidean" ) );

        List<Rule> list = digester.getRules().rules();

        // test that matching null namespace brings back namespace and non-namespace rules
        list = digester.getRules().match( null, "alpha/beta/gamma", null, null );

        assertEquals( 2, list.size(), "Null namespace match (A)" );

        Iterator<Rule> it = list.iterator();
        assertEquals( "No-Namespace", ( ( TestRule ) it.next() ).getIdentifier(), "Null namespace match (B)" );
        assertEquals( "Euclidean-Namespace", ( ( TestRule ) it.next() ).getIdentifier(), "Null namespace match (C)" );

        // test that matching euclid namespace brings back namespace and non-namespace rules
        list = digester.getRules().match( "euclidean", "alpha/beta/gamma", null, null );

        assertEquals( 2, list.size(), "Matching namespace match (A)" );

        it = list.iterator();
        assertEquals( "No-Namespace", ( ( TestRule ) it.next() ).getIdentifier(), "Matching namespace match (B)" );
        assertEquals( "Euclidean-Namespace", ( ( TestRule ) it.next() ).getIdentifier(), "Matching namespace match (C)" );

        // test that matching another namespace brings back only non-namespace rule
        list = digester.getRules().match( "hyperbolic", "alpha/beta/gamma", null, null );

        assertEquals( 1, list.size(), "Non matching namespace match (A)" );

        it = list.iterator();
        assertEquals( "No-Namespace", ( ( TestRule ) it.next() ).getIdentifier(), "Non matching namespace match (B)" );

        // clean up
        digester.getRules().clear();

    }

    /**
     * Rules must always be returned in the correct order.
     */
    @Test
    public void testOrdering()
    {

        // clear any existing rules
        digester.getRules().clear();

        assertEquals( 0, digester.getRules().rules().size(), "Initial rules list is empty" );

        // Set up rules
        digester.addRule( "alpha/beta/gamma", new TestRule( "one" ) );
        digester.addRule( "alpha/beta/gamma", new TestRule( "two" ) );
        digester.addRule( "alpha/beta/gamma", new TestRule( "three" ) );

        // test that rules are returned in set order
        final List<Rule> list = digester.getRules().match( null, "alpha/beta/gamma", null, null );

        assertEquals( 3, list.size(), "Testing ordering mismatch (A)" );

        final Iterator<Rule> it = list.iterator();
        assertEquals( "one", ( ( TestRule ) it.next() ).getIdentifier(), "Testing ordering mismatch (B)" );
        assertEquals( "two", ( ( TestRule ) it.next() ).getIdentifier(), "Testing ordering mismatch (C)" );
        assertEquals( "three", ( ( TestRule ) it.next() ).getIdentifier(), "Testing ordering mismatch (D)" );

        // clean up
        digester.getRules().clear();

    }

    /**
     * Basic test for rule creation and matching.
     */
    @Test
    public void testRules()
    {

        // clear any existing rules
        digester.getRules().clear();

        // perform tests

        assertEquals( 0, digester.getRules().match( null, "a", null, null ).size(), "Initial rules list is empty" );
        digester.addSetProperties( "a" );
        assertEquals( 1, digester.getRules().match( null, "a", null, null ).size(), "Add a matching rule" );
        digester.addSetProperties( "b" );
        assertEquals( 1, digester.getRules().match( null, "a", null, null ).size(), "Add a non-matching rule" );
        digester.addSetProperties( "a/b" );
        assertEquals( 1, digester.getRules().match( null, "a", null, null ).size(), "Add a non-matching nested rule" );
        digester.addSetProperties( "a/b" );
        assertEquals( 2, digester.getRules().match( null, "a/b", null, null ).size(), "Add a second matching rule" );

        // clean up
        digester.getRules().clear();

    }

    /**
     * <p>
     * Test matching rules in {@link RulesBase}.
     * </p>
     * <p>
     * Tests:
     * </p>
     * <ul>
     * <li>exact match</li>
     * <li>tail match</li>
     * <li>longest pattern rule</li>
     * </ul>
     */
    @Test
    public void testRulesBase()
    {

        // clear any existing rules
        digester.getRules().clear();

        assertEquals( 0, digester.getRules().rules().size(), "Initial rules list is empty" );

        // We're going to set up
        digester.addRule( "a/b/c/d", new TestRule( "a/b/c/d" ) );
        digester.addRule( "*/d", new TestRule( "*/d" ) );
        digester.addRule( "*/c/d", new TestRule( "*/c/d" ) );

        // Test exact match
        assertEquals( 1, digester.getRules().match( null, "a/b/c/d", null, null ).size(), "Exact match takes precedence 1" );
        assertEquals( "a/b/c/d", ( ( TestRule ) digester.getRules().match( null, "a/b/c/d", null, null ).iterator().next() ).getIdentifier(), "Exact match takes precedence 2" );

        // Test wildcard tail matching
        assertEquals( 1, digester.getRules().match( null, "a/b/d", null, null ).size(), "Wildcard tail matching rule 1" );
        assertEquals( "*/d", ( ( TestRule ) digester.getRules().match( null, "a/b/d", null, null ).iterator().next() ).getIdentifier(), "Wildcard tail matching rule 2" );

        // Test the longest matching pattern rule
        assertEquals( 1, digester.getRules().match( null, "x/c/d", null, null ).size(), "Longest tail rule 1" );
        assertEquals( "*/c/d", ( ( TestRule ) digester.getRules().match( null, "x/c/d", null, null ).iterator().next() ).getIdentifier(), "Longest tail rule 2" );

        // Test wildcard tail matching at the top level,
        // i.e. the wildcard is nothing
        digester.addRule( "*/a", new TestRule( "*/a" ) );
        assertEquals( 1, digester.getRules().match( null, "a", null, null ).size(), "Wildcard tail matching rule 3" );

        assertEquals( 0, digester.getRules().match( null, "aa", null, null ).size(), "Wildcard tail matching rule 3 (match too much)" );
        // clean up
        digester.getRules().clear();

    }

    /** Tests the behavior when a rule is added with a trailing slash */
    @Test
    public void testTrailingSlash()
    {
        // clear any existing rules
        digester.getRules().clear();

        assertEquals( 0, digester.getRules().rules().size(), "Initial rules list is empty" );

        // Set up rules
        digester.addRule( "alpha/beta/gamma/", new TestRule( "one" ) );
        digester.addRule( "alpha/beta/", new TestRule( "two" ) );
        digester.addRule( "beta/gamma/alpha", new TestRule( "three" ) );

        // test that rules are returned in set order
        final List<Rule> list = digester.getRules().match( null, "alpha/beta/gamma", null, null );

        assertEquals( 1, list.size(), "Testing number of matches" );

        final Iterator<Rule> it = list.iterator();
        assertEquals( "one", ( ( TestRule ) it.next() ).getIdentifier(), "Testing ordering (A)" );

        // clean up
        digester.getRules().clear();
    }
}
