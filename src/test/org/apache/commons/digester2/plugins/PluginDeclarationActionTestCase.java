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


package org.apache.commons.digester2.plugins;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import java.io.StringReader;
import org.xml.sax.InputSource;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.digester2.*;
import org.apache.commons.digester2.actions.*;
import org.apache.commons.digester2.plugins.*;

/**
 * Test cases for basic PluginDeclarationAction behaviour.
 */

public class PluginDeclarationActionTestCase extends TestCase {
    // -----------------------------------------------------------
    // Helper classes
    // -----------------------------------------------------------

    public interface Widget {}

    public static class TextLabel implements Widget {
        private String label;
        public void setLabel(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public static class Container implements Widget {
        private LinkedList children = new LinkedList();

        public Container() {}

        public void addChild(Widget child) {
            children.add(child);
        }

        public List getChildren() {
            return children;
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
    public PluginDeclarationActionTestCase(String name) {
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
        return (new TestSuite(PluginDeclarationActionTestCase.class));
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
     * Test that rules can be declared via a PluginDeclarationAction.
     * Also tests that plugged-in classes with no custom rules defined
     * automatically get a SetPropertiesAction associated with them.
     */
    public void testPredeclaration() throws Exception {
        String inputText =
            "<root>" +
            " <plugin id='textlabel' class='" + TextLabel.class.getName() + "'/>" +
            " <widget plugin-id='textlabel' label='label1'/>" +
            " <widget plugin-id='textlabel' label='label2'/>" +
            "</root>";

        Digester digester = new Digester();

        PluginDeclarationAction pdr = new PluginDeclarationAction();
        digester.addRule("/root/plugin", pdr);

        PluginCreateAction pca = new PluginCreateAction(Widget.class);
        digester.addRule("/root/widget", pca);
        digester.addRule("/root/widget", new LinkObjectsAction("addChild"));

        Container root = new Container();
        digester.setInitialObject(root);

        InputSource source = new InputSource(new StringReader(inputText));
        digester.parse(source);

        // now check results

        Object child;
        List children = root.getChildren();
        assertTrue(children != null);
        assertEquals(2, children.size());

        child = children.get(0);
        assertTrue(child != null);
        assertEquals(TextLabel.class, child.getClass());
        assertEquals("label1", ((TextLabel)child).getLabel());

        child = children.get(1);
        assertTrue(child != null);
        assertEquals(TextLabel.class, child.getClass());
        assertEquals("label2", ((TextLabel)child).getLabel());
    }
}
