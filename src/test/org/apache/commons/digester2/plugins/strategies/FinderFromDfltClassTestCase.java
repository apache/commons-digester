/* $Id$
 *
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.commons.digester2.plugins.strategies;

import java.util.List;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.digester2.*;
import org.apache.commons.digester2.actions.*;
import org.apache.commons.digester2.plugins.*;

/**
 * Test cases for FinderFromDfltClass behaviour.
 */

public class FinderFromDfltClassTestCase extends TestCase {
    // -----------------------------------------------------------
    // Helper classes
    // -----------------------------------------------------------

    public static class DummyAction extends AbstractAction {
    }

    public static class DummyAction1 extends DummyAction {
    }

    public static class DummyAction2 extends DummyAction {
    }

    public static class DummyPlugin1 {
    }
    
    public static class DummyPlugin2 {
    }
    
    public static class DummyPlugin1RuleInfo {
        public static void addRules(RuleManager rm, String pathPrefix)
        throws InvalidRuleException {
            rm.addRule(pathPrefix, new DummyAction1());
            rm.addRule(pathPrefix + "/baz", new DummyAction2());
        }
    }

    public static class DummyPlugin1Rules {
        public static void doRules(RuleManager rm, String pathPrefix)
        throws InvalidRuleException {
            rm.addRule(pathPrefix, new DummyAction1());
            rm.addRule(pathPrefix + "/baz", new DummyAction2());
            rm.addRule(pathPrefix + "/rum", new DummyAction2());
        }
    }

    // -----------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public FinderFromDfltClassTestCase(String name) {
        super(name);
    }

    // --------------------------------------------------
    // Overall Test Methods
    // --------------------------------------------------

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(FinderFromDfltClassTestCase.class));
    }

    // ------------------------------------------------
    // Individual Test Methods
    // ------------------------------------------------

    /**
     * Test the basic functionality of this class, ie what happens when there
     * exists a class with name of plugin-class-name + "RuleInfo".
     */
    public void testFinderMatched() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        RuleManager ruleManager = saxHandler.getRuleManager();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, ruleManager);

        FinderFromDfltClass finder = new FinderFromDfltClass();

        Class pluginClass = DummyPlugin1.class;
        Properties props = new Properties();
        RuleLoader loader = finder.findLoader(context, pluginClass, props);
        assertNotNull("No loader found", loader);

        // prepare to add rules
        context.pushMatchPath("", "foo");
        context.pushMatchPath("", "bar");
        String path = context.getMatchPath();
        assertEquals("Unexpected path", "/foo/bar", path); // paranoia test!

        assertEquals("RuleManager not empty", 0, ruleManager.getActions().size());
        loader.addRules(context);

        // assert ruleManager has two rules
        assertEquals("RuleManager has unexpected number of actions",
            2,
            ruleManager.getActions().size());
        assertEquals("Action1 is not of expected type",
            DummyAction1.class, ruleManager.getActions().get(0).getClass());
        assertEquals("Action2 is not of expected type",
            DummyAction2.class, ruleManager.getActions().get(1).getClass());

        // check actions returned for various paths
        List matchingActions = ruleManager.getMatchingActions("/foo");
        assertEquals("Unexpected actions", 0, matchingActions.size());
        matchingActions = ruleManager.getMatchingActions("/foo/bar");
        assertEquals("Unexpected actions", 1, matchingActions.size());
        assertEquals("Unexpected action type",
            DummyAction1.class, matchingActions.get(0).getClass());
        matchingActions = ruleManager.getMatchingActions("/foo/bar/baz");
        assertEquals("Unexpected actions", 1, matchingActions.size());
        assertEquals("Unexpected action type",
            DummyAction2.class, matchingActions.get(0).getClass());
        matchingActions = ruleManager.getMatchingActions("/foo/bar/bif");
        assertEquals("Unexpected actions", 0, matchingActions.size());
    }

    /**
     * Test what happens when there is no class with name equal to the
     * plugin-class-name + "RuleInfo".
     */
    public void testFinderNotMatched() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        RuleManager ruleManager = saxHandler.getRuleManager();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, ruleManager);

        FinderFromDfltClass finder = new FinderFromDfltClass();
        Properties props = new Properties();

        // No loader should be returned for DummyPlugin2 because that class
        // doesn't have a method of name "addRules".
        RuleLoader loader = finder.findLoader(context, DummyPlugin2.class, props);
        assertNull("Loader unexpectedly found", loader);
    }

    /**
     * Test the ability to configure the suffix and method name.
     */
    public void testNonDefaultNames() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        RuleManager ruleManager = saxHandler.getRuleManager();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, ruleManager);

        FinderFromDfltClass finder = new FinderFromDfltClass("Rules", "doRules");

        Class pluginClass = DummyPlugin1.class;
        Properties props = new Properties();
        RuleLoader loader = finder.findLoader(context, pluginClass, props);
        assertNotNull("No loader found", loader);

        // prepare to add rules
        context.pushMatchPath("", "foo");
        context.pushMatchPath("", "bar");
        String path = context.getMatchPath();
        assertEquals("Unexpected path", "/foo/bar", path); // paranoia test!

        assertEquals("RuleManager not empty", 0, ruleManager.getActions().size());
        loader.addRules(context);

        // assert ruleManager has two rules
        assertEquals("RuleManager has unexpected number of actions",
            3,
            ruleManager.getActions().size());
        assertEquals("Action1 is not of expected type",
            DummyAction1.class, ruleManager.getActions().get(0).getClass());
        assertEquals("Action2 is not of expected type",
            DummyAction2.class, ruleManager.getActions().get(1).getClass());
        assertEquals("Action2 is not of expected type",
            DummyAction2.class, ruleManager.getActions().get(2).getClass());

        // check actions returned for various paths
        List matchingActions = ruleManager.getMatchingActions("/foo");
        assertEquals("Unexpected actions", 0, matchingActions.size());
        matchingActions = ruleManager.getMatchingActions("/foo/bar");
        assertEquals("Unexpected actions", 1, matchingActions.size());
        assertEquals("Unexpected action type",
            DummyAction1.class, matchingActions.get(0).getClass());
        matchingActions = ruleManager.getMatchingActions("/foo/bar/baz");
        assertEquals("Unexpected actions", 1, matchingActions.size());
        assertEquals("Unexpected action type",
            DummyAction2.class, matchingActions.get(0).getClass());
        matchingActions = ruleManager.getMatchingActions("/foo/bar/bif");
        assertEquals("Unexpected actions", 0, matchingActions.size());
    }
}
