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
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.NodeList;
        

import org.apache.commons.logging.Log;
import org.apache.commons.digester2.Digester;

/**
 * <p>Test Cases for the CreateNodeActionTestCase class.</p>
 */

public class CreateNodeActionTestCase extends TestCase {
    
    // ----------------------------------------------------------- 
    // Constructors
    // ----------------------------------------------------------- 

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public CreateNodeActionTestCase(String name) {
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
        return (new TestSuite(CreateNodeActionTestCase.class));
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
     * Utility method useful when tests aren't working and you need
     * to figure out why. 
     */
    private void dumpNode(String indent, org.w3c.dom.Node node) {
        
        System.out.print(indent);
        System.out.println("node [" + node.getNodeName() + "] = [" + node.getNodeValue() + "]");
        org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
        if (attrs != null) {
            int nAttrs = attrs.getLength();
            for(int i=0; i<nAttrs; ++i) {
                org.w3c.dom.Node attr = attrs.item(i);
                System.out.print(indent);
                System.out.println(
                  "{" + attr.getNamespaceURI() + "}" 
                  + attr.getNodeName() + "=" + attr.getNodeValue());
            }
        }
        node = node.getFirstChild();
        while (node != null) {
            dumpNode(indent + "  ", node);
            node = node.getNextSibling();
        }
    }
        

    /**
     * Test basic operations.
     */
    public void testBasicOperations() throws Exception {
        String inputText = 
            "<root>" +
            "  <node attr1='1' attr2='2'>" + 
            "    <child attr='c1'/>" + 
            "    <child>bodytext</child>" +
            "  </node>" +
            "</root>";

        InputSource source = new InputSource(new StringReader(inputText));

        Digester d = new Digester();
        
        CreateNodeAction action = new CreateNodeAction();
        d.addRule("/root/node", action);
        d.parse(source);

        Element element = null;
        try {
            element = (Element) d.getRoot();
        } catch(ClassCastException ex) {
            fail("Root element is not an Element.");
        }
        
        assertNotNull("Root object not null", element);
        assertEquals("Unexpected element name ", "node", element.getTagName());

        // check root element attributes        
        assertTrue("Root node has attributes", element.hasAttributes());
        assertEquals("Root node has two attributes", 
            2, element.getAttributes().getLength());
        String attr1Value = element.getAttributeNS(null, "attr1");
        assertEquals("Attribute attr1 has incorrect value", "1", attr1Value);
        String attr2Value = element.getAttributeNS(null, "attr2");
        assertEquals("Attribute attr2 has incorrect value", "2", attr2Value);
        
        // check root element has child elements
        NodeList childElements = element.getElementsByTagNameNS(null, "child");
        assertEquals("Two child elements expected", 2, childElements.getLength());

        // the zeroth child should have a single attribute
        Element child0 = (Element) childElements.item(0);
        assertEquals("Child0 has one attributes", 
            1, child0.getAttributes().getLength());
        String attrValue = child0.getAttributeNS(null, "attr");
        assertEquals("Child0 attribute has incorrect value", "c1", attrValue);
        assertNull("Child0 not expected to have children", child0.getFirstChild());
        
        // the 1th child should have no attributes and some body text
        Element child1 = (Element) childElements.item(1);
        assertEquals("Child1 has unexpected attributes",
            0, child1.getAttributes().getLength());
        Text text = (Text) child1.getFirstChild();
        assertNotNull("Child1 text missing", text);
        assertEquals("Child1 text incorrect", "bodytext", text.getNodeValue());
    }
}
