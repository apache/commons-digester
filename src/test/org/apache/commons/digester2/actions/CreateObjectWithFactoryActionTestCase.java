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
import java.util.List;
import java.util.ArrayList;

import org.xml.sax.InputSource;

import org.apache.commons.logging.Log;
import org.apache.commons.digester2.Digester;
import org.apache.commons.digester2.Context;

/**
 * <p>Test Cases for the CreateObjectWithFactoryAction class.</p>
 */

public class CreateObjectWithFactoryActionTestCase extends TestCase {

    // ----------------------------------------------------------- 
    // Local classes
    // ----------------------------------------------------------- 

    public static class CustomRoot {
        public List list = new ArrayList();
        public void addInteger(Integer i) {
            list.add(i);
        }
    }

    public static class IntegerFactory extends AbstractObjectFactory {
        public Object createObject(
        Context context, 
        org.xml.sax.Attributes attrs) {
            // note that when retrieving attributes from a sax Attributes
            // object, an empty string should be passed to indicate the
            // null namespace. In a DOM node, you must use null. Yay.
            String value = attrs.getValue("", "value");
            return new Integer(value);
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
    public CreateObjectWithFactoryActionTestCase(String name) {
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
        return (new TestSuite(CreateObjectWithFactoryActionTestCase.class));
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
     * Test basic operations. Here we provide an already-instantiated
     * ObjectFactory to the CreateObjectWithFactoryAction.
     */
    public void testBasicOperations1() throws Exception {
        String inputText = 
            "<root>" + 
            " <int value='16'/>" +
            " <int value='32'/>" +
            " <int value='64'/>" +
            "</root>";

        InputSource source = new InputSource(new StringReader(inputText));

        CustomRoot root = new CustomRoot();
        IntegerFactory factory = new IntegerFactory();
        
        Digester d = new Digester();
        d.setInitialObject(root);
        d.addRule("/root/int", new CreateObjectWithFactoryAction(factory));
        d.addRule("/root/int", new LinkObjectsAction("addInteger"));

        d.parse(source);

        Integer i;        
        List list = root.list;
        assertEquals("3 objects created", 3, list.size());
        assertEquals("Count 16", 16, ((Integer) list.get(0)).intValue());
        assertEquals("Count 32", 32, ((Integer) list.get(1)).intValue());
        assertEquals("Count 64", 64, ((Integer) list.get(2)).intValue());
    }

    /**
     * Test basic operations. Here we pass the name of a factory to the
     * CreateObjectWithFactoryAction and allow the action to instantiate
     * the factory.
     */
    public void testBasicOperations2() throws Exception {
        String inputText = 
            "<root>" + 
            " <int value='16'/>" +
            " <int value='32'/>" +
            " <int value='64'/>" +
            "</root>";

        InputSource source = new InputSource(new StringReader(inputText));

        CustomRoot root = new CustomRoot();
        
        Digester d = new Digester();
        d.setInitialObject(root);
        d.addRule("/root/int", new CreateObjectWithFactoryAction(IntegerFactory.class));
        d.addRule("/root/int", new LinkObjectsAction("addInteger"));

        d.parse(source);

        Integer i;        
        List list = root.list;
        assertEquals("3 objects created", 3, list.size());
        assertEquals("Count 16", 16, ((Integer) list.get(0)).intValue());
        assertEquals("Count 32", 32, ((Integer) list.get(1)).intValue());
        assertEquals("Count 64", 64, ((Integer) list.get(2)).intValue());
    }
}
