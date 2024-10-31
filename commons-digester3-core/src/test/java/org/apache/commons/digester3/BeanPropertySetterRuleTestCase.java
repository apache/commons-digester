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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester3.binder.AbstractRulesModule;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p>
 * Test case for {@code BeanPropertySetterRule}. This contains tests for the main applications of the rule and two
 * more general tests of digester functionality used by this rule.
 */
public class BeanPropertySetterRuleTestCase
{

    /**
     * Simple test XML document used in the tests.
     */
    protected final static String TEST_XML = "<?xml version='1.0'?>" + "<root>ROOT BODY" + "<alpha>ALPHA BODY</alpha>"
        + "<beta>BETA BODY</beta>" + "<gamma>GAMMA BODY</gamma>" + "<delta>DELTA BODY</delta>" + "</root>";

    /**
     * Test that you can successfully automatically set properties.
     */
    @Test
    public void testAutomaticallySetProperties()
        throws SAXException, IOException
    {
        final Digester digester = newLoader(new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root" ).createObject().ofType( "org.apache.commons.digester3.SimpleTestBean" );
                forPattern( "root/?" ).setBeanProperty();
            }

        }).newDigester( new ExtendedBaseRules() );

        final SimpleTestBean bean = digester.parse( xmlTestReader() );

        // check properties are set correctly
        assertEquals( "ALPHA BODY", bean.getAlpha(), "Property alpha not set correctly" );

        assertEquals( "BETA BODY", bean.getBeta(), "Property beta not set correctly" );

        assertEquals( "GAMMA BODY", bean.getGamma(), "Property gamma not set correctly" );

    }

    /**
     * This is a general digester test but it fits into here pretty well. This tests that the body text stack is
     * functioning correctly.
     */
    @Test
    public void testDigesterBodyTextStack()
        throws SAXException, IOException
    {
        final List<Rule> callOrder = new ArrayList<>();

        final Digester digester = newLoader(new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root" ).addRuleCreatedBy( new TestRule.TestRuleProvider( "root", callOrder ) );
                forPattern( "root/alpha" ).addRuleCreatedBy( new TestRule.TestRuleProvider( "root/alpha", callOrder ) );
                forPattern( "root/beta" ).addRuleCreatedBy( new TestRule.TestRuleProvider( "root/beta", callOrder ) );
                forPattern( "root/gamma" ).addRuleCreatedBy( new TestRule.TestRuleProvider( "root/gamma", callOrder ) );
            }

        }).newDigester();

        digester.parse( xmlTestReader() );

        assertEquals( "ROOT BODY", ( ( TestRule ) callOrder.get( 0 ) ).getBodyText(), "Root body text not set correct." );

        assertEquals( "ALPHA BODY", ( ( TestRule ) callOrder.get( 1 ) ).getBodyText(), "Alpha body text not set correct." );

        assertEquals( "BETA BODY", ( ( TestRule ) callOrder.get( 4 ) ).getBodyText(), "Beta body text not set correct." );

        assertEquals( "GAMMA BODY", ( ( TestRule ) callOrder.get( 7 ) ).getBodyText(), "Gamma body text not set correct." );

    }

    /**
     * This is a general digester test but it fits into here pretty well. This tests that the rule calling order is
     * properly enforced.
     */
    @Test
    public void testDigesterRuleCallOrder()
        throws SAXException, IOException
    {

        final List<Rule> callOrder = new ArrayList<>();

        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                // add first test rule
                forPattern( "root/alpha" ).addRuleCreatedBy( new TestRule.TestRuleProvider( "first", callOrder ) );
                // add second test rule
                forPattern( "root/alpha" ).addRuleCreatedBy( new TestRule.TestRuleProvider( "second", callOrder ) );
                // add third test rule
                forPattern( "root/alpha" ).addRuleCreatedBy( new TestRule.TestRuleProvider( "third", callOrder ) );
            }

        }).newDigester();

        digester.parse( xmlTestReader() );

        // we should have nine entries in our list of calls

        assertEquals( 9, callOrder.size(), "Nine calls should have been made." );

        // begin should be called in the order added
        assertEquals( "first", ( ( TestRule ) callOrder.get( 0 ) ).getIdentifier(), "First rule begin not called first." );

        assertEquals( "second", ( ( TestRule ) callOrder.get( 1 ) ).getIdentifier(), "Second rule begin not called second." );

        assertEquals( "third", ( ( TestRule ) callOrder.get( 2 ) ).getIdentifier(), "Third rule begin not called third." );

        // body text should be called in the order added
        assertEquals( "first", ( ( TestRule ) callOrder.get( 3 ) ).getIdentifier(), "First rule body text not called first." );

        assertEquals( "second", ( ( TestRule ) callOrder.get( 4 ) ).getIdentifier(), "Second rule body text not called second." );

        assertEquals( "third", ( ( TestRule ) callOrder.get( 5 ) ).getIdentifier(), "Third rule body text not called third." );

        // end should be called in reverse order
        assertEquals( "third", ( ( TestRule ) callOrder.get( 6 ) ).getIdentifier(), "Third rule end not called first." );

        assertEquals( "second", ( ( TestRule ) callOrder.get( 7 ) ).getIdentifier(), "Second rule end not called second." );

        assertEquals( "first", ( ( TestRule ) callOrder.get( 8 ) ).getIdentifier(), "First rule end not called third." );

    }

    @Test
    public void testExtractPropertyNameFromAttribute() throws Exception
    {
        final Employee expected = new Employee( "John", "Doe" );

        final Employee actual = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "employee" ).createObject().ofType( Employee.class );
                forPattern( "employee/property" ).setBeanProperty().extractPropertyNameFromAttribute( "name" );
            }

        } )
        .newDigester()
        .parse( getClass().getResource( "extractPropertyNameFromAttribute.xml" ) );

        assertEquals( expected.getFirstName(), actual.getFirstName() );
        assertEquals( expected.getLastName(), actual.getLastName() );
    }

    /**
     * Test that you can successfully set a given property
     */
    @Test
    public void testSetGivenProperty()
        throws SAXException, IOException
    {
        final Digester digester = newLoader(new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root" ).createObject().ofType( SimpleTestBean.class );
                forPattern( "root" ).setBeanProperty().withName( "alpha" );

                // we'll set property beta with the body text of child element alpha
                forPattern( "root/alpha" ).setBeanProperty().withName( "beta" );
                // we'll leave property gamma alone

                // we'll set property delta (a write-only property) also
                forPattern( "root/delta" ).setBeanProperty().withName( "delta" );
            }

        }).newDigester();

        final SimpleTestBean bean = digester.parse( xmlTestReader() );

        // check properties are set correctly
        assertEquals( "ROOT BODY", bean.getAlpha(), "Property alpha not set correctly" );

        assertEquals( "ALPHA BODY", bean.getBeta(), "Property beta not set correctly" );

        assertNull( bean.getGamma(), "Property gamma not set correctly" );

        assertEquals( "DELTA BODY", bean.getDeltaValue(), "Property delta not set correctly" );

    }

    /**
     * Test that trying to set an unknown property throws an exception.
     */
    @Test
    public void testSetUnknownProperty()
    {
        final Digester digester = newLoader(new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root" ).createObject().ofType( "org.apache.commons.digester3.SimpleTestBean" );
                forPattern( "root" ).setBeanProperty().withName( "alpha" );

                // attempt to set an unknown property name
                forPattern( "root/alpha" ).setBeanProperty().withName( "unknown" );
            }

        }).newDigester();

        // Attempt to parse the input
        SAXParseException e = assertThrows( SAXParseException.class, () -> digester.parse( xmlTestReader() ) );
        assertInstanceOf( NoSuchMethodException.class, e.getException(), "Should have thrown SAXParseException->NoSuchMethodException");
    }

    /**
     * Gets input stream from {@link #TEST_XML}.
     */
    private Reader xmlTestReader()
    {
        return new StringReader( TEST_XML );
    }

}
