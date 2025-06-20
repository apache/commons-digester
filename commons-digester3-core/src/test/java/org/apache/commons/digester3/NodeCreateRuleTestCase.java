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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.digester3.binder.AbstractRulesModule;
import org.apache.commons.digester3.binder.NodeCreateRuleProvider.NodeType;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>
 * Test case for the {@code NodeCreateRule}.
 */
public class NodeCreateRuleTestCase
{

    /**
     * Simple test XML document used in the tests.
     */
    protected static final String TEST_XML = "<?xml version='1.0'?><root>ROOT BODY<alpha>ALPHA BODY</alpha>"
        + "<beta>BETA BODY</beta><gamma>GAMMA BODY</gamma></root>";

    /**
     * Gets an appropriate InputStream for the specified test file (which must be inside our current package.
     *
     * @param name Name of the test file we want
     * @throws IOException if an input/output error occurs
     */
    protected InputStream getInputStream( final String name )
    {

        return this.getClass().getResourceAsStream( "/org/apache/commons/digester3/" + name );

    }

    /**
     * Tests whether attributes are correctly imported into the fragment, using the example in the Test1 XML file.
     */
    @Test
    void testAttributes()
        throws SAXException, IOException
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "employee" ).createNode().ofType( NodeType.DOCUMENT_FRAGMENT );
            }

        }).newDigester();

        final DocumentFragment fragment = digester.parse( getInputStream( "Test1.xml" ) );

        assertNotNull( fragment );
        assertEquals( 2, fragment.getChildNodes().getLength() );

        assertEquals( Node.ELEMENT_NODE, fragment.getFirstChild().getNodeType() );
        final Element address1 = (Element) fragment.getFirstChild();
        assertEquals( "address", address1.getNodeName() );
        assertEquals( 5, address1.getAttributes().getLength() );
        assertEquals( "home", address1.getAttribute( "type" ) );
        assertEquals( "Home Street", address1.getAttribute( "street" ) );
        assertEquals( "Home City", address1.getAttribute( "city" ) );
        assertEquals( "HS", address1.getAttribute( "state" ) );
        assertEquals( "HmZip", address1.getAttribute( "zipCode" ) );

        assertEquals( Node.ELEMENT_NODE, fragment.getLastChild().getNodeType() );
        final Element address2 = (Element) fragment.getLastChild();
        assertEquals( "address", address2.getNodeName() );
        assertEquals( 5, address2.getAttributes().getLength() );
        assertEquals( "office", address2.getAttribute( "type" ) );
        assertEquals( "Office Street", address2.getAttribute( "street" ) );
        assertEquals( "Office City", address2.getAttribute( "city" ) );
        assertEquals( "OS", address2.getAttribute( "state" ) );
        assertEquals( "OfZip", address2.getAttribute( "zipCode" ) );

    }

    /**
     * Tests simple fragment construction, using the {@link #TEST_XML} XML input data.
     */
    @Test
    void testDocumentFragment()
        throws SAXException, IOException
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root" ).createNode().ofType( NodeType.DOCUMENT_FRAGMENT );
            }

        }).newDigester();

        final DocumentFragment fragment = digester.parse( new StringReader( TEST_XML ) );

        assertNotNull( fragment );
        assertEquals( 4, fragment.getChildNodes().getLength() );

        final Node rootBody = fragment.getFirstChild();
        assertEquals( Node.TEXT_NODE, rootBody.getNodeType() );
        assertEquals( "ROOT BODY", rootBody.getNodeValue() );

        final Node alpha = fragment.getChildNodes().item( 1 );
        assertEquals( Node.ELEMENT_NODE, alpha.getNodeType() );
        assertEquals( "alpha", alpha.getNodeName() );
        assertNull( alpha.getLocalName() );
        assertNull( alpha.getNamespaceURI() );
        assertEquals( 1, alpha.getChildNodes().getLength() );
        assertEquals( "ALPHA BODY", alpha.getFirstChild().getNodeValue() );

        final Node beta = fragment.getChildNodes().item( 2 );
        assertEquals( Node.ELEMENT_NODE, beta.getNodeType() );
        assertEquals( "beta", beta.getNodeName() );
        assertNull( beta.getLocalName() );
        assertNull( beta.getNamespaceURI() );
        assertEquals( 1, beta.getChildNodes().getLength() );
        assertEquals( "BETA BODY", beta.getFirstChild().getNodeValue() );

        final Node gamma = fragment.getChildNodes().item( 3 );
        assertEquals( Node.ELEMENT_NODE, gamma.getNodeType() );
        assertEquals( "gamma", gamma.getNodeName() );
        assertNull( gamma.getLocalName() );
        assertNull( gamma.getNamespaceURI() );
        assertEquals( 1, gamma.getChildNodes().getLength() );
        assertEquals( "GAMMA BODY", gamma.getFirstChild().getNodeValue() );

    }

    /**
     * Tests simple element construction, using the {@link #TEST_XML} XML input data.
     */
    @Test
    void testElement()
        throws SAXException, IOException
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root/alpha" ).createNode();
            }

        }).newDigester();

        final Element element = digester.parse( new StringReader( TEST_XML ) );

        assertNotNull( element );
        assertEquals( "alpha", element.getNodeName() );
        assertNull( element.getLocalName() );
        assertNull( element.getNamespaceURI() );
        assertEquals( 1, element.getChildNodes().getLength() );
        assertEquals( "ALPHA BODY", element.getFirstChild().getNodeValue() );

    }

    /**
     * This unit test checks that text nodes are correctly created when XML entities are used. In particular, this
     * usually causes the XML parser to make multiple invocations of the characters(..) sax callback, rather than just
     * one.
     */
    @Test
    void testEntityText()
        throws Exception
    {
        final String TEST_XML2 = "<?xml version='1.0'?><root><alpha>&#65; &#65;</alpha></root>";

        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root/alpha" ).createNode();
            }

        })
        .newDigester();

        final Element element = digester.parse( new StringReader( TEST_XML2 ) );

        assertNotNull( element );
        assertEquals( "alpha", element.getNodeName() );
        assertNull( element.getLocalName() );
        assertNull( element.getNamespaceURI() );
        assertEquals( 1, element.getChildNodes().getLength() );
        assertEquals( "A A", element.getFirstChild().getNodeValue() );
    }

    /**
     * Tests whether the created fragment can be imported into an existing document.
     */
    @Test
    void testImport()
        throws SAXException, ParserConfigurationException, IOException
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root" ).createNode().ofType( NodeType.DOCUMENT_FRAGMENT );
            }

        })
        .newDigester();

        final DocumentFragment fragment = digester.parse( new StringReader( TEST_XML ) );

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.newDocument();
        final Node importedFragment = doc.importNode( fragment, true );
        doc.appendChild( doc.createElement( "root" ) );
        doc.getFirstChild().appendChild( importedFragment );

    }

    /**
     * Tests simple element construction, using the {@link #TEST_XML} XML input data.
     */
    @Test
    void testInvalidAttributeNode()
    {
        assertThrows( IllegalArgumentException.class, () -> new NodeCreateRule( Node.ATTRIBUTE_NODE ) );
    }

    @Test
    void testInvalidCDATANode()
    {
        assertThrows( IllegalArgumentException.class, () -> new NodeCreateRule( Node.CDATA_SECTION_NODE ) );
    }

    @Test
    void testInvalidCommentNode()
    {
        assertThrows( IllegalArgumentException.class, () -> new NodeCreateRule( Node.COMMENT_NODE ) );
    }

    @Test
    void testInvalidDocumentNode()
    {
        assertThrows( IllegalArgumentException.class, () -> new NodeCreateRule( Node.DOCUMENT_NODE ) );
    }

    @Test
    void testInvalidTypeNode()
    {
        assertThrows( IllegalArgumentException.class, () -> new NodeCreateRule( Node.DOCUMENT_TYPE_NODE ) );
    }

    @Test
    void testInvalidEntityNode()
    {
        assertThrows( IllegalArgumentException.class, () -> new NodeCreateRule( Node.ENTITY_NODE ) );
    }

    @Test
    void testInvalidReferenceNode()
    {
        assertThrows( IllegalArgumentException.class, () -> new NodeCreateRule( Node.ENTITY_REFERENCE_NODE ) );
    }

    @Test
    void testInvalidNotationNode()
    {
        assertThrows( IllegalArgumentException.class, () -> new NodeCreateRule( Node.NOTATION_NODE ) );
    }

    @Test
    void testInvalidProcessingInstructionNode()
    {
        assertThrows( IllegalArgumentException.class, () -> new NodeCreateRule( Node.PROCESSING_INSTRUCTION_NODE ) );
    }

    @Test
    void testInvalidTextNode()
    {
        assertThrows( IllegalArgumentException.class, () -> new NodeCreateRule( Node.TEXT_NODE ) );
    }

    /**
     * Tests whether namespaced attributes are handled correctly, using the example from the file Test10 XML file.
     */
    @Test
    void testNamespacedAttribute()
        throws SAXException, IOException
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "employee" ).createNode().ofType( NodeType.ELEMENT );
            }

        })
        .setNamespaceAware( true )
        .newDigester();

        final Element element = digester.parse( getInputStream( "Test10.xml" ) );

        assertNotNull( element );

        assertNotNull( element.getAttributeNodeNS( "http://commons.apache.org/digester/Bar", "test" ) );
        assertEquals( "MyTestAttribute", element.getAttributeNodeNS( "http://commons.apache.org/digester/Bar", "test" ).getNodeValue() );
        assertEquals( "test", element.getAttributeNodeNS( "http://commons.apache.org/digester/Bar", "test" ).getLocalName() );
        assertEquals( "bar", element.getAttributeNodeNS( "http://commons.apache.org/digester/Bar", "test" ).getPrefix() );
        assertEquals( "bar:test", element.getAttributeNodeNS( "http://commons.apache.org/digester/Bar", "test" ).getName() );

    }

    /**
     * Tests whether namespaces are handled correctly, using the example from the file Test3 XML file.
     */
    @Test
    void testNamespaces()
        throws SAXException, IOException
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "employee" ).createNode().ofType( NodeType.DOCUMENT_FRAGMENT );
            }

        })
        .setNamespaceAware( true )
        .newDigester();

        final DocumentFragment fragment = digester.parse( getInputStream( "Test3.xml" ) );

        assertNotNull( fragment );
        assertEquals( 2, fragment.getChildNodes().getLength() );

        assertEquals( Node.ELEMENT_NODE, fragment.getFirstChild().getNodeType() );
        final Element address1 = (Element) fragment.getFirstChild();
        assertEquals( "address", address1.getNodeName() );
        assertEquals( "http://commons.apache.org/digester/Bar", address1.getNamespaceURI() );
        assertEquals( "address", address1.getLocalName() );
        assertEquals( 5, address1.getAttributes().getLength() );
        assertEquals( "home", address1.getAttribute( "type" ) );
        assertEquals( "Home Street", address1.getAttribute( "street" ) );
        assertEquals( "Home City", address1.getAttribute( "city" ) );
        assertEquals( "HS", address1.getAttribute( "state" ) );
        assertEquals( "HmZip", address1.getAttribute( "zipCode" ) );

        assertEquals( Node.ELEMENT_NODE, fragment.getLastChild().getNodeType() );
        final Element address2 = (Element) fragment.getLastChild();
        assertEquals( "address", address2.getNodeName() );
        assertEquals( "http://commons.apache.org/digester/Bar", address2.getNamespaceURI() );
        assertEquals( "address", address2.getLocalName() );
        assertEquals( 5, address2.getAttributes().getLength() );
        assertEquals( "office", address2.getAttribute( "type" ) );
        assertEquals( "Office Street", address2.getAttribute( "street" ) );
        assertEquals( "Office City", address2.getAttribute( "city" ) );
        assertEquals( "OS", address2.getAttribute( "state" ) );
        assertEquals( "OfZip", address2.getAttribute( "zipCode" ) );

    }

    /**
     * Tests whether control is returned to digester after fragment construction.
     */
    @Test
    void testNested()
        throws SAXException, IOException
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "root" ).createObject().ofType( ArrayList.class );
                forPattern( "root/a/b" ).createNode().ofType( NodeType.DOCUMENT_FRAGMENT )
                    .then()
                    .setRoot( "add" );
                forPattern( "root/b" ).createObject().ofType( String.class )
                    .then()
                    .setRoot( "add" );
            }

        }).newDigester();

        final List<?> list = digester.parse( getInputStream( "Test4.xml" ) );

        assertNotNull( list );
        assertEquals( 2, list.size() );

        assertInstanceOf( DocumentFragment.class, list.get( 0 ) );
        final DocumentFragment fragment = (DocumentFragment) list.get( 0 );

        assertEquals( Node.ELEMENT_NODE, fragment.getFirstChild().getNodeType() );
        final Element a = (Element) fragment.getFirstChild();
        assertEquals( "a", a.getNodeName() );
        assertEquals( 1, a.getAttributes().getLength() );
        assertEquals( "THREE", a.getAttribute( "name" ) );

        assertInstanceOf( String.class, list.get( 1 ) );

    }

    /**
     * Tests whether non-namespaced attributes are handled correctly, using the example from the file Test11 XML file.
     */
    @Test
    void testNonNamespacedAttribute()
        throws SAXException, IOException
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "employee" ).createNode().ofType( NodeType.ELEMENT );
            }

        })
        .setNamespaceAware( true )
        .newDigester();

        final Element element = digester.parse( getInputStream( "Test10.xml" ) );

        assertNotNull( element );

        assertNotNull( element.getAttributeNode( "firstName" ) );
        assertEquals( "First Name", element.getAttributeNode( "firstName" ).getNodeValue() );
        assertEquals( "firstName", element.getAttributeNode( "firstName" ).getLocalName() );
        assertNull( element.getAttributeNode( "firstName" ).getPrefix() );
        assertEquals( "firstName", element.getAttributeNode( "firstName" ).getName() );

    }

}
