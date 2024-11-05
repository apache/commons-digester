/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.digester3;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Tests that exercise the new (in 1.8) methods for passing in {@code URL} arguments instead of strings.
 * </p>
 */
public class URLTestCase
{

    /**
     * <p>
     * Public identifier of the Digester Rules DTD.
     * </p>
     */
    private static final String DIGESTER_RULES_PUBLIC_ID = "-//Apache Commons //DTD digester-rules XML V1.0//EN";

    /**
     * <p>
     * System identifier of the Digester Rules DTD.
     * </p>
     */
    private static final String DIGESTER_RULES_SYSTEM_ID = "/org/apache/commons/digester3/xmlrules/digester-rules.dtd";

    /**
     * <p>
     * System identifier for the Digester Rules file that we will parse.
     * </p>
     */
    private static final String TEST_INPUT_SYSTEM_ID =
        "/org/apache/commons/digester3/xmlrules/test-call-param-rules.xml";

    /**
     * <p>
     * The {@code Digester} instance under test.
     * </p>
     */
    private Digester digester;

    /**
     * Sets up instance variables required by this test case.
     */
    @BeforeEach
    public void setUp()
    {

        digester = new Digester();

    }

    /**
     * Tear down instance variables required by this test case.
     */
    @AfterEach
    public void tearDown()
    {

        digester = null;

    }

    // Test a pristine instance
    @Test
    public void testPristine()
    {

        assertNotNull( digester );

    }

    // Test parsing a resource, using a registered DTD, both passed with URLs
    @Test
    public void testResource()
        throws Exception
    {

        // Register the Digester Rules DTD
        final URL dtd = URLTestCase.class.getResource( DIGESTER_RULES_SYSTEM_ID );
        assertNotNull( dtd );
        digester.register( DIGESTER_RULES_PUBLIC_ID, dtd );

        // Parse one of the existing test resources twice with
        // the same Digester instance
        final URL xml = URLTestCase.class.getResource( TEST_INPUT_SYSTEM_ID );
        assertNotNull( xml );
        digester.parse( xml );
        digester.parse( xml );

    }

}
