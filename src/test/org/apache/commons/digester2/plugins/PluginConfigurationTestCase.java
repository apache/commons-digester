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


package org.apache.commons.digester2.plugins;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.digester2.*;
import org.apache.commons.digester2.actions.*;
import org.apache.commons.digester2.plugins.*;

/**
 * Test cases for basic PluginConfiguration behaviour.
 */

public class PluginConfigurationTestCase extends TestCase {
    // -----------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public PluginConfigurationTestCase(String name) {
        super(name);
    }

    // --------------------------------------------------
    // Overall Test Methods
    // --------------------------------------------------

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(PluginConfigurationTestCase.class));
    }

    // ------------------------------------------------
    // Individual Test Methods
    // ------------------------------------------------

    /**
     * Test the getInstance methods of this class.
     */
    public void testGetInstance() throws Exception {
        Digester digester = new Digester();
        SAXHandler saxHandler = digester.getSAXHandler();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, null);
        
        // object created on first call
        PluginConfiguration pc = PluginConfiguration.getInstance(digester);
        
        // later calls return same object
        assertSame("Unexpected PluginConfiguration object", pc,
            PluginConfiguration.getInstance(digester));
        assertSame("Unexpected PluginConfiguration object", pc,
            PluginConfiguration.getInstance(saxHandler));
        assertSame("Unexpected PluginConfiguration object", pc,
            PluginConfiguration.getInstance(context));
    }

    /**
     * Test the get/set RuleFinder methods of this class.
     */
    public void testRuleFinders() throws Exception {
        Digester digester = new Digester();
        SAXHandler saxHandler = digester.getSAXHandler();
        
        // object created on first call
        PluginConfiguration pc = PluginConfiguration.getInstance(digester);

        // check that the list has at least 4 entries, that each entry is
        // of type RuleFinder, and that none of the objects are the same.
        List ruleFinders = pc.getRuleFinders();
        int nFinders = ruleFinders.size();
        assertTrue("Finders list has finders", nFinders >= 4);
        for(int i=0; i<nFinders; ++i) {
            RuleFinder finder = (RuleFinder) ruleFinders.get(i);
            assertTrue("Finder object duplicated",
                !ruleFinders.subList(i+1, nFinders).contains(finder));
        }
        
        // set the rulefinder list, and check that getting the list back
        // returns the modified list.
        ruleFinders = new ArrayList();
        pc.setRuleFinders(ruleFinders);
        assertEquals("RuleFinder list not set",
            0,
            PluginConfiguration.getInstance(saxHandler).getRuleFinders().size());
    }
    
    /**
     * Test the ability to configure global settings for the
     * plugin-id and plugin-class attributes. Note that testing that
     * PluginDeclarationAction and PluginCreateAction actually respect
     * these settings is done instead in the tests for those classes.
     */
    public void testAttributes() throws Exception {
        Digester digester = new Digester();
        PluginConfiguration pc = PluginConfiguration.getInstance(digester);
        
        // plugin class attribute
        assertEquals("Unexpected initial plugin class attr ns", 
            "", pc.getPluginClassAttrNS());
        assertEquals("Unexpected initial plugin class attr", 
            "plugin-class", pc.getPluginClassAttr());

        pc.setPluginClassAttribute("http://acme.com/plugin", "class");

        assertEquals("Unexpected plugin class attr ns", 
            "http://acme.com/plugin", pc.getPluginClassAttrNS());
        assertEquals("Unexpected plugin class attr", 
            "class", pc.getPluginClassAttr());

        // plugin id attribute
        assertEquals("Unexpected initial plugin id attr ns", 
            "", pc.getPluginIdAttrNS());
        assertEquals("Unexpected initial plugin id attr", 
            "plugin-id", pc.getPluginIdAttr());

        pc.setPluginIdAttribute("http://acme.com/plugin", "id");

        assertEquals("Unexpected plugin id attr ns", 
            "http://acme.com/plugin", pc.getPluginIdAttrNS());
        assertEquals("Unexpected plugin id attr", 
            "id", pc.getPluginIdAttr());

        // test that getting the pluginconfig again from the same digester
        // returns the modified values
        pc = PluginConfiguration.getInstance(digester);
        assertEquals("Unexpected plugin class attr", 
            "class", pc.getPluginClassAttr());
        assertEquals("Unexpected plugin id attr", 
            "id", pc.getPluginIdAttr());
        
        // test that getting a pluginconfig from a different digester
        // returns the initial values.
        Digester digester2 = new Digester();
        PluginConfiguration pc2 = PluginConfiguration.getInstance(digester2);
        assertEquals("Unexpected plugin class attr", 
            "plugin-class", pc2.getPluginClassAttr());
        assertEquals("Unexpected plugin id attr", 
            "plugin-id", pc2.getPluginIdAttr());
    }
}
