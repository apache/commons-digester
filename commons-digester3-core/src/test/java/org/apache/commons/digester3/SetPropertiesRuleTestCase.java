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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.digester3.binder.AbstractRulesModule;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

/**
 * <p>
 * Test case for {@code SetPropertiesRule}.
 * </p>
 */
public class SetPropertiesRuleTestCase
{

    /**
     * Simple test xml document used in the tests.
     */
    protected final static String TEST_XML_1 =
        "<?xml version='1.0'?><root alpha='ALPHA VALUE' beta='BETA VALUE' delta='DELTA VALUE'/>";

    /**
     * Simple test xml document used in the tests.
     */
    protected final static String TEST_XML_2 =
        "<?xml version='1.0'?><root alpa='ALPA VALUE' beta='BETA VALUE' delta='DELTA VALUE'/>";

    /**
     * Simple test xml document used in the tests.
     */
    protected final static String TEST_XML_3 =
        "<?xml version='1.0'?><root alpha='ALPHA VALUE' beta='BETA VALUE' delta='DELTA VALUE' ignore='ignore value'/>";

    /**
     * Positive test for SetPropertyRule ignoring missing properties.
     */
    @Test
    public void testIgnoreMissing()
        throws Exception
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root" ).createObject().ofType( "org.apache.commons.digester3.SimpleTestBean" )
                    .then()
                    .setProperties();
            }

        }).newDigester();

        // Parse the input
        final SimpleTestBean bean = digester.parse( xmlTestReader( TEST_XML_2 ) );

        // Check that the properties were set correctly
        assertNull( bean.getAlpha(), "alpha property not set" );
        assertEquals( "BETA VALUE", bean.getBeta(), "beta property set" );
        assertNull( bean.getGamma(), "gamma property not set" );
        assertEquals( "DELTA VALUE", bean.getDeltaValue(), "delta property set" );

    }

    /**
     * Negative test for SetPropertyRule ignoring missing properties.
     */
    @Test
    public void testNegativeNotIgnoreMissing()
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root" ).createObject().ofType( "org.apache.commons.digester3.SimpleTestBean" )
                    .then()
                    .setProperties().ignoreMissingProperty( false );
            }

        }).newDigester();

        // Parse the input
        SAXException e = assertThrows( SAXException.class, () -> digester.parse( xmlTestReader( TEST_XML_2 ) ) );
        assertInstanceOf( NoSuchMethodException.class, e.getException(), "Should have thrown NoSuchMethodException" + e.getClass().getName() );
    }

    /**
     * Positive test for SetPropertiesRule.
     */
    @Test
    public void testPositive()
        throws Exception
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root" ).createObject().ofType( "org.apache.commons.digester3.SimpleTestBean" )
                    .then()
                    .setProperties();
            }

        }).newDigester();

        // Parse the input
        final SimpleTestBean bean = digester.parse( xmlTestReader( TEST_XML_1 ) );

        // Check that the properties were set correctly
        assertEquals( "ALPHA VALUE", bean.getAlpha(), "alpha property set" );
        assertEquals( "BETA VALUE", bean.getBeta(), "beta property set" );
        assertNull( bean.getGamma(), "gamma property not set" );
        assertEquals( "DELTA VALUE", bean.getDeltaValue(), "delta property set" );

    }

    /**
     * Negative test for SetPropertyRule ignoring missing properties.
     */
    @Test
    public void testPositiveNotIgnoreMissingWithIgnoreAttributes()
        throws Exception
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root" ).createObject().ofType( "org.apache.commons.digester3.SimpleTestBean" )
                    .then()
                    .setProperties()
                        .addAlias( "ignore" ).forProperty( null )
                        .ignoreMissingProperty( false );
            }

        }).newDigester();

        // Parse the input
        final SimpleTestBean bean = digester.parse( xmlTestReader( TEST_XML_3 ) );

        // Check that the properties were set correctly
        assertEquals( "ALPHA VALUE", bean.getAlpha(), "alpha property set" );
        assertEquals( "BETA VALUE", bean.getBeta(), "beta property set" );
        assertNull( bean.getGamma(), "gamma property not set" );
        assertEquals( "DELTA VALUE", bean.getDeltaValue(), "delta property set" );
    }

    /**
     * Gets input stream from specified String containing XML data.
     */
    private Reader xmlTestReader( final String xml )
    {
        return new StringReader( xml );
    }

}
