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
import java.util.HashMap;

import org.xml.sax.InputSource;

import org.apache.commons.logging.Log;
import org.apache.commons.digester2.Digester;

/**
 * <p>Test Cases for the SetPropertiesActionTestCase class.</p>
 */

public class SetPropertiesActionTestCase extends TestCase {
    
    // TODO: file bug on BeanUtils re unable to call BeanUtils.populate
    // on a class declared within a function.
    public static class TestObject {
        private String name;
        private int i;
        private float f;
        private String firstCode;
        private String secondCode;
        
        public void setName(String name) { this.name = name; }
        public void setInt(int i) { this.i = i; }
        public void setFloat(float f) { this.f = f; }
        public void setFirstCode(String s) { firstCode = s; }
        public void setSecondCode(String s) { secondCode = s; }
        
        public String getName() { return name; }
        public int getInt() { return i; }
        public float getFloat() { return f; }
        public String getFirstCode() { return firstCode; }
        public String getSecondCode() { return secondCode; }
    }
        
    // ----------------------------------------------------------- 
    // Constructors
    // ----------------------------------------------------------- 

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public SetPropertiesActionTestCase(String name) {
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
        return (new TestSuite(SetPropertiesActionTestCase.class));
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
            "<root" + 
            " name='a'" +             // plain string->string mapping
            " int='1'" +              // string->int mapping
            " firstCode='first'" +    // camelCase attribute
            " second-code='second'" + // auto-hyphenation-conversion
            " noProperty='xxx'" +     // attribute with no equivalent property
            "/>";

        InputSource source = new InputSource(new StringReader(inputText));

        Digester d = new Digester();
        TestObject testObject = new TestObject();
        
        SetPropertiesAction action = new SetPropertiesAction();
        d.addRule("/root", action);
        d.setInitialObject(testObject);
        d.parse(source);
        
        // string was passed ok
        assertEquals("name property", "a", testObject.getName());

        // string was auto-converted to int
        assertEquals("int property", 1, testObject.getInt());
        
        // mixed-case attribute got passed ok
        assertEquals("firstCode property", "first", testObject.getFirstCode());
        
        // hyphenated attribute got mapped to camelCase property
        assertEquals("secondCode property", "second", testObject.getSecondCode());
    }

    /**
     * Helper for testMappedOperations. See comments on that method.
     */
    private void doMappingTest(SetPropertiesAction action) throws Exception {
        String inputText = 
            "<root" + 
            " alt-name='a'" +         // plain string->string mapping
            " altInt='1'" +           // string->int mapping
            " firstCode='first'" +    // camelCase attribute
            "/>";

        InputSource source = new InputSource(new StringReader(inputText));

        Digester d = new Digester();
        TestObject testObject = new TestObject();
        
        d.addRule("/root", action);
        d.setInitialObject(testObject);
        d.parse(source);
        
        // alt-name got mapped to setName()
        assertEquals("name property", "a", testObject.getName());

        // altInt got mapped to setInt()
        assertEquals("id property", 1, testObject.getInt());
        
        // firstCode was not in map, so defaults applied
        assertEquals("firstCode property", "first", testObject.getFirstCode());
    }
    
    /**
     * Test custom mappings of attributes -> properties using the
     * constructors that takes a Map, strings, string-arrays, and also
     * the addAlias method.
     */
    public void testMappedOperations1() throws Exception {
        // part1: use a Map to specify the custom mappings
        HashMap map = new HashMap(1);
        map.put("alt-name", "name");
        map.put("altInt", "int");
        SetPropertiesAction action1 = new SetPropertiesAction(map);
        doMappingTest(action1);

        // part2: use a string array to specify the custom mappings
        SetPropertiesAction action2 = new SetPropertiesAction(
            new String[] {"alt-name", "altInt"},
            new String[] {"name", "int"});
        doMappingTest(action2);
        
        // part3: use addAlias
        SetPropertiesAction action3 = new SetPropertiesAction();
        action3.addAlias("alt-name", "name");
        action3.addAlias("altInt", "int");
        doMappingTest(action3);
        
        // part4: use constructor + addAlias
        SetPropertiesAction action4 = new SetPropertiesAction("alt-name", "name");
        action4.addAlias("altInt", "int");
        doMappingTest(action4);
    }

    public void testIgnoreAttribute() throws Exception {
        // tests custom mappings to ignore xml attributes
    }

    public void testIgnoreNamespacedAttributes() throws Exception {
        // verify that namespaced attributes are ignored.
    }

    public void testHyphenationConversion() throws Exception {
    }
}
