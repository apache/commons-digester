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


package org.apache.commons.digester2.actions;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.StringReader;

import org.xml.sax.InputSource;

import org.apache.commons.logging.Log;
import org.apache.commons.digester2.Digester;

/**
 * <p>Test Cases for the CreateObjectAction class.</p>
 */

public class CreateObjectActionTestCase extends TestCase {

    public static class Item {
    }
    
    public static class TestObject {
        private Item item;
        
        public void addItem(Item item) { this.item = item; }
        public Item getItem() { return item; }
    }
        
    // ----------------------------------------------------------- 
    // Constructors
    // ----------------------------------------------------------- 

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public CreateObjectActionTestCase(String name) {
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
        return (new TestSuite(CreateObjectActionTestCase.class));
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
        d.addRule("/root/item", new CreateObjectAction(Item.class));
        d.addRule("/root/item", new LinkObjectsAction("addItem"));

        TestObject testObject = new TestObject();
        d.setInitialObject(testObject);
        d.parse(source);
        
        // string was passed ok
        Item item = testObject.getItem();
        assertNotNull("CreateObjectAction works", item);
    }
}
