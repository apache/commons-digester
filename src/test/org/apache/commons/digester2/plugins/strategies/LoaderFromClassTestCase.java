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
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.beanutils.MethodUtils;

import org.apache.commons.digester2.*;
import org.apache.commons.digester2.actions.*;
import org.apache.commons.digester2.plugins.*;

/**
 * Test cases for LoaderFromClass behaviour.
 */

public class LoaderFromClassTestCase extends TestCase {
    // -----------------------------------------------------------
    // Helper classes
    // -----------------------------------------------------------

    public static class DummyAction extends AbstractAction {
    }
    
    public static class DummyAction1 extends DummyAction {
    }
    
    public static class DummyAction2 extends DummyAction {
    }
    
    public static class MyRuleClass {
        public static void doRules(RuleManager rm, String pathPrefix) 
        throws InvalidRuleException {
            rm.addRule(pathPrefix, new DummyAction1());
            rm.addRule(pathPrefix + "/baz", new DummyAction2());
        }
        public static void doBadRules(RuleManager rm, String pathPrefix) 
        throws Exception { 
            throw new Exception("Cannot add rules");
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
    public LoaderFromClassTestCase(String name) {
        super(name);
    }

    // --------------------------------------------------
    // Overall Test Methods
    // --------------------------------------------------

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(LoaderFromClassTestCase.class));
    }

    // ------------------------------------------------
    // Individual Test Methods
    // ------------------------------------------------


    private void doBasicTests(LoaderFromClass loader) throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        RuleManager ruleManager = saxHandler.getRuleManager();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, ruleManager);

        context.pushMatchPath("", "foo");
        context.pushMatchPath("", "bar");
        String path = context.getMatchPath();

        // paranoia test...
        assertEquals("Unexpected path", "/foo/bar", path);

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
     * Test LoaderFromClass with string methodName parameter to constructor.
     */
    public void testMethodName() throws Exception {
        LoaderFromClass loader = new LoaderFromClass(MyRuleClass.class, "doRules");
        doBasicTests(loader);
    }

    /**
     * Test LoaderFromClass with explicit Method parameter to constructor.
     */
    public void testMethodObject() throws Exception {
        String methodName = "doRules";
        Class[] paramSpec = { RuleManager.class, String.class };
        Method rulesMethod = MethodUtils.getAccessibleMethod(
            MyRuleClass.class, methodName, paramSpec);
        LoaderFromClass loader = new LoaderFromClass(MyRuleClass.class, rulesMethod);
        doBasicTests(loader);
    }

    /**
     * Test LoaderFromClass when the method that is expected to add rules
     * throws an exception instead.
     */
    public void testAddRulesException() throws Exception {
        try {
            LoaderFromClass loader = new LoaderFromClass(MyRuleClass.class, "doBadRules");
            doBasicTests(loader);
            fail("Exception not thrown when expected.");
        } catch(PluginException ex) {
            // ok
        } catch(Throwable t) {
            fail("Exception thrown was not of type PluginException:"
                + t.getClass());
        }
    }
}
