package org.apache.commons.digester3;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A rule implementation that creates a DOM {@link org.w3c.dom.Node Node} containing the XML at the element that matched
 * the rule. Two concrete types of nodes can be created by this rule:
 * <ul>
 * <li>the default is to create an {@link org.w3c.dom.Element Element} node. The created element will correspond to the
 * element that matched the rule, containing all XML content underneath that element.</li>
 * <li>alternatively, this rule can create nodes of type {@link org.w3c.dom.DocumentFragment DocumentFragment}, which
 * will contain only the XML content under the element the rule was trigged on.</li>
 * </ul>
 * The created node will be normalized, meaning it will not contain text nodes that only contain white space characters.
 * <p>
 * The created {@code Node} will be pushed on Digester's object stack when done. To use it in the context of
 * another DOM {@link org.w3c.dom.Document Document}, it must be imported first, using the Document method
 * {@link org.w3c.dom.Document#importNode(org.w3c.dom.Node, boolean) importNode()}.
 * </p>
 * <p>
 * <strong>Important Note:</strong> This is implemented by replacing the SAX {@link org.xml.sax.ContentHandler
 * ContentHandler} in the parser used by Digester, and resetting it when the matched element is closed. As a side
 * effect, rules that would match XML nodes under the element that matches a {@code NodeCreateRule} will never be
 * triggered by Digester, which usually is the behavior one would expect.
 * </p>
 * <p>
 * <strong>Note</strong> that the current implementation does not set the namespace prefixes in the exported nodes. The
 * (usually more important) namespace URIs are set, of course.
 * </p>
 *
 * @since 1.4
 */
public class NodeCreateRule
    extends Rule
{

    /**
     * The SAX content handler that does all the actual work of assembling the DOM node tree from the SAX events.
     */
    private final class NodeBuilder
        extends DefaultHandler
    {

        /**
         * The content handler used by Digester before it was set to this content handler.
         */
        protected ContentHandler oldContentHandler;

        /**
         * Depth of the current node, relative to the element where the content handler was put into action.
         */
        protected int depth;

        /**
         * A DOM Document used to create the various Node instances.
         */
        protected Document doc;

        /**
         * The DOM node that will be pushed on Digester's stack.
         */
        protected Node root;

        /**
         * The current top DOM mode.
         */
        protected Node top;

        /**
         * The text content of the current top DOM node.
         */
        protected StringBuilder topText = new StringBuilder();

        /**
         * Constructs a new instance.
         * <p>
         * Stores the content handler currently used by Digester so it can be reset when done, and initializes the DOM
         * objects needed to build the node.
         * </p>
         *
         * @param doc the document to use to create nodes
         * @param root the root node
         * @throws ParserConfigurationException if the DocumentBuilderFactory could not be instantiated
         * @throws SAXException if the XMLReader could not be instantiated by Digester (should not happen)
         */
        NodeBuilder( final Document doc, final Node root )
            throws ParserConfigurationException, SAXException
        {
            this.doc = doc;
            this.root = root;
            this.top = root;

            oldContentHandler = getDigester().getCustomContentHandler();
        }

        /**
         * Appends a {@link org.w3c.dom.Text Text} node to the current node if the content reported by the parser is not
         * purely whitespace.
         */
        private void addTextIfPresent()
            throws SAXException
        {
            if ( topText.length() > 0 )
            {
                final String str = topText.toString();
                topText.setLength( 0 );

                if ( !str.trim().isEmpty() )
                {
                    // The contained text is not *pure* whitespace, so create
                    // a text node to hold it. Note that the "untrimmed" text
                    // is stored in the node.
                    try
                    {
                        top.appendChild( doc.createTextNode( str ) );
                    }
                    catch ( final DOMException e )
                    {
                        throw new SAXException( e.getMessage() );
                    }
                }
            }
        }

        /**
         * Handle notification about text embedded within the current node.
         * <p>
         * An xml parser calls this when text is found. We need to ensure that this text gets attached to the new Node
         * we are creating - except in the case where the only text in the node is whitespace.
         * <p>
         * There is a catch, however. According to the sax specification, a parser does not need to pass all of the text
         * content of a node in one go; it can make multiple calls passing part of the data on each call. In particular,
         * when the body of an element includes xml entity-references, at least some parsers make a separate call to
         * this method to pass just the entity content.
         * <p>
         * In this method, we therefore just append the provided text to a "current text" buffer. When the element end
         * is found, or a child element is found then we can check whether we have all-whitespace. See method
         * addTextIfPresent.
         *
         * @param ch the characters from the XML document
         * @param start the start position in the array
         * @param length the number of characters to read from the array
         * @throws SAXException if the DOM implementation throws an exception
         */
        @Override
        public void characters( final char[] ch, final int start, final int length )
            throws SAXException
        {
            topText.append( ch, start, length );
        }

        /**
         * Checks whether control needs to be returned to Digester.
         *
         * @param namespaceURI the namespace URI
         * @param localName the local name
         * @param qName the qualified (prefixed) name
         * @throws SAXException if the DOM implementation throws an exception
         */
        @Override
        public void endElement( final String namespaceURI, final String localName, final String qName )
            throws SAXException
        {
            addTextIfPresent();

            try
            {
                if ( depth == 0 )
                {
                    getDigester().setCustomContentHandler( oldContentHandler );
                    getDigester().push( root );
                    getDigester().endElement( namespaceURI, localName, qName );
                }

                top = top.getParentNode();
                depth--;
            }
            catch ( final DOMException e )
            {
                throw new SAXException( e.getMessage() );
            }
        }

        /**
         * Adds a new {@link org.w3c.dom.ProcessingInstruction ProcessingInstruction} to the current node.
         *
         * @param target the processing instruction target
         * @param data the processing instruction data, or null if none was supplied
         * @throws SAXException if the DOM implementation throws an exception
         */
        @Override
        public void processingInstruction( final String target, final String data )
            throws SAXException
        {
            try
            {
                top.appendChild( doc.createProcessingInstruction( target, data ) );
            }
            catch ( final DOMException e )
            {
                throw new SAXException( e.getMessage() );
            }
        }

        /**
         * Adds a new child {@link org.w3c.dom.Element Element} to the current node.
         *
         * @param namespaceURI the namespace URI
         * @param localName the local name
         * @param qName the qualified (prefixed) name
         * @param atts the list of attributes
         * @throws SAXException if the DOM implementation throws an exception
         */
        @Override
        public void startElement( final String namespaceURI, final String localName, final String qName, final Attributes atts )
            throws SAXException
        {
            addTextIfPresent();

            try
            {
                final Node previousTop = top;
                if ( localName == null || localName.isEmpty() )
                {
                    top = doc.createElement( qName );
                }
                else
                {
                    top = doc.createElementNS( namespaceURI, localName );
                }
                for ( int i = 0; i < atts.getLength(); i++ )
                {
                    Attr attr = null;
                    if ( atts.getLocalName( i ) == null || atts.getLocalName( i ).isEmpty() )
                    {
                        attr = doc.createAttribute( atts.getQName( i ) );
                        attr.setNodeValue( atts.getValue( i ) );
                        ( (Element) top ).setAttributeNode( attr );
                    }
                    else
                    {
                        attr = doc.createAttributeNS( atts.getURI( i ), atts.getLocalName( i ) );
                        attr.setNodeValue( atts.getValue( i ) );
                        ( (Element) top ).setAttributeNodeNS( attr );
                    }
                }
                previousTop.appendChild( top );
                depth++;
            }
            catch ( final DOMException e )
            {
                throw new SAXException( e.getMessage() );
            }
        }
    }

    /**
     * The JAXP {@code DocumentBuilder} to use.
     */
    private final DocumentBuilder documentBuilder;

    /**
     * The type of the node that should be created. Must be one of the constants defined in {@link org.w3c.dom.Node
     * Node}, but currently only {@link org.w3c.dom.Node#ELEMENT_NODE Node.ELEMENT_NODE} and
     * {@link org.w3c.dom.Node#DOCUMENT_FRAGMENT_NODE Node.DOCUMENT_FRAGMENT_NODE} are allowed values.
     */
    private int nodeType = Node.ELEMENT_NODE;

    /**
     * Default constructor. Creates an instance of this rule that will create a DOM {@link org.w3c.dom.Element Element}.
     *
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the
     *         configuration requested.
     * @see DocumentBuilderFactory#newDocumentBuilder()
     */
    public NodeCreateRule()
        throws ParserConfigurationException
    {
        this( Node.ELEMENT_NODE );
    }

    /**
     * Constructs a new instance. Creates an instance of this rule that will create a DOM {@link org.w3c.dom.Element Element}, but
     * lets you specify the JAXP {@code DocumentBuilder} that should be used when constructing the node tree.
     *
     * @param documentBuilder the JAXP {@code DocumentBuilder} to use
     */
    public NodeCreateRule( final DocumentBuilder documentBuilder )
    {
        this( Node.ELEMENT_NODE, documentBuilder );
    }

    /**
     * Constructs a new instance. Creates an instance of this rule that will create either a DOM {@link org.w3c.dom.Element Element}
     * or a DOM {@link org.w3c.dom.DocumentFragment DocumentFragment}, depending on the value of the
     * {@code nodeType} parameter.
     *
     * @param nodeType the type of node to create, which can be either {@link org.w3c.dom.Node#ELEMENT_NODE
     *            Node.ELEMENT_NODE} or {@link org.w3c.dom.Node#DOCUMENT_FRAGMENT_NODE Node.DOCUMENT_FRAGMENT_NODE}
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the
     *         configuration requested.
     * @see DocumentBuilderFactory#newDocumentBuilder()
     */
    public NodeCreateRule( final int nodeType )
        throws ParserConfigurationException
    {
        this( nodeType, DocumentBuilderFactory.newInstance().newDocumentBuilder() );
    }

    /**
     * Constructs a new instance. Creates an instance of this rule that will create either a DOM {@link org.w3c.dom.Element Element}
     * or a DOM {@link org.w3c.dom.DocumentFragment DocumentFragment}, depending on the value of the
     * {@code nodeType} parameter. This constructor lets you specify the JAXP {@code DocumentBuilder} that
     * should be used when constructing the node tree.
     *
     * @param nodeType the type of node to create, which can be either {@link org.w3c.dom.Node#ELEMENT_NODE
     *            Node.ELEMENT_NODE} or {@link org.w3c.dom.Node#DOCUMENT_FRAGMENT_NODE Node.DOCUMENT_FRAGMENT_NODE}
     * @param documentBuilder the JAXP {@code DocumentBuilder} to use
     */
    public NodeCreateRule( final int nodeType, final DocumentBuilder documentBuilder )
    {
        if ( !( nodeType == Node.DOCUMENT_FRAGMENT_NODE || nodeType == Node.ELEMENT_NODE ) )
        {
            throw new IllegalArgumentException( "Can only create nodes of type DocumentFragment and Element" );
        }
        this.nodeType = nodeType;
        this.documentBuilder = documentBuilder;
    }

    /**
     * When this method fires, the digester is told to forward all SAX ContentHandler events to the builder object,
     * resulting in a DOM being built instead of normal digester rule-handling occurring. When the end of the current
     * xml element is encountered, the original content handler is restored (expected to be NULL, allowing normal
     * Digester operations to continue).
     *
     * @param namespaceURI the namespace URI of the matching element, or an empty string if the parser is not namespace
     *            aware or the element has no namespace
     * @param name the local name if the parser is namespace aware, or just the element name otherwise
     * @param attributes The attribute list of this element
     * @throws Exception indicates a JAXP configuration problem
     */
    @Override
    public void begin( final String namespaceURI, final String name, final Attributes attributes )
        throws Exception
    {
        final Document doc = documentBuilder.newDocument();
        NodeBuilder builder;
        if ( nodeType == Node.ELEMENT_NODE )
        {
            Element element;
            if ( getDigester().getNamespaceAware() )
            {
                element = doc.createElementNS( namespaceURI, name );
                for ( int i = 0; i < attributes.getLength(); i++ )
                {
                    element.setAttributeNS( attributes.getURI( i ), attributes.getQName( i ),
                                            attributes.getValue( i ) );
                }
            }
            else
            {
                element = doc.createElement( name );
                for ( int i = 0; i < attributes.getLength(); i++ )
                {
                    element.setAttribute( attributes.getQName( i ), attributes.getValue( i ) );
                }
            }
            builder = new NodeBuilder( doc, element );
        }
        else
        {
            builder = new NodeBuilder( doc, doc.createDocumentFragment() );
        }
        // the NodeBuilder constructor has already saved the original
        // value of the digester's custom content handler (expected to
        // be null, but we save it just in case). So now we just
        // need to tell the digester to forward events to the builder.
        getDigester().setCustomContentHandler( builder );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void end( final String namespace, final String name )
        throws Exception
    {
        getDigester().pop();
    }

}
