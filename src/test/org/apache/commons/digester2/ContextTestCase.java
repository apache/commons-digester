/* $Id: $
 *
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * <p>Test Case for the Context class.</p>
 */

public class ContextTestCase extends TestCase {

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public ContextTestCase(String name) {
        super(name);
    }

    // -------------------------------------------------- Overall Test Methods

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
    }

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(ContextTestCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
    }

    // ------------------------------------------------ Individual Test Methods

    /**
     * Test the Constructor, plus the getters that access constructor params.
     */
    public void testConstructor() {
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log);
        
        assertNotNull("saxHandler log is not null", log);
        assertEquals("getSAXHandler", context.getSAXHandler(), saxHandler);
        assertEquals("getLogger", context.getLogger(), saxHandler.getLogger());
    }

    /**
     * Test the Path behaviour
     */
    public void testPathMethods() {
        // setup
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log);

        // initially, the context path is empty
        assertEquals("Initial matchPath empty", "", context.getMatchPath());
        
        context.pushMatchPath("", "e1");
        assertEquals("Matchpath of 1", "/e1", context.getMatchPath());
        
        context.pushMatchPath("ns1", "e2");
        assertEquals("Matchpath of 2", "/e1/{ns1}e2", context.getMatchPath());
        
        context.pushMatchPath("", "e3");
        assertEquals("Matchpath of 3", "/e1/{ns1}e2/e3", context.getMatchPath());
        
        context.popMatchPath();
        assertEquals("Matchpath of 2", "/e1/{ns1}e2", context.getMatchPath());
        
        context.popMatchPath();
        assertEquals("Matchpath of 1", "/e1", context.getMatchPath());
        
        context.popMatchPath();
        assertEquals("Matchpath of 0", "", context.getMatchPath());
    }

    /**
     * Test storage of the 'root' variable
     */
    public void testRoot1() {
        // setRoot, getRoot
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log);

        Object root;

        // initially, getRoot returns null
        root = context.getRoot();
        assertNull("Initial root object is null", root);
        
        // after setRoot, returns the object set
        context.setRoot("root1");
        root = context.getRoot();
        assertEquals("setRoot/getRoot retrieves set object", "root1", root);
        
        // can set multiple times, always returns the last object set
        // also, stack depth should be max of 1
        context.setRoot("root2");
        context.setRoot("root3");
        context.setRoot("root4");
        root = context.getRoot();
        assertEquals("setRoot multiple times", "root4", root);
        assertEquals(context.getStackSize(), 1);
    }
    
    /**
     * Test storage of the 'root' variable
     */
    public void testRoot2() {
        // setRoot, getRoot
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log);

        Object root;

        // initially, getRoot returns null
        root = context.getRoot();
        assertNull("Initial root object is null", root);
        
        // after pushing an object, root is set
        context.push("item1");
        root = context.getRoot();
        assertEquals("push sets root", "item1", root);
        
        // after pushing other objects, root does not change
        context.push("item2");
        context.push("item3");
        context.push("item4");
        root = context.getRoot();
        assertEquals("push sets root", "item1", root);
        assertEquals("push increases stackdepth", 4, context.getStackSize());
        
        // after popping all objects off stack, root remains set
        context.pop();
        context.pop();
        context.pop();
        context.pop();
        root = context.getRoot();
        assertEquals("root remains set after pop", "item1", root);
        assertEquals("root remains set after pop", 0, context.getStackSize());
    }
    
    /**
     * Test the classloader behaviour
     */
    public void testClassLoader() {
        // getClassLoader
    }
    
    /**
     * Test the Actions stack
     */
    public void testActionStackMethods() {
        // setup
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log);
        
        // pushMatchingActions, popMatchingActions, peekMatchingActions
    }

    /**
     * Test the basic object stack mechanisms.
     */
    public void testObjectStackMethods() {
        // setup
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log);
        
        Object value = null;

        // Object stack of new Context must be empty
        assertTrue("New stack is empty", context.isEmpty());
        assertEquals("New stack is empty", 0, context.getStackSize());
        
        // peek on empty stack fails
        try {
            value = context.peek();
            fail("Stack exception on peek of empty stack");
        } catch(EmptyStackException ex) {
            // ok
        }
        
        // peek(0) on empty stack fails
        try {
            value = context.peek(0);
            fail("Stack exception on peek(0) of empty stack");
        } catch(EmptyStackException ex) {
            // ok
        }
        
        // peek(1) on empty stack fails
        try {
            value = context.peek(1);
            fail("Stack exception on peek(1) of empty stack");
        } catch(EmptyStackException ex) {
            // ok
        }
        
        // pop on empty stack fails
        try {
            value = context.pop();
            fail("Stack exception on pop of empty stack");
        } catch(EmptyStackException ex) {
            // ok
        }
        
        // push, pop and peek
        context.push("First Item");
        assertEquals("Push one item -> size = 1", 1, context.getStackSize());
        assertTrue("Stack of size 1 -> nonempty", !context.isEmpty());
        value = context.peek();
        assertNotNull("Peeked first item is not null", value);
        assertEquals("Peeked first item value", "First Item", (String) value);

        context.push("Second Item");
        assertEquals("Pushed two items size", 2, context.getStackSize());
        assertTrue("Stack of size 2 -> nonempty", !context.isEmpty());
        value = context.peek();
        assertNotNull("Peeked second item is not null", value);
        assertEquals("Peeked second item value", "Second Item", (String) value);
        value = context.peek(0);
        assertNotNull("Peeked second item is not null", value);
        assertEquals("Peeked second item value", "Second Item", (String) value);
        value = context.peek(1);
        assertNotNull("Peeked first item is not null", value);
        assertEquals("Peeked first item value", "First Item", (String) value);

        value = context.pop();
        assertEquals("Popped stack size", 1, context.getStackSize());
        assertTrue("Stack of size 1 -> nonempty", !context.isEmpty());
        assertNotNull("Popped second item is not null", value);
        assertEquals("Popped second item value", "Second Item", (String) value);
        value = context.peek();
        assertNotNull("Remaining item is not null", value);
        assertEquals("Remaining item value", "First Item", (String) value);

        value = context.pop();
        assertEquals("Popped stack size", 0, context.getStackSize());
        assertTrue("Stack of size 0 -> empty", context.isEmpty());
        assertNotNull("Popped first item is not null", value);
        assertEquals("Popped first item value", "First Item", (String) value);
    }


    /**
     * Test the basic named stack mechanisms.
     */
    public void testNamedStackMethods() {
        // peek, peek(n), pop, push, isEmpty
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log);
        
        Object value = null;
        String stack1 = "stack1";
        String stack2 = "stack2";

        // Unused named stacks must be empty
        assertTrue("New stack is empty", context.isEmpty(stack1));
        
        // peek on empty stack fails
        try {
            value = context.peek(stack1);
            fail("Stack exception on peek of empty stack");
        } catch(EmptyStackException ex) {
            // ok
        }

        // pop on empty stack fails
        try {
            value = context.pop(stack1);
            fail("Stack exception on pop of empty stack");
        } catch(EmptyStackException ex) {
            // ok
        }
        
        // push and pop
        context.push(stack1, "First Item");
        assertEquals("Push one item -> size = 1", 1, context.getStackSize(stack1));
        assertTrue("Stack of size 1 -> nonempty", !context.isEmpty(stack1));
        value = context.peek(stack1);
        assertNotNull("Peeked first item is not null", value);
        assertEquals("Peeked first item value", "First Item", (String) value);

        context.push(stack1, "Second Item");
        assertEquals("Pushed two items size", 2, context.getStackSize(stack1));
        assertTrue("Stack of size 2 -> nonempty", !context.isEmpty(stack1));
        value = context.peek(stack1);
        assertNotNull("Peeked second item is not null", value);
        assertEquals("Peeked second item value", "Second Item", (String) value);

        value = context.pop(stack1);
        assertEquals("Popped stack size", 1, context.getStackSize(stack1));
        assertTrue("Stack of size 1 -> nonempty", !context.isEmpty(stack1));
        assertNotNull("Popped second item is not null", value);
        assertEquals("Popped second item value", "Second Item", (String) value);
        value = context.peek(stack1);
        assertNotNull("Remaining item is not null", value);
        assertEquals("Remaining item value", "First Item", (String) value);

        value = context.pop(stack1);
        assertEquals("Popped stack size", 0, context.getStackSize(stack1));
        assertTrue("Stack of size 0 -> empty", context.isEmpty(stack1));
        assertNotNull("Popped first item is not null", value);
        assertEquals("Popped first item value", "First Item", (String) value);
    }

    
    /** Tests the push-peek-pop cycle for a named stack */
    public void testNamedStackPushPeekPop() throws Exception
    {
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log);
        
        BigDecimal archimedesAveragePi = new BigDecimal("3.1418");
        String testStackName = "org.apache.commons.digester.tests.testNamedStackPushPeekPop";
        assertTrue("Stack starts empty:", context.isEmpty(testStackName));
        context.push(testStackName, archimedesAveragePi);
        assertEquals("Peeked value:", archimedesAveragePi, context.peek(testStackName));
        assertEquals("Popped value:", archimedesAveragePi, context.pop(testStackName));
        assertTrue("Stack ends empty:", context.isEmpty(testStackName));
    }
    
    /** Tests that values are stored independently */
    public void testNamedIndependence()
    {
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log);
        
        String testStackOneName = "org.apache.commons.digester.tests.testNamedIndependenceOne";
        String testStackTwoName = "org.apache.commons.digester.tests.testNamedIndependenceTwo";
        context.push(testStackOneName, "Tweedledum");
        context.push(testStackTwoName, "Tweedledee");
        assertEquals("Popped value one:", "Tweedledum", context.pop(testStackOneName));
        assertEquals("Popped value two:", "Tweedledee", context.pop(testStackTwoName));
    }
    
    /** Tests popping named stack not yet pushed */
    public void testPopNamedStackNotPushed() 
    {
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log);
        
        String testStackName = "org.apache.commons.digester.tests.testPopNamedStackNotPushed";
        try {
            context.pop(testStackName);
            fail("Expected an EmptyStackException");
        } catch (EmptyStackException e) {
            // expected
        }
        
        try {
            context.peek(testStackName);
            fail("Expected an EmptyStackException");
        } catch (EmptyStackException e) {
            // expected
        }
    }
    
    /** Tests for isEmpty */
    public void testNamedStackIsEmpty()
    {
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log);
        
        String testStackName = "org.apache.commons.digester.tests.testNamedStackIsEmpty";
        assertTrue(
            "A named stack that has no object pushed onto it yet should be empty", 
            context.isEmpty(testStackName));
            
        context.push(testStackName, "Some test value");
        assertFalse(
            "A named stack that has an object pushed onto it should be not empty",
            context.isEmpty(testStackName));
            
        context.peek(testStackName);
        assertFalse(
            "Peek should not effect whether the stack is empty",
            context.isEmpty(testStackName));
        
        context.pop(testStackName);
        assertTrue(
            "A named stack that has it's last object popped is empty", 
            context.isEmpty(testStackName));
    }

    /**
     * Test the getRoot method.
     */
    public void testGetRoot() throws Exception {
    }
}
