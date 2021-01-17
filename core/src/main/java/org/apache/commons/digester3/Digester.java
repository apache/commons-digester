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

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>
 * A <strong>Digester</strong> processes an XML input stream by matching a series of element nesting patterns to execute
 * Rules that have been added prior to the start of parsing.
 * </p>
 * <p>
 * See the <a href="package-summary.html#package_description">Digester Developer Guide</a> for more information.
 * </p>
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - A single Digester instance may only be used within the context of a single
 * thread at a time, and a call to <code>parse()</code> must be completed before another can be initiated even from the
 * same thread.
 * </p>
 * <p>
 * A Digester instance should not be used for parsing more than one input document. The problem is that the Digester
 * class has quite a few member variables whose values "evolve" as SAX events are received during a parse. When reusing
 * the Digester instance, all these members must be reset back to their initial states before the second parse begins.
 * The "clear()" method makes a stab at resetting these, but it is actually rather a difficult problem. If you are
 * determined to reuse Digester instances, then at the least you should call the clear() method before each parse, and
 * must call it if the Digester parse terminates due to an exception during a parse.
 * </p>
 * <p>
 * <strong>LEGACY IMPLEMENTATION NOTE</strong> - When using the legacy XML schema support (instead of using the
 * {@link Schema} class), a bug in Xerces 2.0.2 prevents the support of XML schema. You need Xerces 2.1/2.3 and up to
 * make this class work with the legacy XML schema support.
 * </p>
 * <p>
 * This package was inspired by the <code>XmlMapper</code> class that was part of Tomcat 3.0 and 3.1, but is organized
 * somewhat differently.
 * </p>
 */
public class Digester
    extends DefaultHandler
{

    // --------------------------------------------------------- Constructors

    /**
     * Construct a new Digester with default properties.
     */
    public Digester()
    {
    }

    /**
     * Construct a new Digester, allowing a SAXParser to be passed in. This allows Digester to be used in environments
     * which are unfriendly to JAXP1.1 (such as WebLogic 6.0). This may help in places where you are able to load JAXP
     * 1.1 classes yourself.
     *
     * @param parser The SAXParser used to parse XML streams
     */
    public Digester( final SAXParser parser )
    {
        this.parser = parser;
    }

    /**
     * Construct a new Digester, allowing an XMLReader to be passed in. This allows Digester to be used in environments
     * which are unfriendly to JAXP1.1 (such as WebLogic 6.0). Note that if you use this option you have to configure
     * namespace and validation support yourself, as these properties only affect the SAXParser and emtpy constructor.
     *
     * @param reader The XMLReader used to parse XML streams
     */
    public Digester( final XMLReader reader )
    {
        this.reader = reader;
    }

    // --------------------------------------------------- Instance Variables

    /**
     * The body text of the current element.
     */
    private StringBuilder bodyText = new StringBuilder();

    /**
     * The stack of body text string buffers for surrounding elements.
     */
    private final Stack<StringBuilder> bodyTexts = new Stack<StringBuilder>();

    /**
     * Stack whose elements are List objects, each containing a list of Rule objects as returned from Rules.getMatch().
     * As each xml element in the input is entered, the matching rules are pushed onto this stack. After the end tag is
     * reached, the matches are popped again. The depth of is stack is therefore exactly the same as the current
     * "nesting" level of the input xml.
     *
     * @since 1.6
     */
    private final Stack<List<Rule>> matches = new Stack<List<Rule>>();

    /**
     * The class loader to use for instantiating application objects. If not specified, the context class loader, or the
     * class loader used to load Digester itself, is used, based on the value of the <code>useContextClassLoader</code>
     * variable.
     */
    private ClassLoader classLoader;

    /**
     * Has this Digester been configured yet.
     */
    private boolean configured;

    /**
     * The EntityResolver used by the SAX parser. By default it use this class
     */
    private EntityResolver entityResolver;

    /**
     * The URLs of entityValidator that have been registered, keyed by the public identifier that corresponds.
     */
    private final HashMap<String, URL> entityValidator = new HashMap<String, URL>();

    /**
     * The application-supplied error handler that is notified when parsing warnings, errors, or fatal errors occur.
     */
    private ErrorHandler errorHandler;

    /**
     * The SAXParserFactory that is created the first time we need it.
     */
    private SAXParserFactory factory;

    /**
     * The Locator associated with our parser.
     */
    private Locator locator;

    /**
     * The current match pattern for nested element processing.
     */
    private String match = "";

    /**
     * Do we want a "namespace aware" parser.
     */
    private boolean namespaceAware;

    /**
     * The executor service to run asynchronous parse method.
     * @since 3.1
     */
    private ExecutorService executorService;

    /**
     * Registered namespaces we are currently processing. The key is the namespace prefix that was declared in the
     * document. The value is an Stack of the namespace URIs this prefix has been mapped to -- the top Stack element is
     * the most current one. (This architecture is required because documents can declare nested uses of the same prefix
     * for different Namespace URIs).
     */
    private final HashMap<String, Stack<String>> namespaces = new HashMap<String, Stack<String>>();

    /**
     * Do we want a "XInclude aware" parser.
     */
    private boolean xincludeAware;

    /**
     * The parameters stack being utilized by CallMethodRule and CallParamRule rules.
     *
     * @since 2.0
     */
    private final Stack<Object[]> params = new Stack<Object[]>();

    /**
     * The SAXParser we will use to parse the input stream.
     */
    private SAXParser parser;

    /**
     * The public identifier of the DTD we are currently parsing under (if any).
     */
    private String publicId;

    /**
     * The XMLReader used to parse digester rules.
     */
    private XMLReader reader;

    /**
     * The "root" element of the stack (in other words, the last object that was popped.
     */
    private Object root;

    /**
     * The <code>Rules</code> implementation containing our collection of <code>Rule</code> instances and associated
     * matching policy. If not established before the first rule is added, a default implementation will be provided.
     */
    private Rules rules;

    /**
     * The XML schema to use for validating an XML instance.
     *
     * @since 2.0
     */
    private Schema schema;

    /**
     * The object stack being constructed.
     */
    private final Stack<Object> stack = new Stack<Object>();

    /**
     * Do we want to use the Context ClassLoader when loading classes for instantiating new objects. Default is
     * <code>true</code>.
     */
    private boolean useContextClassLoader = true;

    /**
     * Do we want to use a validating parser.
     */
    private boolean validating;

    /**
     * The Log to which most logging calls will be made.
     */
    private Log log = LogFactory.getLog( "org.apache.commons.digester3.Digester" );

    /**
     * The Log to which all SAX event related logging calls will be made.
     */
    private Log saxLog = LogFactory.getLog( "org.apache.commons.digester3.Digester.sax" );

    /**
     * The schema language supported. By default, we use this one.
     */
    protected static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    /**
     * An optional class that substitutes values in attributes and body text. This may be null and so a null check is
     * always required before use.
     */
    private Substitutor substitutor;

    /** Stacks used for interrule communication, indexed by name String */
    private final HashMap<String, Stack<Object>> stacksByName = new HashMap<String, Stack<Object>>();

    /**
     * If not null, then calls by the parser to this object's characters, startElement, endElement and
     * processingInstruction methods are forwarded to the specified object. This is intended to allow rules to
     * temporarily "take control" of the sax events. In particular, this is used by NodeCreateRule.
     * <p>
     * See setCustomContentHandler.
     */
    private ContentHandler customContentHandler;

    /**
     * Object which will receive callbacks for every pop/push action on the default stack or named stacks.
     */
    private StackAction stackAction;

    // ------------------------------------------------------------- Properties

    /**
     * Return the currently mapped namespace URI for the specified prefix, if any; otherwise return <code>null</code>.
     * These mappings come and go dynamically as the document is parsed.
     *
     * @param prefix Prefix to look up
     * @return the currently mapped namespace URI for the specified prefix
     */
    public String findNamespaceURI( final String prefix )
    {
        final Stack<String> nsStack = namespaces.get( prefix );
        if ( nsStack == null )
        {
            return null;
        }
        try
        {
            return ( nsStack.peek() );
        }
        catch ( final EmptyStackException e )
        {
            return null;
        }
    }

    /**
     * Return the class loader to be used for instantiating application objects when required. This is determined based
     * upon the following rules:
     * <ul>
     * <li>The class loader set by <code>setClassLoader()</code>, if any</li>
     * <li>The thread context class loader, if it exists and the <code>useContextClassLoader</code> property is set to
     * true</li>
     * <li>The class loader used to load the Digester class itself.
     * </ul>
     *
     * @return the class loader to be used for instantiating application objects.
     */
    public ClassLoader getClassLoader()
    {
        if ( this.classLoader != null )
        {
            return ( this.classLoader );
        }
        if ( this.useContextClassLoader )
        {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if ( classLoader != null )
            {
                return ( classLoader );
            }
        }
        return ( this.getClass().getClassLoader() );
    }

    /**
     * Set the class loader to be used for instantiating application objects when required.
     *
     * @param classLoader The new class loader to use, or <code>null</code> to revert to the standard rules
     */
    public void setClassLoader( final ClassLoader classLoader )
    {
        this.classLoader = classLoader;
    }

    /**
     * Return the current depth of the element stack.
     *
     * @return the current depth of the element stack.
     */
    public int getCount()
    {
        return ( stack.size() );
    }

    /**
     * Return the name of the XML element that is currently being processed.
     *
     * @return the name of the XML element that is currently being processed.
     */
    public String getCurrentElementName()
    {
        String elementName = match;
        final int lastSlash = elementName.lastIndexOf( '/' );
        if ( lastSlash >= 0 )
        {
            elementName = elementName.substring( lastSlash + 1 );
        }
        return ( elementName );
    }

    /**
     * Return the error handler for this Digester.
     *
     * @return the error handler for this Digester.
     */
    public ErrorHandler getErrorHandler()
    {
        return ( this.errorHandler );
    }

    /**
     * Set the error handler for this Digester.
     *
     * @param errorHandler The new error handler
     */
    public void setErrorHandler( final ErrorHandler errorHandler )
    {
        this.errorHandler = errorHandler;
    }

    /**
     * Return the SAXParserFactory we will use, creating one if necessary.
     *
     * @return the SAXParserFactory we will use, creating one if necessary.
     */
    public SAXParserFactory getFactory()
    {
        if ( factory == null )
        {
            factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware( namespaceAware );
            factory.setXIncludeAware( xincludeAware );
            factory.setValidating( validating );
            factory.setSchema( schema );
        }
        return ( factory );
    }

    /**
     * Returns a flag indicating whether the requested feature is supported by the underlying implementation of
     * <code>org.xml.sax.XMLReader</code>. See <a href="http://www.saxproject.org">the saxproject website</a> for
     * information about the standard SAX2 feature flags.
     *
     * @param feature Name of the feature to inquire about
     * @return true, if the requested feature is supported by the underlying implementation of
     *         <code>org.xml.sax.XMLReader</code>, false otherwise
     * @throws ParserConfigurationException if a parser configuration error occurs
     * @throws SAXNotRecognizedException if the property name is not recognized
     * @throws SAXNotSupportedException if the property name is recognized but not supported
     */
    public boolean getFeature( final String feature )
        throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException
    {
        return ( getFactory().getFeature( feature ) );
    }

    /**
     * Sets a flag indicating whether the requested feature is supported by the underlying implementation of
     * <code>org.xml.sax.XMLReader</code>. See <a href="http://www.saxproject.org">the saxproject website</a> for
     * information about the standard SAX2 feature flags. In order to be effective, this method must be called
     * <strong>before</strong> the <code>getParser()</code> method is called for the first time, either directly or
     * indirectly.
     *
     * @param feature Name of the feature to set the status for
     * @param value The new value for this feature
     * @throws ParserConfigurationException if a parser configuration error occurs
     * @throws SAXNotRecognizedException if the property name is not recognized
     * @throws SAXNotSupportedException if the property name is recognized but not supported
     */
    public void setFeature( final String feature, final boolean value )
        throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException
    {
        getFactory().setFeature( feature, value );
    }

    /**
     * Return the current Logger associated with this instance of the Digester
     *
     * @return the current Logger associated with this instance of the Digester
     */
    public Log getLogger()
    {
        return log;
    }

    /**
     * Set the current logger for this Digester.
     *
     * @param log the current logger for this Digester.
     */
    public void setLogger( final Log log )
    {
        this.log = log;
    }

    /**
     * Gets the logger used for logging SAX-related information. <strong>Note</strong> the output is finely grained.
     *
     * @return the logger used for logging SAX-related information
     * @since 1.6
     */
    public Log getSAXLogger()
    {
        return saxLog;
    }

    /**
     * Sets the logger used for logging SAX-related information. <strong>Note</strong> the output is finely grained.
     *
     * @param saxLog the logger used for logging SAX-related information, not null
     * @since 1.6
     */
    public void setSAXLogger( final Log saxLog )
    {
        this.saxLog = saxLog;
    }

    /**
     * Return the current rule match path
     *
     * @return the current rule match path
     */
    public String getMatch()
    {
        return match;
    }

    /**
     * Return a Stack whose elements are List objects, each containing a list of
     * Rule objects as returned from Rules.getMatch().
     *
     * @return a Stack whose elements are List objects, each containing a list of
     *         Rule objects as returned from Rules.getMatch().
     * @since 3.0
     */
    public Stack<List<Rule>> getMatches()
    {
        return matches;
    }

    /**
     * Return the "namespace aware" flag for parsers we create.
     *
     * @return the "namespace aware" flag for parsers we create.
     */
    public boolean getNamespaceAware()
    {
        return ( this.namespaceAware );
    }

    /**
     * Set the "namespace aware" flag for parsers we create.
     *
     * @param namespaceAware The new "namespace aware" flag
     */
    public void setNamespaceAware( final boolean namespaceAware )
    {
        this.namespaceAware = namespaceAware;
    }

    /**
     * Return the XInclude-aware flag for parsers we create. XInclude functionality additionally requires
     * namespace-awareness.
     *
     * @return The XInclude-aware flag
     * @see #getNamespaceAware()
     * @since 2.0
     */
    public boolean getXIncludeAware()
    {
        return ( this.xincludeAware );
    }

    /**
     * Set the XInclude-aware flag for parsers we create. This additionally requires namespace-awareness.
     *
     * @param xincludeAware The new XInclude-aware flag
     * @see #setNamespaceAware(boolean)
     * @since 2.0
     */
    public void setXIncludeAware( final boolean xincludeAware )
    {
        this.xincludeAware = xincludeAware;
    }

    /**
     * Set the public id of the current file being parse.
     *
     * @param publicId the DTD/Schema public's id.
     */
    public void setPublicId( final String publicId )
    {
        this.publicId = publicId;
    }

    /**
     * Return the public identifier of the DTD we are currently parsing under, if any.
     *
     * @return the public identifier of the DTD we are currently parsing under, if any.
     */
    public String getPublicId()
    {
        return ( this.publicId );
    }

    /**
     * Return the namespace URI that will be applied to all subsequently added <code>Rule</code> objects.
     *
     * @return the namespace URI that will be applied to all subsequently added <code>Rule</code> objects.
     */
    public String getRuleNamespaceURI()
    {
        return ( getRules().getNamespaceURI() );
    }

    /**
     * Set the namespace URI that will be applied to all subsequently added <code>Rule</code> objects.
     *
     * @param ruleNamespaceURI Namespace URI that must match on all subsequently added rules, or <code>null</code> for
     *            matching regardless of the current namespace URI
     */
    public void setRuleNamespaceURI( final String ruleNamespaceURI )
    {
        getRules().setNamespaceURI( ruleNamespaceURI );
    }

    /**
     * Return the SAXParser we will use to parse the input stream.
     *
     * If there is a problem creating the parser, return <code>null</code>.
     *
     * @return the SAXParser we will use to parse the input stream
     */
    public SAXParser getParser()
    {
        // Return the parser we already created (if any)
        if ( parser != null )
        {
            return ( parser );
        }

        // Create a new parser
        try
        {
            parser = getFactory().newSAXParser();
        }
        catch ( final Exception e )
        {
            log.error( "Digester.getParser: ", e );
            return ( null );
        }

        return ( parser );
    }

    /**
     * Return the current value of the specified property for the underlying <code>XMLReader</code> implementation.
     *
     * See <a href="http://www.saxproject.org">the saxproject website</a> for information about the standard SAX2
     * properties.
     *
     * @param property Property name to be retrieved
     * @return the current value of the specified property for the underlying <code>XMLReader</code> implementation.
     * @throws SAXNotRecognizedException if the property name is not recognized
     * @throws SAXNotSupportedException if the property name is recognized but not supported
     */
    public Object getProperty( final String property )
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        return ( getParser().getProperty( property ) );
    }

    /**
     * Set the current value of the specified property for the underlying <code>XMLReader</code> implementation. See <a
     * href="http://www.saxproject.org">the saxproject website</a> for information about the standard SAX2 properties.
     *
     * @param property Property name to be set
     * @param value Property value to be set
     * @throws SAXNotRecognizedException if the property name is not recognized
     * @throws SAXNotSupportedException if the property name is recognized but not supported
     */
    public void setProperty( final String property, final Object value )
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        getParser().setProperty( property, value );
    }

    /**
     * Return the <code>Rules</code> implementation object containing our rules collection and associated matching
     * policy. If none has been established, a default implementation will be created and returned.
     *
     * @return the <code>Rules</code> implementation object.
     */
    public Rules getRules()
    {
        if ( this.rules == null )
        {
            this.rules = new RulesBase();
            this.rules.setDigester( this );
        }
        return ( this.rules );
    }

    /**
     * Set the <code>Rules</code> implementation object containing our rules collection and associated matching policy.
     *
     * @param rules New Rules implementation
     */
    public void setRules( final Rules rules )
    {
        this.rules = rules;
        this.rules.setDigester( this );
    }

    /**
     * Return the XML Schema used when parsing.
     *
     * @return The {@link Schema} instance in use.
     * @since 2.0
     */
    public Schema getXMLSchema()
    {
        return ( this.schema );
    }

    /**
     * Set the XML Schema to be used when parsing.
     *
     * @param schema The {@link Schema} instance to use.
     * @since 2.0
     */
    public void setXMLSchema( final Schema schema )
    {
        this.schema = schema;
    }

    /**
     * Return the boolean as to whether the context ClassLoader should be used.
     *
     * @return true, if the context ClassLoader should be used, false otherwise.
     */
    public boolean getUseContextClassLoader()
    {
        return useContextClassLoader;
    }

    /**
     * Determine whether to use the Context ClassLoader (the one found by calling
     * <code>Thread.currentThread().getContextClassLoader()</code>) to resolve/load classes that are defined in various
     * rules. If not using Context ClassLoader, then the class-loading defaults to using the calling-class' ClassLoader.
     *
     * @param use determines whether to use Context ClassLoader.
     */
    public void setUseContextClassLoader( final boolean use )
    {
        useContextClassLoader = use;
    }

    /**
     * Return the validating parser flag.
     *
     * @return the validating parser flag.
     */
    public boolean getValidating()
    {
        return ( this.validating );
    }

    /**
     * Set the validating parser flag. This must be called before <code>parse()</code> is called the first time. 
     * By default the value of this is set to false.
     * 
     * It essentially just controls the DTD validation. To use modern schema languages use the 
     * {@link #setXMLSchema(Schema)} method to associate a schema to a parser.
     *
     * @param validating The new validating parser flag.
     * @see javax.xml.parsers.SAXParserFactory#setValidating(boolean) for more detail.
     */
    public void setValidating( final boolean validating )
    {
        this.validating = validating;
    }

    /**
     * Return the XMLReader to be used for parsing the input document.
     *
     * FIXME: there is a bug in JAXP/XERCES that prevent the use of a parser that contains a schema with a DTD.
     *
     * @return the XMLReader to be used for parsing the input document.
     * @throws SAXException if no XMLReader can be instantiated
     */
    public XMLReader getXMLReader()
        throws SAXException
    {
        if ( reader == null )
        {
            reader = getParser().getXMLReader();
        }

        reader.setDTDHandler( this );
        reader.setContentHandler( this );

        if ( entityResolver == null )
        {
            reader.setEntityResolver( this );
        }
        else
        {
            reader.setEntityResolver( entityResolver );
        }

        if ( this.errorHandler != null )
        {
            reader.setErrorHandler( this.errorHandler );
        }
        else
        {
            reader.setErrorHandler( this );
        }

        return reader;
    }

    /**
     * Gets the <code>Substitutor</code> used to convert attributes and body text.
     *
     * @return the <code>Substitutor</code> used to convert attributes and body text,
     *         null if not substitutions are to be performed.
     */
    public Substitutor getSubstitutor()
    {
        return substitutor;
    }

    /**
     * Sets the <code>Substitutor</code> to be used to convert attributes and body text.
     *
     * @param substitutor the Substitutor to be used to convert attributes and body text or null if not substitution of
     *            these values is to be performed.
     */
    public void setSubstitutor( final Substitutor substitutor )
    {
        this.substitutor = substitutor;
    }

    /**
     * returns the custom SAX ContentHandler where events are redirected.
     *
     * @return the custom SAX ContentHandler where events are redirected.
     * @see #setCustomContentHandler(ContentHandler)
     * @since 1.7
     */
    public ContentHandler getCustomContentHandler()
    {
        return customContentHandler;
    }

    /**
     * Redirects (or cancels redirecting) of SAX ContentHandler events to an external object.
     * <p>
     * When this object's customContentHandler is non-null, any SAX events received from the parser will simply be
     * passed on to the specified object instead of this object handling them. This allows Rule classes to take control
     * of the SAX event stream for a while in order to do custom processing. Such a rule should save the old value
     * before setting a new one, and restore the old value in order to resume normal digester processing.
     * <p>
     * An example of a Rule which needs this feature is NodeCreateRule.
     * <p>
     * Note that saving the old value is probably not needed as it should always be null; a custom rule that wants to
     * take control could only have been called when there was no custom content handler. But it seems cleaner to
     * properly save/restore the value and maybe some day this will come in useful.
     * <p>
     * Note also that this is not quite equivalent to
     *
     * <pre>
     * digester.getXMLReader().setContentHandler( handler )
     * </pre>
     *
     * for these reasons:
     * <ul>
     * <li>Some xml parsers don't like having setContentHandler called after parsing has started. The Aelfred parser is
     * one example.</li>
     * <li>Directing the events via the Digester object potentially allows us to log information about those SAX events
     * at the digester level.</li>
     * </ul>
     *
     * @param handler the custom SAX ContentHandler where events are redirected.
     * @since 1.7
     */
    public void setCustomContentHandler( final ContentHandler handler )
    {
        customContentHandler = handler;
    }

    /**
     * Define a callback object which is invoked whenever an object is pushed onto a digester object stack,
     * or popped off one.
     *
     * @param stackAction the callback object which is invoked whenever an object is pushed onto a digester
     *        object stack, or popped off one.
     * @since 1.8
     */
    public void setStackAction( final StackAction stackAction )
    {
        this.stackAction = stackAction;
    }

    /**
     * Return the callback object which is invoked whenever an object is pushed onto a digester object stack,
     * or popped off one.
     *
     * @return the callback object which is invoked whenever an object is pushed onto a digester object stack,
     *         or popped off one.
     * @see #setStackAction(StackAction)
     * @since 1.8
     */
    public StackAction getStackAction()
    {
        return stackAction;
    }

    /**
     * Get the most current namespaces for all prefixes.
     *
     * @return Map A map with namespace prefixes as keys and most current namespace URIs for the corresponding prefixes
     *         as values
     * @since 1.8
     */
    public Map<String, String> getCurrentNamespaces()
    {
        if ( !namespaceAware )
        {
            log.warn( "Digester is not namespace aware" );
        }
        final Map<String, String> currentNamespaces = new HashMap<String, String>();
        for ( final Map.Entry<String, Stack<String>> nsEntry : namespaces.entrySet() )
        {
            try
            {
                currentNamespaces.put( nsEntry.getKey(), nsEntry.getValue().peek() );
            }
            catch ( final RuntimeException e )
            {
                // rethrow, after logging
                log.error( e.getMessage(), e );
                throw e;
            }
        }
        return currentNamespaces;
    }

    /**
     * Returns the executor service used to run asynchronous parse method.
     *
     * @return the executor service used to run asynchronous parse method
     * @since 3.1
     */
    public ExecutorService getExecutorService()
    {
        return executorService;
    }

    /**
     * Sets the executor service to run asynchronous parse method.
     *
     * @param executorService the executor service to run asynchronous parse method
     * @since 3.1
     */
    public void setExecutorService( final ExecutorService executorService )
    {
        this.executorService = executorService;
    }

    // ------------------------------------------------- ContentHandler Methods

    /**
     * {@inheritDoc}
     */
    @Override
    public void characters( final char buffer[], final int start, final int length )
        throws SAXException
    {
        if ( customContentHandler != null )
        {
            // forward calls instead of handling them here
            customContentHandler.characters( buffer, start, length );
            return;
        }

        if ( saxLog.isDebugEnabled() )
        {
            saxLog.debug( "characters(" + new String( buffer, start, length ) + ")" );
        }

        bodyText.append( buffer, start, length );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDocument()
        throws SAXException
    {
        if ( saxLog.isDebugEnabled() )
        {
            if ( getCount() > 1 )
            {
                saxLog.debug( "endDocument():  " + getCount() + " elements left" );
            }
            else
            {
                saxLog.debug( "endDocument()" );
            }
        }

        // Fire "finish" events for all defined rules
        for ( final Rule rule : getRules().rules() )
        {
            try
            {
                rule.finish();
            }
            catch ( final Exception e )
            {
                log.error( "Finish event threw exception", e );
                throw createSAXException( e );
            }
            catch ( final Error e )
            {
                log.error( "Finish event threw error", e );
                throw e;
            }
        }

        // Perform final cleanup
        clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement( final String namespaceURI, final String localName, final String qName )
        throws SAXException
    {
        if ( customContentHandler != null )
        {
            // forward calls instead of handling them here
            customContentHandler.endElement( namespaceURI, localName, qName );
            return;
        }

        final boolean debug = log.isDebugEnabled();

        if ( debug )
        {
            if ( saxLog.isDebugEnabled() )
            {
                saxLog.debug( "endElement(" + namespaceURI + "," + localName + "," + qName + ")" );
            }
            log.debug( "  match='" + match + "'" );
            log.debug( "  bodyText='" + bodyText + "'" );
        }

        // the actual element name is either in localName or qName, depending
        // on whether the parser is namespace aware
        String name = localName;
        if ( ( name == null ) || ( name.length() < 1 ) )
        {
            name = qName;
        }

        // Fire "body" events for all relevant rules
        final List<Rule> rules = matches.pop();
        if ( ( rules != null ) && ( !rules.isEmpty() ) )
        {
            String bodyText = this.bodyText.toString();
            final Substitutor substitutor = getSubstitutor();
            if ( substitutor != null )
            {
                bodyText = substitutor.substitute( bodyText );
            }
            for (final Rule rule : rules) {
                try
                {
                    if ( debug )
                    {
                        log.debug( "  Fire body() for " + rule );
                    }
                    rule.body( namespaceURI, name, bodyText );
                }
                catch ( final Exception e )
                {
                    log.error( "Body event threw exception", e );
                    throw createSAXException( e );
                }
                catch ( final Error e )
                {
                    log.error( "Body event threw error", e );
                    throw e;
                }
            }
        }
        else
        {
            if ( debug )
            {
                log.debug( "  No rules found matching '" + match + "'." );
            }
        }

        // Recover the body text from the surrounding element
        bodyText = bodyTexts.pop();
        if ( debug )
        {
            log.debug( "  Popping body text '" + bodyText.toString() + "'" );
        }

        // Fire "end" events for all relevant rules in reverse order
        if ( rules != null )
        {
            for ( int i = 0; i < rules.size(); i++ )
            {
                final int j = ( rules.size() - i ) - 1;
                try
                {
                    final Rule rule = rules.get( j );
                    if ( debug )
                    {
                        log.debug( "  Fire end() for " + rule );
                    }
                    rule.end( namespaceURI, name );
                }
                catch ( final Exception e )
                {
                    log.error( "End event threw exception", e );
                    throw createSAXException( e );
                }
                catch ( final Error e )
                {
                    log.error( "End event threw error", e );
                    throw e;
                }
            }
        }

        // Recover the previous match expression
        final int slash = match.lastIndexOf( '/' );
        if ( slash >= 0 )
        {
            match = match.substring( 0, slash );
        }
        else
        {
            match = "";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endPrefixMapping( final String prefix )
        throws SAXException
    {
        if ( saxLog.isDebugEnabled() )
        {
            saxLog.debug( "endPrefixMapping(" + prefix + ")" );
        }

        // Deregister this prefix mapping
        final Stack<String> stack = namespaces.get( prefix );
        if ( stack == null )
        {
            return;
        }
        try
        {
            stack.pop();
            if ( stack.empty() )
            {
                namespaces.remove( prefix );
            }
        }
        catch ( final EmptyStackException e )
        {
            throw createSAXException( "endPrefixMapping popped too many times" );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ignorableWhitespace( final char buffer[], final int start, final int len )
        throws SAXException
    {
        if ( saxLog.isDebugEnabled() )
        {
            saxLog.debug( "ignorableWhitespace(" + new String( buffer, start, len ) + ")" );
        }

        // No processing required
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processingInstruction( final String target, final String data )
        throws SAXException
    {
        if ( customContentHandler != null )
        {
            // forward calls instead of handling them here
            customContentHandler.processingInstruction( target, data );
            return;
        }

        if ( saxLog.isDebugEnabled() )
        {
            saxLog.debug( "processingInstruction('" + target + "','" + data + "')" );
        }

        // No processing is required
    }

    /**
     * Gets the document locator associated with our parser.
     *
     * @return the Locator supplied by the document parser
     */
    public Locator getDocumentLocator()
    {
        return locator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDocumentLocator( final Locator locator )
    {
        if ( saxLog.isDebugEnabled() )
        {
            saxLog.debug( "setDocumentLocator(" + locator + ")" );
        }

        this.locator = locator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void skippedEntity( final String name )
        throws SAXException
    {
        if ( saxLog.isDebugEnabled() )
        {
            saxLog.debug( "skippedEntity(" + name + ")" );
        }

        // No processing required
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startDocument()
        throws SAXException
    {
        if ( saxLog.isDebugEnabled() )
        {
            saxLog.debug( "startDocument()" );
        }

        // ensure that the digester is properly configured, as
        // the digester could be used as a SAX ContentHandler
        // rather than via the parse() methods.
        configure();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement( final String namespaceURI, final String localName, final String qName, Attributes list )
        throws SAXException
    {
        final boolean debug = log.isDebugEnabled();

        if ( customContentHandler != null )
        {
            // forward calls instead of handling them here
            customContentHandler.startElement( namespaceURI, localName, qName, list );
            return;
        }

        if ( saxLog.isDebugEnabled() )
        {
            saxLog.debug( "startElement(" + namespaceURI + "," + localName + "," + qName + ")" );
        }

        // Save the body text accumulated for our surrounding element
        bodyTexts.push( bodyText );
        if ( debug )
        {
            log.debug( "  Pushing body text '" + bodyText.toString() + "'" );
        }
        bodyText = new StringBuilder();

        // the actual element name is either in localName or qName, depending
        // on whether the parser is namespace aware
        String name = localName;
        if ( ( name == null ) || ( name.length() < 1 ) )
        {
            name = qName;
        }

        // Compute the current matching rule
        final StringBuilder sb = new StringBuilder( match );
        if ( !match.isEmpty() )
        {
            sb.append( '/' );
        }
        sb.append( name );
        match = sb.toString();
        if ( debug )
        {
            log.debug( "  New match='" + match + "'" );
        }

        // Fire "begin" events for all relevant rules
        final List<Rule> rules = getRules().match( namespaceURI, match, localName, list );
        matches.push( rules );
        if ( ( rules != null ) && ( !rules.isEmpty() ) )
        {
            final Substitutor substitutor = getSubstitutor();
            if ( substitutor != null )
            {
                list = substitutor.substitute( list );
            }
            for (final Rule rule : rules) {
                try
                {
                    if ( debug )
                    {
                        log.debug( "  Fire begin() for " + rule );
                    }
                    rule.begin( namespaceURI, name, list );
                }
                catch ( final Exception e )
                {
                    log.error( "Begin event threw exception", e );
                    throw createSAXException( e );
                }
                catch ( final Error e )
                {
                    log.error( "Begin event threw error", e );
                    throw e;
                }
            }
        }
        else
        {
            if ( debug )
            {
                log.debug( "  No rules found matching '" + match + "'." );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startPrefixMapping( final String prefix, final String namespaceURI )
        throws SAXException
    {
        if ( saxLog.isDebugEnabled() )
        {
            saxLog.debug( "startPrefixMapping(" + prefix + "," + namespaceURI + ")" );
        }

        // Register this prefix mapping
        Stack<String> stack = namespaces.get( prefix );
        if ( stack == null )
        {
            stack = new Stack<String>();
            namespaces.put( prefix, stack );
        }
        stack.push( namespaceURI );
    }

    // ----------------------------------------------------- DTDHandler Methods

    /**
     * {@inheritDoc}
     */
    @Override
    public void notationDecl( final String name, final String publicId, final String systemId )
    {
        if ( saxLog.isDebugEnabled() )
        {
            saxLog.debug( "notationDecl(" + name + "," + publicId + "," + systemId + ")" );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unparsedEntityDecl( final String name, final String publicId, final String systemId, final String notation )
    {
        if ( saxLog.isDebugEnabled() )
        {
            saxLog.debug( "unparsedEntityDecl(" + name + "," + publicId + "," + systemId + "," + notation + ")" );
        }
    }

    // ----------------------------------------------- EntityResolver Methods

    /**
     * Set the <code>EntityResolver</code> used by SAX when resolving public id and system id. This must be called
     * before the first call to <code>parse()</code>.
     *
     * @param entityResolver a class that implement the <code>EntityResolver</code> interface.
     */
    public void setEntityResolver( final EntityResolver entityResolver )
    {
        this.entityResolver = entityResolver;
    }

    /**
     * Return the Entity Resolver used by the SAX parser.
     *
     * @return the Entity Resolver used by the SAX parser.
     */
    public EntityResolver getEntityResolver()
    {
        return entityResolver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputSource resolveEntity( final String publicId, final String systemId )
        throws SAXException
    {
        if ( saxLog.isDebugEnabled() )
        {
            saxLog.debug( "resolveEntity('" + publicId + "', '" + systemId + "')" );
        }

        if ( publicId != null )
        {
            this.publicId = publicId;
        }

        // Has this system identifier been registered?
        URL entityURL = null;
        if ( publicId != null )
        {
            entityURL = entityValidator.get( publicId );
        }

        // Redirect the schema location to a local destination
        if ( entityURL == null && systemId != null )
        {
            entityURL = entityValidator.get( systemId );
        }

        if ( entityURL == null )
        {
            if ( systemId == null )
            {
                // cannot resolve
                if ( log.isDebugEnabled() )
                {
                    log.debug( " Cannot resolve null entity, returning null InputSource" );
                }
                return ( null );

            }
            // try to resolve using system ID
            if ( log.isDebugEnabled() )
            {
                log.debug( " Trying to resolve using system ID '" + systemId + "'" );
            }
            try
            {
                entityURL = new URL( systemId );
            }
            catch ( final MalformedURLException e )
            {
                throw new IllegalArgumentException( "Malformed URL '" + systemId + "' : " + e.getMessage() );
            }
        }

        // Return an input source to our alternative URL
        if ( log.isDebugEnabled() )
        {
            log.debug( " Resolving to alternate DTD '" + entityURL + "'" );
        }

        try
        {
            return createInputSourceFromURL( entityURL );
        }
        catch ( final Exception e )
        {
            throw createSAXException( e );
        }
    }

    // ------------------------------------------------- ErrorHandler Methods

    /**
     * {@inheritDoc}
     */
    @Override
    public void error( final SAXParseException exception )
        throws SAXException
    {
    	log.error( "Parse Error at line " + exception.getLineNumber() + " column " + exception.getColumnNumber() + ": "
                + exception.getMessage(), exception );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fatalError( final SAXParseException exception )
        throws SAXException
    {
        log.error( "Parse Fatal Error at line " + exception.getLineNumber() + " column " + exception.getColumnNumber()
                + ": " + exception.getMessage(), exception );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warning( final SAXParseException exception )
        throws SAXException
    {
        log.warn( "Parse Warning Error at line " + exception.getLineNumber() + " column "
                + exception.getColumnNumber() + ": " + exception.getMessage(), exception );
    }

    // ------------------------------------------------------- Public Methods

    /**
     * Parse the content of the specified file using this Digester. Returns the root element from the object stack (if
     * any).
     *
     * @param <T> the type used to auto-cast the returned object to the assigned variable type
     * @param file File containing the XML data to be parsed
     * @return the root element from the object stack (if any)
     * @throws IOException if an input/output error occurs
     * @throws SAXException if a parsing exception occurs
     */
    public <T> T parse( final File file )
        throws IOException, SAXException
    {
        if ( file == null )
        {
            throw new IllegalArgumentException( "File to parse is null" );
        }

        final InputSource input = new InputSource( new FileInputStream( file ) );
        input.setSystemId( file.toURI().toURL().toString() );

        return ( this.<T> parse( input ) );
    }

    /**
     * Creates a Callable instance that parse the content of the specified reader using this Digester.
     *
     * @param <T> The result type returned by the returned Future's {@code get} method
     * @param file File containing the XML data to be parsed
     * @return a Future that can be used to track when the parse has been fully processed.
     * @see Digester#parse(File)
     * @since 3.1
     */
    public <T> Future<T> asyncParse( final File file )
    {
        return asyncParse( new Callable<T>()
        {

            @Override
            public T call()
                throws Exception
            {
                return Digester.this.<T> parse( file );
            }

        } );
    }

    /**
     * Parse the content of the specified input source using this Digester. Returns the root element from the object
     * stack (if any).
     *
     * @param <T> the type used to auto-cast the returned object to the assigned variable type
     * @param input Input source containing the XML data to be parsed
     * @return the root element from the object stack (if any)
     * @throws IOException if an input/output error occurs
     * @throws SAXException if a parsing exception occurs
     */
    public <T> T parse( final InputSource input )
        throws IOException, SAXException
    {
        if ( input == null )
        {
            throw new IllegalArgumentException( "InputSource to parse is null" );
        }

        configure();

        String systemId = input.getSystemId();
        if ( systemId == null )
        {
            systemId = "(already loaded from stream)";
        }

        try
        {
            getXMLReader().parse( input );
        }
        catch ( final IOException e )
        {
            log.error( format( "An error occurred while reading stream from '%s', see nested exceptions", systemId ),
                       e );
            throw e;
        }
        cleanup();
        return this.<T> getRoot();
    }

    /**
     * Creates a Callable instance that parse the content of the specified reader using this Digester.
     *
     * @param <T> The result type returned by the returned Future's {@code get} method
     * @param input Input source containing the XML data to be parsed
     * @return a Future that can be used to track when the parse has been fully processed.
     * @see Digester#parse(InputSource)
     * @since 3.1
     */
    public <T> Future<T> asyncParse( final InputSource input )
    {
        return asyncParse( new Callable<T>()
        {

            @Override
            public T call()
                throws Exception
            {
                return Digester.this.<T> parse( input );
            }

        } );
    }

    /**
     * Parse the content of the specified input stream using this Digester. Returns the root element from the object
     * stack (if any).
     *
     * @param <T> the type used to auto-cast the returned object to the assigned variable type
     * @param input Input stream containing the XML data to be parsed
     * @return the root element from the object stack (if any)
     * @throws IOException if an input/output error occurs
     * @throws SAXException if a parsing exception occurs
     */
    public <T> T parse( final InputStream input )
        throws IOException, SAXException
    {
        if ( input == null )
        {
            throw new IllegalArgumentException( "InputStream to parse is null" );
        }

        return ( this.<T> parse( new InputSource( input ) ) );
    }

    /**
     * Creates a Callable instance that parse the content of the specified reader using this Digester.
     *
     * @param <T> The result type returned by the returned Future's {@code get} method
     * @param input Input stream containing the XML data to be parsed
     * @return a Future that can be used to track when the parse has been fully processed.
     * @see Digester#parse(InputStream)
     * @since 3.1
     */
    public <T> Future<T> asyncParse( final InputStream input )
    {
        return asyncParse( new Callable<T>()
        {

            @Override
            public T call()
                throws Exception
            {
                return Digester.this.<T> parse( input );
            }

        } );
    }

    /**
     * Parse the content of the specified reader using this Digester. Returns the root element from the object stack (if
     * any).
     *
     * @param <T> the type used to auto-cast the returned object to the assigned variable type
     * @param reader Reader containing the XML data to be parsed
     * @return the root element from the object stack (if any)
     * @throws IOException if an input/output error occurs
     * @throws SAXException if a parsing exception occurs
     */
    public <T> T parse( final Reader reader )
        throws IOException, SAXException
    {
        if ( reader == null )
        {
            throw new IllegalArgumentException( "Reader to parse is null" );
        }

        return ( this.<T> parse( new InputSource( reader ) ) );
    }

    /**
     * Creates a Callable instance that parse the content of the specified reader using this Digester.
     *
     * @param <T> The result type returned by the returned Future's {@code get} method
     * @param reader Reader containing the XML data to be parsed
     * @return a Future that can be used to track when the parse has been fully processed.
     * @see Digester#parse(Reader)
     * @since 3.1
     */
    public <T> Future<T> asyncParse( final Reader reader )
    {
        return asyncParse( new Callable<T>()
        {

            @Override
            public T call()
                throws Exception
            {
                return Digester.this.<T> parse( reader );
            }

        } );
    }

    /**
     * Parse the content of the specified URI using this Digester. Returns the root element from the object stack (if
     * any).
     *
     * @param <T> the type used to auto-cast the returned object to the assigned variable type
     * @param uri URI containing the XML data to be parsed
     * @return the root element from the object stack (if any)
     * @throws IOException if an input/output error occurs
     * @throws SAXException if a parsing exception occurs
     */
    public <T> T parse( final String uri )
        throws IOException, SAXException
    {
        if ( uri == null )
        {
            throw new IllegalArgumentException( "String URI to parse is null" );
        }

        return ( this.<T> parse( createInputSourceFromURL( uri ) ) );
    }

    /**
     * Creates a Callable instance that parse the content of the specified reader using this Digester.
     *
     * @param <T> The result type returned by the returned Future's {@code get} method
     * @param uri URI containing the XML data to be parsed
     * @return a Future that can be used to track when the parse has been fully processed.
     * @see Digester#parse(String)
     * @since 3.1
     */
    public <T> Future<T> asyncParse( final String uri )
    {
        return asyncParse( new Callable<T>()
        {

            @Override
            public T call()
                throws Exception
            {
                return Digester.this.<T> parse( uri );
            }

        } );
    }

    /**
     * Parse the content of the specified URL using this Digester. Returns the root element from the object stack (if
     * any).
     *
     * @param <T> the type used to auto-cast the returned object to the assigned variable type
     * @param url URL containing the XML data to be parsed
     * @return the root element from the object stack (if any)
     * @throws IOException if an input/output error occurs
     * @throws SAXException if a parsing exception occurs
     * @since 1.8
     */
    public <T> T parse( final URL url )
        throws IOException, SAXException
    {
        if ( url == null )
        {
            throw new IllegalArgumentException( "URL to parse is null" );
        }

        return ( this.<T> parse( createInputSourceFromURL( url ) ) );
    }

    /**
     * Creates a Callable instance that parse the content of the specified reader using this Digester.
     *
     * @param <T> The result type returned by the returned Future's {@code get} method
     * @param url URL containing the XML data to be parsed
     * @return a Future that can be used to track when the parse has been fully processed.
     * @see Digester#parse(URL)
     * @since 3.1
     */
    public <T> Future<T> asyncParse( final URL url )
    {
        return asyncParse( new Callable<T>()
        {

            @Override
            public T call()
                throws Exception
            {
                return Digester.this.<T> parse( url );
            }

        } );
    }

    /**
     * Execute the parse in async mode.
     *
     * @param <T> the type used to auto-cast the returned object to the assigned variable type
     * @param callable
     * @return a Future that can be used to track when the parse has been fully processed.
     * @since 3.1
     */
    private <T> Future<T> asyncParse( final Callable<T> callable )
    {
        if ( executorService == null )
        {
            throw new IllegalStateException( "ExecutorService not set" );
        }

        return executorService.submit( callable );
    }

    /**
     * <p>
     * Register the specified DTD URL for the specified public identifier. This must be called before the first call to
     * <code>parse()</code>.
     * </p>
     * <p>
     * <code>Digester</code> contains an internal <code>EntityResolver</code> implementation. This maps
     * <code>PUBLICID</code>'s to URLs (from which the resource will be loaded). A common use case for this method is to
     * register local URLs (possibly computed at runtime by a classloader) for DTDs. This allows the performance
     * advantage of using a local version without having to ensure every <code>SYSTEM</code> URI on every processed xml
     * document is local. This implementation provides only basic functionality. If more sophisticated features are
     * required, using {@link #setEntityResolver} to set a custom resolver is recommended.
     * </p>
     * <p>
     * <strong>Note:</strong> This method will have no effect when a custom <code>EntityResolver</code> has been set.
     * (Setting a custom <code>EntityResolver</code> overrides the internal implementation.)
     * </p>
     *
     * @param publicId Public identifier of the DTD to be resolved
     * @param entityURL The URL to use for reading this DTD
     * @since 1.8
     */
    public void register( final String publicId, final URL entityURL )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "register('" + publicId + "', '" + entityURL + "'" );
        }
        entityValidator.put( publicId, entityURL );
    }

    /**
     * <p>
     * Convenience method that registers the string version of an entity URL instead of a URL version.
     * </p>
     *
     * @param publicId Public identifier of the entity to be resolved
     * @param entityURL The URL to use for reading this entity
     */
    public void register( final String publicId, final String entityURL )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "register('" + publicId + "', '" + entityURL + "'" );
        }
        try
        {
            entityValidator.put( publicId, new URL( entityURL ) );
        }
        catch ( final MalformedURLException e )
        {
            throw new IllegalArgumentException( "Malformed URL '" + entityURL + "' : " + e.getMessage() );
        }
    }

    /**
     * Convenience method that registers DTD URLs for the specified public identifiers.
     *
     * @param entityValidator The URLs of entityValidator that have been registered, keyed by the public
     *                        identifier that corresponds.
     * @since 3.0
     */
    public void registerAll( final Map<String, URL> entityValidator )
    {
        this.entityValidator.putAll( entityValidator );
    }

    /**
     * <p>
     * <code>List</code> of <code>InputSource</code> instances created by a <code>createInputSourceFromURL()</code>
     * method call. These represent open input streams that need to be closed to avoid resource leaks, as well as
     * potentially locked JAR files on Windows.
     * </p>
     */
    protected List<InputSource> inputSources = new ArrayList<InputSource>( 5 );

    /**
     * Given a URL, return an InputSource that reads from that URL.
     * <p>
     * Ideally this function would not be needed and code could just use <code>new InputSource(entityURL)</code>.
     * Unfortunately it appears that when the entityURL points to a file within a jar archive a caching mechanism inside
     * the InputSource implementation causes a file-handle to the jar file to remain open. On Windows systems this then
     * causes the jar archive file to be locked on disk ("in use") which makes it impossible to delete the jar file -
     * and that really stuffs up "undeploy" in webapps in particular.
     * <p>
     * In JDK1.4 and later, Apache XercesJ is used as the xml parser. The InputSource object provided is converted into
     * an XMLInputSource, and eventually passed to an instance of XMLDocumentScannerImpl to specify the source data to
     * be converted into tokens for the rest of the XMLReader code to handle. XMLDocumentScannerImpl calls
     * fEntityManager.startDocumentEntity(source), where fEntityManager is declared in ancestor class XMLScanner to be
     * an XMLEntityManager. In that class, if the input source stream is null, then:
     *
     * <pre>
     * URL location = new URL( expandedSystemId );
     * URLConnection connect = location.openConnection();
     * if ( connect instanceof HttpURLConnection )
     * {
     *     setHttpProperties( connect, xmlInputSource );
     * }
     * stream = connect.getInputStream();
     * </pre>
     *
     * This method pretty much duplicates the standard behavior, except that it calls URLConnection.setUseCaches(false)
     * before opening the connection.
     *
     * @param url The URL has to be read
     * @return The InputSource that reads from the input URL
     * @throws IOException if any error occurs while reading the input URL
     * @since 1.8
     */
    public InputSource createInputSourceFromURL( final URL url )
        throws IOException
    {
        final URLConnection connection = url.openConnection();
        connection.setUseCaches( false );
        final InputStream stream = connection.getInputStream();
        final InputSource source = new InputSource( stream );
        source.setSystemId( url.toExternalForm() );
        inputSources.add( source );
        return source;
    }

    /**
     * <p>
     * Convenience method that creates an <code>InputSource</code> from the string version of a URL.
     * </p>
     *
     * @param url URL for which to create an <code>InputSource</code>
     * @return The InputSource that reads from the input URL
     * @throws IOException if any error occurs while reading the input URL
     * @since 1.8
     */
    public InputSource createInputSourceFromURL( final String url )
        throws IOException
    {
        return createInputSourceFromURL( new URL( url ) );
    }

    // --------------------------------------------------------- Rule Methods

    /**
     * <p>
     * Register a new Rule matching the specified pattern. This method sets the <code>Digester</code> property on the
     * rule.
     * </p>
     *
     * @param pattern Element matching pattern
     * @param rule Rule to be registered
     */
    public void addRule( final String pattern, final Rule rule )
    {
        rule.setDigester( this );
        getRules().add( pattern, rule );
    }

    /**
     * Register a set of Rule instances defined in a RuleSet.
     *
     * @param ruleSet The RuleSet instance to configure from
     */
    public void addRuleSet( final RuleSet ruleSet )
    {
        final String oldNamespaceURI = getRuleNamespaceURI();
        final String newNamespaceURI = ruleSet.getNamespaceURI();
        if ( log.isDebugEnabled() )
        {
            if ( newNamespaceURI == null )
            {
                log.debug( "addRuleSet() with no namespace URI" );
            }
            else
            {
                log.debug( "addRuleSet() with namespace URI " + newNamespaceURI );
            }
        }
        setRuleNamespaceURI( newNamespaceURI );
        ruleSet.addRuleInstances( this );
        setRuleNamespaceURI( oldNamespaceURI );
    }

    /**
     * Add a "bean property setter" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @see BeanPropertySetterRule
     */
    public void addBeanPropertySetter( final String pattern )
    {
        addRule( pattern, new BeanPropertySetterRule() );
    }

    /**
     * Add a "bean property setter" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param propertyName Name of property to set
     * @see BeanPropertySetterRule
     */
    public void addBeanPropertySetter( final String pattern, final String propertyName )
    {
        addRule( pattern, new BeanPropertySetterRule( propertyName ) );
    }

    /**
     * Add an "call method" rule for a method which accepts no arguments.
     *
     * @param pattern Element matching pattern
     * @param methodName Method name to be called
     * @see CallMethodRule
     */
    public void addCallMethod( final String pattern, final String methodName )
    {
        addRule( pattern, new CallMethodRule( methodName ) );
    }

    /**
     * Add an "call method" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param methodName Method name to be called
     * @param paramCount Number of expected parameters (or zero for a single parameter from the body of this element)
     * @see CallMethodRule
     */
    public void addCallMethod( final String pattern, final String methodName, final int paramCount )
    {
        addRule( pattern, new CallMethodRule( methodName, paramCount ) );
    }

    /**
     * Add an "call method" rule for the specified parameters. If <code>paramCount</code> is set to zero the rule will
     * use the body of the matched element as the single argument of the method, unless <code>paramTypes</code> is null
     * or empty, in this case the rule will call the specified method with no arguments.
     *
     * @param pattern Element matching pattern
     * @param methodName Method name to be called
     * @param paramCount Number of expected parameters (or zero for a single parameter from the body of this element)
     * @param paramTypes Set of Java class names for the types of the expected parameters (if you wish to use a
     *            primitive type, specify the corresonding Java wrapper class instead, such as
     *            <code>java.lang.Boolean</code> for a <code>boolean</code> parameter)
     * @see CallMethodRule
     */
    public void addCallMethod( final String pattern, final String methodName, final int paramCount, final String paramTypes[] )
    {
        addRule( pattern, new CallMethodRule( methodName, paramCount, paramTypes ) );
    }

    /**
     * Add an "call method" rule for the specified parameters. If <code>paramCount</code> is set to zero the rule will
     * use the body of the matched element as the single argument of the method, unless <code>paramTypes</code> is null
     * or empty, in this case the rule will call the specified method with no arguments.
     *
     * @param pattern Element matching pattern
     * @param methodName Method name to be called
     * @param paramCount Number of expected parameters (or zero for a single parameter from the body of this element)
     * @param paramTypes The Java class names of the arguments (if you wish to use a primitive type, specify the
     *            corresonding Java wrapper class instead, such as <code>java.lang.Boolean</code> for a
     *            <code>boolean</code> parameter)
     * @see CallMethodRule
     */
    public void addCallMethod( final String pattern, final String methodName, final int paramCount, final Class<?> paramTypes[] )
    {
        addRule( pattern, new CallMethodRule( methodName, paramCount, paramTypes ) );
    }

    /**
     * Add a "call parameter" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param paramIndex Zero-relative parameter index to set (from the body of this element)
     * @see CallParamRule
     */
    public void addCallParam( final String pattern, final int paramIndex )
    {
        addRule( pattern, new CallParamRule( paramIndex ) );
    }

    /**
     * Add a "call parameter" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param paramIndex Zero-relative parameter index to set (from the specified attribute)
     * @param attributeName Attribute whose value is used as the parameter value
     * @see CallParamRule
     */
    public void addCallParam( final String pattern, final int paramIndex, final String attributeName )
    {
        addRule( pattern, new CallParamRule( paramIndex, attributeName ) );
    }

    /**
     * Add a "call parameter" rule. This will either take a parameter from the stack or from the current element body
     * text.
     *
     * @param pattern Element matching pattern
     * @param paramIndex The zero-relative parameter number
     * @param fromStack Should the call parameter be taken from the top of the stack?
     * @see CallParamRule
     */
    public void addCallParam( final String pattern, final int paramIndex, final boolean fromStack )
    {
        addRule( pattern, new CallParamRule( paramIndex, fromStack ) );
    }

    /**
     * Add a "call parameter" rule that sets a parameter from the stack. This takes a parameter from the given position
     * on the stack.
     *
     * @param pattern Element matching pattern
     * @param paramIndex The zero-relative parameter number
     * @param stackIndex set the call parameter to the stackIndex'th object down the stack, where 0 is the top of the
     *            stack, 1 the next element down and so on
     * @see CallMethodRule
     */
    public void addCallParam( final String pattern, final int paramIndex, final int stackIndex )
    {
        addRule( pattern, new CallParamRule( paramIndex, stackIndex ) );
    }

    /**
     * Add a "call parameter" rule that sets a parameter from the current <code>Digester</code> matching path. This is
     * sometimes useful when using rules that support wildcards.
     *
     * @param pattern the pattern that this rule should match
     * @param paramIndex The zero-relative parameter number
     * @see CallMethodRule
     */
    public void addCallParamPath( final String pattern, final int paramIndex )
    {
        addRule( pattern, new PathCallParamRule( paramIndex ) );
    }

    /**
     * Add a "call parameter" rule that sets a parameter from a caller-provided object. This can be used to pass
     * constants such as strings to methods; it can also be used to pass mutable objects, providing ways for objects to
     * do things like "register" themselves with some shared object.
     * <p>
     * Note that when attempting to locate a matching method to invoke, the true type of the paramObj is used, so that
     * despite the paramObj being passed in here as type Object, the target method can declare its parameters as being
     * the true type of the object (or some ancestor type, according to the usual type-conversion rules).
     *
     * @param pattern Element matching pattern
     * @param paramIndex The zero-relative parameter number
     * @param paramObj Any arbitrary object to be passed to the target method.
     * @see CallMethodRule
     * @since 1.6
     */
    public void addObjectParam( final String pattern, final int paramIndex, final Object paramObj )
    {
        addRule( pattern, new ObjectParamRule( paramIndex, paramObj ) );
    }

    /**
     * Add a "factory create" rule for the specified parameters. Exceptions thrown during the object creation process
     * will be propagated.
     *
     * @param pattern Element matching pattern
     * @param className Java class name of the object creation factory class
     * @see FactoryCreateRule
     */
    public void addFactoryCreate( final String pattern, final String className )
    {
        addFactoryCreate( pattern, className, false );
    }

    /**
     * Add a "factory create" rule for the specified parameters. Exceptions thrown during the object creation process
     * will be propagated.
     *
     * @param pattern Element matching pattern
     * @param clazz Java class of the object creation factory class
     * @see FactoryCreateRule
     */
    public void addFactoryCreate( final String pattern, final Class<? extends ObjectCreationFactory<?>> clazz )
    {
        addFactoryCreate( pattern, clazz, false );
    }

    /**
     * Add a "factory create" rule for the specified parameters. Exceptions thrown during the object creation process
     * will be propagated.
     *
     * @param pattern Element matching pattern
     * @param className Java class name of the object creation factory class
     * @param attributeName Attribute name which, if present, overrides the value specified by <code>className</code>
     * @see FactoryCreateRule
     */
    public void addFactoryCreate( final String pattern, final String className, final String attributeName )
    {
        addFactoryCreate( pattern, className, attributeName, false );
    }

    /**
     * Add a "factory create" rule for the specified parameters. Exceptions thrown during the object creation process
     * will be propagated.
     *
     * @param pattern Element matching pattern
     * @param clazz Java class of the object creation factory class
     * @param attributeName Attribute name which, if present, overrides the value specified by <code>className</code>
     * @see FactoryCreateRule
     */
    public void addFactoryCreate( final String pattern, final Class<? extends ObjectCreationFactory<?>> clazz,
                                  final String attributeName )
    {
        addFactoryCreate( pattern, clazz, attributeName, false );
    }

    /**
     * Add a "factory create" rule for the specified parameters. Exceptions thrown during the object creation process
     * will be propagated.
     *
     * @param pattern Element matching pattern
     * @param creationFactory Previously instantiated ObjectCreationFactory to be utilized
     * @see FactoryCreateRule
     */
    public void addFactoryCreate( final String pattern, final ObjectCreationFactory<?> creationFactory )
    {
        addFactoryCreate( pattern, creationFactory, false );
    }

    /**
     * Add a "factory create" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param className Java class name of the object creation factory class
     * @param ignoreCreateExceptions when <code>true</code> any exceptions thrown during object creation will be
     *            ignored.
     * @see FactoryCreateRule
     */
    public void addFactoryCreate( final String pattern, final String className, final boolean ignoreCreateExceptions )
    {
        addRule( pattern, new FactoryCreateRule( className, ignoreCreateExceptions ) );
    }

    /**
     * Add a "factory create" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param clazz Java class of the object creation factory class
     * @param ignoreCreateExceptions when <code>true</code> any exceptions thrown during object creation will be
     *            ignored.
     * @see FactoryCreateRule
     */
    public void addFactoryCreate( final String pattern, final Class<? extends ObjectCreationFactory<?>> clazz,
                                  final boolean ignoreCreateExceptions )
    {
        addRule( pattern, new FactoryCreateRule( clazz, ignoreCreateExceptions ) );
    }

    /**
     * Add a "factory create" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param className Java class name of the object creation factory class
     * @param attributeName Attribute name which, if present, overrides the value specified by <code>className</code>
     * @param ignoreCreateExceptions when <code>true</code> any exceptions thrown during object creation will be
     *            ignored.
     * @see FactoryCreateRule
     */
    public void addFactoryCreate( final String pattern, final String className, final String attributeName,
                                  final boolean ignoreCreateExceptions )
    {
        addRule( pattern, new FactoryCreateRule( className, attributeName, ignoreCreateExceptions ) );
    }

    /**
     * Add a "factory create" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param clazz Java class of the object creation factory class
     * @param attributeName Attribute name which, if present, overrides the value specified by <code>className</code>
     * @param ignoreCreateExceptions when <code>true</code> any exceptions thrown during object creation will be
     *            ignored.
     * @see FactoryCreateRule
     */
    public void addFactoryCreate( final String pattern, final Class<? extends ObjectCreationFactory<?>> clazz,
                                  final String attributeName, final boolean ignoreCreateExceptions )
    {
        addRule( pattern, new FactoryCreateRule( clazz, attributeName, ignoreCreateExceptions ) );
    }

    /**
     * Add a "factory create" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param creationFactory Previously instantiated ObjectCreationFactory to be utilized
     * @param ignoreCreateExceptions when <code>true</code> any exceptions thrown during object creation will be
     *            ignored.
     * @see FactoryCreateRule
     */
    public void addFactoryCreate( final String pattern, final ObjectCreationFactory<?> creationFactory,
                                  final boolean ignoreCreateExceptions )
    {
        creationFactory.setDigester( this );
        addRule( pattern, new FactoryCreateRule( creationFactory, ignoreCreateExceptions ) );
    }

    /**
     * Add an "object create" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param className Java class name to be created
     * @see ObjectCreateRule
     */
    public void addObjectCreate( final String pattern, final String className )
    {
        addRule( pattern, new ObjectCreateRule( className ) );
    }

    /**
     * Add an "object create" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param clazz Java class to be created
     * @see ObjectCreateRule
     */
    public void addObjectCreate( final String pattern, final Class<?> clazz )
    {
        addRule( pattern, new ObjectCreateRule( clazz ) );
    }

    /**
     * Add an "object create" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param className Default Java class name to be created
     * @param attributeName Attribute name that optionally overrides the default Java class name to be created
     * @see ObjectCreateRule
     */
    public void addObjectCreate( final String pattern, final String className, final String attributeName )
    {
        addRule( pattern, new ObjectCreateRule( className, attributeName ) );
    }

    /**
     * Add an "object create" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param attributeName Attribute name that optionally overrides
     * @param clazz Default Java class to be created the default Java class name to be created
     * @see ObjectCreateRule
     */
    public void addObjectCreate( final String pattern, final String attributeName, final Class<?> clazz )
    {
        addRule( pattern, new ObjectCreateRule( attributeName, clazz ) );
    }

    /**
     * Adds an {@link SetNestedPropertiesRule}.
     *
     * @param pattern register the rule with this pattern
     * @since 1.6
     */
    public void addSetNestedProperties( final String pattern )
    {
        addRule( pattern, new SetNestedPropertiesRule() );
    }

    /**
     * Adds an {@link SetNestedPropertiesRule}.
     *
     * @param pattern register the rule with this pattern
     * @param elementName elment name that a property maps to
     * @param propertyName property name of the element mapped from
     * @since 1.6
     */
    public void addSetNestedProperties( final String pattern, final String elementName, final String propertyName )
    {
        addRule( pattern, new SetNestedPropertiesRule( elementName, propertyName ) );
    }

    /**
     * Adds an {@link SetNestedPropertiesRule}.
     *
     * @param pattern register the rule with this pattern
     * @param elementNames elment names that (in order) map to properties
     * @param propertyNames property names that (in order) elements are mapped to
     * @since 1.6
     */
    public void addSetNestedProperties( final String pattern, final String[] elementNames, final String[] propertyNames )
    {
        addRule( pattern, new SetNestedPropertiesRule( elementNames, propertyNames ) );
    }

    /**
     * Add a "set next" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param methodName Method name to call on the parent element
     * @see SetNextRule
     */
    public void addSetNext( final String pattern, final String methodName )
    {
        addRule( pattern, new SetNextRule( methodName ) );
    }

    /**
     * Add a "set next" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param methodName Method name to call on the parent element
     * @param paramType Java class name of the expected parameter type (if you wish to use a primitive type, specify the
     *            corresonding Java wrapper class instead, such as <code>java.lang.Boolean</code> for a
     *            <code>boolean</code> parameter)
     * @see SetNextRule
     */
    public void addSetNext( final String pattern, final String methodName, final String paramType )
    {
        addRule( pattern, new SetNextRule( methodName, paramType ) );
    }

    /**
     * Add {@link SetRootRule} with the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param methodName Method name to call on the root object
     * @see SetRootRule
     */
    public void addSetRoot( final String pattern, final String methodName )
    {
        addRule( pattern, new SetRootRule( methodName ) );
    }

    /**
     * Add {@link SetRootRule} with the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param methodName Method name to call on the root object
     * @param paramType Java class name of the expected parameter type
     * @see SetRootRule
     */
    public void addSetRoot( final String pattern, final String methodName, final String paramType )
    {
        addRule( pattern, new SetRootRule( methodName, paramType ) );
    }

    /**
     * Add a "set properties" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @see SetPropertiesRule
     */
    public void addSetProperties( final String pattern )
    {
        addRule( pattern, new SetPropertiesRule() );
    }

    /**
     * Add a "set properties" rule with a single overridden parameter. See
     * {@link SetPropertiesRule#SetPropertiesRule(String attributeName, String propertyName)}
     *
     * @param pattern Element matching pattern
     * @param attributeName map this attribute
     * @param propertyName to this property
     * @see SetPropertiesRule
     */
    public void addSetProperties( final String pattern, final String attributeName, final String propertyName )
    {
        addRule( pattern, new SetPropertiesRule( attributeName, propertyName ) );
    }

    /**
     * Add a "set properties" rule with overridden parameters. See
     * {@link SetPropertiesRule#SetPropertiesRule(String [] attributeNames, String [] propertyNames)}
     *
     * @param pattern Element matching pattern
     * @param attributeNames names of attributes with custom mappings
     * @param propertyNames property names these attributes map to
     * @see SetPropertiesRule
     */
    public void addSetProperties( final String pattern, final String[] attributeNames, final String[] propertyNames )
    {
        addRule( pattern, new SetPropertiesRule( attributeNames, propertyNames ) );
    }

    /**
     * Add a "set property" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param name Attribute name containing the property name to be set
     * @param value Attribute name containing the property value to set
     * @see SetPropertyRule
     */
    public void addSetProperty( final String pattern, final String name, final String value )
    {
        addRule( pattern, new SetPropertyRule( name, value ) );
    }

    /**
     * Add a "set top" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param methodName Method name to call on the parent element
     * @see SetTopRule
     */
    public void addSetTop( final String pattern, final String methodName )
    {
        addRule( pattern, new SetTopRule( methodName ) );
    }

    /**
     * Add a "set top" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param methodName Method name to call on the parent element
     * @param paramType Java class name of the expected parameter type (if you wish to use a primitive type, specify the
     *            corresonding Java wrapper class instead, such as <code>java.lang.Boolean</code> for a
     *            <code>boolean</code> parameter)
     * @see SetTopRule
     */
    public void addSetTop( final String pattern, final String methodName, final String paramType )
    {
        addRule( pattern, new SetTopRule( methodName, paramType ) );
    }

    // --------------------------------------------------- Object Stack Methods

    /**
     * Clear the current contents of the default object stack, the param stack, all named stacks, and other internal
     * variables.
     * <p>
     * Calling this method <i>might</i> allow another document of the same type to be correctly parsed. However this
     * method was not intended for this purpose (just to tidy up memory usage). In general, a separate Digester object
     * should be created for each document to be parsed.
     * <p>
     * Note that this method is called automatically after a document has been successfully parsed by a Digester
     * instance. However it is not invoked automatically when a parse fails, so when reusing a Digester instance (which
     * is not recommended) this method <i>must</i> be called manually after a parse failure.
     */
    public void clear()
    {
        match = "";
        bodyTexts.clear();
        params.clear();
        publicId = null;
        stack.clear();
        stacksByName.clear();
        customContentHandler = null;
    }

    /**
     * Return the top object on the stack without removing it.
     *
     * If there are no objects on the stack, return <code>null</code>.
     *
     * @param <T> the type used to auto-cast the returned object to the assigned variable type
     * @return the top object on the stack without removing it.
     */
    public <T> T peek()
    {
        try
        {
            return this.<T> npeSafeCast( stack.peek() );
        }
        catch ( final EmptyStackException e )
        {
            log.warn( "Empty stack (returning null)" );
            return ( null );
        }
    }

    /**
     * Return the n'th object down the stack, where 0 is the top element and [getCount()-1] is the bottom element. If
     * the specified index is out of range, return <code>null</code>.
     *
     * @param <T> the type used to auto-cast the returned object to the assigned variable type
     * @param n Index of the desired element, where 0 is the top of the stack, 1 is the next element down, and so on.
     * @return the n'th object down the stack
     */
    public <T> T peek( final int n )
    {
        final int index = ( stack.size() - 1 ) - n;
        if ( index < 0 )
        {
            log.warn( "Empty stack (returning null)" );
            return ( null );
        }
        try
        {
            return this.<T> npeSafeCast( stack.get( index ) );
        }
        catch ( final EmptyStackException e )
        {
            log.warn( "Empty stack (returning null)" );
            return ( null );
        }
    }

    /**
     * Pop the top object off of the stack, and return it. If there are no objects on the stack, return
     * <code>null</code>.
     *
     * @param <T> the type used to auto-cast the returned object to the assigned variable type
     * @return the top object popped off of the stack
     */
    public <T> T pop()
    {
        try
        {
            T popped = this.<T> npeSafeCast( stack.pop() );
            if ( stackAction != null )
            {
                popped = stackAction.onPop( this, null, popped );
            }
            return popped;
        }
        catch ( final EmptyStackException e )
        {
            log.warn( "Empty stack (returning null)" );
            return ( null );
        }
    }

    /**
     * Push a new object onto the top of the object stack.
     *
     * @param <T> any type of the pushed object
     * @param object The new object
     */
    public <T> void push( T object )
    {
        if ( stackAction != null )
        {
            object = stackAction.onPush( this, null, object );
        }

        if ( stack.isEmpty() )
        {
            root = object;
        }
        stack.push( object );
    }

    /**
     * Pushes the given object onto the stack with the given name. If no stack already exists with the given name then
     * one will be created.
     *
     * @param <T> any type of the pushed object
     * @param stackName the name of the stack onto which the object should be pushed
     * @param value the Object to be pushed onto the named stack.
     * @since 1.6
     */
    public <T> void push( final String stackName, T value )
    {
        if ( stackAction != null )
        {
            value = stackAction.onPush( this, stackName, value );
        }

        Stack<Object> namedStack = stacksByName.get( stackName );
        if ( namedStack == null )
        {
            namedStack = new Stack<Object>();
            stacksByName.put( stackName, namedStack );
        }
        namedStack.push( value );
    }

    /**
     * <p>
     * Pops (gets and removes) the top object from the stack with the given name.
     * </p>
     * <p>
     * <strong>Note:</strong> a stack is considered empty if no objects have been pushed onto it yet.
     * </p>
     *
     * @param <T> the type used to auto-cast the returned object to the assigned variable type
     * @param stackName the name of the stack from which the top value is to be popped.
     * @return the top <code>Object</code> on the stack or throws {@code EmptyStackException}
     *         if the stack is either empty or has not been created yet
     * @since 1.6
     */
    public <T> T pop( final String stackName )
    {
        final Stack<Object> namedStack = stacksByName.get( stackName );
        if ( namedStack == null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Stack '" + stackName + "' is empty" );
            }
            throw new EmptyStackException();
        }

        T result = this.<T> npeSafeCast( namedStack.pop() );

        if ( stackAction != null )
        {
            result = stackAction.onPop( this, stackName, result );
        }

        return result;
    }

    /**
     * <p>
     * Gets the top object from the stack with the given name. This method does not remove the object from the stack.
     * </p>
     * <p>
     * <strong>Note:</strong> a stack is considered empty if no objects have been pushed onto it yet.
     * </p>
     *
     * @param <T> the type used to auto-cast the returned object to the assigned variable type
     * @param stackName the name of the stack to be peeked
     * @return the top <code>Object</code> on the stack or null if the stack is either empty or has not been created yet
     * @since 1.6
     */
    public <T> T peek( final String stackName )
    {
        return this.<T> npeSafeCast( peek( stackName, 0 ) );
    }

    /**
     * <p>
     * Gets the top object from the stack with the given name. This method does not remove the object from the stack.
     * </p>
     * <p>
     * <strong>Note:</strong> a stack is considered empty if no objects have been pushed onto it yet.
     * </p>
     *
     * @param <T> the type used to auto-cast the returned object to the assigned variable type
     * @param stackName the name of the stack to be peeked
     * @param n Index of the desired element, where 0 is the top of the stack, 1 is the next element down, and so on.
     * @return the specified <code>Object</code> on the stack.
     * @since 1.6
     */
    public <T> T peek( final String stackName, final int n )
    {
        T result;
        final Stack<Object> namedStack = stacksByName.get( stackName );
        if ( namedStack == null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Stack '" + stackName + "' is empty" );
            }
            throw new EmptyStackException();
        }

        final int index = ( namedStack.size() - 1 ) - n;
        if ( index < 0 )
        {
            throw new EmptyStackException();
        }
        result = this.<T> npeSafeCast( namedStack.get( index ) );

        return result;
    }

    /**
     * <p>
     * Is the stack with the given name empty?
     * </p>
     * <p>
     * <strong>Note:</strong> a stack is considered empty if no objects have been pushed onto it yet.
     * </p>
     *
     * @param stackName the name of the stack whose emptiness should be evaluated
     * @return true if the given stack if empty
     * @since 1.6
     */
    public boolean isEmpty( final String stackName )
    {
        boolean result = true;
        final Stack<Object> namedStack = stacksByName.get( stackName );
        if ( namedStack != null )
        {
            result = namedStack.isEmpty();
        }
        return result;
    }

    /**
     * Returns the root element of the tree of objects created as a result of applying the rule objects to the input
     * XML.
     * <p>
     * If the digester stack was "primed" by explicitly pushing a root object onto the stack before parsing started,
     * then that root object is returned here.
     * <p>
     * Alternatively, if a Rule which creates an object (eg ObjectCreateRule) matched the root element of the xml, then
     * the object created will be returned here.
     * <p>
     * In other cases, the object most recently pushed onto an empty digester stack is returned. This would be a most
     * unusual use of digester, however; one of the previous configurations is much more likely.
     * <p>
     * Note that when using one of the Digester.parse methods, the return value from the parse method is exactly the
     * same as the return value from this method. However when the Digester is being used as a SAXContentHandler, no
     * such return value is available; in this case, this method allows you to access the root object that has been
     * created after parsing has completed.
     *
     * @param <T> the type used to auto-cast the returned object to the assigned variable type
     * @return the root object that has been created after parsing or null if the digester has not parsed any XML yet.
     */
    public <T> T getRoot()
    {
        return this.<T> npeSafeCast( root );
    }

    /**
     * This method allows the "root" variable to be reset to null.
     * <p>
     * It is not considered safe for a digester instance to be reused to parse multiple xml documents. However if you
     * are determined to do so, then you should call both clear() and resetRoot() before each parse.
     *
     * @since 1.7
     */
    public void resetRoot()
    {
        root = null;
    }

    // ------------------------------------------------ Parameter Stack Methods

    // ------------------------------------------------------ Protected Methods

    /**
     * <p>
     * Clean up allocated resources after parsing is complete. The default method closes input streams that have been
     * created by Digester itself. If you override this method in a subclass, be sure to call
     * <code>super.cleanup()</code> to invoke this logic.
     * </p>
     *
     * @since 1.8
     */
    protected void cleanup()
    {
        // If we created any InputSource objects in this instance,
        // they each have an input stream that should be closed
        for ( final InputSource source : inputSources )
        {
            try
            {
                source.getByteStream().close();
            }
            catch ( final IOException e )
            {
                // Fall through so we get them all
                if ( log.isWarnEnabled() )
                {
                    log.warn( format( "An error occurred while closing resource %s (%s)",
                                      source.getPublicId(),
                                      source.getSystemId() ), e );
                }
            }
        }
        inputSources.clear();
    }

    /**
     * <p>
     * Provide a hook for lazy configuration of this <code>Digester</code> instance. The default implementation does
     * nothing, but subclasses can override as needed.
     * </p>
     * <p>
     * <strong>Note</strong> This method may be called more than once. Once only initialization code should be placed in
     * {@link #initialize} or the code should take responsibility by checking and setting the {@link #configured} flag.
     * </p>
     */
    protected void configure()
    {
        // Do not configure more than once
        if ( configured )
        {
            return;
        }

        // Perform lazy configuration as needed
        initialize(); // call hook method for subclasses that want to be initialized once only
        // Nothing else required by default

        // Set the configuration flag to avoid repeating
        configured = true;
    }

    /**
     * Checks the Digester instance has been configured.
     *
     * @return true, if the Digester instance has been configured, false otherwise
     * @since 3.0
     */
    public boolean isConfigured()
    {
        return configured;
    }

    /**
     * <p>
     * Provides a hook for lazy initialization of this <code>Digester</code> instance. The default implementation does
     * nothing, but subclasses can override as needed. Digester (by default) only calls this method once.
     * </p>
     * <p>
     * <strong>Note</strong> This method will be called by {@link #configure} only when the {@link #configured} flag is
     * false. Subclasses that override <code>configure</code> or who set <code>configured</code> may find that this
     * method may be called more than once.
     * </p>
     *
     * @since 1.6
     */
    protected void initialize()
    {
        // Perform lazy initialization as needed
        // Nothing required by default
    }

    // -------------------------------------------------------- Package Methods

    /**
     * Return the set of DTD URL registrations, keyed by public identifier. NOTE: the returned map is in read-only mode.
     *
     * @return the read-only Map of DTD URL registrations.
     */
    Map<String, URL> getRegistrations()
    {
        return Collections.unmodifiableMap( entityValidator );
    }

    /**
     * <p>
     * Return the top object on the parameters stack without removing it. If there are no objects on the stack, return
     * <code>null</code>.
     * </p>
     * <p>
     * The parameters stack is used to store <code>CallMethodRule</code> parameters. See {@link #params}.
     * </p>
     *
     * @return the top object on the parameters stack without removing it.
     */
    public Object[] peekParams()
    {
        try
        {
            return ( params.peek() );
        }
        catch ( final EmptyStackException e )
        {
            log.warn( "Empty stack (returning null)" );
            return ( null );
        }
    }

    /**
     * <p>
     * Return the n'th object down the parameters stack, where 0 is the top element and [getCount()-1] is the bottom
     * element. If the specified index is out of range, return <code>null</code>.
     * </p>
     * <p>
     * The parameters stack is used to store <code>CallMethodRule</code> parameters. See {@link #params}.
     * </p>
     *
     * @param n Index of the desired element, where 0 is the top of the stack, 1 is the next element down, and so on.
     * @return the n'th object down the parameters stack
     */
    public Object[] peekParams( final int n )
    {
        final int index = ( params.size() - 1 ) - n;
        if ( index < 0 )
        {
            log.warn( "Empty stack (returning null)" );
            return ( null );
        }
        try
        {
            return ( params.get( index ) );
        }
        catch ( final EmptyStackException e )
        {
            log.warn( "Empty stack (returning null)" );
            return ( null );
        }
    }

    /**
     * <p>
     * Pop the top object off of the parameters stack, and return it. If there are no objects on the stack, return
     * <code>null</code>.
     * </p>
     * <p>
     * The parameters stack is used to store <code>CallMethodRule</code> parameters. See {@link #params}.
     * </p>
     *
     * @return the top object popped off of the parameters stack
     */
    public Object[] popParams()
    {
        try
        {
            if ( log.isTraceEnabled() )
            {
                log.trace( "Popping params" );
            }
            return ( params.pop() );
        }
        catch ( final EmptyStackException e )
        {
            log.warn( "Empty stack (returning null)" );
            return ( null );
        }
    }

    /**
     * <p>
     * Push a new object onto the top of the parameters stack.
     * </p>
     * <p>
     * The parameters stack is used to store <code>CallMethodRule</code> parameters. See {@link #params}.
     * </p>
     *
     * @param object The new object
     */
    public void pushParams( final Object... object )
    {
        if ( log.isTraceEnabled() )
        {
            log.trace( "Pushing params" );
        }
        params.push( object );
    }

    /**
     * Create a SAX exception which also understands about the location in the digester file where the exception occurs
     *
     * @param message the custom SAX exception message
     * @param e the exception cause
     * @return the new SAX exception
     */
    public SAXException createSAXException( final String message, Exception e )
    {
        if ( ( e != null ) && ( e instanceof InvocationTargetException ) )
        {
            final Throwable t = ( (InvocationTargetException) e ).getTargetException();
            if ( ( t != null ) && ( t instanceof Exception ) )
            {
                e = (Exception) t;
            }
        }
        if ( locator != null )
        {
            final String error =
                "Error at line " + locator.getLineNumber() + " char " + locator.getColumnNumber() + ": " + message;
            if ( e != null )
            {
                return new SAXParseException( error, locator, e );
            }
            return new SAXParseException( error, locator );
        }
        log.error( "No Locator!" );
        if ( e != null )
        {
            return new SAXException( message, e );
        }
        return new SAXException( message );
    }

    /**
     * Create a SAX exception which also understands about the location in the digester file where the exception occurs
     *
     * @param e the exception cause
     * @return the new SAX exception
     */
    public SAXException createSAXException( Exception e )
    {
        if ( e instanceof InvocationTargetException )
        {
            final Throwable t = ( (InvocationTargetException) e ).getTargetException();
            if ( ( t != null ) && ( t instanceof Exception ) )
            {
                e = (Exception) t;
            }
        }
        return createSAXException( e.getMessage(), e );
    }

    /**
     * Create a SAX exception which also understands about the location in the digester file where the exception occurs
     *
     * @param message the custom SAX exception message
     * @return the new SAX exception
     */
    public SAXException createSAXException( final String message )
    {
        return createSAXException( message, null );
    }

    /**
     * Helps casting the input object to given type, avoiding NPEs.
     *
     * @since 3.0
     * @param <T> the type the input object has to be cast.
     * @param obj the object has to be cast.
     * @return the casted object, if input object is not null, null otherwise.
     */
    private <T> T npeSafeCast( final Object obj )
    {
        if ( obj == null )
        {
            return null;
        }

        @SuppressWarnings( "unchecked" )
        final
        T result = (T) obj;
        return result;
    }

}
