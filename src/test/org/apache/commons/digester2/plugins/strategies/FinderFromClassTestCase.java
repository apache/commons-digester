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
 * Test cases for FinderFromClass behaviour.
 */

public class FinderFromClassTestCase extends TestCase {
    // -----------------------------------------------------------
    // Helper classes
    // -----------------------------------------------------------

    public static class DummyPlugin {
    }

    public static class DummyAction extends AbstractAction {
    }

    public static class DummyAction1 extends DummyAction {
    }

    public static class DummyAction2 extends DummyAction {
    }

    public static class DummyRuleInfo1 {
        public static void addRules(RuleManager rm, String pathPrefix)
        throws InvalidRuleException {
            rm.addRule(pathPrefix, new DummyAction1());
            rm.addRule(pathPrefix + "/baz", new DummyAction2());
        }
    }

    public static class DummyRuleInfo2 {
        public static void doRules(RuleManager rm, String pathPrefix)
        throws InvalidRuleException {
            rm.addRule(pathPrefix, new DummyAction1());
            rm.addRule(pathPrefix + "/baz", new DummyAction2());
        }
    }

    public static class DummyRuleInfo3 {
        public static void addRules(String s1, String s2){
        }

        public void nonStatic(RuleManager rm, String pathPrefix)
        throws InvalidRuleException {
            rm.addRule(pathPrefix, new DummyAction1());
            rm.addRule(pathPrefix + "/baz", new DummyAction2());
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
    public FinderFromClassTestCase(String name) {
        super(name);
    }

    // --------------------------------------------------
    // Overall Test Methods
    // --------------------------------------------------

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(FinderFromClassTestCase.class));
    }

    // ------------------------------------------------
    // Individual Test Methods
    // ------------------------------------------------

    /**
     * Test what happens when the properties don't have an attribute
     * that specifies which method to call.
     */
    public void testFinderNotMatched() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        RuleManager ruleManager = saxHandler.getRuleManager();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, ruleManager);

        FinderFromClass finder = new FinderFromClass();

        Properties props = new Properties();
        RuleLoader loader = finder.findLoader(context, DummyPlugin.class, props);

        // Null should have been returned, because the properties didn't
        // indicate which ruleinfo class to use.
        assertNull("Loader unexpectedly found", loader);
    }

    /**
     * Test the basic functionality of this class, ie what happens when the
     * properties include an attribute of name "ruleclass" which has the
     * name of a class with a valid "addRules" method.
     */
    public void testFinderMatched() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        RuleManager ruleManager = saxHandler.getRuleManager();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, ruleManager);

        FinderFromClass finder = new FinderFromClass();

        Properties props = new Properties();
        props.put("ruleclass", DummyRuleInfo1.class.getName());
        RuleLoader loader = finder.findLoader(context, DummyPlugin.class, props);
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
     * Test what happens when the ruleclass attribute contains the name of
     * a class that doesn't exist.
     */
    public void testNoSuchRuleClass() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        RuleManager ruleManager = saxHandler.getRuleManager();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, ruleManager);

        FinderFromClass finder = new FinderFromClass();
        Properties props = new Properties();
        RuleLoader loader;

        loader = finder.findLoader(context, DummyPlugin.class, props);
    }

    /**
     * Test that an exception occurs if the specified method does not exist.
     */
    public void testNoSuchMethod() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        RuleManager ruleManager = saxHandler.getRuleManager();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, ruleManager);

        FinderFromClass finder = new FinderFromClass();

        Properties props = new Properties();
        props.put("ruleclass", DummyRuleInfo1.class.getName());
        props.put("method", "noSuchMethod");
        try {
            RuleLoader loader = finder.findLoader(context, DummyPlugin.class, props);
            fail("No exception thrown when target method does not exist");
        } catch(PluginException ex) {
            // ok, expected
        }
    }

    /**
     * Test the ability to use alternative names for the xml attributes
     * that specify class and method.
     */
    public void testNonDefaultAttributes() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        RuleManager ruleManager = saxHandler.getRuleManager();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, ruleManager);

        FinderFromClass finder = new FinderFromClass(
            "the-rules", "the-method", "dfltAddRules");

        Properties props = new Properties();
        props.put("the-rules", DummyRuleInfo2.class.getName());
        props.put("the-method", "doRules");
        RuleLoader loader = finder.findLoader(context, DummyPlugin.class, props);
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
     * Test what happens when the user has specified a custom default
     * method name.
     */
    public void testAlternativeDefaultMethodName() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        RuleManager ruleManager = saxHandler.getRuleManager();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, ruleManager);

        FinderFromClass finder = new FinderFromClass(
            "the-rules", "the-method", "doRules");

        Properties props = new Properties();
        props.put("the-rules", DummyRuleInfo2.class.getName());
        // deliberately don't add any xml attribute of name "the-method",
        // so the default gets used.
        RuleLoader loader = finder.findLoader(context, DummyPlugin.class, props);
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
     * Test that an exception occurs if the specified method is not static.
     */
    public void testNonStaticMethod() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        RuleManager ruleManager = saxHandler.getRuleManager();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, ruleManager);

        FinderFromClass finder = new FinderFromClass();

        Properties props = new Properties();
        props.put("ruleclass", DummyRuleInfo3.class.getName());

        try {
            RuleLoader loader = finder.findLoader(context, DummyPlugin.class, props);
            fail("No exception thrown when target method is not static");
        } catch(PluginException ex) {
            // ok, expected
        }
    }
}
