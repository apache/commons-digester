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

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.EmptyStackException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xml.sax.InputSource;

import org.apache.commons.logging.Log;

/**
 * <p>Test Case for the Context class.</p>
 */

public class ContextTestCase extends TestCase {

    private static class DummyAction extends AbstractAction {
    }
    
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

    // ------------------------------------------------ 
    // Individual Test Methods
    // ------------------------------------------------ 

    /**
     * Test the Constructor, plus the getters that access constructor params.
     */
    public void testConstructor() {
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log, null);
        
        assertNotNull("saxHandler log is not null", log);
        assertSame("getSAXHandler", context.getSAXHandler(), saxHandler);
        assertSame("getLogger", context.getLogger(), saxHandler.getLogger());
    }

    /**
     * Test the Path behaviour
     */
    public void testPathMethods() {
        // setup
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log, null);

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
        Context context = new Context(saxHandler, log, null);

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
        assertEquals("SetRoot multiple times does not increase stack depth",
            context.getStackSize(), 1);
    }
    
    /**
     * Test storage of the 'root' variable
     */
    public void testRoot2() {
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log, null);

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
     * Test the classloader behaviour.
     */
    public void testClassLoader() {
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log, null);

        ClassLoader cl = context.getClassLoader();
        assertSame("get classloader", cl, saxHandler.getClassLoader());
    }
    
    /**
     * Test the Actions stack
     */
    public void testActionStackMethods() {
        // setup
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log, null);
        
        Action action0 = new DummyAction();
        Action action1 = new DummyAction();
        Action action2 = new DummyAction();
        
        ArrayList list0 = new ArrayList(2);
        list0.add(action0);
        list0.add(action1);
        
        context.pushMatchingActions(list0);
        List list = context.peekMatchingActions();
        assertSame("Push/peek matching actions", list, list0);
        assertEquals("Push/peek matching actions", 2, list.size());
        assertSame("Push/peek matching actions", action0, list.get(0));
        assertSame("Push/peek matching actions", action1, list.get(1));
        
        ArrayList list1 = new ArrayList();
        list1.add(action2);
        context.pushMatchingActions(list1);
        list = context.peekMatchingActions();
        assertSame("Push/peek matching actions 2", list, list1);
        assertEquals("Push/peek matching actions 2", 1, list.size());
        assertSame("Push/peek matching actions 2", action2, list.get(0));
        
        list = context.popMatchingActions();
        assertSame("Push/peek matching actions", list, list1);
        list = context.popMatchingActions();
        assertSame("Push/peek matching actions", list, list0);
        
        try {
            list = context.popMatchingActions();
            fail("Popping an empty stack");
        } catch(EmptyStackException ex) {
            // ok
        }
    }

    /**
     * Test the basic object stack mechanisms.
     */
    public void testObjectStackMethods() {
        // setup
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log, null);
        
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
     * Test the basic scratch stack mechanisms.
     */
    public void testScratchStackMethods() {
        // peek, peek(n), pop, push, isEmpty
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log, null);
        
        Object value = null;
        Context.StackId stack1 = new Context.StackId(ContextTestCase.class, "stack1", this);
        Context.StackId stack2 = new Context.StackId(ContextTestCase.class, "stack2", this);

        // Unused scratch stacks must be empty
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

    
    /** Tests the push-peek-pop cycle for a scratch stack */
    public void testScratchStackPushPeekPop() throws Exception
    {
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log, null);
        
        Object o1 = new Object();

        Context.StackId testStack = new Context.StackId(ContextTestCase.class, "stack", this);
        assertTrue("Stack starts empty:", context.isEmpty(testStack));
        context.push(testStack, o1);
        
        assertSame("Peeked value:", o1, context.peek(testStack));
        assertSame("Popped value:", o1, context.pop(testStack));
        assertTrue("Stack ends empty:", context.isEmpty(testStack));
    }
    
    /** Tests that values are stored independently */
    public void testScratchStackIndependence()
    {
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log, null);
        
        Context.StackId stack1 = new Context.StackId(ContextTestCase.class, "stack1", this);
        Context.StackId stack2 = new Context.StackId(ContextTestCase.class, "stack2", this);

        context.push(stack1, "Tweedledum");
        context.push(stack2, "Tweedledee");
        assertEquals("Popped value one:", "Tweedledum", context.pop(stack1));
        assertEquals("Popped value two:", "Tweedledee", context.pop(stack2));
    }
    
    /** Tests popping scratch stack not yet pushed */
    public void testPopScratchStackNotPushed() 
    {
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log, null);
        
        Context.StackId testStack = new Context.StackId(ContextTestCase.class, "stack", this);

        try {
            context.pop(testStack);
            fail("Expected an EmptyStackException");
        } catch (EmptyStackException e) {
            // expected
        }
        
        try {
            context.peek(testStack);
            fail("Expected an EmptyStackException");
        } catch (EmptyStackException e) {
            // expected
        }
    }
    
    /** Tests for isEmpty */
    public void testScratchStackIsEmpty()
    {
        SAXHandler saxHandler = new SAXHandler();
        Log log = saxHandler.getLogger();
        Context context = new Context(saxHandler, log, null);
        
        Context.StackId testStack = new Context.StackId(ContextTestCase.class, "stack", this);

        assertTrue(
            "A scratch stack that has no object pushed onto it yet should be empty", 
            context.isEmpty(testStack));
            
        context.push(testStack, "Some test value");
        assertFalse(
            "A scratch stack that has an object pushed onto it should be not empty",
            context.isEmpty(testStack));
            
        context.peek(testStack);
        assertFalse(
            "Peek should not effect whether the stack is empty",
            context.isEmpty(testStack));
        
        context.pop(testStack);
        assertTrue(
            "A scratch stack that has its last object popped is empty", 
            context.isEmpty(testStack));
    }
}
