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


package org.apache.commons.digester2.plugins;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.digester2.*;
import org.apache.commons.digester2.actions.*;
import org.apache.commons.digester2.plugins.*;

/**
 * Test cases for basic Declaration behaviour.
 */

public class DeclarationTestCase extends TestCase {
    // -----------------------------------------------------------
    // Helper classes
    // -----------------------------------------------------------

    public static class DummyPlugin1 {
    }

    public static class DummyAction extends AbstractAction {
    }

    /**
     * Test RuleLoader that always adds to the specified context the
     * (pattern, action) pairs it was explicitly configured with.
     */
    public static class MyRuleLoader extends RuleLoader {
        private ArrayList patterns = new ArrayList();
        private ArrayList actions = new ArrayList();

        public void addRule(String pattern, Action action) {
            patterns.add(pattern);
            actions.add(action);
        }

        public void addRules(Context context) throws PluginException {
            RuleManager rm = context.getRuleManager();
            try {
                for(int i=0; i<patterns.size(); ++i) {
                    rm.addRule((String)patterns.get(i), (Action)actions.get(i));
                }
            } catch(InvalidRuleException ex) {
                // wrap exception in a PluginException, as specified in
                // javadoc for the RuleLoader.addRules method.
                throw new PluginException(ex);
            }
        }
    }

    /**
     * Test RuleFinder that always returns the one RuleLoader it is
     * configured with.
     */
    public static class MyRuleFinder extends RuleFinder {
        RuleLoader loader;
        public MyRuleFinder(RuleLoader loader) {
            this.loader = loader;
        }
        public RuleLoader findLoader(Context c, Class pc, Properties p) {
            return loader;
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
    public DeclarationTestCase(String name) {
        super(name);
    }

    // --------------------------------------------------
    // Overall Test Methods
    // --------------------------------------------------

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(DeclarationTestCase.class));
    }

    // ------------------------------------------------
    // Individual Test Methods
    // ------------------------------------------------

    /**
     * Test the constructor variant that takes a string classname as the
     * plugin class.
     */
    public void testDeclareByName() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, saxHandler.getRuleManager());
        Class pluginClass = DummyPlugin1.class;
        Properties props = new Properties();
        Declaration decl = new Declaration(
            context,
            pluginClass.getName(),
            props);

        // possible additional tests:
        // test cannot find class
        // test cannot find loader
        // test what happens when context.getClassLoader is not the same
        // classloader as our saxHandler was loaded through.
        assertEquals("getPluginClass failed", pluginClass, decl.getPluginClass());
    }

    /**
     * Test the constructor variant that takes a Class object as the
     * plugin class.
     */
    public void testDeclareByClass() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, saxHandler.getRuleManager());
        Class pluginClass = DummyPlugin1.class;
        Properties props = new Properties();
        Declaration decl = new Declaration(
            context,
            pluginClass,
            props);

        assertEquals("getPluginClass failed", pluginClass, decl.getPluginClass());
    }

    /**
     * Test the constructor when the specified class does not exist.
     */
    public void testBadDeclaration() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, saxHandler.getRuleManager());
        Properties props = new Properties();
        try {
            Declaration decl = new Declaration(
                context,
                "no.such.class",
                props);
            fail("Declaration constructor failed to throw exception for bad classname.");
        } catch(PluginException ex) {
            // ok, expected
        }
    }

    /**
     * Test setting/getting the id associated with a declaration.
     */
    public void testId() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, saxHandler.getRuleManager());
        Class pluginClass = DummyPlugin1.class;
        Properties props = new Properties();
        Declaration decl = new Declaration(
            context,
            pluginClass,
            props);

        assertNull("Initial id not null", decl.getId());
        decl.setId("foo");
        assertEquals("setId/getId failed", "foo", decl.getId());
    }

    /**
     * Test the basic functionality of this class.
     */
    public void testBasicOperation() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, saxHandler.getRuleManager());
        Class pluginClass = DummyPlugin1.class;
        Properties props = new Properties();

        Action action1 = new DummyAction();
        Action action2 = new DummyAction();
        MyRuleLoader myRuleLoader = new MyRuleLoader();
        myRuleLoader.addRule("/foo", action1);
        myRuleLoader.addRule("/foo/bar", action2);

        RuleFinder myRuleFinder = new MyRuleFinder(myRuleLoader);
        List finderList = new ArrayList(1);
        finderList.add(myRuleFinder);
        PluginConfiguration pc = PluginConfiguration.getInstance(context);
        pc.setRuleFinders(finderList);

        Declaration decl = new Declaration(context, pluginClass, props);

        // Now add the rules to the context's current RuleManager. The
        // declaration should locate the custom MyRuleFinder configured for
        // the PluginConfiguration. And that RuleFinder should return a
        // MyRuleLoader instance. And that loader should add the two rules
        // shown above to the context's rulemanager when configure is called.
        RuleManager ruleManager = context.getRuleManager();
        assertEquals("Rulemanager has rules", 0, ruleManager.getActions().size());
        decl.configure(context);
        assertEquals("Configure did not add 2 rules", 2, ruleManager.getActions().size());
        assertSame("Configure: unexpected rule",
            action1, ruleManager.getActions().get(0));
        assertEquals("Configure: unexpected rule",
            action2, ruleManager.getActions().get(1));
    }
}
