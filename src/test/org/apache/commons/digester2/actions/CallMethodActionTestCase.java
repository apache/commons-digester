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
import java.util.List;
import java.util.ArrayList;

import org.xml.sax.InputSource;

import org.apache.commons.logging.Log;
import org.apache.commons.digester2.Digester;

/**
 * Test Cases for the CallMethodAction class.
 */

public class CallMethodActionTestCase extends TestCase {

    public static class Target {
        private List operations;

        public Target(List operations) {
            this.operations = operations;
        }
        
        public void noParamMethod() {
            operations.add("noParamMethod called");
        }
        
        public void oneStringMethod(String s) {
            operations.add("oneStringMethod:" + s);
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
    public CallMethodActionTestCase(String name) {
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
        return (new TestSuite(CallMethodActionTestCase.class));
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
     * Test invoking a method with no parameters.
     */
    public void testNoParams() throws Exception {
        String inputText = 
            "<root>" + 
            " <item/>" +
            "</root>";

        InputSource source = new InputSource(new StringReader(inputText));

        ArrayList operations = new ArrayList();
        Target target = new Target(operations);
        
        Digester d = new Digester();
        d.addRule("/root/item", new CallMethodAction("noParamMethod", 0));

        d.setInitialObject(target);
        d.parse(source);
        
        assertEquals("Correct number of operations invoked", 1, operations.size());
        assertEquals("noParamMethod called", "noParamMethod called", operations.get(0));
    }

    /**
     * Test invoking a method with one string parameter.
     */
    public void testOneString() throws Exception {
        String inputText = 
            "<root>" + 
            " <item/>" +
            "</root>";

        InputSource source = new InputSource(new StringReader(inputText));

        ArrayList operations = new ArrayList();
        Target target = new Target(operations);
        
        Digester d = new Digester();
        d.addRule("/root/item", new CallMethodAction("oneStringMethod", 1));
        d.addRule("/root/item", new CallParamLiteralAction(0, "param0"));

        d.setInitialObject(target);
        d.parse(source);
        
        assertEquals("Correct number of operations invoked", 1, operations.size());
        assertEquals("oneStringMethod called", "oneStringMethod:param0", operations.get(0));
    }
}
