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

package org.apache.commons.digester2;

import java.io.StringReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

/**
 * Test Case for the SimpleMatchUtils class.
 */

public class SimpleMatchUtilsTestCase extends TestCase {

    private static class NullAction extends AbstractAction {
    }

    private static class XMLLikeAction extends AbstractAction {
        boolean rootFoundAbsolute = false;
        boolean rootFoundRelative = false;
        boolean wrongRelativeFound = false;
        boolean longAbsoluteFound = false;

        public void begin(Context context, String namespace, String name, Attributes attrs) {
            String path = context.getMatchPath();
            if (SimpleMatchUtils.matches(path, "/root")) {
                rootFoundAbsolute = true;
            }

            if (SimpleMatchUtils.matches(path, "root")) {
                rootFoundRelative = true;
            }

            if (SimpleMatchUtils.matches(path, "/root/p/em")) {
                longAbsoluteFound = true;
            }

            if (SimpleMatchUtils.matches(path, "ot/p")) {
                wrongRelativeFound = true;
            }
        }
    };

    // -----------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------

    /**
     * Construct a new instance of this test case.
     * 
     * @param name
     *            Name of the test case
     */
    public SimpleMatchUtilsTestCase(String name) {
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
        return (new TestSuite(SupplementaryRuleManagerTestCase.class));
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
     * SimpleMatchUtils is expected to be used in situations where the user
     * has written a custom Action class that performs its own rule-matching
     * (aka "xmlio-style"). So we test that here.
     */
    public void testBasicOperation() throws Exception {
       String inputText = "<root><p>Hi <em>There</em></p></root>";

        // xmlio-style digester
        XMLLikeAction xmlioLikeAction = new XMLLikeAction();

        Digester d = new Digester();
        d.getRuleManager().addMandatoryAction(xmlioLikeAction);
        d.addRule("root", new NullAction());

        // try twice to check caching
        for (int i = 0; i < 2; i++) {
            InputSource source = new InputSource(new StringReader(inputText));
            d.parse(source);

            assertTrue("Root element was found absolute", xmlioLikeAction.rootFoundAbsolute);
            assertTrue("Root element was found relative", xmlioLikeAction.rootFoundRelative);
            assertTrue("Long absolute path was found", xmlioLikeAction.longAbsoluteFound);
            assertFalse("Incomplete relative was not found", xmlioLikeAction.wrongRelativeFound);
        }
    }
}