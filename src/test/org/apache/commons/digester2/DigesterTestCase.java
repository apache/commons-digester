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

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.apache.commons.logging.Log;


/**
 * <p>Test Case for the Digester class.  These tests exercise the individual
 * methods of a Digester, but do not attempt to process complete documents.
 * </p>
 */

public class DigesterTestCase extends TestCase {

    private static class AppenderAction extends AbstractAction {
        private List list;
        private String str;

        public AppenderAction(List list, String str) {
            this.list = list;
            this.str = str;
        }

        public void begin(
        Context context,
        String namespace, String name,
        org.xml.sax.Attributes attrs) {
            list.add(str);
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
    public DigesterTestCase(String name) {
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
        return (new TestSuite(DigesterTestCase.class));
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
     * Test the basic constructor functionality.
     */
    public void testConstructor1() {
        Digester d = new Digester();

        assertNotNull("Default constructor", d.getSAXHandler());
    }

    /**
     * Test the basic constructor functionality.
     */
    public void testConstructor2() {
        SAXHandler h = new SAXHandler();
        Digester d = new Digester(h);

        assertSame("Constructor with SAXHandler", d.getSAXHandler(), h);
    }

    /**
     * Test that digester auto-creates an XMLReader if needed,
     * and that parsing works ok with that reader.
     */
    public void testXMLReaderAuto() throws Exception {
        String inputText = "<root/>";
        InputSource source = new InputSource(new StringReader(inputText));

        Digester d = new Digester();

        XMLReader reader = d.getXMLReader();
        assertNotNull("getXMLReader", reader);

        ArrayList list = new ArrayList();
        d.addRule("/root", new AppenderAction(list, "action1"));

        d.parse(source);

        assertEquals("Parse works with auto-created parser", 1, list.size());
        assertEquals("Parse works with auto-created parser", "action1", list.get(0));
    }

    /**
     * Same as testXMLReaderAuto except that getXMLReader is not called.
     * This is just to make sure getXMLReader hasn't had some necessary
     * side-effect that causes a parse to fail without it.
     */
    public void testXMLReaderAuto2() throws Exception {
        String inputText = "<root/>";
        InputSource source = new InputSource(new StringReader(inputText));

        Digester d = new Digester();

        ArrayList list = new ArrayList();
        d.addRule("/root", new AppenderAction(list, "action1"));

        d.parse(source);

        assertEquals("Parse works with auto-created parser", 1, list.size());
        assertEquals("Parse works with auto-created parser", "action1", list.get(0));
    }

    /**
     * Test that digester works if an XMLReader has been explicitly created
     * and passed in to the digester.
     */
    public void testXMLReaderManual() throws Exception {
        // test that digester auto-creates an XMLReader if needed,
        // and that parsing works ok with that reader.
        String inputText = "<root/>";
        InputSource source = new InputSource(new StringReader(inputText));

        // create XMLReader
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser parser = factory.newSAXParser();
        XMLReader reader = parser.getXMLReader();

        // Create the digester
        Digester d = new Digester();

        // connect XMLReader to saxHandler
        d.setXMLReader(reader, true);

        ArrayList list = new ArrayList();
        d.addRule("/root", new AppenderAction(list, "action1"));

        d.parse(source);

        assertEquals("Parse works with manual-created parser", 1, list.size());
        assertEquals("Parse works with manual-created parser", "action1", list.get(0));
    }

    // TODO: add test for setValidating/getValidating

    // TODO: add test for get/set explicit classloader
    // TODO: add test for setUseContextClassloader

    // TODO: add test for get/set logger. This should probably wait until
    // we figure out whether to revamp the logging approach though.

    // TODO: add tests for setRuleManager/getRuleManager

    /**
     * Test getDTDPublicId and getDTDSystemId methods.
     *
     * This also happens to test the digester when parse is called with
     * no rules added, and to test ignoring of external DTDs.
     */
    public void testDtdInfo1() throws Exception {
        String inputText =
              "<!DOCTYPE root PUBLIC 'test-public-id' 'test-system-id'>"
            + "<root/>";

        InputSource source = new InputSource(new StringReader(inputText));

        // initially, no info is available
        Digester d = new Digester();
        assertNull("initial dtd public id is null", d.getDTDPublicId());
        assertNull("initial dtd system id is null", d.getDTDSystemId());

        // ignore any attempt to load an external dtd with the specified
        // public-id
        d.registerKnownEntity("test-public-id", "");

        // and parse...
        d.parse(source);

        String pub = d.getDTDPublicId();
        String sys = d.getDTDSystemId();
        assertEquals("DTD public id obtained", "test-public-id", pub);
        assertEquals("DTD system id obtained", "test-system-id", sys);
    }

    /**
     * Test getDTDPublicId and getDTDSystemId methods.
     *
     * In this test, the public is null, and an internal DTD is present.
     */
    public void testDtdInfo2() throws Exception {
        String inputText =
              "<!DOCTYPE root SYSTEM 'test-system-id'"
            + "[<!ELEMENT root EMPTY>]>"
            + "<root/>";

        InputSource source = new InputSource(new StringReader(inputText));

        // initially, no info is available
        Digester d = new Digester();
        assertNull("initial dtd public id is null", d.getDTDPublicId());
        assertNull("initial dtd system id is null", d.getDTDSystemId());

        // Ignore any attempt to load an external dtd. Note that it is
        // impossible to use registerKnownEntity to ignore the DTD, as it
        // doesn't have a public id, and the systemId is expanded by the
        // parser to a full path at runtime so we cannot know what the
        // complete systemId will be..
        assertFalse("External DTD not ignored by default", d.getIgnoreExternalDTD());
        d.setIgnoreExternalDTD(true);
        assertTrue("External DTD ignored", d.getIgnoreExternalDTD());

        // and parse...
        d.parse(source);

        String pub = d.getDTDPublicId();
        String sys = d.getDTDSystemId();
        assertNull("DTD public id is null", pub);
        assertEquals("DTD system id obtained", "test-system-id", sys);
    }

    // TODO: add tests for get/set substitutor

    // TODO: add tests for get/set/register known entities
    // including testing for allowUnknownExternalEntity

    // TODO: add tests for various parse methods

    /**
     * Test that setInitialObject works.
     */
    public void testInitialObject() throws Exception {
        // TODO: verify that Action classes see the initial object as
        // the root object on the stack...
        
        String inputText = "<root/>";
        InputSource source = new InputSource(new StringReader(inputText));

        Object initial = new Object();
        
        // Create the digester
        Digester d = new Digester();
        d.setInitialObject(initial);
        d.parse(source);
        
        assertSame("Initial object is root", initial, d.getRoot());
    }
    
    // TODO: add test for setInitialObject and getRoot

    /**
     * Test the basic property getters and setters.
     */
    public void testProperties() {
        DefaultHandler defaultHandler = new org.xml.sax.helpers.DefaultHandler();

        Digester d = new Digester();

        // check we can set and get a custom error handler
        assertNull("Initial error handler is null", d.getErrorHandler());
        d.setErrorHandler(defaultHandler);
        assertSame("Set/get error handler failed",
            defaultHandler, d.getErrorHandler());

        d.setErrorHandler(null);
        assertNull("Reset error handler failed", d.getErrorHandler());

         // check the validation property
        assertTrue("Initial validating is false", !d.getValidating());
        d.setValidating(true);
        assertTrue("Set validating is true", d.getValidating());
        d.setValidating(false);
        assertTrue("Reset validating is false", !d.getValidating());

        // set and get classloader, and useContextClassLoader
        // get and set saxlogger
        // get and set RuleManager
        // get and set Substitutor
    }


    /**
     * Test registration of URLs for specified public identifiers.
     */
    public void testRegistrations() {
        Digester d = new Digester();

        Map map = d.getKnownEntities();
        assertEquals("Initially zero registrations", 0, map.size());

        d.registerKnownEntity("public-1", "url-1");
        d.registerKnownEntity("public-2", "url-2");
        d.registerKnownEntity("public-3", "");

        assertEquals("Registered URLs", 3, map.size());
        assertEquals("Retrieved registered URL", "url-2", map.get("public-2"));
    }
}
