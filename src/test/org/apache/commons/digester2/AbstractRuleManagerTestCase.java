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
 * Test Case for the AbstractRuleManager class.
 * <p>
 * Mostly, this involves checking that the methods are invoked at
 * the times that they are expected to be invoked.
 */

public class AbstractRuleManagerTestCase extends TestCase {

    private static class MyRuleManager extends AbstractRuleManager {
        private List operations;
        
        public MyRuleManager(List operations) {
            this.operations = operations;
        }
        
        public RuleManager copy() {
            operations.add("copy");
            return null; // there's no point testing abstract copy!
        }
        
        public void startParse(Context context) throws DigestionException {
            operations.add("startParse");
        }
        
        public void finishParse(Context context) throws DigestionException {
            operations.add("finishParse");
        }
        
        public void addNamespace(String prefix, String uri) {
            operations.add("addNamespace");
        }
        
        public void addRule(String pattern, Action action) {
            operations.add("addRule");
        }

        public List getMatchingActions(String path) {
            operations.add("getMatchingActions");
            return java.util.Collections.EMPTY_LIST;
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
    public AbstractRuleManagerTestCase(String name) {
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
        return (new TestSuite(AbstractRuleManagerTestCase.class));
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
     * Test everything.
     */
    public void testEverything() throws Exception {
        String inputText = "<root/>";

        ArrayList opsList = new ArrayList(10);
        MyRuleManager rm = new MyRuleManager(opsList);

        rm.addRule(
            "/root", 
            new org.apache.commons.digester2.actions.SetPropertiesAction());

        Digester d = new Digester();
        d.setRuleManager(rm);
        InputSource source = new InputSource(new StringReader(inputText));
        d.parse(source);

        assertEquals("Expected number of operations", 4, opsList.size());
        assertEquals("First op: addRule", "addRule", opsList.get(0)); 
        assertEquals("Second op: startParse", "startParse", opsList.get(1));
        assertEquals("Third op: getMatchingActions", 
            "getMatchingActions", opsList.get(2));
        assertEquals("Fourth op: finishParse", 
            "finishParse", opsList.get(3));
    }
}
