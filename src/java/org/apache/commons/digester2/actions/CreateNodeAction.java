/* $Id$
 *
 * Copyright 2002-2005 The Apache Software Foundation.
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


package org.apache.commons.digester2.actions;


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
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


import org.apache.commons.digester2.Context;
import org.apache.commons.digester2.AbstractAction;
import org.apache.commons.digester2.ParseException;
import org.apache.commons.digester2.SAXHandler;

/**
 * <p>An Action that creates a DOM {@link org.w3c.dom.Node Node}
 * containing the XML at the element that matched the action.</p>
 *
 * <p>The node can then be passed as a parameter to another object on
 * the stack (usually the previous one) using SetNextAction, CallMethodAction
 * or a similar Action.</p>
 *
 * <p>Two concrete types of nodes can be created by this action:
 * <ul>
 *   <li>the default is to create an {@link org.w3c.dom.Element Element} node.
 *   The created element will correspond to the element that matched the action,
 *   containing all XML content underneath that element.</li>
 *   <li>alternatively, this action can create nodes of type
 *   {@link org.w3c.dom.DocumentFragment DocumentFragment}, which will contain
 *   only the XML content under the element the action was trigged on.</li>
 * </ul>
 * The created node will be normalized, meaning it will not contain text nodes
 * that only contain white space characters.
 * </p>
 *
 * <p>The created <code>Node</code> will be pushed on Digester's object stack
 * when done. To use it in the context of another DOM
 * {@link org.w3c.dom.Document Document}, it must be imported first, using the
 * Document method
 * {@link org.w3c.dom.Document#importNode(org.w3c.dom.Node, boolean) importNode()}.
 * </p>
 *
 * <p><strong>Important Note:</strong>While processing the content of this
 * xml element (both text and child elements) all other Digester actions are
 * disabled and will not be invoked.</p>
 *
 * <p><strong>Note</strong> that the current implementation does not set the
 * namespace prefixes in the exported nodes. The (usually more important)
 * namespace URIs are set, of course.</p>
 */

public class CreateNodeAction extends AbstractAction {

    // ----------------------------------------------------------
    // Inner Classes
    // ----------------------------------------------------------

    /**
     * The SAX content handler that does all the actual work of assembling the
     * DOM node tree from the SAX events. After this Action's
     */
    private class NodeBuilder extends DefaultHandler {

        // -------------------------------------------------
        // Instance Variables
        //
        // Note that these change during a parse, which is normally
        // forbidden for an Action instance. However a different
        // NodeBuilder instance is created each time a CreateNodeAction's
        // begin method fires, so there are no thread-safety issues in
        // this case.
        // -------------------------------------------------

        /**
         * The parse context currently being used.
         */
        protected Context context;

        /**
         * The SAXHandler being used to process SAX events.
         */
        protected SAXHandler saxHandler;

        /**
         * The contentHandler that the SAXHandler was forwarding to at the
         * time this action fired. Expected to be null, as the only Action
         * that currently sets a custom contentHandler is this one, and by
         * definition this action cannot be invoked recursively. But it's
         * good style to save/restore the old value anyway.
         */
        protected ContentHandler oldContentHandler;

        /**
         * Depth of the current node, relative to the element where the content
         * handler was put into action.
         */
        protected int depth = 0;

        /**
         * A DOM Document used to create the various Node instances.
         */
        protected Document doc = null;

        /**
         * The DOM node that will be pushed on Digester's stack.
         */
        protected Node root = null;

        /**
         * The current top DOM mode.
         */
        protected Node top = null;

        // -------------------------------------------------------
        // Constructors
        // -------------------------------------------------------

        /**
         * Creates an object which can build a DOM tree from a series
         * of SAX events.
         *
         * @param context is the object which holds the current parse context.
         *  It also provides a way to access the current saxHandler object
         *  which receives sax events from the xml parser.
         */
        public NodeBuilder(Context context) throws SAXException {
            this.context = context;
        }

        /**
         * Prepare this object to receive sax events, and configure the
         * current saxHandler to forward those events to this object.
         *
         * @throws ParserConfigurationException if the DocumentBuilderFactory
         *   could not be instantiated
         */
        public void init(String namespaceURI, String name, Attributes attributes) {
            saxHandler = context.getSAXHandler();
            oldContentHandler = context.getContentHandler();

            // Access the documentBuilder in the enclosing class to build a
            // Document object that we just use as a factory for creating
            // elements.
            doc = documentBuilder.newDocument();

            if (nodeType == Node.ELEMENT_NODE) {
                // create an element object to represent the "triggering"
                // element that caused all this to start, then copy all
                // the xml attributes over to the new node.
                Element element;
                if (useNamespaces) {
                    element = doc.createElementNS(namespaceURI, name);
                    for (int i = 0; i < attributes.getLength(); i++) {
                        element.setAttributeNS(attributes.getURI(i),
                                               attributes.getLocalName(i),
                                               attributes.getValue(i));
                    }
                } else {
                    element = doc.createElement(name);
                    for (int i = 0; i < attributes.getLength(); i++) {
                        element.setAttribute(
                            attributes.getLocalName(i),
                            attributes.getValue(i));
                    }
                }

                root = element;
            } else {
                // document fragments don't have any place to store the
                // attribute info..
                root = doc.createDocumentFragment();
            }

            // mark which element we are currently working with when sax
            // events are received..
            top = root;

            // Tell the SAXHandler to forward events it receives from the
            // sax parser to the methods on this object
            context.setContentHandler(this);
        }

        // ---------------------------------------------
        // ContentHandler Methods
        // ---------------------------------------------

        /**
         * Appends a {@link org.w3c.dom.Text Text} node to the current node.
         *
         * @param ch the characters from the XML document
         * @param start the start position in the array
         * @param length the number of characters to read from the array
         * @throws SAXException if the DOM implementation throws an exception
         */
        public void characters(char[] ch, int start, int length)
        throws SAXException {
            try {
                String str = new String(ch, start, length);
                if (str.trim().length() > 0) {
                    top.appendChild(doc.createTextNode(str));
                }
            } catch (DOMException e) {
                throw new SAXException(e.getMessage());
            }
        }

        /**
         * Adds a new
         * {@link org.w3c.dom.ProcessingInstruction ProcessingInstruction} to
         * the current node.
         *
         * @param target the processing instruction target
         * @param data the processing instruction data, or null if none was
         *   supplied
         * @throws SAXException if the DOM implementation throws an exception
         */
        public void processingInstruction(String target, String data)
        throws SAXException {
            try {
                top.appendChild(doc.createProcessingInstruction(target, data));
            } catch (DOMException e) {
                throw new SAXException(e.getMessage());
            }
        }

        /**
         * Adds a new child {@link org.w3c.dom.Element Element} to the current
         * node.
         *
         * @param namespaceURI the namespace URI
         * @param localName the local name
         * @param qName the qualified (prefixed) name
         * @param atts the list of attributes
         * @throws SAXException if the DOM implementation throws an exception
         */
        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes attrs)
            throws SAXException {

            try {
                Element element;

                if (useNamespaces) {
                    element = doc.createElementNS(namespaceURI, localName);
                    for (int i = 0; i < attrs.getLength(); i++) {
                        element.setAttributeNS(
                            attrs.getURI(i),
                            attrs.getLocalName(i),
                            attrs.getValue(i));
                    }
                } else {
                    element = doc.createElement(localName);
                    for (int i = 0; i < attrs.getLength(); i++) {
                        element.setAttribute(
                            attrs.getLocalName(i),
                            attrs.getValue(i));
                    }
                }

                top.appendChild(element);
                top = element;
                depth++;
            } catch (DOMException e) {
                throw new SAXException(e.getMessage());
            }
        }

        /**
         * Checks whether control needs to be returned to Digester.
         *
         * @param namespaceURI the namespace URI
         * @param localName the local name
         * @param qName the qualified (prefixed) name
         * @throws SAXException if the DOM implementation throws an exception
         */
        public void endElement(String namespaceURI, String localName,
                               String qName)
            throws SAXException {

            try {
                if (depth == 0) {
                    // Restore sax event handler.
                    context.setContentHandler(oldContentHandler);

                    // push built node onto stack so that other actions can
                    // access it. Note that this node gets popped in the
                    // end method of the parent NodeCreateAction, so it won't
                    // be there very long...
                    context.push(root);

                    // and manually fire the actions that would have been fired
                    // had the normal SAXHandler been receiving parse events
                    // instead of this temporary handler.
                    saxHandler.endElement(namespaceURI, localName, qName);
                }

                top = top.getParentNode();
                depth--;
            } catch (DOMException e) {
                throw new SAXException(e.getMessage());
            }
        }
    }

    // -----------------------------------------------------
    // Instance Variables
    // -----------------------------------------------------

    /**
     * A <code>DocumentBuilder</code> to use as a factory for creating
     * Document objects that are themselves used as factories for creating
     * elements, attributes, etc.
     */
    private DocumentBuilder documentBuilder;

    /**
     * The type of the node that should be created. Must be one of the
     * constants defined in {@link org.w3c.dom.Node Node}, but currently only
     * {@link org.w3c.dom.Node#ELEMENT_NODE Node.ELEMENT_NODE} and
     * {@link org.w3c.dom.Node#DOCUMENT_FRAGMENT_NODE Node.DOCUMENT_FRAGMENT_NODE}
     * are allowed values.
     */
    private int nodeType;

    /**
     * Specifies whether elements and attributes should be created using
     * the namespace-aware methods (eg createElementNS, createAttributeNS)
     * or not (createElement, createAttribute).
     */
    private boolean useNamespaces;

    // -----------------------------------------------------------
    // CreateNodeAction Constructors
    // -----------------------------------------------------------

    /**
     * Default constructor. Creates an instance of this action that will
     * create a DOM {@link org.w3c.dom.Element Element}, and will create
     * namespace-aware elements and attributes.
     */
    public CreateNodeAction() throws ParserConfigurationException {
        this(Node.ELEMENT_NODE, true);
    }

    /**
     * Default constructor. Creates an instance of this action that will
     * create a DOM {@link org.w3c.dom.Element Element}.
     *
     * @param useNamespaces indicates whether or not the generated DOM
     * nodes should be namespace-aware (created with createElementNS etc)
     * or not (created with createElement etc).
     */
    public CreateNodeAction(boolean useNamespaces)
    throws ParserConfigurationException {
        this(Node.ELEMENT_NODE, useNamespaces);
    }

    /**
     * Constructor. Creates an instance of this action that will create either a
     * DOM {@link org.w3c.dom.Element Element} or a DOM
     * {@link org.w3c.dom.DocumentFragment DocumentFragment}, depending on the
     * value of the <code>nodeType</code> parameter.
     *
     * @param nodeType the type of node to create, which can be either
     *   {@link org.w3c.dom.Node#ELEMENT_NODE Node.ELEMENT_NODE} or
     *   {@link org.w3c.dom.Node#DOCUMENT_FRAGMENT_NODE Node.DOCUMENT_FRAGMENT_NODE}
     * @throws IllegalArgumentException if the node type is not supported
     */
    public CreateNodeAction(int nodeType) throws ParserConfigurationException {
        this(nodeType, true);
    }

    /**
     * Constructor. Creates an instance of this action that will create either a
     * DOM {@link org.w3c.dom.Element Element} or a DOM
     * {@link org.w3c.dom.DocumentFragment DocumentFragment}, depending on the
     * value of the <code>nodeType</code> parameter.
     *
     * @param nodeType the type of node to create, which can be either
     *   {@link org.w3c.dom.Node#ELEMENT_NODE Node.ELEMENT_NODE} or
     *   {@link org.w3c.dom.Node#DOCUMENT_FRAGMENT_NODE Node.DOCUMENT_FRAGMENT_NODE}
     * @throws IllegalArgumentException if the node type is not supported
     */
    public CreateNodeAction(int nodeType, boolean useNamespaces)
    throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(useNamespaces);
        init(nodeType, useNamespaces, dbf.newDocumentBuilder());
    }

    /**
     * Constructor. Creates an instance of this action that will create a DOM
     * {@link org.w3c.dom.Element Element}, but lets you specify the JAXP
     * <code>DocumentBuilder</code> that should be used when constructing the
     * node tree.
     *
     * If the documentBuilder is namespace-aware, then elements and attributes
     * are created using namespaces (createElementNS etc). If the documentBuilder
     * is not namespace-aware then they are created using createElement etc.
     *
     * @param documentBuilder the JAXP <code>DocumentBuilder</code> to use
     */
    public CreateNodeAction(DocumentBuilder documentBuilder) {
        init(Node.ELEMENT_NODE, documentBuilder.isNamespaceAware(), documentBuilder);
    }

    /**
     * Constructor. Creates an instance of this action that will create a DOM
     * {@link org.w3c.dom.Element Element}, but lets you specify the JAXP
     * <code>DocumentBuilder</code> that should be used when constructing the
     * node tree.
     *
     * @param documentBuilder the JAXP <code>DocumentBuilder</code> to use
     */
    public CreateNodeAction(int nodeType, DocumentBuilder documentBuilder) {
        init(nodeType, documentBuilder.isNamespaceAware(), documentBuilder);
    }

    /**
     * Constructor. Creates an instance of this action that will create either a
     * DOM {@link org.w3c.dom.Element Element} or a DOM
     * {@link org.w3c.dom.DocumentFragment DocumentFragment}, depending on the
     * value of the <code>nodeType</code> parameter. This constructor lets you
     * specify the JAXP <code>DocumentBuilder</code> that should be used when
     * constructing the node tree.
     *
     * @param nodeType the type of node to create, which can be either
     *   {@link org.w3c.dom.Node#ELEMENT_NODE Node.ELEMENT_NODE} or
     *   {@link org.w3c.dom.Node#DOCUMENT_FRAGMENT_NODE Node.DOCUMENT_FRAGMENT_NODE}
     * @param documentBuilder the JAXP <code>DocumentBuilder</code> to use
     * @throws IllegalArgumentException if the node type is not supported
     */
    public CreateNodeAction(
    int nodeType,
    boolean useNamespaces,
    DocumentBuilder documentBuilder) {
        init(nodeType, useNamespaces, documentBuilder);
    }

    /**
     * Helper method for the constructors.
     */
    private void init(
    int nodeType,
    boolean useNamespaces,
    DocumentBuilder documentBuilder) {
        if (!((nodeType == Node.DOCUMENT_FRAGMENT_NODE) ||
              (nodeType == Node.ELEMENT_NODE))) {
            throw new IllegalArgumentException(
                "Can only create nodes of type DocumentFragment and Element");
        }

        this.nodeType = nodeType;
        this.useNamespaces = useNamespaces;
        this.documentBuilder = documentBuilder;
    }

    // -----------------------------------------------------------
    // Action Methods
    // -----------------------------------------------------------

    /**
     * Implemented to replace the content handler currently in use by a
     * {@link NodeBuilder NodeCreateAction.NodeBuilder}.
     *
     * @param namespaceURI the namespace URI of the matching element, or an
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just
     *   the element name otherwise
     * @param attributes The attribute list of this element
     * @throws Exception indicates a JAXP configuration problem
     */
    public void begin(
    Context context,
    String namespaceURI, String name, Attributes attributes)
    throws ParseException {
        try {
            NodeBuilder builder = new NodeBuilder(context);
            builder.init(namespaceURI, name, attributes);
        } catch(SAXException ex) {
            throw new ParseException(ex);
        }
    }

    /**
     * <p>Pop the Node off the top of the stack.</p>
     */
    public void end(Context context, String namespaceURI, String name)
    throws ParseException {
        // Note that while the begin method sets up a custom contenthandler,
        // it is not this method that undoes that work; the SAXHandler is
        // unable to call this method until redirection of sax events has
        // been cancelled!

        Object top = context.pop();
    }
}
