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
 * Test cases for FinderFromMethod behaviour.
 */

public class FinderFromMethodTestCase extends TestCase {
    // -----------------------------------------------------------
    // Helper classes
    // -----------------------------------------------------------

    public static class DummyAction extends AbstractAction {
    }
    
    public static class DummyAction1 extends DummyAction {
    }
    
    public static class DummyAction2 extends DummyAction {
    }
    
    public static class DummyPlugin {
        public static void doRules(RuleManager rm, String pathPrefix)
        throws InvalidRuleException {
            rm.addRule(pathPrefix, new DummyAction1());
            rm.addRule(pathPrefix + "/baz", new DummyAction2());
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
    public FinderFromMethodTestCase(String name) {
        super(name);
    }

    // --------------------------------------------------
    // Overall Test Methods
    // --------------------------------------------------

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(FinderFromMethodTestCase.class));
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

        FinderFromMethod finder = new FinderFromMethod();
        
        Class pluginClass = DummyPlugin.class;
        Properties props = new Properties();
        RuleLoader loader = finder.findLoader(context, pluginClass, props);
        
        // Null should have been returned, because the properties didn't
        // indicate which method to call.
        assertNull("Loader unexpectedly found", loader);
    }

    /**
     * Test the basic functionality of this class.
     */
    public void testFinderMatched() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        RuleManager ruleManager = saxHandler.getRuleManager();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, ruleManager);

        FinderFromMethod finder = new FinderFromMethod();
        
        Class pluginClass = DummyPlugin.class;
        Properties props = new Properties();
        props.put("method", "doRules");
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
     * Test the ability to configure the attribute that specifies what
     * method to call.
     */
    public void testNonDefaultAttribute() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        RuleManager ruleManager = saxHandler.getRuleManager();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, ruleManager);

        FinderFromMethod finder = new FinderFromMethod("target");
        
        Class pluginClass = DummyPlugin.class;
        Properties props = new Properties();
        props.put("target", "doRules");
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
     * Test that an exception is thrown if the specified method does not exist.
     */
    public void testNoSuchMethod() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        RuleManager ruleManager = saxHandler.getRuleManager();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, ruleManager);

        FinderFromMethod finder = new FinderFromMethod();
        
        Class pluginClass = DummyPlugin.class;
        Properties props = new Properties();
        props.put("method", "noSuchMethod");
        try {
            RuleLoader loader = finder.findLoader(context, pluginClass, props);
            fail("Exception not thrown for bad method name.");
        } catch(PluginException ex) {
            // ok, expected
        }
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

        FinderFromMethod finder = new FinderFromMethod();
        
        Class pluginClass = DummyPlugin.class;
        Properties props = new Properties();
        props.put("method", "nonStatic");
        RuleLoader loader = finder.findLoader(context, pluginClass, props);
        assertNotNull("No loader found", loader);

        try {        
            loader.addRules(context);
            fail("Exception not thrown for non-static method");
        } catch(PluginException ex) {
            // ok, expected
        }
    }
}
