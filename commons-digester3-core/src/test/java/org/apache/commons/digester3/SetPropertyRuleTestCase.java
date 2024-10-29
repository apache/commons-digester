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

import static org.apache.commons.digester3.binder.DigesterLoader.newLoader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.digester3.binder.AbstractRulesModule;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * <p>
 * Test case for {@code SetPropertyRule}.
 * </p>
 */
public class SetPropertyRuleTestCase
{

    /**
     * Simple test xml document used in the tests.
     */
    protected final static String TEST_XML_1 = "<?xml version='1.0'?><root>"
        + "<set name='alpha' value='ALPHA VALUE'/>" + "<set name='beta' value='BETA VALUE'/>"
        + "<set name='delta' value='DELTA VALUE'/>" + "</root>";

    /**
     * Simple test xml document used in the tests.
     */
    protected final static String TEST_XML_2 = "<?xml version='1.0'?><root>"
        + "<set name='unknown' value='UNKNOWN VALUE'/>" + "</root>";

    private final DigesterLoader loader = newLoader( new AbstractRulesModule()
    {

        @Override
        protected void configure()
        {
            forPattern( "root" ).createObject().ofType( "org.apache.commons.digester3.SimpleTestBean" );
            forPattern( "root/set" ).setProperty( "name" ).extractingValueFromAttribute( "value" );
        }

    });

    /**
     * The digester instance we will be processing.
     */
    protected Digester digester;

    /**
     * Sets up instance variables required by this test case.
     */
    @Before
    public void setUp()
    {

        digester = loader.newDigester();

    }

    /**
     * Tear down instance variables required by this test case.
     */
    @After
    public void tearDown()
    {

        digester = null;

    }

    /**
     * Test SetPropertyRule when matched XML element has no attributes. See: DIGESTER-114
     */
    @Test
    public void testElementWithNoAttributes()
        throws Exception
    {
        final String TEST_XML_3 = "<?xml version='1.0'?><root><set/></root>";

        // Parse the input - should not throw an exception
        @SuppressWarnings( "unused" )
        final
        SimpleTestBean bean = digester.parse( xmlTestReader( TEST_XML_3 ) );
    }

    /**
     * Negative test for SetPropertyRule.
     */
    @Test
    public void testNegative()
    {
        // Parse the input (should fail)
        SAXException e = assertThrows( SAXException.class, () -> digester.parse( xmlTestReader( TEST_XML_2 ) ) );
        assertTrue( "Should have thrown SAXException->NoSuchMethodException, threw " + e.getException(), e.getException() instanceof NoSuchMethodException );
    }

    /**
     * Positive test for SetPropertyRule.
     */
    @Test
    public void testPositive()
        throws Exception
    {
        // Parse the input
        final SimpleTestBean bean = digester.parse( xmlTestReader( TEST_XML_1 ) );

        // Check that the properties were set correctly
        assertEquals( "alpha property set", "ALPHA VALUE", bean.getAlpha() );
        assertEquals( "beta property set", "BETA VALUE", bean.getBeta() );
        assertNull( "gamma property not set", bean.getGamma() );
        assertEquals( "delta property set", "DELTA VALUE", bean.getDeltaValue() );

    }

    /**
     * Gets input stream from specified String containing XML data.
     */
    private Reader xmlTestReader( final String xml )
    {
        return new StringReader( xml );
    }

}
