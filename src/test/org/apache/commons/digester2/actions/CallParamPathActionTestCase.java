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


package org.apache.commons.digester2.actions;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.StringReader;

import org.xml.sax.InputSource;

import org.apache.commons.logging.Log;
import org.apache.commons.digester2.Digester;

/**
 * Test Cases for the CallParamPathAction class.
 */

public class CallParamPathActionTestCase extends TestCase {

    public static class TargetObject {
        private Object item;

        // allows us to tell the difference between the method not being
        // called, and being called with a null parameter.
        private int invocationCount = 0;
        
        public void addItem(Object item) {
            this.item = item;
            ++this.invocationCount;
        }
        public Object getItem() { return item; }
        public int getInvocationCount() { return invocationCount; }
    }
        
    // ----------------------------------------------------------- 
    // Constructors
    // ----------------------------------------------------------- 

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public CallParamPathActionTestCase(String name) {
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
        return (new TestSuite(CallParamPathActionTestCase.class));
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
     * Test basic operations.
     */
    public void testBasicOperations() throws Exception {
        String inputText = 
            "<root>" + 
            " <item/>" +
            "</root>";

        InputSource source = new InputSource(new StringReader(inputText));

        Digester d = new Digester();
        d.addRule("/root/item", new CallMethodAction("addItem", 1));
        d.addRule("/root/item", new CallParamPathAction(0));

        TargetObject targetObject = new TargetObject();
        d.setInitialObject(targetObject);
        d.parse(source);
        
        // created object was passed ok
        Object item = targetObject.getItem();
        assertNotNull("Item set", item);
        assertSame("Object is a string", String.class, item.getClass());
        assertEquals("Path is correct", "/root/item", item);
    }
}
