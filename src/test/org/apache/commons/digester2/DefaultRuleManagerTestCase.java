/* $Id$
 *
 * Copyright 2001-2005 The Apache Software Foundation.
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


package org.apache.commons.digester2;

import java.math.BigDecimal;
import java.net.URL;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.EmptyStackException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.InputSource;

import org.apache.commons.logging.Log;

/**
 * <p>Test Case for the DefaultRuleManager class.</p>
 */

public class DefaultRuleManagerTestCase extends TestCase {

    private static class DummyAction extends AbstractAction {
        private String name;

        public DummyAction(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
        
        public String toString() {
            return name;
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
    public DefaultRuleManagerTestCase(String name) {
        super(name);
    }

    // --------------------------------------------------
    // Overall Test Methods
    // --------------------------------------------------

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
    }

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(DefaultRuleManagerTestCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
    }

    // ------------------------------------------------
    // Individual Test Methods
    // ------------------------------------------------

    /**
     * Test the Constructor, plus the getters that access constructor params.
     */
    public void testConstructor() {
        DefaultRuleManager rm = new DefaultRuleManager();

        List actions = rm.getActions();
        assertTrue("Initial action list is empty", actions.isEmpty());
    }

    /**
     * Test the copy method.
     */
    public void testCopy() throws Exception {
        DefaultRuleManager drm1 = new DefaultRuleManager();

        drm1.addRule("/root", new DummyAction("one"));
        drm1.addRule("/root", new DummyAction("two"));

        RuleManager rmCopy = drm1.copy();
        List actions;

        assertNotNull("Copy works", rmCopy);
        assertEquals("Copy returns same class",
            DefaultRuleManager.class, rmCopy.getClass());

        DefaultRuleManager drm2 = (DefaultRuleManager) rmCopy;
        
        assertNotSame("Actions list is copied",
            drm1.getActions(), drm2.getActions());

        assertEquals("Actions lists are same size",
            drm1.getActions().size(), drm2.getActions().size());

        assertSame("Actions are not copied",
            drm1.getActions().get(0), drm2.getActions().get(0));

        assertSame("Actions are not copied",
            drm1.getActions().get(1), drm2.getActions().get(1));
    }

    /**
     * Test basic matching of paths to rules.
     */
    public void testBasicMatching() throws Exception {
        DefaultRuleManager rm = new DefaultRuleManager();
        
        Action actionA = new DummyAction("a");
        Action actionB1 = new DummyAction("b1");
        Action actionB2 = new DummyAction("b2");
        
        List list = null;

        rm.addRule("/a", actionA);
        rm.addRule("/a/b", actionB1);
        rm.addRule("/a/b", actionB2);

        // match nothing
        list = rm.getMatchingActions("/c");
        assertEquals("Action list is empty for no match", 0, list.size());
        
        // match one action
        list = rm.getMatchingActions("/a");
        assertEquals("One rule matched", 1, list.size());
        assertSame("A rule matched", actionA, list.get(0));

        // match multiple actions
        list = rm.getMatchingActions("/a/b");
        assertEquals("Multiple rules matched", 2, list.size());
        assertSame("B1 rule matched", actionB1, list.get(0));
        assertSame("B2 rule matched", actionB2, list.get(1));
    }
    
    public void testDefaultActions() throws Exception {
        DefaultRuleManager rm = new DefaultRuleManager();
        
        Action actionA = new DummyAction("a");
        Action actionB = new DummyAction("b");
        Action actionC = new DummyAction("c");
        
        List list = null;

        rm.addRule("/a", actionA);
        rm.addFallbackAction(actionB);
        rm.addFallbackAction(actionC);
        
        // verify that all actions are reported in getActions
        List allActions = rm.getActions();
        assertTrue("ActionA present", allActions.contains(actionA));
        assertTrue("ActionB present", allActions.contains(actionB));
        assertTrue("ActionC present", allActions.contains(actionC));

        // match one action
        list = rm.getMatchingActions("/a");
        assertEquals("One rule matched", 1, list.size());
        assertSame("A rule matched", actionA, list.get(0));

        // match nothing
        list = rm.getMatchingActions("/c");
        assertEquals("Action list contains fallbacks no match", 2, list.size());
        assertSame("B rule matched", actionB, list.get(0));
        assertSame("C rule matched", actionC, list.get(1));
    }
    
    /**
     * Test mandatory actions in absence of fallback actions
     */
    public void testMandatoryActions1() throws Exception {
        DefaultRuleManager rm = new DefaultRuleManager();
        
        Action actionA = new DummyAction("a");
        Action actionB = new DummyAction("b");
        Action actionC = new DummyAction("c");
        
        List list = null;

        rm.addRule("/a", actionA);
        rm.addMandatoryAction(actionB);
        rm.addMandatoryAction(actionC);
        
        // verify that all actions are reported in getActions
        List allActions = rm.getActions();
        assertTrue("ActionA present", allActions.contains(actionA));
        assertTrue("ActionB present", allActions.contains(actionB));
        assertTrue("ActionC present", allActions.contains(actionC));

        // match one action
        list = rm.getMatchingActions("/a");
        assertEquals("Three rules matched", 3, list.size());
        assertSame("A rule matched", actionA, list.get(0));
        assertSame("B rule matched", actionB, list.get(1));
        assertSame("C rule matched", actionC, list.get(2));

        // match nothing
        list = rm.getMatchingActions("/c");
        assertEquals("Action list contains mandatory when no match", 2, list.size());
        assertSame("B rule matched", actionB, list.get(0));
        assertSame("C rule matched", actionC, list.get(1));
    }
    
    /**
     * Test mandatory actions in presence of fallback actions
     */
    public void testMandatoryActions2() throws Exception {
        DefaultRuleManager rm = new DefaultRuleManager();
        
        Action actionA = new DummyAction("a");
        Action actionB = new DummyAction("b");
        Action actionC = new DummyAction("c");
        
        List list = null;

        rm.addRule("/a", actionA);
        rm.addFallbackAction(actionB);
        rm.addMandatoryAction(actionC);
        
        // verify that all actions are reported in getActions
        List allActions = rm.getActions();
        assertTrue("ActionA present", allActions.contains(actionA));
        assertTrue("ActionB present", allActions.contains(actionB));
        assertTrue("ActionC present", allActions.contains(actionC));

        // match one action
        list = rm.getMatchingActions("/a");
        assertEquals("Two rules matched", 2, list.size());
        assertSame("A rule matched", actionA, list.get(0));
        assertSame("C rule matched", actionC, list.get(1));

        // match nothing
        list = rm.getMatchingActions("/c");
        assertEquals("Two rules matched", 2, list.size());
        assertSame("B rule matched", actionB, list.get(0));
        assertSame("C rule matched", actionC, list.get(1));
    }
}
