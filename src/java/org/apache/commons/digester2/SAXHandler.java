/* $Id$
 *
 * Copyright 2001-2005 The Apache Software Foundation.
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

package org.apache.commons.digester2;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import java.lang.reflect.InvocationTargetException;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.collections.ArrayStack;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.ext.LexicalHandler;

/**
 * <p>An object which handles SAX events generated during parsing of an
 * input document, and invokes the appropriate methods on the appropriate
 * rule objects.</p>
 *
 * <p>This class is not responsible for instantiating a SAX parser to
 * parse the input; it just handles the resulting events.
 * <p>
 * When a parser is built by a Digester instance, the resulting parser will
 * have this object set as the content-handler, entity-resolver, error-handler,
 * and dtd-handler. However if the user has used Digester APIs to explicitly
 * specify their own entity-resolver, error-handler or dtd-handler then this
 * object will delegate to the user-provided class (after some basic logging
 * and housekeeping).
 * <p>
 * If the user chooses to instantiate the parser themselves, then they must
 * set the content-handler to a SAXHandler, but may point the entity-resolver,
 * error-handler and dtd-handler callbacks at any object they wish.
 * <p>
 * The content-handler callbacks implemented here (beginDocument, endDocument,
 * beginElement, characters and endElement) are used to provide the standard
 * digester functionality.
 * <p>
 * The entity-resolver callbacks implemented here perform a lookup of the
 * entity in a (publicid->url) table configured via calls to the register(...)
 * method. For entities which are not found to be registered, there systemid
 * (if any) is used to attempt to locate them.
 * <p>
 * The error-handler callbacks implemented here cause a DigestionException
 * to be thrown if the xml parser reports an error. Warnings are logged
 * but otherwise ignored.
 * <p>
 * The dtd-handler callbacks (notationDecl and unparsedEntityDecl) are simply
 * logged, then ignored.
 */

public class SAXHandler extends DefaultHandler implements LexicalHandler {

    // --------------------------------------------------------- Constructors

    /**
     * Construct a new SAXHandler.
     */
    public SAXHandler() {
        super();
    }

    // --------------------------------------------------- Instance Variables

    /**
     * The EntityResolver used to look up any external entities referenced
     * from within the input xml. Note that this class always receives the
     * event initally, so that they can be logged, but if the user has
     * specified an entityResolver then the events are also forwarded to
     * the provided object.
     */
    private EntityResolver entityResolver = null;

    /**
     * The application-supplied error handler that is notified when parsing
     * warnings, errors, or fatal errors occur. Note that this class always
     * handles the errors initially, so that they can be logged, but if the
     * user has specified an errorHandler then the events are also forwarded
     * to the provided object.
     */
    private ErrorHandler errorHandler = null;

    /**
     * The Locator associated with our parser.
     */
    private Locator locator = null;

    /**
     * A count of the number of entities resolved. Currently, we only
     * care whether this is zero or one, so a boolean could do as well.
     * However it seems likely that a count could be useful at some time.
     */
    private int numEntitiesResolved = 0;
    
    /**
     * A map of known external entities that input xml documents may refer to.
     * via public or system IDs. The keys of the map entries are public or
     * system IDs, and the values are URLs (typically local files) pointing
     * to locations where those entities can be found.
     * <p>
     * See #setKnownEntities, #getKnownEntities, #registerKnownEntity
     */
    private Map knownEntities = new HashMap();

    /**
     * See setAllowUnknownExternalEntities.
     */
    private boolean allowUnknownExternalEntities = false;

    /**
     * See setIgnoreExternalDTD.
     */
    private boolean ignoreExternalDTD = false;
    
    /**
     * An object which contains state information that evolves
     * as the parse progresses. Rule object commonly interact with
     * the context object.
     */
    private Context context = null;

    /**
     * The <code>Rules</code> implementation containing our collection of
     * <code>Rule</code> instances and associated matching policy.  If not
     * established before the first rule is added, a default implementation
     * will be provided.
     */
    private RuleManager ruleManager = null;

    /**
     * The initial object (if any) that the stack should be primed with
     * before parsing starts.
     */
    private Object initialObject = null;

    /**
     * The Log to which most logging calls will be made.
     */
    private Log log =
        LogFactory.getLog("org.apache.commons.digester.Digester");

    /**
     * The Log to which all SAX event related logging calls will be made.
     */
    private Log saxLog =
        LogFactory.getLog("org.apache.commons.digester.Digester.sax");

    /**
     * An optional class that substitutes values in attributes and body text.
     * This may be null and so a null check is always required before use.
     */
    private Substitutor substitutor;

    /**
     * The class loader to use for instantiating application objects.
     * If not specified, the context class loader, or the class loader
     * used to load Digester itself, is used, based on the value of the
     * <code>useContextClassLoader</code> variable.
     */
    private ClassLoader explicitClassLoader;

    /**
     * Do we want to use the Context classloader when loading classes for
     * instantiating new objects. Default is false.
     */
    private boolean useContextClassLoader = false;

    /**
     * Has this instance had its initialize method called yet?
     */
    private boolean initialized = false;

    // -------------------------------------------------------------------
    // Instance variables that are modified during a parse.
    // -------------------------------------------------------------------

    /**
     * If null, then calls to this objects' characters, startElement, endElement
     * and processingInstruction methods are forwarded to the specified object.
     * This is intended to allow rules to temporarily "take control" of the
     * sax events. In particular, this is used by NodeCreateAction.
     */
    private ContentHandler contentHandler = null;

    /**
     * The public identifier of the DTD we are currently parsing under
     * (if any). See method {@link #startDTD}.
     *
     * TODO: Consider if this should be moved to Context.
     */
    private String dtdPublicId = null;

    /**
     * The system identifier of the DTD we are currently parsing under
     * (if any). See method {@link #startDTD}.
     *
     * TODO: Consider if this should be moved to Context.
     */
    private String dtdSystemId = null;

    /**
     * The body text of the current element. As the parser reports chunks
     * of text associated with the current element, they are appended here.
     * When the end of the element is reported, the full text content of the
     * current element should be here. Note that if the element has mixed
     * content, ie text intermingled with child elements, then this buffer
     * ends up with all the different text pieces mixed together.
     */
    private StringBuffer bodyText = new StringBuffer();

    /**
     * When processing an element with mixed content (ie text and child
     * elements), then when we start a child element we need to store the
     * current text seen so far, and restore it after we have finished
     * with the child element. This stack therefore contains StringBuffer
     * items containing the body text of "interrupted" xml elements.
     */
    private ArrayStack bodyTexts = new ArrayStack();

    /**
     * Registered namespaces we are currently processing.  The key is the
     * namespace prefix that was declared in the document.  The value is an
     * ArrayStack of the namespace URIs this prefix has been mapped to --
     * the top Stack element is the most current one.  (This architecture
     * is required because documents can declare nested uses of the same
     * prefix for different Namespace URIs).
     */
    private HashMap namespaces = new HashMap();

    // ---------------------------------------------------------------------
    // General object configuration methods
    //
    // These methods are expected to be called by the user or by the
    // Digester class in order to set this object up ready to perform
    // parsing.
    //
    // Some methods (particularly the getters) are also used during
    // parsing.
    // ---------------------------------------------------------------------

    /**
     * Set this object to be the handler for all the event interfaces
     * provided by the specified reader. This method is called by the
     * Digester class after the XMLReader is created to ensure this object
     * gets all the necessary callbacks. If an xml parser has been created
     * directly rather than via the Digester class, then this method should
     * be called to configure the callbacks on the parser.
     */
    public void initCallbacks(XMLReader reader) {
        reader.setDTDHandler(this);
        reader.setContentHandler(this);
        reader.setEntityResolver(this);
        reader.setErrorHandler(this);

        try {
            reader.setProperty(
                "http://xml.org/sax/properties/lexical-handler",
                this);
        } catch(SAXNotRecognizedException ex) {
            // The getDTDPublicId and getDTDSystemId methods will not
            // work if the LexicalHandler interface is not supported by
            // the parser. That's not a very important feature, though,
            // so it's not worth throwing an exception for.
            log.warn(
                "This sax parser does not recognize the LexicalHandler"
                + " interface. Information on dtd public and system ids"
                + " will not be available.");
        } catch(SAXNotSupportedException ex) {
            // The getDTDPublicId and getDTDSystemId methods will not
            // work if the LexicalHandler interface is not supported by
            // the parser. That's not a very important feature, though,
            // so it's not worth throwing an exception for.
            log.warn(
                "This sax parser does not support the LexicalHandler"
                + " interface. Information on dtd public and system ids"
                + " will not be available.");
        }
    }

    /**
     * Specify a contentHandler to forward calls to. If non-null, then
     * whenever this object receives calls from the XMLReader to any of
     * the following methods, the call will be forwarded on to the specified
     * objects instead of being processed in the normal manner.
     * <p>
     * This allows an Action to assume complete control of input handling
     * for a period of time. For example, this allows the NodeCreateAction
     * to build a DOM tree representing a portion of input.
     * <p>
     * Passing null restores normal operation, ie this object then resumes
     * processing of the callbacks itself.
     */
    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    /**
     * See {@link #setContentHandler}.
     */
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    /**
     * Get the public identifier of the DTD associated with the document
     * currently being parsed, or most recently parsed.
     * <p>
     * If the input document has no DOCTYPE declaration, then null will
     * be returned.
     * <p>
     * Note that this method requires the underlying xml parser to support
     * the org.xml.sax.ext.LexicalHandler interface. If the parser does not
     * provide callbacks via this interface, then no public id information
     * will be available (null will be returned).
     */
    public String getDTDPublicId() {
        return this.dtdPublicId;
    }

    /**
     * Get the system identifier of the DTD associated with the document
     * currently being parsed, or most recently parsed.
     *
     * <p>
     * If the input document has no DOCTYPE declaration, then null will
     * be returned.
     * <p>
     * Note that this method requires the underlying xml parser to support
     * the org.xml.sax.ext.LexicalHandler interface. If the parser does not
     * provide callbacks via this interface, then no system id information
     * will be available (null will be returned).
     * <p>
     * Note also that the SystemId value returned is exactly as it was
     * defined in the DOCTYPE tag; relative URLs are NOT resolved relative
     * to the base of the current document.
     */
    public String getDTDSystemId() {
        return this.dtdSystemId;
    }

    /**
     * Set the current logger. This call should be made before parsing
     * starts.
     */
    public void setLogger(Log log) {
        this.log = log;
    }

    /**
     * Return the current Logger associated with this instance.
     */
    public Log getLogger() {
        return log;
    }

    /**
     * Sets the logger used for logging SAX-related information.
     * <strong>Note</strong> the output is finely grained.
     * @param saxLog Log, not null
     */
    public void setSAXLogger(Log saxLog) {

        this.saxLog = saxLog;
    }

    /**
     * Gets the logger used for logging SAX-related information.
     * <strong>Note</strong> the output is finely grained.
     */
    public Log getSAXLogger() {
        return saxLog;
    }

    /**
     * Set the <code>RuleManager</code> implementation object containing our
     * rules collection and associated matching policy.
     *
     * @param ruleManager New RuleManager implementation
     */
    public void setRuleManager(RuleManager ruleManager) {
        this.ruleManager = ruleManager;
    }

    /**
     * Return the <code>Rules</code> implementation object containing our
     * rules collection and associated matching policy.  If none has been
     * established, a default implementation will be created and returned.
     */
    public RuleManager getRuleManager() {
        if (ruleManager == null) {
            ruleManager = new DefaultRuleManager();
        }
        return ruleManager;
    }

    /**
     * Determine whether to use the Context ClassLoader (the one found by
     * calling <code>Thread.currentThread().getContextClassLoader()</code>)
     * to resolve/load classes that are defined in various rules.  If not
     * using Context ClassLoader, then the class-loading defaults to
     * using the calling-class' ClassLoader.
     *
     * @param use determines whether to use Context ClassLoader.
     */
    public void setUseContextClassLoader(boolean use) {
        useContextClassLoader = use;
    }

    /**
     * Return the boolean as to whether the context classloader should be used.
     */
    public boolean getUseContextClassLoader() {
        return useContextClassLoader;
    }

    /**
     * Set the class loader to be used for instantiating application objects
     * when required.
     *
     * @param classLoader The new class loader to use, or <code>null</code>
     *  to revert to the standard rules
     */
    public void setExplicitClassLoader(ClassLoader classLoader) {
        this.explicitClassLoader = classLoader;
    }

    /**
     * Get the class loader to be used by actions when instantiating objects
     * as a result of processing xml input.
     * </ul>
     */
    public ClassLoader getExplicitClassLoader() {
        return explicitClassLoader;
    }

    /**
     * Return the class loader to be used by actions when instantiating objects
     * as a result of processing xml input.
     * <p>
     * The classloader used is determined using the following procedure:
     * <ul>
     * <li>The class loader set by <code>setClassLoader()</code>, if any</li>
     * <li>The thread context class loader, if it exists and the
     *     <code>useContextClassLoader</code> property is set to true</li>
     * <li>The class loader used to load this class itself.
     * </ul>
     */
    public ClassLoader getClassLoader() {
        if (this.explicitClassLoader != null) {
            return this.explicitClassLoader;
        }

        if (this.useContextClassLoader) {
            ClassLoader classLoader =
                    Thread.currentThread().getContextClassLoader();
            if (classLoader != null) {
                return classLoader;
            }
        }

        return this.getClass().getClassLoader();
    }

    /**
     * Sets the <code>Substitutor</code> to be used to convert attributes and
     * body text. This allows manipulation of the xml input to be performed
     * before any action sees it. One application of this is to allow
     * variable substitution in the xml text, eg "&lt;foo id='$id'&gt;".
     *
     * @param substitutor the Substitutor to be used to convert attributes
     * and body text or null if no substitution of these values is to be
     * performed.
     */
    public void setSubstitutor(Substitutor substitutor) {
        this.substitutor = substitutor;
    }

    /**
     * Gets the <code>Substitutor</code> used to convert attributes and body text.
     * @return Substitutor, null if not substitutions are to be performed.
     */
    public Substitutor getSubstitutor() {
        return substitutor;
    }

    /**
     * Set the object that the stack should be primed with before parsing
     * starts.
     */
    public void setInitialObject(Object o) {
        initialObject = o;
    }

    /**
     * Return the root of the Context's object stack. Obviously, this method
     * should not be called until parsing has completed.
     */
    public Object getRoot() {
        if (context == null) {
            return null;
        } else {
            return context.getRoot();
        }
    }

    /**
     * Set the <code>EntityResolver</code> used by SAX when resolving
     * any entity references present in the input xml (including resolving
     * any DTD or schema declaration).
     * <p>
     * If null is passed, then the default behaviour is restored.
     * <p>
     * The default behaviour is to use the default behaviour of the XMLReader,
     * which is usually to attempt to download from the SYSTEM-ID value (if any)
     * of the entity definition in the input xml.
     *
     * @param entityResolver a class that implement the
     * <code>EntityResolver</code> interface.
     */
    public void setEntityResolver(EntityResolver entityResolver){
        this.entityResolver = entityResolver;
    }

    /**
     * Return the Entity Resolver used by the SAX parser.
     * @return Return the Entity Resolver used by the SAX parser.
     */
    public EntityResolver getEntityResolver(){
        return entityResolver;
    }

    /**
     * Set the error handler. If none is expliticly set, then the default
     * behaviour occurs, which is to log an error then throw an exception
     * which terminates the parsing. To restore the default behaviour after
     * setting an explicit erorr handler, pass <i>null</i>.
     *
     * @param errorHandler The new error handler
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * Return the error handler. Null indicates the default behaviour will
     * be used; see {@link #setErrorHandler}.
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Specifies a map of (publicId->URI) pairings that will be used when
     * resolving entities in the input xml (including the DTD specified with
     * DOCTYPE, or schema specified with xsi:schemaLocation).
     * <p>
     * If the value in a map entry (ie the "URI") is an empty string, then
     * when the parser asks for the entity to be resolved, an empty InputSource
     * will be returned, effectively ignoring the entity.
     */
    public void setKnownEntities(Map knownEntities) {
        this.knownEntities = knownEntities;
    }

    /**
    * Returns a map of (publicId->URI) pairings. See {@link #setKnownEntities}.
     */
    public Map getKnownEntities() {
        return knownEntities;
    }

    /**
     * <p>Register a mapping between a public or system ID (denoting an external
     * entity, aka resource) and a URL indicating where that resource can be
     * found.</p>
     *
     *<p>When the input xml refers to the entity via a public or system id, the
     * resource pointed to by the registered URL is returned. This is commonly
     * done for the input document's DTD, so that the DTD can be retrieved
     * from a local file.</p>
     *
     * <p>If the value in a map entry (ie the "URI") is an empty string, then
     * when the parser asks for the entity to be resolved, an empty InputSource
     * will be returned, effectively ignoring the entity.</p>
     *
     * <p>This implementation provides only basic functionality. If more
     * sophisticated features are required,using {@link #setEntityResolver} to
     * set a custom resolver is recommended. Note in particular that if the
     * input xml uses a system-ID to refer to an entity that is not registered,
     * then the parser will attempt to use the system-id directly, potentially
     * downloading the resource from a remote location.</p>
     *
     * <p>
     * <strong>Note:</strong> This method will have no effect when a custom
     * <code>EntityResolver</code> has been set. (Setting a custom
     * <code>EntityResolver</code> overrides the internal implementation.)
     * </p>
     * @param publicOrSystemId Public or system identifier of the entity to be
     *  resolved
     * @param entityURL The URL to use for reading this entity
     */
    public void registerKnownEntity(String publicOrSystemId, String entityURL) {
        if (log.isDebugEnabled()) {
            log.debug("register('" + publicOrSystemId + "', '" + entityURL + "'");
        }
        knownEntities.put(publicOrSystemId, entityURL);
    }

    /**
     * Specify whether an input xml document is permitted to reference external
     * entities (including external DTDs, schemas, and include-files) that have
     * not been specified by methods registerKnownEntity or setKnownEntities.
     * <p>
     * If this is allowed, then documents can take unbounded amounts of time
     * to process, as they can attempt to download entities from the network
     * (particularly via http urls).
     * <p>
     * This flag defaults to false (ie unknown external entities are not allowed).
     * In this case, any occurrence of such an entity within an xml document
     * will cause an exception to be thrown.
     */
    public void setAllowUnknownExternalEntities(boolean state) {
        allowUnknownExternalEntities = state;
    }
     
    /**
     * See setAllowUnknownExternalEntities.
     */
    public boolean getAllowUnknownExternalEntities() {
        return allowUnknownExternalEntities;
    }
     
    /**
     * Specify whether an external DTD should be ignored, ie treated as if
     * it were an empty file. This can be dangerous; DTDs can potentially
     * contain definitions for default attribute values and entities that
     * affect the meaning of the xml document, so skipping them can cause
     * incorrect output. However in many cases it is known that the DTD 
     * does no such thing, so processing of it can be suppressed.
     * <p>
     * This flag defaults to false (ie external dtds are read during the parse).
     */
    public void setIgnoreExternalDTD(boolean state) {
        ignoreExternalDTD = state;
    }
     
    /**
     * See setIgnoreExternalDTD.
     */
    public boolean getIgnoreExternalDTD() {
        return ignoreExternalDTD;
    }
     
    /**
     * Add a (pattern, action) pair to the RuleManager instance associated
     * with this saxHandler. This is equivalent to
     * <pre>
     *  getRuleManager().addRule(pattern, action);
     * </pre>
     */
    public void addRule(String pattern, Action action)
    throws InvalidRuleException {
        getRuleManager().addRule(pattern, action);
    }

    /**
     * Cleanup method which releases any memory that is no longer needed
     * after a parse has completed. There is one exception: the root
     * object generated during the parse is retained so that it can be
     * retrieved by getRoot(). If this is no longer needed, then setRoot(null)
     * should be called to release this member.
     */
    public void clear() {
        namespaces.clear();

        // It would be nice to set
        //   context = null;
        // but currently that would stuff up the getRoot() method.
    }

    /**
     * Return the currently mapped namespace URI for the specified prefix,
     * if any; otherwise return <code>null</code>.  These mappings come and
     * go dynamically as the document is parsed.
     *
     * @param prefix Prefix to look up
     */
    public String findNamespaceURI(String prefix) {
        ArrayStack stack = (ArrayStack) namespaces.get(prefix);
        if (stack == null) {
            return null;
        }
        try {
            return (String) stack.peek();
        } catch (EmptyStackException e) {
            // This should never happen, as endPrefixMapping removes
            // the prefix from the namespaces map when the stack becomes
            // empty. Still, better safe than sorry..
            return null;
        }
    }

    /**
     * Gets the document locator associated with our parser.
     * See {@link #setDocumentLocator}.
     *
     * @return the Locator supplied by the document parser
     */
    public Locator getDocumentLocator() {
        return locator;
    }

    // -------------------------------------------------------
    // Package Methods
    //
    // These methods are intended mainly for the use of Action
    // classes and other similar "implementation" classes.
    // -------------------------------------------------------

    /**
     * Create a SAX exception which also understands about the location in
     * the digester file where the exception occurs. This method is expected
     * to be called by Action classes when they detect a problem.
     *
     * @return the new exception
     */
    public SAXException createSAXException(String message, Exception e) {
        if ((e != null) &&
            (e instanceof InvocationTargetException)) {
            Throwable t = ((InvocationTargetException) e).getTargetException();
            if ((t != null) && (t instanceof Exception)) {
                e = (Exception) t;
            }
        }

        if (locator != null) {
            String error = "Error at (" + locator.getLineNumber() + ", " +
                    locator.getColumnNumber() + ": " + message;
            if (e != null) {
                return new SAXParseException(error, locator, e);
            } else {
                return new SAXParseException(error, locator);
            }
        }

        // The SAX parser doesn't have location info enabled, so we'll just
        // generate the best error message we can without it.
        log.error("No Locator!");
        if (e != null) {
            return new SAXException(message, e);
        } else {
            return new SAXException(message);
        }
    }

    /**
     * Create a SAX exception which also understands about the location in
     * the digester file where the exception occurs. This method is expected
     * to be called by Action classes when they detect a problem.
     *
     * @return the new exception
     */
    public SAXException createSAXException(Exception e) {
        if (e instanceof InvocationTargetException) {
            Throwable t = ((InvocationTargetException) e).getTargetException();
            if ((t != null) && (t instanceof Exception)) {
                e = (Exception) t;
            }
        }
        return createSAXException(e.getMessage(), e);
    }

    /**
     * Create a SAX exception which also understands about the location in
     * the digester file where the exception occurs. This method is expected
     * to be called by Action classes when they detect a problem.
     *
     * @return the new exception
     */
    public SAXException createSAXException(String message) {
        return createSAXException(message, null);
    }

    // -------------------------------------------------------
    // Overridable methods
    //
    // These methods provide hooks for users to customise the
    // behavior of this SAXHandler class by subclassing if they
    // wish.
    // -------------------------------------------------------

    /**
     * <p>Provide a hook for lazy configuration of this <code>SAXHandler</code>
     * instance. </p>
     *
     * <p>This code is called once only, immediately before the first document
     * is to be parsed. The default implementation does nothing, but subclasses
     * can override as needed.</p>
     */
     private void initialize() {
     }

    /**
     * <p>Provide a hook for lazy configuration of this <code>SAXHandler</code>
     * instance. </p>
     *
     * <p>This code is called once at the start of each input document being
     * parsed. The default implementation does nothing, but subclasses
     * can override as needed.</p>
     */
     private void initializePerParse() {
     }

    // -------------------------------------------------
    // Private methods for use of this class only
    // -------------------------------------------------

    /**
     * Invoke the initialize and initializePerParse methods.
     */
    private void configure() {
        if (!initialized) {
            // Perform lazy configuration as needed, by calling a hook method for
            // subclasses that want to be initialised once only.
            initialize();
            initialized = true;
        }

        initializePerParse();
    }

    // -------------------------------------------------
    // ContentHandler Methods
    // -------------------------------------------------

    /**
     * Sets the document locator associated with our parser. This method
     * is called by the sax parser before any other parse events are
     * dispatched this way. The object that was passed here is then
     * updated before each SAX event is dispatched to this class.
     *
     * @param locator The new locator
     *
     * TODO: Consider whether this object (and the associated createSAXException
     * method) should be on the Context rather than this object, to make it
     * easier for Action instances to access.
     *
     */
    public void setDocumentLocator(Locator locator) {
        if (saxLog.isDebugEnabled()) {
            saxLog.debug("setDocumentLocator(" + locator + ")");
        }

        this.locator = locator;
    }

    /**
     * Process notification of the beginning of the document being reached.
     * Here we perform all the once-per-input-document initialisation.
     *
     * @exception SAXException if a parsing error is to be reported
     * The SAXException thrown may be of subclass NestedSAXException,
     * in which case it has a "getCause" method that returns a nested
     * exception.
     */
    public void startDocument() throws SAXException {
        if (saxLog.isDebugEnabled()) {
            saxLog.debug("startDocument()");
        }

        numEntitiesResolved = 0;
        dtdPublicId = null;
        dtdSystemId = null;

        // This shouldn't be necesary if a parse has completed cleanly, as
        // endPrefixMapping should have been called for each namespace. But
        // on error, problems can occur.
        namespaces.clear();
        bodyText.setLength(0);
        bodyTexts.clear();

        // Create a new parsing context. This guarantees that Actions have
        // a clean slate for handling this new input document.
        context = new Context(this, log);

        if (initialObject != null) {
            context.setRoot(initialObject);
        }

        // give subclasses a chance to do custom configuration before each
        // parse if they wish.
        configure();

        try {
            // This also has the side-effect of creating a RuleManager if
            // one has not been created before. Of course this would only
            // happen if no rules had ever been added...
            getRuleManager().startParse(context);
        } catch(DigestionException ex) {
            throw new NestedSAXException(ex);
        }
    }

    /**
     * Process notification of the end of the document being reached.
     * Here we perform all the once-per-input-document initialisation.
     * Note, however, that if an error occurs during parsing of an input
     * document then this method doesn't get called.
     *
     * @exception SAXException if a parsing error is to be reported.
     * The SAXException thrown may be of subclass NestedSAXException,
     * in which case it has a "getCause" method that returns a nested
     * exception.
     */
    public void endDocument() throws SAXException {
        if (saxLog.isDebugEnabled()) {
            if (context.getStackSize() > 1) {
                // A stack depth of one is ok if an initial object was pushed
                // onto the stack. More than one is very likely to be an error.
                saxLog.debug("endDocument():  " + context.getStackSize() +
                             " elements left");
            } else {
                saxLog.debug("endDocument()");
            }
        }

        // Fire "finish" events for all defined rules
        try {
            ruleManager.finishParse(context);
        } catch(DigestionException ex) {
            log.error("finishParse threw exception", ex);
            throw new NestedSAXException(ex);
        }

        // Perform final cleanup to release memory that is no longer needed.
        clear();
    }

    /**
     * Process notification that a namespace prefix is coming in to scope.
     *
     * @param prefix Prefix that is being declared
     * @param namespaceURI Corresponding namespace URI being mapped to
     *
     * @exception SAXException if a parsing error is to be reported
     */
    public void startPrefixMapping(String prefix, String namespaceURI) {
        if (saxLog.isDebugEnabled()) {
            saxLog.debug(
                "startPrefixMapping(" + prefix + "," + namespaceURI + ")");
        }

        // Register this prefix mapping
        ArrayStack stack = (ArrayStack) namespaces.get(prefix);
        if (stack == null) {
            stack = new ArrayStack();
            namespaces.put(prefix, stack);
        }
        stack.push(namespaceURI);
    }

    /**
     * Process notification that a namespace prefix is going out of scope.
     *
     * @param prefix Prefix that is going out of scope
     *
     * @exception SAXException if a parsing error is to be reported
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        if (saxLog.isDebugEnabled()) {
            saxLog.debug("endPrefixMapping(" + prefix + ")");
        }

        // Deregister this prefix mapping
        ArrayStack stack = (ArrayStack) namespaces.get(prefix);
        if (stack == null) {
            return;
        }
        try {
            stack.pop();
            if (stack.empty())
                namespaces.remove(prefix);
        } catch (EmptyStackException e) {
            // This should never happen; it would indicate a serious
            // internal software flaw.
            throw createSAXException("endPrefixMapping popped too many times");
        }
    }

    /**
     * Process notification of character data received from the body of
     * an XML element. Note that a sax parser is allowed to split contiguous
     * text into multiple calls to this method.
     *
     * @param buffer The characters from the XML document
     * @param start Starting offset into the buffer
     * @param length Number of characters from the buffer
     *
     * @exception SAXException if a parsing error is to be reported
     */
    public void characters(char buffer[], int start, int length)
    throws SAXException {
        if (contentHandler != null) {
            // forward calls instead of handling them here
            contentHandler.characters(buffer, start, length);
            return;
        }

        if (saxLog.isDebugEnabled()) {
            saxLog.debug("characters(" + new String(buffer, start, length) + ")");
        }

        bodyText.append(buffer, start, length);
    }

    /**
     * Process notification of ignorable whitespace received from the body of
     * an XML element. Ignorable-whitespace is whitespace (tabs, spaces, etc)
     * within an element which the DTD/schema has stated has "element content"
     * only; in this case the text is regarded as being for xml layout only,
     * and is not semantically significant.
     *
     * @param buffer The characters from the XML document
     * @param start Starting offset into the buffer
     * @param len Number of characters from the buffer
     *
     * @exception SAXException if a parsing error is to be reported
     */
    public void ignorableWhitespace(char buffer[], int start, int len) {
        if (saxLog.isDebugEnabled()) {
            saxLog.debug("ignorableWhitespace(" +
                    new String(buffer, start, len) + ")");
        }

        ;   // No processing required
    }

    /**
     * Process notification of a processing instruction that was encountered.
     * In text form, a processing instruction is of form
     * <pre>
     * &lt;? sometext ?&gt;
     * </pre>
     *
     * Note, however, that a DOCTYPE is not a processing instruction, though
     * it does look like one.
     *
     * @param target The processing instruction target
     * @param data The processing instruction data (if any)
     *
     * @exception SAXException if a parsing error is to be reported
     */
    public void processingInstruction(String target, String data)
    throws SAXException {
        if (contentHandler != null) {
            // forward calls instead of handling them here
            contentHandler.processingInstruction(target, data);
            return;
        }

        if (saxLog.isDebugEnabled()) {
            saxLog.debug("processingInstruction('" + target + "','" + data + "')");
        }

        ;   // No processing is required
    }

    /**
     * Process notification of a skipped entity.
     *
     * TODO: document what a skipped entity is!
     *
     * @param name Name of the skipped entity
     *
     * @exception SAXException if a parsing error is to be reported
     */
    public void skippedEntity(String name) throws SAXException {
        if (saxLog.isDebugEnabled()) {
            saxLog.debug("skippedEntity(" + name + ")");
        }

        ; // No processing required
    }

    /**
     * Process notification of the start of an XML element being reached.
     *
     * @param namespaceURI The Namespace URI, or the empty string if the element
     *   has no Namespace URI or if Namespace processing is not being performed.
     * @param localName The local name (without prefix), or the empty
     *   string if Namespace processing is not being performed.
     * @param qName The qualified name (with prefix), or the empty
     *   string if qualified names are not available.\
     * @param attrs The attributes attached to the element. If there are
     *   no attributes, it shall be an empty Attributes object.
     * @exception SAXException if a parsing error is to be reported
     *
     * Note that this class expects the XMLReader providing sax events
     * to be namespace-aware. We do make some effort to make the basics
     * work without a namespace-aware parser, but no promises...
     */
    public void startElement(
    String namespaceURI,
    String localName,
    String qName,
    Attributes attrs)
    throws SAXException {
        if (contentHandler != null) {
            // forward calls instead of handling them here
            contentHandler.startElement(namespaceURI, localName, qName, attrs);
            return;
        }

        boolean debug = log.isDebugEnabled();

        if (saxLog.isDebugEnabled()) {
            saxLog.debug(
                "startElement(" + namespaceURI + "," + localName + "," +
                    qName + ")");
        }

        // Save the body text accumulated for our surrounding element
        bodyTexts.push(bodyText);
        if (debug) {
            log.debug("  Pushing body text '" + bodyText.toString() + "'");
        }
        bodyText = new StringBuffer();

        // the actual element name is either in localName or qName, depending
        // on whether the parser is namespace aware
        String name = localName;
        if ((name == null) || (name.length() < 1)) {
            name = qName;
        }

        // Compute the current matching rule
        String matchPath = context.pushMatchPath(namespaceURI, name);
        if (debug) {
            log.debug("  New matchpath='" + matchPath + "'");
        }

        // Fire "begin" events for all relevant rules
        List actions;
        try {
            // NB: don't need getRuleManager here, as we know it was
            // created at startDocument if not before..
            actions = ruleManager.getMatchingActions(matchPath);
        } catch(DigestionException ex) {
            throw new NestedSAXException(ex);
        }

        context.pushMatchingActions(actions);
        if ((actions != null) && !actions.isEmpty()) {
            Substitutor substitutor = getSubstitutor();
            if (substitutor!= null) {
                attrs = substitutor.substitute(attrs);
            }
            for (int i = 0; i < actions.size(); ++i) {
                try {
                    Action action = (Action) actions.get(i);
                    if (debug) {
                        log.debug("  Fire begin() for " + action);
                    }
                    action.begin(context, namespaceURI, name, attrs);
                } catch (Exception e) {
                    log.error("Begin event threw exception", e);
                    throw createSAXException(e);
                } catch (Error e) {
                    log.error("Begin event threw error", e);
                    throw e;
                }
            }
        } else {
            if (debug) {
                log.debug("  No rules found matching '" + matchPath + "'.");
            }
        }
    }

    /**
     * Process notification of the end of an XML element being reached.
     * Here we retrieve the set of matching actions, then fire the body()
     * methods in order, followed by the end() methods in reverse order.
     *
     * @param namespaceURI - The Namespace URI, or the empty string if the
     *   element has no Namespace URI.
     * @param localName - The local name (without prefix).
     * @param qName - The qualified XML 1.0 name (with prefix), or the
     *   empty string if qualified names are not available.
     * @exception SAXException if a parsing error is to be reported
     */
    public void endElement(
    String namespaceURI,
    String localName,
    String qName)
    throws SAXException {
        if (contentHandler != null) {
            // forward calls instead of handling them here
            contentHandler.endElement(namespaceURI, localName, qName);
            return;
        }

        boolean debug = log.isDebugEnabled();
        String matchPath = context.getMatchPath();

        if (debug) {
            if (saxLog.isDebugEnabled()) {
                saxLog.debug("endElement(" + namespaceURI + "," + localName +
                        "," + qName + ")");
            }
            log.debug("  matchPath='" + matchPath + "'");
            log.debug("  bodyText='" + bodyText + "'");
        }

        // The actual element name is either in localName or qName, depending
        // on whether the parser is namespace aware. We do of course expect
        // the parser to be namespace-aware, but there's no harm in handling
        // the non-namespace case here anyway.
        String name = localName;
        if ((name == null) || (name.length() < 1)) {
            name = qName;
        }

        // Fire "body" events for all relevant rules
        List actions = (List) context.peekMatchingActions();
        if ((actions != null) && (actions.size() > 0)) {
            String bodyText = this.bodyText.toString();
            Substitutor substitutor = getSubstitutor();
            if (substitutor!= null) {
                bodyText = substitutor.substitute(bodyText);
            }
            for (int i = 0; i < actions.size(); ++i) {
                try {
                    Action action = (Action) actions.get(i);
                    if (debug) {
                        log.debug("  Fire body() for " + action);
                    }
                    action.body(context, namespaceURI, name, bodyText);
                } catch (Exception e) {
                    log.error("Body event threw exception", e);
                    throw createSAXException(e);
                } catch (Error e) {
                    log.error("Body event threw error", e);
                    throw e;
                }
            }
        } else {
            if (debug) {
                log.debug("  No rules found matching '" + matchPath + "'.");
            }
        }

        // Restore the body text for the parent element (now that we have
        // finished with the body text for this current element).
        bodyText = (StringBuffer) bodyTexts.pop();
        if (debug) {
            log.debug("  Popping body text '" + bodyText.toString() + "'");
        }

        // Fire "end" events for all relevant rules in reverse order
        // Note that the actions list is actually an ArrayStack, so
        // out-of-order access isn't a performance problem.
        if (actions != null) {
            for (int i = actions.size() - 1; i >= 0; --i) {
                try {
                    Action action = (Action) actions.get(i);
                    if (debug) {
                        log.debug("  Fire end() for " + action);
                    }
                    action.end(context, namespaceURI, name);
                } catch (Exception e) {
                    log.error("End event threw exception", e);
                    throw createSAXException(e);
                } catch (Error e) {
                    log.error("End event threw error", e);
                    throw e;
                }
            }
        }

        // Recover the previous match expression
        context.popMatchPath();

        // Discard the list of matching actions
        context.popMatchingActions();
    }

    // -----------------------------------------------------
    // DTDHandler Methods
    // -----------------------------------------------------

    /**
     * Receive notification of a notation declaration event. Currently
     * we just log these.
     *
     * @param name The notation name
     * @param publicId The public identifier (if any)
     * @param systemId The system identifier (if any)
     */
    public void notationDecl(String name, String publicId, String systemId) {
        if (saxLog.isDebugEnabled()) {
            saxLog.debug("notationDecl(" + name + "," + publicId + "," +
                    systemId + ")");
        }
    }

    /**
     * Receive notification of an unparsed entity declaration event.
     *
     * @param name The unparsed entity name
     * @param publicId The public identifier (if any)
     * @param systemId The system identifier (if any)
     * @param notation The name of the associated notation
     */
    public void unparsedEntityDecl(String name, String publicId,
                                   String systemId, String notation) {
        if (saxLog.isDebugEnabled()) {
            saxLog.debug("unparsedEntityDecl(" + name + "," + publicId + "," +
                    systemId + "," + notation + ")");
        }
    }

    // -----------------------------------------------
    // EntityResolver Methods
    // -----------------------------------------------

    /**
     * Resolve the requested external entity. The procedure used is:
     * <ul>
     * <li>if the user has registered a custom entity resolver, then invoke
     *  that; otherwise </li>
     * <li>if the entities' public or system id has been registered via the
     * method Digester.registerknownEntity or SAXHandler.setKnownEntities, then
     * load the entity from the specified URL; otherwise</li>
     * <li>if the input xml defines a system-id for the entity, then use that
     * as a URL; otherwise</li>
     * <li>fall back to whatever the default behaviour is for the XMLParser.</li>
     * </ul>
     *
     * @param publicId The public identifier of the entity being referenced
     * @param systemId The system identifier of the entity being referenced
     *
     * @exception SAXException if a parsing exception occurs
     *
     */
    public InputSource resolveEntity(String publicId, String systemId)
    throws SAXException {
        if (saxLog.isDebugEnabled()) {
            saxLog.debug("resolveEntity('" + publicId + "', '" + systemId + "')");
        }

        if (entityResolver != null) {
            try {
                // the user has specified their own EntityResolver, so we just
                // forward the call to that object:
                return entityResolver.resolveEntity(publicId, systemId);
            } catch (IOException e) {
                throw new NestedSAXException(e);
            }
        }

        // Keep count of the number of entities resolved. Currently, we only
        // care whether this is zero or one, so a boolean could do as well.
        // However it seems likely that a count could be useful at some time.
        ++numEntitiesResolved;
        
        // Is this the DTD? If there *is* a DTD (ie one was reported to the
        // lexical handler) then it is presumed here that it will be the first
        // entity resolved.
        //
        // Note that we can't just check whether this systemId is the same
        // as the dtdSystemId, because the systemId parameter here has been
        // expanded to an absolute ref, while the one passed to the 
        // LexicalHandler is in its original (possibly relative) form.
        //
        // It would be great to be able to use the EntityResolver2 interface
        // which provides both the original and system ids, but that is 
        // probably not supported widely enough yet.
        if ((numEntitiesResolved == 1) && (dtdSystemId != null)) {
            if (ignoreExternalDTD) {
            // this entity is the DTD, and the user wants to completely
            // ignore it, so we return an "empty file".
            return new InputSource(new StringReader(""));
            }
        }
       
        // Has this public identifier been registered?
        String entityURL = null;
        if (publicId != null) {
            entityURL = (String) knownEntities.get(publicId);
        }

        // Has this system identifier been registered?
        if (entityURL == null && systemId != null) {
            entityURL = (String)knownEntities.get(systemId);
        }

        if ((entityURL == null) && !allowUnknownExternalEntities) {
            // refuse to load unknown external entity
            String pub = "null";
            if (publicId != null) {
                pub = "'" + publicId + "'";
            }
            
            String sys = "null";
            if (systemId != null) {
                sys = "'" + systemId + "'";
            }
            
            throw createSAXException(
                "The external entity with publicId = " + pub
                + " and systemId = " + sys
                + " has not been registered as a known entity.");
        }
        
        if ((entityURL == null) && (systemId != null)) {
            // allow unknown external entity
            entityURL = systemId;
        }
        
        if (entityURL == null) {
            // Cannot resolve. PublicId does not map to anything, and
            // systemId is null.
            if (log.isDebugEnabled()) {
                log.debug(" Cannot resolve entity: '" + entityURL + "'");
            }
            
            throw createSAXException(
                "Cannot resolve entity. PublicId is null or has not been"
                + " registered as a known entity, and systemId is null.");
        }

        if (entityURL.length() == 0) {
            // special case: when the user has mapped an empty to a URL being
            // the empty string, we return an empty InputSource to the parser,
            // effectively ignoring the entity.
            return new InputSource(new StringReader(""));
        } else {
            // Return an input source to our alternative URL
            if (log.isDebugEnabled()) {
                log.debug(" Resolving entity to '" + entityURL + "'");
            }
    
            return new InputSource(entityURL);
        }
    }

    // -------------------------------------------------
    // ErrorHandler Methods
    // -------------------------------------------------

    /**
     * Handle notification from the XMLReader of a problem in the input xml.
     * <p>
     * If the user has registered a custom ErrorHandler via setErrorHandler
     * then the notification is forwarded to the user object.
     * <p>
     * If there is no custom ErrorHandler, then this method does nothing.
     *
     * @param exception The warning information
     *
     * @exception SAXException if a parsing exception occurs
     */
    public void warning(SAXParseException exception) throws SAXException {
        log.warn(
            "Parse Warning Error at line " + exception.getLineNumber() +
            " column " + exception.getColumnNumber() + ": " +
            exception.getMessage(), exception);

        // default behaviour: ignore the warning
        if (errorHandler != null) {
            errorHandler.warning(exception);
        }
    }

    /**
     * Handle notification from the XMLReader of an error in the input xml.
     * <p>
     * If the user has registered a custom ErrorHandler via setErrorHandler
     * then the notification is forwarded to the user object (which will
     * normally throw the exception provided as a parameter after doing any
     * custom processing).
     * <p>
     * If there is no custom ErrorHandler, then this method just throws the
     * exception provided as a parameter.
     *
     * @param exception The error information
     *
     * @exception SAXException if a parsing exception occurs
     */
    public void error(SAXParseException exception) throws SAXException {
        log.error("Parse Error at line " + exception.getLineNumber() +
                " column " + exception.getColumnNumber() + ": " +
                exception.getMessage(), exception);

        if (errorHandler == null) {
            // default behaviour: report the error
            throw exception;
        } else {
            errorHandler.error(exception);
        }
    }


    /**
     * Handle notification from the XMLReader of an error in the input xml.
     * <p>
     * If the user has registered a custom ErrorHandler via setErrorHandler
     * then the notification is forwarded to the user object (which will
     * normally throw the exception provided as a parameter after doing any
     * custom processing).
     * <p>
     * If there is no custom ErrorHandler, then this method just throws the
     * exception provided as a parameter.
     *
     * @param exception The fatal error information
     *
     * @exception SAXException if a parsing exception occurs
     */
    public void fatalError(SAXParseException exception) throws SAXException {
        log.error("Parse Fatal Error at line " + exception.getLineNumber() +
                " column " + exception.getColumnNumber() + ": " +
                exception.getMessage(), exception);

        if (errorHandler == null) {
            // default behaviour: report the fatal error
            throw exception;
        } else {
            errorHandler.error(exception);
        }
    }

    // -------------------------------------------------
    // LexicalHandler Methods
    // -------------------------------------------------

    /**
     * Invoked when the xml parser finds an xml comment.
     */
    public void comment(char[] ch, int start, int length) {
        ; // ignore
    }

    /**
     * Invoked when the xml parser finds the start of a CDATA section.
     * The contents of the cdata section are passed to the characters()
     * methods anyway; this method merely allows code to tell whether the
     * data was escaped via CDATA or not.
     */
    public void startCDATA() {
        ; // ignore
    }

    /**
     * See {@link #startCDATA}.
     */
    public void endCDATA() {
        ; // ignore
    }

    /**
     * Invoked when the xml parser starts expanding an internal or external
     * entity. The expanded version is reported via the normal ContentHandler
     * methods anyway; this method merely allows code to tell whether the
     * data was an entity, or inline in the normal manner.
     */
    public void startEntity(String name) {
        ; // ignore
    }

    /**
    * See {@link #startEntity}.
     */
    public void endEntity(String name) {
        ; // ignore
    }

    /**
     * Invoked when the DOCTYPE tag is found in the input xml. The public
     * and system ids present in that declaration are stored and can be
     * retrieved later via the getDTDPublicId and getDTDSystemId methods.
     * <p>
     * This method is always preceded by startDocument.
     */
    public void startDTD(String name, String publicId, String systemId) {
        dtdPublicId = publicId;
        dtdSystemId = systemId;
    }
    
    /**
     * See {@link #startDTD}.
     * <p>
     * This method always precedes the first startElement.
     */
    public void endDTD() {
        ; // ignore
    }
}
