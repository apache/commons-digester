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


package org.apache.commons.digester2.plugins.strategies;

import java.util.List;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.digester2.*;
import org.apache.commons.digester2.actions.*;
import org.apache.commons.digester2.plugins.*;

/**
 * Test cases for FinderSetProperties behaviour.
 */

public class FinderSetPropertiesTestCase extends TestCase {
    // -----------------------------------------------------------
    // Helper classes
    // -----------------------------------------------------------

    public static class DummyPlugin {
    }

    // -----------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public FinderSetPropertiesTestCase(String name) {
        super(name);
    }

    // --------------------------------------------------
    // Overall Test Methods
    // --------------------------------------------------

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(FinderSetPropertiesTestCase.class));
    }

    // ------------------------------------------------
    // Individual Test Methods
    // ------------------------------------------------


    /**
     * Test the basic functionality of this class.
     */
    public void testBasicOperation() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        RuleManager ruleManager = saxHandler.getRuleManager();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, ruleManager);

        FinderSetProperties finder = new FinderSetProperties();

        Properties props = new Properties();
        props.put("assign-props", "nope");
        Class pluginClass = DummyPlugin.class;
        RuleLoader loader = finder.findLoader(context, pluginClass, props);

        assertNotNull("Loader is null", loader);

        // now test the loader that was returned does defined a
        // SetPropertiesAction

        context.pushMatchPath("", "foo");
        context.pushMatchPath("", "bar");
        String path = context.getMatchPath();

        // paranoia test...
        assertEquals("Unexpected path", "/foo/bar", path);

        assertEquals("RuleManager not empty", 0, ruleManager.getActions().size());
        loader.addRules(context);

        // assert ruleManager has one rule
        assertEquals("RuleManager has unexpected number of actions",
            1,
            ruleManager.getActions().size());
        assertEquals("Action is not of expected type",
            SetPropertiesAction.class, ruleManager.getActions().get(0).getClass());

        // assert that match(context.getMatchPath()) returns a SetPropertiesAction
        List matchingActions = ruleManager.getMatchingActions("/foo");
        assertEquals("Unexpected actions", 0, matchingActions.size());
        matchingActions = ruleManager.getMatchingActions("/foo/bar");
        assertEquals("Unexpected actions", 1, matchingActions.size());
        assertEquals("Unexpected action type",
            SetPropertiesAction.class, matchingActions.get(0).getClass());
    }

    /**
     * Test disabling of SetPropertiesAction, and custom attribute use.
     */
    public void testDisable() throws Exception {
        SAXHandler saxHandler = new SAXHandler();
        RuleManager ruleManager = saxHandler.getRuleManager();
        Context context = new Context(
            saxHandler, saxHandler.getLogger(),
            null, ruleManager);

        FinderSetProperties finder = new FinderSetProperties("assign-props", "nope");

        Properties props = new Properties();
        props.put("assign-props", "nope");
        Class pluginClass = DummyPlugin.class;
        RuleLoader loader = finder.findLoader(context, pluginClass, props);

        // we expect null to be returned; the properties indicated that this
        // finder should be ignored.
        assertNull("Loader is not null", loader);
    }
}
