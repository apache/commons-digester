/* $Id: $
 *
 * Copyright 2001-2004 The Apache Software Foundation.
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
import java.util.EmptyStackException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;

import org.apache.commons.logging.Log;


/**
 * <p>Test Case for the Digester class.  These tests exercise the individual
 * methods of a Digester, but do not attempt to process complete documents.
 * </p>
 */

public class DigesterTestCase extends TestCase {

    // ----------------------------------------------------- Instance Variables

    /**
     * The digester instance we will be processing.
     */
    protected Digester digester = null;

    /**
     * The set of public identifiers, and corresponding resource names,
     * for the versions of the DTDs that we know about.  There
     * <strong>MUST</strong> be an even number of Strings in this array.
     */
    protected static final String registrations[] = {
        "-//Netscape Communications//DTD RSS 0.9//EN",
        "/org/apache/commons/digester/rss/rss-0.9.dtd",
        "-//Netscape Communications//DTD RSS 0.91//EN",
        "/org/apache/commons/digester/rss/rss-0.91.dtd",
    };

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public DigesterTestCase(String name) {
        super(name);
    }

    // -------------------------------------------------- Overall Test Methods

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        digester = new Digester();
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
        digester = null;
    }

    // ------------------------------------------------ Individual Test Methods

    /**
     * Test the basic property getters and setters.
     */
    public void testProperties() {
        DefaultHandler defaultHandler = new org.xml.sax.helpers.DefaultHandler();
        
        // check we can set and get a custom error handler
        assertNull("Initial error handler is null",
                digester.getErrorHandler());
        digester.setErrorHandler(defaultHandler);
        assertTrue("Set/get error handler failed",
                digester.getErrorHandler() == defaultHandler);
        digester.setErrorHandler(null);
        assertNull("Reset error handler failed",
                digester.getErrorHandler());

         // check the validation property
        assertTrue("Initial validating is false",
                !digester.getValidating());
        digester.setValidating(true);
        assertTrue("Set validating is true",
                digester.getValidating());
        digester.setValidating(false);
        assertTrue("Reset validating is false",
                !digester.getValidating());
                
        // set and get xml reader
        // set and get classloader, and useContextClassLoader
        // get and set logger
        // get and set saxlogger
        // get and set RuleManager
        // get and set Substitutor
    }


    /**
     * Test registration of URLs for specified public identifiers.
     */
    public void testRegistrations() {

        Map map = digester.getKnownEntities();
        assertEquals("Initially zero registrations", 0, map.size());
        int n = 0;
        for (int i = 0; i < registrations.length; i += 2) {
            URL url = this.getClass().getResource(registrations[i + 1]);
            if (url != null) {
                digester.registerKnownEntity(registrations[i], url.toString());
                n++;
            }
        }

        assertEquals("Registered two URLs", n, map.size());

        int count[] = new int[n];
        for (int i = 0; i < n; i++) {
            count[i] = 0;
        }
        Iterator keys = map.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            for (int i = 0; i < n; i++) {
                if (key.equals(registrations[i * 2])) {
                    count[i]++;
                    break;
                }
            }
        }
        for (int i = 0; i < n; i++)
            assertEquals("Count for key " + registrations[i * 2],
                    1, count[i]);
    }
}
