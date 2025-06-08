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

package org.apache.commons.digester3.xmlrules;

import static org.apache.commons.digester3.binder.DigesterLoader.newLoader;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.digester3.Address;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.ObjectCreationFactoryTestImpl;
import org.apache.commons.digester3.binder.RulesModule;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

/**
 * Tests loading Digester rules from an XML file.
 */

class FromXmlRuleSetTest
{

    private RulesModule createRules( final String xmlText )
    {
        return new FromXmlRulesModule()
        {

            @Override
            protected void loadRules()
            {
                loadXMLRulesFromText( xmlText );
            }

        };
    }

    private RulesModule createRules( final URL xmlRules )
    {
        return new FromXmlRulesModule()
        {

            @Override
            protected void loadRules()
            {
                loadXMLRules( xmlRules );
            }

        };
    }

    /**
     * Test the FromXmlRules.addRuleInstances(digester, path) method, ie
     * test loading rules at a base position other than the root.
     */
    @Test
    void testBasePath()
        throws Exception
    {
        final String xmlRules =
            "<?xml version='1.0'?>"
            + "<digester-rules>"
            + "   <pattern value='root/foo'>"
            + "      <call-method-rule methodname='setProperty' usingElementBodyAsArgument='true' />"
            + "   </pattern>"
            + "</digester-rules>";

        final String xml =
            "<?xml version='1.0'?>"
            + "<root>"
            + "  <foo>success</foo>"
            + "</root>";

        final ObjectTestImpl testObject = new ObjectTestImpl();
        final Digester digester = newLoader( createRules( xmlRules ) ).newDigester();

        digester.push( testObject );
        digester.parse( new InputSource( new StringReader( xml ) ) );

        assertEquals( "success", testObject.getProperty() );
    }

    @Test
    void testCallParamRule()
        throws Exception
    {
        final URL rules = getClass().getResource( "test-call-param-rules.xml" );

         final String xml = "<?xml version='1.0' ?>"
                      + "<root><foo attr='long'><bar>short</bar><foobar><ping>tosh</ping></foobar></foo></root>";

        final CallParamTestObject testObject = new CallParamTestObject();

        final Digester digester =
            newLoader( createRules( rules ) ).setClassLoader( this.getClass().getClassLoader() ).newDigester();
        digester.push( testObject );
        digester.parse( new StringReader( xml ) );

        assertEquals( "long", testObject.getLeft(), "Incorrect left value" );
        assertEquals( "short", testObject.getMiddle(), "Incorrect middle value" );
        assertEquals( "", testObject.getRight(), "Incorrect right value" );
     }

    /**
     * Tests the DigesterLoader.createDigester(), with multiple
     * included rule sources: testrules.xml includes another rules XML
     * file, and also includes programmatically created rules.
     */
    @Test
    void testCreateDigester()
        throws Exception
    {
        final URL rules = getClass().getResource( "testrules.xml" );
        final URL input = getClass().getResource( "test.xml" );

        final Digester digester = newLoader( createRules( rules ) ).newDigester();
        digester.push( new ArrayList<>() );
        final Object root = digester.parse( input.openStream() );
        assertEquals( "[foo1 baz1 foo2, foo3 foo4]", root.toString() );
    }

    @Test
    void testFactoryCreateRule()
        throws Exception
    {
        final URL rules = getClass().getResource( "testfactory.xml" );
        final String xml = "<?xml version='1.0' ?><root one='good' two='bad' three='ugly'><foo/></root>";

        final Digester digester =
            newLoader( createRules( rules ) ).setClassLoader( this.getClass().getClassLoader() ).newDigester();
        digester.push( new ArrayList<ObjectCreationFactoryTestImpl>() );

        final Object obj = digester.parse( new StringReader( xml ) );
        assertInstanceOf( ArrayList.class, obj, "Unexpected object returned from DigesterLoader" );

        @SuppressWarnings("unchecked") // root is an ArrayList of TestObjectCreationFactory
        final
        ArrayList<ObjectCreationFactoryTestImpl> list = (ArrayList<ObjectCreationFactoryTestImpl>) obj;

        assertEquals( list.size(), 1, "List should contain only the factory object" );
        final ObjectCreationFactoryTestImpl factory = list.get( 0 );
        assertTrue( factory.called, "Object create not called(1)" );
        assertEquals( factory.attributes.getValue( "one" ), "good", "Attribute not passed (1)" );
        assertEquals( factory.attributes.getValue( "two" ), "bad", "Attribute not passed (2)" );
        assertEquals( factory.attributes.getValue( "three" ), "ugly", "Attribute not passed (3)" );
    }

    @Test
    void testFactoryIgnoreCreateRule()
    {
        final URL rules = getClass().getResource( "testfactoryignore.xml" );

        final String xml = "<?xml version='1.0' ?><root one='good' two='bad' three='ugly'><foo/></root>";
        try {
            newLoader(createRules(rules)).newDigester().parse(new StringReader(xml));
        } catch (final Exception e) {
            fail("This exception should have been ignored: " + e.getClass().getName());
        }
    }

    @Test
    void testFactoryNotIgnoreCreateRule()
    {
        final URL rules = getClass().getResource( "testfactorynoignore.xml" );

        final String xml = "<?xml version='1.0' ?><root one='good' two='bad' three='ugly'><foo/></root>";
        Digester digester = newLoader( createRules( rules ) ).newDigester();
        assertThrows( SAXParseException.class, () -> digester.parse( new StringReader( xml ) ), "Exception should have been propagated from create method." );
    }

    @Test
    void testInputSourceLoader() throws Exception {
        final String rulesXml = "<?xml version='1.0'?>"
                + "<digester-rules>"
                + " <pattern value='root'>"
                + "   <pattern value='foo'>"
                + "     <call-method-rule methodname='triple' paramcount='3'"
                + "            paramtypes='java.lang.String,java.lang.String,java.lang.String'/>"
                + "     <call-param-rule paramnumber='0' attrname='attr'/>"
                + "        <pattern value='bar'>"
                + "            <call-param-rule paramnumber='1' from-stack='false'/>"
                + "        </pattern>"
                + "        <pattern value='foobar'>"
                + "            <object-create-rule classname='java.lang.String'/>"
                + "            <pattern value='ping'>"
                + "                <call-param-rule paramnumber='2' from-stack='true'/>"
                + "            </pattern>"
                + "         </pattern>"
                + "   </pattern>"
                + " </pattern>"
                + "</digester-rules>";

        final String xml = "<?xml version='1.0' ?>"
                     + "<root><foo attr='long'><bar>short</bar><foobar><ping>tosh</ping></foobar></foo></root>";

        final CallParamTestObject testObject = new CallParamTestObject();

        final Digester digester = newLoader( createRules( rulesXml ) ).newDigester();
        digester.push( testObject );
        digester.parse( new StringReader( xml ) );

        assertEquals( "long", testObject.getLeft(), "Incorrect left value" );
        assertEquals( "short", testObject.getMiddle(), "Incorrect middle value" );
        assertEquals( "", testObject.getRight(), "Incorrect right value" );
    }

    /**
     * Tests the DigesterLoader.load(), with multiple included rule
     * sources: testrules.xml includes another rules XML file, and
     * also includes programmatically created rules.
     */
    @Test
    void testLoad1()
        throws Exception
    {
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL rules = getClass().getResource( "testrules.xml" );
        final URL input = getClass().getResource( "test.xml" );

        final Digester digester = newLoader( createRules( rules ) ).setClassLoader( classLoader ).newDigester();
        digester.push( new ArrayList<>() );

        final Object root = digester.parse( input );
        assertInstanceOf( ArrayList.class, root, "Unexpected object returned from DigesterLoader" );
        assertEquals( "[foo1 baz1 foo2, foo3 foo4]", root.toString() );

        @SuppressWarnings( "unchecked" )
        final
        // root is an ArrayList
        ArrayList<Object> al = (ArrayList<Object>) root;
        final Object obj = al.get( 0 );
        assertInstanceOf( ObjectTestImpl.class, obj, "Unexpected object returned from DigesterLoader" );
        final ObjectTestImpl to = (ObjectTestImpl) obj;
        assertEquals( 555L, to.getLongValue() );
        assertEquals( "foo", to.getMapValue( "test1" ) );
        assertEquals( "bar", to.getMapValue( "test2" ) );
    }

    /**
     * The same as testLoad1, exception the input file is passed to
     * DigesterLoader as an InputStream instead of a URL.
     */
    @Test
    void testLoad2()
        throws Exception
    {
        final URL rules = getClass().getResource( "testrules.xml" );
        final InputStream input = getClass().getResourceAsStream( "test.xml" );
        final Digester digester =
            newLoader( createRules( rules ) ).setClassLoader( this.getClass().getClassLoader() ).newDigester();
        digester.push( new ArrayList<>() );

        final ArrayList<Object> list = digester.parse( input );

        assertEquals( list.toString(), "[foo1 baz1 foo2, foo3 foo4]" );
        assertEquals( 2, list.size(), "Wrong number of classes created" );
        assertTrue( ( ( ObjectTestImpl ) list.get( 0 ) ).isPushed(), "Pushed first" );
        assertFalse( ( ( ObjectTestImpl ) list.get( 1 ) ).isPushed(), "Didn't push second" );
        assertEquals( "I am a property!", ( ( ObjectTestImpl ) list.get( 0 ) ).getProperty(), "Property was set properly" );
    }

    /**
     */
    @Test
    void testSetCustomProperties()
        throws Exception
    {
        final URL rules = this.getClass().getResource( "testPropertyAliasRules.xml" );
        final InputStream input =
            getClass().getClassLoader().getResource( "org/apache/commons/digester3/Test7.xml" ).openStream();

        final Digester digester =
            newLoader( createRules( rules ) ).setClassLoader( this.getClass().getClassLoader() ).newDigester();
        digester.push( new ArrayList<>() );

        final Object obj = digester.parse( input );

        assertInstanceOf( ArrayList.class, obj, "Unexpected object returned from DigesterLoader" );

        @SuppressWarnings("unchecked") // root is an ArrayList of Address
        final
        ArrayList<Address> root = (ArrayList<Address>) obj;

        assertEquals( 4, root.size(), "Wrong array size" );

        // note that the array is in popped order (rather than pushed)

        Address add = root.get( 0 );
        final Address addressOne = add;
        assertEquals( "New Street", addressOne.getStreet(), "(1) Street attribute" );
        assertEquals( "Las Vegas", addressOne.getCity(), "(1) City attribute" );
        assertEquals( "Nevada", addressOne.getState(), "(1) State attribute" );

        add = root.get( 1 );
        final Address addressTwo = add;
        assertEquals( "Old Street", addressTwo.getStreet(), "(2) Street attribute" );
        assertEquals( "Portland", addressTwo.getCity(), "(2) City attribute" );
        assertEquals( "Oregon", addressTwo.getState(), "(2) State attribute" );

        add = root.get( 2 );
        final Address addressThree = add;
        assertEquals( "4th Street", addressThree.getStreet(), "(3) Street attribute" );
        assertEquals( "Dayton", addressThree.getCity(), "(3) City attribute" );
        assertEquals( "US", addressThree.getState(), "(3) State attribute" );

        add = root.get( 3 );
        final Address addressFour = add;
        assertEquals( "6th Street", addressFour.getStreet(), "(4) Street attribute" );
        assertEquals( "Cleveland", addressFour.getCity(), "(4) City attribute" );
        assertEquals( "Ohio", addressFour.getState(), "(4) State attribute" );
    }
}
