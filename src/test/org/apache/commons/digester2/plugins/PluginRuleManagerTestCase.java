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
 * Test cases for basic PluginRuleManager behaviour.
 */

public class PluginRuleManagerTestCase extends TestCase {
    // -----------------------------------------------------------
    // Helper classes
    // -----------------------------------------------------------

    private static class DummyAction extends AbstractAction {
    }
    
    // -----------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public PluginRuleManagerTestCase(String name) {
        super(name);
    }

    // --------------------------------------------------
    // Overall Test Methods
    // --------------------------------------------------

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(PluginRuleManagerTestCase.class));
    }

    // ------------------------------------------------
    // Individual Test Methods
    // ------------------------------------------------

    /**
     * Test basic operation of this class.
     */
    public void testBasicOperation() throws Exception {
        RuleManager parentRuleManager = new DefaultRuleManager();
        RuleManager delegateRuleManager = new DefaultRuleManager();

        PluginRuleManager pluginRuleManager =
            new PluginRuleManager(
                parentRuleManager,
                delegateRuleManager,
                "/foo/bar");

        // getParent returns object passed to constructor
        assertSame("Unexpected parent object", parentRuleManager,
            pluginRuleManager.getParent());

        // addRule adds to delegate only
        Action action1 = new DummyAction();
        pluginRuleManager.addRule("/foo/bar/baz", action1);
        assertEquals("Rule added to parent",
            0, parentRuleManager.getActions().size());
        assertEquals("Rule not added to delegate",
            1, delegateRuleManager.getActions().size());

        // getMatchingActions returns delegate rules only
        Action action2 = new DummyAction();
        parentRuleManager.addRule("/foo/bar/baz", action2);
        List actions = delegateRuleManager.getMatchingActions("/foo/bar/baz");
        assertEquals("Unexpected matching actions", 1, actions.size());
        assertEquals("Unexpected matching action",
            action1, actions.get(0));
    }
}
