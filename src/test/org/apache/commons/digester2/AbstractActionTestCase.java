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
 * Test Case for the AbstractAction class.
 * <p>
 * Mostly, this involves checking that the methods are invoked at
 * the times that they are expected to be invoked.
 */

public class AbstractActionTestCase extends TestCase {

    private static class MyAction extends AbstractAction {
        private List operations;

        public MyAction(List operations) {
            this.operations = operations;
        }

        public void startParse(Context context) {
            operations.add("startParse");
        }

        public void finishParse(Context context) {
            operations.add("finishParse");
        }

        public void begin(
        Context context,
        String namespace, String name,
        Attributes attributes) {
            operations.add(
                "begin"
                + ": namespace='" + namespace + "'"
                + ": name='" + name + "'");
        }

        public void bodySegment(
        Context context,
        String namespace, String name,
        String text) {
            operations.add(
                "bodySegment"
                + ": namespace='" + namespace + "'"
                + ": name='" + name + "'"
                + ": text='" + text + "'");
        }

        public void body(
        Context context,
        String namespace, String name,
        String text) {
            operations.add(
                "body"
                + ": namespace='" + namespace + "'"
                + ": name='" + name + "'"
                + ": text='" + text + "'");
        }

        public void end(Context context, String namespace, String name) {
            operations.add(
                "end"
                + ": namespace='" + namespace + "'"
                + ": name='" + name + "'");
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
    public AbstractActionTestCase(String name) {
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
        return (new TestSuite(AbstractActionTestCase.class));
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
        String inputText =
              "<root>"
            + " <item>"
            + "  text1"
            + "  text2"
            + "  <child/>"
            + "  text3"
            + "  <child/>"
            + " </item>"
            + "</root>";

        ArrayList opsList = new ArrayList(10);
        MyAction action = new MyAction(opsList);

        Digester d = new Digester();
        d.addRule("root/item", action);
        InputSource source = new InputSource(new StringReader(inputText));
        d.parse(source);

        String[] operations = {
            "startParse",
            "begin: namespace='': name='item'",
            "bodySegment: namespace='': name='item': text='  text1  text2  '",
            "bodySegment: namespace='': name='item': text='  text3  '",
            "bodySegment: namespace='': name='item': text=' '",
            "body: namespace='': name='item': text='  text1  text2    text3   '",
            "end: namespace='': name='item'",
            "finishParse",
        };

        int nOperations = opsList.size();
        for(int i=0; i<nOperations && i<operations.length; ++i) {
            String expected = operations[i];
            String actual = (String) opsList.get(i);
            assertEquals("Unexpected operation #" + i, expected, actual);
        }
        assertEquals("Expected number of operations", operations.length, nOperations);
    }
}
