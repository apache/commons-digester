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
 * Test cases for basic PluginDeclarationScope behaviour.
 */

public class PluginDeclarationScopeTestCase extends TestCase {
    // -----------------------------------------------------------
    // Helper classes
    // -----------------------------------------------------------

    public static class DummyPlugin1 {}
    public static class DummyPlugin2 {}
    public static class DummyPlugin3 {}
    public static class DummyPlugin4 {}

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
    public PluginDeclarationScopeTestCase(String name) {
        super(name);
    }

    // --------------------------------------------------
    // Overall Test Methods
    // --------------------------------------------------

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(PluginDeclarationScopeTestCase.class));
    }

    // ------------------------------------------------
    // Individual Test Methods
    // ------------------------------------------------

    /**
     * Test the getInstance methods of this class.
     */
    public void testGetInstance() throws Exception {
        Digester digester = new Digester();
        SAXHandler saxHandler = digester.getSAXHandler();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, null);

        // object created on first call
        PluginDeclarationScope pds = PluginDeclarationScope.getInstance(context);
        assertNotNull("PluginDeclarationScope is null.", pds);

        // later calls return same object
        assertSame("Unexpected PluginDeclarationScope object", pds,
            PluginDeclarationScope.getInstance(context));

        // different context is different instance
        Context context2 = new Context(
            saxHandler, saxHandler.getLogger(),
            null, null);
        PluginDeclarationScope pds2 = PluginDeclarationScope.getInstance(context2);
        assertNotNull("PluginDeclarationScope is null.", pds2);
        assertTrue("Different contexts returned same scope", pds != pds2);
    }

    /**
     * Test the beginScope/endScope methods of this class.
     */
    public void testScoping1() throws Exception {
        Digester digester = new Digester();
        SAXHandler saxHandler = digester.getSAXHandler();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, null);

        // test beginScope after getInstance. The first getInstance call
        // will create an initial object, so beginScope will see a valid
        // parent, not a null parent.
        PluginDeclarationScope pds1 = PluginDeclarationScope.getInstance(context);
        assertNotNull("PluginDeclarationScope is null.", pds1);

        PluginDeclarationScope pds2 = PluginDeclarationScope.beginScope(context);
        assertNotNull(pds2);
        assertTrue(pds1!=pds2);

        assertSame(pds2, PluginDeclarationScope.getInstance(context));

        PluginDeclarationScope.endScope(context);
        assertSame(pds1, PluginDeclarationScope.getInstance(context));
    }

    /**
     * Test the beginScope/endScope methods of this class.
     */
    public void testScoping2() throws Exception {
        Digester digester = new Digester();
        SAXHandler saxHandler = digester.getSAXHandler();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, null);

        // test beginScope before getInstance
        // The beginScope will see a null parent...

        PluginDeclarationScope pds1 = PluginDeclarationScope.beginScope(context);
        assertNotNull("PluginDeclarationScope is null.", pds1);

        assertSame("Unexpected PluginDeclarationScope object", 
            pds1, PluginDeclarationScope.getInstance(context));

        PluginDeclarationScope pds2 = PluginDeclarationScope.beginScope(context);
        assertNotNull(pds2);
        assertTrue(pds1!=pds2);

        assertSame(pds2, PluginDeclarationScope.getInstance(context));

        PluginDeclarationScope.endScope(context);
        assertSame(pds1, PluginDeclarationScope.getInstance(context));

        PluginDeclarationScope.endScope(context);
        // head scope should again be null, but we cannot test that, as
        // calling getInstance would just create one :-)
    }

    /**
     * Test the ability to retrieve earlier declarations by specifying the
     * plugin class. This also happens to test most functionality of the
     * PluginDeclarationScope class.
     */
    public void testRetrievalByClass() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, saxHandler.getRuleManager());

        Properties emptyProps = new Properties();

        // -----------------------------------------------------------
        // declare a few plugins at "global" scope
        // -----------------------------------------------------------
        PluginDeclarationScope globalPds = PluginDeclarationScope.getInstance(context);

        Declaration decl1 = new Declaration(context, DummyPlugin1.class, emptyProps);
        globalPds.addDeclaration(decl1);

        Declaration decl2 = new Declaration(context, DummyPlugin2.class, emptyProps);
        globalPds.addDeclaration(decl2);

        // -----------------------------------------------------------
        // create a new scope, add a few declarations
        // -----------------------------------------------------------
        PluginDeclarationScope pds1 = PluginDeclarationScope.beginScope(context);
        assertTrue("New scope didn't create new object.", globalPds != pds1);
        assertSame("New scope is not default scope", 
            pds1, PluginDeclarationScope.getInstance(context));

        // hide declaration for class DummyPlugin1
        Declaration decl3 = new Declaration(context, DummyPlugin1.class, emptyProps);
        pds1.addDeclaration(decl3);

        Declaration decl4 = new Declaration(context, DummyPlugin3.class, emptyProps);
        pds1.addDeclaration(decl4);

        // -----------------------------------------------------------
        // create a new scope, add a few declarations
        // -----------------------------------------------------------
        PluginDeclarationScope pds2 = PluginDeclarationScope.beginScope(context);
        assertTrue("New scope didn't create new object.", globalPds != pds1);
        assertTrue("New scope didn't create new object.", pds1 != pds2);
        assertSame("New scope is not default scope", 
            pds2, PluginDeclarationScope.getInstance(context));

        // hide declaration for class DummyPlugin1
        Declaration decl5 = new Declaration(context, DummyPlugin1.class, emptyProps);
        pds2.addDeclaration(decl5);

        Declaration decl6 = new Declaration(context, DummyPlugin4.class, emptyProps);
        pds2.addDeclaration(decl6);

        // -----------------------------------------------------------
        // check expected objects are found
        // -----------------------------------------------------------

        Declaration decl; 
        PluginDeclarationScope pds = PluginDeclarationScope.getInstance(context);

        decl = pds.getDeclarationByClass("no.such.class");
        assertNull("Unknown class returned declaration", decl);

        decl = pds.getDeclarationByClass(DummyPlugin1.class.getName());
        assertSame("Unexpected declaration object found", decl, decl5);

        decl = pds.getDeclarationByClass(DummyPlugin2.class.getName());
        assertSame("Unexpected declaration object found", decl, decl2);

        decl = pds.getDeclarationByClass(DummyPlugin3.class.getName());
        assertSame("Unexpected declaration object found", decl, decl4);

        decl = pds.getDeclarationByClass(DummyPlugin4.class.getName());
        assertSame("Unexpected declaration object found", decl, decl6);
        
        // discard top scope, test again
        PluginDeclarationScope.endScope(context);
        pds = PluginDeclarationScope.getInstance(context);
        assertSame("Unexpected scope after endScope", pds1, pds); 

        decl = pds.getDeclarationByClass("no.such.class");
        assertNull("Unknown class returned declaration", decl);

        decl = pds.getDeclarationByClass(DummyPlugin1.class.getName());
        assertSame("Unexpected declaration object found", decl, decl3);

        // discard top scope, test again
        PluginDeclarationScope.endScope(context);
        pds = PluginDeclarationScope.getInstance(context);
        assertSame("Unexpected scope after endScope", globalPds, pds); 

        decl = pds.getDeclarationByClass("no.such.class");
        assertNull("Unknown class returned declaration", decl);

        decl = pds.getDeclarationByClass(DummyPlugin1.class.getName());
        assertSame("Unexpected declaration object found", decl, decl1);
    }

    /**
     * Test the ability to retrieve earlier declarations by specifying the
     * plugin id. This also happens to test most functionality of the
     * PluginDeclarationScope class.
     */
    public void testRetrievalById() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, saxHandler.getRuleManager());

        Properties emptyProps = new Properties();

        // -----------------------------------------------------------
        // declare a few plugins at "global" scope
        // -----------------------------------------------------------
        PluginDeclarationScope globalPds = PluginDeclarationScope.getInstance(context);

        Declaration decl1 = new Declaration(context, DummyPlugin1.class, emptyProps);
        decl1.setId("id1");
        globalPds.addDeclaration(decl1);

        Declaration decl2 = new Declaration(context, DummyPlugin2.class, emptyProps);
        decl2.setId("id2");
        globalPds.addDeclaration(decl2);

        // -----------------------------------------------------------
        // create a new scope, add a few declarations
        // -----------------------------------------------------------
        PluginDeclarationScope pds1 = PluginDeclarationScope.beginScope(context);
        assertTrue("New scope didn't create new object.", globalPds != pds1);
        assertSame("New scope is not default scope", 
            pds1, PluginDeclarationScope.getInstance(context));

        // hide declaration for class DummyPlugin1
        Declaration decl3 = new Declaration(context, DummyPlugin1.class, emptyProps);
        decl3.setId("id1");
        pds1.addDeclaration(decl3);

        Declaration decl4 = new Declaration(context, DummyPlugin3.class, emptyProps);
        decl4.setId("id3");
        pds1.addDeclaration(decl4);

        // -----------------------------------------------------------
        // create a new scope, add a few declarations
        // -----------------------------------------------------------
        PluginDeclarationScope pds2 = PluginDeclarationScope.beginScope(context);
        assertTrue("New scope didn't create new object.", globalPds != pds1);
        assertTrue("New scope didn't create new object.", pds1 != pds2);
        assertSame("New scope is not default scope", 
            pds2, PluginDeclarationScope.getInstance(context));

        // hide declaration for class DummyPlugin1
        Declaration decl5 = new Declaration(context, DummyPlugin1.class, emptyProps);
        decl5.setId("id1");
        pds2.addDeclaration(decl5);

        Declaration decl6 = new Declaration(context, DummyPlugin4.class, emptyProps);
        decl6.setId("id4");
        pds2.addDeclaration(decl6);

        // -----------------------------------------------------------
        // check expected objects are found
        // -----------------------------------------------------------

        Declaration decl; 
        PluginDeclarationScope pds = PluginDeclarationScope.getInstance(context);

        decl = pds.getDeclarationById("bad-id");
        assertNull("Unknown id returned declaration", decl);

        decl = pds.getDeclarationById("id1");
        assertSame("Unexpected declaration object found", decl, decl5);

        decl = pds.getDeclarationById("id2");
        assertSame("Unexpected declaration object found", decl, decl2);

        decl = pds.getDeclarationById("id3");
        assertSame("Unexpected declaration object found", decl, decl4);

        decl = pds.getDeclarationById("id4");
        assertSame("Unexpected declaration object found", decl, decl6);
        
        // discard top scope, test again
        PluginDeclarationScope.endScope(context);
        pds = PluginDeclarationScope.getInstance(context);
        assertSame("Unexpected scope after endScope", pds1, pds); 

        decl = pds.getDeclarationById("no-such-id");
        assertNull("Unknown class returned declaration", decl);

        decl = pds.getDeclarationById("id1");
        assertSame("Unexpected declaration object found", decl, decl3);

        // discard top scope, test again
        PluginDeclarationScope.endScope(context);
        pds = PluginDeclarationScope.getInstance(context);
        assertSame("Unexpected scope after endScope", globalPds, pds); 

        decl = pds.getDeclarationById("unknown");
        assertNull("Unknown class returned declaration", decl);

        decl = pds.getDeclarationById("id1");
        assertSame("Unexpected declaration object found", decl, decl1);

        decl = pds.getDeclarationById("id2");
        assertSame("Unexpected declaration object found", decl, decl2);
    }

    /**
     * Test what happens when the same class/id is declared more than once
     * in the same scope object. The result should simply be that later
     * declarations replace earlier ones.
     */
    public void testRedeclarations() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, saxHandler.getRuleManager());

        Properties emptyProps = new Properties();

        Declaration decl;
        PluginDeclarationScope pds = PluginDeclarationScope.getInstance(context);

        Declaration decl1 = new Declaration(context, DummyPlugin1.class, emptyProps);
        decl1.setId("id1");
        pds.addDeclaration(decl1);

        // retrieve declaration by class and id
        decl = pds.getDeclarationByClass(DummyPlugin1.class.getName());
        assertSame(decl1, decl);
        decl = pds.getDeclarationById("id1");
        assertSame(decl1, decl);
        
        // redeclare the same plugin
        Declaration decl2 = new Declaration(context, DummyPlugin1.class, emptyProps);
        decl2.setId("id1");
        pds.addDeclaration(decl2);

        // retrieve declaration by class and id
        decl = pds.getDeclarationByClass(DummyPlugin1.class.getName());
        assertSame(decl2, decl);
        decl = pds.getDeclarationById("id1");
        assertSame(decl2, decl);
        
        // redeclare the same class, but a different id
        Declaration decl3 = new Declaration(context, DummyPlugin1.class, emptyProps);
        decl3.setId("id2");
        pds.addDeclaration(decl3);

        // retrieve declaration by class and id
        decl = pds.getDeclarationByClass(DummyPlugin1.class.getName());
        assertSame(decl3, decl);
        decl = pds.getDeclarationById("id1");
        assertSame(decl2, decl);
        decl = pds.getDeclarationById("id2");
        assertSame(decl3, decl);

        // redeclare a different class but the same id
        Declaration decl4 = new Declaration(context, DummyPlugin2.class, emptyProps);
        decl4.setId("id2");
        pds.addDeclaration(decl4);

        // retrieve declaration by class and id
        decl = pds.getDeclarationByClass(DummyPlugin2.class.getName());
        assertSame(decl4, decl);
        decl = pds.getDeclarationById("id2");
        assertSame(decl4, decl);
        decl = pds.getDeclarationByClass(DummyPlugin1.class.getName());
        assertSame(decl3, decl);
    }
}
