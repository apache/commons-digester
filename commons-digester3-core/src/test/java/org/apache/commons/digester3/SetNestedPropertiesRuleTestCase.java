/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.digester3.binder.AbstractRulesModule;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p>
 * Test case for {@code SetNestedPropertiesRule}. This contains tests for the main applications of the rule and two
 * more general tests of digester functionality used by this rule.
 */
public class SetNestedPropertiesRuleTestCase
{

    /**
     * Simple test XML document used in the tests.
     */
    protected static final String TEST_XML = "<?xml version='1.0'?><root>ROOT BODY<alpha>ALPHA BODY</alpha>"
            + "<beta>BETA BODY</beta><gamma>GAMMA BODY</gamma><delta>DELTA BODY</delta></root>";

    /**
     * Test that you can successfully automatically set properties.
     */
    @Test
    void testAutomaticallySetProperties() throws Exception
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root" ).createObject().ofType( "org.apache.commons.digester3.SimpleTestBean" )
                    .then()
                    .setNestedProperties();
            }

        }).newDigester();

        final SimpleTestBean bean = digester.parse( xmlTestReader() );

        // check properties are set correctly
        assertEquals( "ALPHA BODY", bean.getAlpha(), "Property alpha not set correctly" );

        assertEquals( "BETA BODY", bean.getBeta(), "Property beta not set correctly" );

        assertEquals( "GAMMA BODY", bean.getGamma(), "Property gamma not set correctly" );

        assertEquals( "DELTA BODY", bean.getDeltaValue(), "Property delta not set correctly" );
    }

    /**
     * Test that you can customize the property mappings using the constructor which takes arrays-of-strings.
     */
    @Test
    void testCustomizedProperties1() throws Exception
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root" ).createObject().ofType( "org.apache.commons.digester3.SimpleTestBean" )
                    .then()
                    .setNestedProperties()
                        .addAlias( "alpha" ).forProperty( null )
                        .addAlias( "gamma-alt" ).forProperty( "gamma" )
                        .addAlias( "delta" ).forProperty(  null );
            }

        }).newDigester();

        final String TEST_XML =
            "<?xml version='1.0'?>" + "<root>ROOT BODY" + "<alpha>ALPHA BODY</alpha>" + "<beta>BETA BODY</beta>"
                + "<gamma-alt>GAMMA BODY</gamma-alt>" + "<delta>DELTA BODY</delta>" + "</root>";

        final SimpleTestBean bean = digester.parse( new StringReader( TEST_XML ) );

        // check properties are set correctly
        assertNull( bean.getAlpha(), "Property alpha was not ignored (it should be)" );

        assertEquals( "BETA BODY", bean.getBeta(), "Property beta not set correctly" );

        assertEquals( "GAMMA BODY", bean.getGamma(), "Property gamma not set correctly" );

        assertNull( bean.getDeltaValue(), "Property delta was not ignored (it should be)" );

        // check no bad rules object is left
        assertInstanceOf( RulesBase.class, digester.getRules(), "Digester rules object not reset." );
    }

    /**
     * Test that you can ignore a single input XML element using the constructor which takes a single remapping.
     */
    @Test
    void testCustomizedProperties2a() throws Exception
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root" ).createObject().ofType( "org.apache.commons.digester3.SimpleTestBean" )
                    .then()
                    .setNestedProperties()
                        .addAlias( "alpha" ).forProperty( null );
            }

        }).newDigester();

        final String TEST_XML =
            "<?xml version='1.0'?>" + "<root>ROOT BODY" + "<alpha>ALPHA BODY</alpha>" + "<beta>BETA BODY</beta>"
                + "<gamma>GAMMA BODY</gamma>" + "<delta>DELTA BODY</delta>" + "</root>";

        final SimpleTestBean bean = digester.parse( new StringReader( TEST_XML ) );

        // check properties are set correctly
        assertNull( bean.getAlpha(), "Property alpha was not ignored (it should be)" );

        assertEquals( "BETA BODY", bean.getBeta(), "Property beta not set correctly" );

        assertEquals( "GAMMA BODY", bean.getGamma(), "Property gamma not set correctly" );

        assertEquals( "DELTA BODY", bean.getDeltaValue(), "Property delta not set correctly" );

        // check no bad rules object is left
        assertInstanceOf( RulesBase.class, digester.getRules(), "Digester rules object not reset." );
    }

    /**
     * Test that you can customize the property mappings using the constructor which takes a single remapping.
     */
    @Test
    void testCustomizedProperties2b() throws Exception
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root" ).createObject().ofType( "org.apache.commons.digester3.SimpleTestBean" )
                    .then()
                    .setNestedProperties()
                        .addAlias( "alpha-alt" ).forProperty( "alpha" );
            }

        }).newDigester();

        final String TEST_XML =
            "<?xml version='1.0'?>" + "<root>ROOT BODY" + "<alpha-alt>ALPHA BODY</alpha-alt>"
                + "<beta>BETA BODY</beta>" + "<gamma>GAMMA BODY</gamma>" + "<delta>DELTA BODY</delta>" + "</root>";

        final SimpleTestBean bean = digester.parse( new StringReader( TEST_XML ) );

        // check properties are set correctly
        assertEquals( "ALPHA BODY", bean.getAlpha(), "Property alpha not set correctly" );

        assertEquals( "BETA BODY", bean.getBeta(), "Property beta not set correctly" );

        assertEquals( "GAMMA BODY", bean.getGamma(), "Property gamma not set correctly" );

        assertEquals( "DELTA BODY", bean.getDeltaValue(), "Property delta not set correctly" );

        // check no bad rules object is left
        assertInstanceOf( RulesBase.class, digester.getRules(), "Digester rules object not reset." );
    }

    /**
     * Test that it is an error when a child element exists but no corresponding Java property exists.
     */
    @Test
    void testMandatoryProperties()
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root" ).createObject().ofType( "org.apache.commons.digester3.SimpleTestBean" )
                    .then()
                    .setNestedProperties();
            }

        }).newDigester();

        final String TEST_XML = "<?xml version='1.0'?>" + "<root>ROOT BODY" + "<badprop>ALPHA BODY</badprop>" + "</root>";

        SAXParseException e = assertThrows( SAXParseException.class, () -> digester.parse( new StringReader( TEST_XML ) ), "No exception thrown by parse when unknown child element found." );
        assertTrue( e.getMessage().contains( "badprop" ), "Unexpected parse exception:" + e.getMessage() ); // there is no "setBadprop" method on the SimpleTestBean class
    }

    /**
     * Test that:
     * <ul>
     * <li>you can have rules matching the same pattern as the SetNestedPropertiesRule,</li>
     * <li>you can have rules matching child elements of the rule,</li>
     * <li>the Rules object is reset nicely.</li>
     * </ul>
     */
    @Test
    void testMultiRuleMatch() throws Exception
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root/testbean" ).createObject().ofType( "org.apache.commons.digester3.SimpleTestBean" )
                    .then()
                    .setProperties()
                    .then()
                    .setNestedProperties();
                forPattern( "root/testbean/gamma/prop" ).setProperty( "name" ).extractingValueFromAttribute( "value" );
            }

        }).newDigester();

        final String testXml =
            "<?xml version='1.0'?>" + "<root>" + "<testbean alpha='alpha-attr'>ROOT BODY" + "<beta>BETA BODY</beta>"
                + "<gamma>GAMMA " + "<prop name='delta' value='delta-prop'/>" + "BODY" + "</gamma>" + "</testbean>"
                + "</root>";

        final Reader reader = new StringReader( testXml );

        final SimpleTestBean bean = digester.parse( reader );

        assertNotNull( bean, "No object created" );

        // check properties are set correctly
        assertEquals( "alpha-attr", bean.getAlpha(), "Property alpha not set correctly" );

        assertEquals( "BETA BODY", bean.getBeta(), "Property beta not set correctly" );

        assertEquals( "GAMMA BODY", bean.getGamma(), "Property gamma not set correctly" );

        assertEquals( "delta-prop", bean.getDeltaValue(), "Property delta not set correctly" );

        // check no bad rules object is left
        assertInstanceOf( RulesBase.class, digester.getRules(), "Digester rules object not reset." );
    }

    /**
     * Test that the rule works in a sane manner when the associated pattern is a wildcard such that the rule matches
     * one of its own child elements.
     * <p>
     * See bugzilla entry 31393.
     */
    @Test
    void testRecursiveNestedProperties() throws Exception
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "*/testbean" ).createObject().ofType( "org.apache.commons.digester3.SimpleTestBean" )
                    .then()
                    .setNestedProperties().allowUnknownChildElements( true );
            }

        }).newDigester();

        final String testXml =
            "<?xml version='1.0'?>" + "<testbean>" + "<beta>BETA BODY</beta>" + "<testbean>" + "<beta>BETA BODY</beta>"
                + "</testbean>" + "</testbean>";

        final Reader reader = new StringReader( testXml );

        final SimpleTestBean bean = digester.parse( reader );
        assertNotNull( bean );
    }

    /**
     * Test that unknown child elements trigger an exception.
     */
    @Test
    void testUnknownChildrenCausesException()
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root" ).createObject().ofType( "org.apache.commons.digester3.SimpleTestBean" )
                    .then()
                    .setNestedProperties();
            }

        }).newDigester();

        final String testXml =
            "<?xml version='1.0'?>" + "<root>" + "<testbean>" + "<beta>BETA BODY</beta>" + "<foo>GAMMA</foo>"
                + "</testbean>" + "</root>";

        final Reader reader = new StringReader( testXml );

        SAXException e = assertThrows( SAXException.class, () -> digester.parse( reader ) );
        assertInstanceOf( NoSuchMethodException.class, e.getException() );
    }

    /**
     * Test that unknown child elements are allowed if the appropriate flag is set.
     */
    @Test
    void testUnknownChildrenExceptionOverride() throws Exception
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root" ).createObject().ofType( "org.apache.commons.digester3.SimpleTestBean" )
                    .then()
                    .setNestedProperties().allowUnknownChildElements( true );
            }

        }).newDigester();

        final String testXml =
            "<?xml version='1.0'?>" + "<root>" + "<testbean>" + "<beta>BETA BODY</beta>" + "<foo>GAMMA</foo>"
                + "</testbean>" + "</root>";

        final Reader reader = new StringReader( testXml );

        final SimpleTestBean bean = digester.parse( reader );
        assertNotNull( bean );
    }

    /**
     * Gets input stream from {@link #TEST_XML}.
     */
    private Reader xmlTestReader()
    {
        return new StringReader( TEST_XML );
    }

}
