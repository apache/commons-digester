/* $Id: DefaultRuleManagerTestCase.java 151644 2005-02-07 00:08:59Z skitching $
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

import java.io.StringReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * <p>
 * Test Case for the DefaultRuleManager class.
 * </p>
 */

public class SupplementaryRuleManagerTestCase extends TestCase {

    private static class XMLLikeAction extends AbstractAction {
        
        boolean rootFoundAbsolute = false;
        boolean rootFoundRelative = false;
        boolean wrongRelativeFound = false;
        boolean longAbsoluteFound = false;
    
        public void begin(Context context, String namespace, String name, Attributes attrs) {

            String path = context.getMatchPath();
            if (SupplementaryRuleManager.matches(path, "/root")) {
                rootFoundAbsolute = true;
            } 
            
            if (SupplementaryRuleManager.matches(path, "root")) {
                rootFoundRelative = true;
            }

            if (SupplementaryRuleManager.matches(path, "/root/p/em")) {
                longAbsoluteFound = true;
            } 

            if (SupplementaryRuleManager.matches(path, "ot/p")) {
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
    public SupplementaryRuleManagerTestCase(String name) {
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

    public void testGlobalCallBack() throws Exception {
        String inputText = "<root><p>Hi <em>There</em></p></root>";
        InputSource source = new InputSource(new StringReader(inputText));

        Digester d = new Digester();

        XMLReader reader = d.getXMLReader();

        // xmlio-style digester
        XMLLikeAction xmlioLikeHandler = new XMLLikeAction();

        RuleManager manager = new SupplementaryRuleManager(xmlioLikeHandler);
        d.setRuleManager(manager);

        d.parse(source);

        assertTrue("Root element was found absolute", xmlioLikeHandler.rootFoundAbsolute);
        assertTrue("Root element was found relative", xmlioLikeHandler.rootFoundRelative);
        assertTrue("Long absolute path was found", xmlioLikeHandler.longAbsoluteFound);
        assertFalse("Incomplete relative was not found", xmlioLikeHandler.wrongRelativeFound);
        
    }

}