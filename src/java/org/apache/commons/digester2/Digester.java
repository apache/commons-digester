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

import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;

import org.apache.commons.logging.Log;

/**
 * <p>A <strong>Digester</strong> processes an XML input stream by matching a
 * series of element nesting patterns to execute Actions that have been added
 * prior to the start of parsing.</p>
 *
 * <p>This class is the one that users interact with to configure the
 * parsing rules and initiate the parse. It is mostly just a user-friendly
 * facade over the SAXHandler class.</p>
 *
 * <p>See the <a href="package-summary.html#package_description">Digester
 * Developer Guide</a> for more information.</p>
 *
 * <p><strong>IMPLEMENTATION NOTES</strong>
 * <ul>
 * <li> A single Digester instance may only be used within the context of a
 *  single thread at a time, and a call to <code>parse()</code> must be
 *  completed before another can be initiated even from the same thread.</li>
 * <li> This class requires that JAXP1.1 and a SAX2-compliant xml parser be
 *  present in the classpath. Versions of java prior to 1.4 include neither
 *  JAXP nor a parser, so simply including a modern xml parser plus a JAXP1.1
 *  implementation in the classpath solves this requirement. Most xml parsers
 *  provide a suitable implementation as a companion jar to the main download
 *  (eg xml-apis.jar for the Apache Xerces parser). The 1.4 version of java
 *  includes JAXP1.1, and bundles an xml parser. While the JAXP apis included
 *  are fine, the xml parser bundled with this version of java is not adequate
 *  for most tasks, and it is recommended that a better one be provided in the
 *  classpath. Using the latest Apache Xerces release can be tricky, however,
 *  as Sun's bundled parser is xerces, with package names unchanged, and
 *  classes in the runtime libraries override classes in the classpath by
 *  default. See documentation on the java "endorsed override" feature for
 *  solutions, or the apache xerces website.
 *  Versions of java from 1.5 onward provide xml parsers that are fine for
 *  most purposes.</li>
 * </ul>
 */

public class Digester {

    // --------------------------------------------------- 
    // Instance Variables
    // --------------------------------------------------- 

    /**
     * The object used to handle event callbacks from the SAX parser as
     * the input xml is being processed. There is a 1:1 relation between
     * a Digester and a SAXHandler.
     */
    private SAXHandler saxHandler;

    /**
     * The reader object used to generate SAX events representing the input.
     * Normally, the object does this by parsing input text, but alternatives
     * (like walking a DOM tree and generating appropriate events) are also
     * possible. In any case, it's not relevant to this app how the sax
     * events are generated.
     */
    private XMLReader reader = null;

    /**
     * See {@link #setValidating}.
     */
    private boolean validating = false;

    // --------------------------------------------------------- 
    // Constructors
    // --------------------------------------------------------- 

    /**
     * Construct a new Digester with default properties.
     */
    public Digester() {
        this.saxHandler = new SAXHandler();
    }

    /**
     * This method allows customisation of the way that sax events are
     * handled, by allowing a modified subclass of SAXHandler to be
     * specified as the target for parsing events.
     */
    public Digester(SAXHandler saxHandler) {
        this.saxHandler = saxHandler;
    }

    // ------------------------------------------------------------- 
    // Properties
    // ------------------------------------------------------------- 

    /**
     * Get the SAXHandler object associated with this instance.
     */
    public SAXHandler getSAXHandler() {
        return saxHandler;
    }

    /**
     * Determine whether we are to validate the xml input against a DTD.
     * If so, then an error will be reported by the parse() methods if the
     * input doesn't comply with the schema. If validation is disabled, then
     * performance will be improved, but no error will be reported if the
     * input is not correct.
     * <p>
     * Note that even when validation is disabled, any external DTD referenced
     * by the input document must still be read, as it can declare default 
     * attributes and similar items which affect xml parsing.
     * <p>
     * In order to validate a document against an xml schema, all of the
     * following must be done:
     * <ul>
     * <li>Validation must be enabled (using this method or by setting feature
     *   "http://xml.org/sax/features/validation" on the XMLReader object).
     * <li>Schema-validation must be enabled in a parser-specific manner. For
     *  the Xerces parser (which is bundled with java 1.4 and 1.5) the feature
     *  "http://apache.org/xml/features/validation/schema" must be set on the
     *  XMLReader object.
     * <li>The input xml document must declare the schema to use by defining
     *   xml attribute "xsi:noNamespaceSchemaLocation" or "xsi:schemaLocation"
     *   on some element (usually the root element)
     * <li>The DOCTYPE declaration on the top of the input xml element must
     *   be removed; schema validation is ignored if DOCTYPE is present.
     * </ul>
     *
     * @param validating The new validating parser flag.
     *
     * This must be called before <code>parse()</code> is called the first time.
     */
    public void setValidating(boolean validating) {
        this.validating = validating;
    }

    /**
     * Get the validating parser flag. See {@link #setValidating}.
     */
    public boolean getValidating() {
        return (this.validating);
    }

    /**
     * <p>Specify the XMLReader to be used to parse the input.</p>
     *
     * <p>This can be particularly useful in environments that are unfriendly
     * to JAXP; it should always be possible to directly instantiate a parser
     * using parser-specific code, and all decent xml parsers should implement
     * (ie be castable to) the org.xml.sax.XMLReader interface even when the
     * JAXP apis are not available or do not function correctly.</p>
     *
     * <p>If you have a SAXParser instance that should be used as the source
     * of the input, then use:
     * <pre>
     *   setXMLReader(saxParser.getXMLReader());
     * </pre>
     * </p>
     *
     * <p>The reader passed here should be configured with namespace-aware
     * parsing enabled, as the digester classes assume this.</p>
     *
     * <p>This method does not set up the SAXHandler as the reader's handler
     *  for content, dtd or other events. You should generally call method
     *  SAXHandler.initCallbacks before starting the parse.</p>
     */
    public void setXMLReader(XMLReader reader, boolean initCallbacks) {
        this.reader = reader;
        if (initCallbacks) {
            saxHandler.initCallbacks(reader);
        }
        
        boolean isNamespaceAware;
        try { 
            isNamespaceAware = 
                reader.getFeature("http://xml.org/sax/features/namespaces");
        } catch(org.xml.sax.SAXNotRecognizedException ex) {
            isNamespaceAware = false;
        } catch(org.xml.sax.SAXNotSupportedException ex) {
            isNamespaceAware = false;
        }
        
        if (!isNamespaceAware) {
            // perhaps we should be throwing an exception here instead of
            // issuing a warning?
            saxHandler.getLogger().warn(
                "Digester.setXMLReader called with a parser that is not"
                + " namespace-aware");
        }
    }

    /**
     * Set the class loader to be used for instantiating application objects
     * when required. If a non-null value is passed to this method, then
     * method {@link #setUseContextClassLoader} will have no effect.
     * <p>
     * When an Action is executed due to some xml input, and that Action
     * wishes to create an object to represent the input, then the class
     * used will be loaded via the specified classloader.
     *
     * @param classLoader The new class loader to use, or <code>null</code>
     *  to revert to the standard rules
     */
    public void setExplicitClassLoader(ClassLoader classLoader) {
        saxHandler.setExplicitClassLoader(classLoader);
    }

    /**
     * Get the explicit class loader to be used by Actions for instantiating 
     * objects when required. Null indicates that there is no explicit
     * classloader set. 
     * <p>
     * When no explicit classloader has been specified, either the context 
     * classloader (see {@link #setUseContextClassLoader}) or the classloader 
     * that loaded the SAXHandler instance will be used.
     *
     * @return the classloader previously passed to setExplicitClassLoader,
     * or null if no explicit classloader has been set. 
     */
    public ClassLoader getExplicitClassLoader() {
        return saxHandler.getExplicitClassLoader();
    }

    /**
     * Set the current logger for this Digester.
     */
    public void setLogger(Log log) {
        saxHandler.setLogger(log);
    }

    /**
     * Get the current Logger associated with this instance of the Digester
     */
    public Log getLogger() {
        return saxHandler.getLogger();
    }

    /**
     * Sets the logger used for logging SAX-related information.
     * <strong>Note</strong> the output is finely grained.
     * @param saxLog Log, not null
     */
    public void setSAXLogger(Log saxLog) {
        saxHandler.setSAXLogger(saxLog);
    }

    /**
     * Gets the logger used for logging SAX-related information.
     * <strong>Note</strong> the output is finely grained.
     */
    public Log getSAXLogger() {
        return saxHandler.getSAXLogger();
    }

    /**
     * Set the <code>RuleManager</code> implementation object containing our
     * rules collection and associated matching policy.
     *
     * @param ruleManager New RuleManager implementation
     */
    public void setRuleManager(RuleManager ruleManager) {
        saxHandler.setRuleManager(ruleManager);
    }

    /**
     * Get the <code>RuleManager</code> object containing our
     * rule collection and associated matching policy.  If none has been
     * established, a default implementation will be created and returned.
     */
    public RuleManager getRuleManager() {
        return saxHandler.getRuleManager();
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
        return saxHandler.getDTDPublicId();
    }

    /**
     * Get the system identifier of the DTD associated with the document
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
    public String getDTDSystemId() {
        return saxHandler.getDTDSystemId();
    }

    /**
     * Determine whether to use the Context Classloader (the one found by
     * calling <code>Thread.currentThread().getContextClassLoader()</code>)
     * to resolve/load classes when an Action needs to create an instance of
     * an object to represent data in the xml input. If this is set to false,
     * and there is no explicit classloader set, then the same classloader
     * that loaded the Action class is used.
     * <p>
     * See {@link #setExplicitClassLoader}.
     *
     * @param use determines whether to use the Context Classloader.
     */
    public void setUseContextClassLoader(boolean use) {
        saxHandler.setUseContextClassLoader(use);
    }

    /**
     * Indicates whether the context classloader will be used by Actions
     * when instantiating objects. See {@link #setUseContextClassLoader}.
     */
    public boolean getUseContextClassLoader() {
        return saxHandler.getUseContextClassLoader();
    }

    /**
     * Sets the <code>Substitutor</code> to be used to perform pre-processing
     * on xml attributes and body text before Actions are applied.
     *
     * @param substitutor the Substitutor to be used, or null if no
     *   substitution (pre-processing) is to be performed.
     */
    public void setSubstitutor(Substitutor substitutor) {
        saxHandler.setSubstitutor(substitutor);
    }

    /**
     * Gets the <code>Substitutor</code> used to perform pre-processing
     * on xml attributes and body text before Actions are applied.
     *
     * @return Substitutor, null if no substitution (pre-processing) is to 
     *   be performed.
     */
    public Substitutor getSubstitutor() {
        return saxHandler.getSubstitutor();
    }

    /**
     * Specifies a map of (publicId->URI) pairings that will be used by the
     * default Entity Resolver when resolving entities in the input xml 
     * (including the DTD or schema specified with the DOCTYPE).
     * <p>
     * If the value in a map entry (ie the "URI") is an empty string, then
     * when the parser asks for the entity to be resolved, an empty InputSource
     * will be returned, effectively ignoring the entity.
     * <p>
     * See {@link #getKnownEntities}, and {@link #setEntityResolver}.
     */
    public void setKnownEntities(Map knownEntities) {
        saxHandler.setKnownEntities(knownEntities);
    }

    /**
     * <p>Register a mapping between a public or system ID (denoting an external
     * entity, aka resource) and a URL indicating where that resource can be
     * found, for use by the default Entity Resolver. It is particularly useful
     * to register local locations for DTDs or schemas that the input xml may 
     * reference.</p>
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
     * <p>If you are trying to register a systemId here, then be aware that
     * the value that must be registered is the <i>absolute</i> URL formed by
     * resolving the system-id in the input document against the base URL of
     * the document being parsed. If the system-id in the document is absolute,
     * then this is not an issue.</p>
     *
     * <p>
     * <strong>Note:</strong> This method will have no effect when a custom
     * <code>EntityResolver</code> has been set. (Setting a custom
     * <code>EntityResolver</code> overrides the internal implementation.)
     * </p>
     *
     * @param publicOrSystemId Public or system identifier of the entity 
     *  to be resolved
     * @param entityURL The URL to use for reading this entity
     */
    public void registerKnownEntity(String publicOrSystemId, String entityURL) {
        saxHandler.registerKnownEntity(publicOrSystemId, entityURL);
    }

    /**
     * Returns a map of (publicId->URI) pairings that will be used by the
     * default EntityResolver. 
     * <p>
     * See {@link #setKnownEntities}, and {@link #setEntityResolver}.
     */
    public Map getKnownEntities() {
        return saxHandler.getKnownEntities();
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
     * <p>
     * Note that this method would not be necessary if people could be
     * relied upon to mark their DOCTYPE declarations with the "standalone"
     * keyword if they can be ignored, as the xml parser would never attempt
     * to load the dtd unless validation was enabled. And this method would 
     * not be necessary if people could be relied upon to use PUBLIC ids in 
     * their DOCTYPES, as method registerKnownEntity(publicId, "") can be used 
     * to ignore an entity with a specific public-id. However there are large 
     * volumes of broken XML documents out there that ignore both of these 
     * basic xml features, so this method provides a workaround. Note that using
     * registerKnownEntity with a systemId is not usually useful, because
     * xml parsers expand relative systemIds into absolute ids before calling
     * the EntityResolver, making it difficult to know before parsing starts
     * what (absolute) system-id value should be registered in order to
     * redirect/disable the entity.
     */
    public void setIgnoreExternalDTD(boolean state) {
        saxHandler.setIgnoreExternalDTD(state);
    }
     
    /**
     * See setIgnoreExternalDTD.
     */
    public boolean getIgnoreExternalDTD() {
        return saxHandler.getIgnoreExternalDTD();
    }
     
    // ------------------------------------------------------- 
    // Public Methods
    // ------------------------------------------------------- 

    /**
     * Parse the content of the specified file using this Digester.  Returns
     * the root element from the object stack (if any).
     *
     * @param file File containing the XML data to be parsed
     *
     * @exception IOException if an input/output error occurs
     * @exception SAXException if a parsing exception occurs
     */
    public Object parse(File file)
    throws DigestionException, IOException, SAXException {
        try {
            InputSource input = new InputSource(new FileInputStream(file));
            input.setSystemId(file.toURL().toString());
            getXMLReader().parse(input);
            return saxHandler.getRoot();
        }
        catch(RuntimeException re) {
            throw new DigestionException(re);
        }
    }

    /**
     * Parse the content of the specified input source using this Digester.
     * Returns the root element from the object stack (if any).
     *
     * @param input Input source containing the XML data to be parsed
     *
     * @exception IOException if an input/output error occurs
     * @exception SAXException if a parsing exception occurs
     */
    public Object parse(InputSource input)
    throws DigestionException, IOException, SAXException {
        try {
            getXMLReader().parse(input);
            return saxHandler.getRoot();
        }
        catch(RuntimeException re) {
            throw new DigestionException(re);
        }
    }

    /**
     * Parse the content of the specified input stream using this Digester.
     * Returns the root element from the object stack (if any).
     * <p>
     * Note that because the xml parser has no idea what the "real location"
     * of the input is supposed to be, no relative referenced from the
     * input xml document will work. If you need this behaviour, then
     * use the parse(org.xml.sax.InputSource) variant, together with the
     * InputSource.setSystemId method to tell the input source what the
     * logical location of the input is supposed to be.
     *
     * @param input Input stream containing the XML data to be parsed
     *
     * @exception IOException if an input/output error occurs
     * @exception SAXException if a parsing exception occurs
     */
    public Object parse(InputStream input)
    throws DigestionException, IOException, SAXException {
        try {
            InputSource is = new InputSource(input);
            getXMLReader().parse(is);
            return saxHandler.getRoot();
        }
        catch(RuntimeException re) {
            throw new DigestionException(re);
        }
    }

    /**
     * Parse the content of the specified reader using this Digester.
     * Returns the root element from the object stack (if any).
     * <p>
     * Note that because the xml parser has no idea what the "real location"
     * of the input is supposed to be, no relative referenced from the
     * input xml document will work. If you need this behaviour, then
     * use the parse(org.xml.sax.InputSource) variant, together with the
     * InputSource.setSystemId method to tell the input source what the
     * logical location of the input is supposed to be.
     *
     * @param reader Reader containing the XML data to be parsed
     *
     * @exception IOException if an input/output error occurs
     * @exception SAXException if a parsing exception occurs
     */
    public Object parse(Reader reader)
    throws DigestionException, IOException, SAXException {

        try {
            InputSource is = new InputSource(reader);
            getXMLReader().parse(is);
            return saxHandler.getRoot();
        }
        catch(RuntimeException re) {
            throw new DigestionException(re);
        }
    }

    /**
     * Parse the content of the specified URI using this Digester.
     * Returns the root element from the object stack (if any).
     * <p>
     * Note that because the xml parser has no idea what the "real location"
     * of the input is supposed to be, no relative referenced from the
     * input xml document will work. If you need this behaviour, then
     * use the parse(org.xml.sax.InputSource) variant, together with the
     * InputSource.setSystemId method to tell the input source what the
     * logical location of the input is supposed to be.
     *
     * @param uri URI containing the XML data to be parsed
     *
     * @exception IOException if an input/output error occurs
     * @exception SAXException if a parsing exception occurs
     */
    public Object parse(String uri)
    throws DigestionException, IOException, SAXException {
        try {
            InputSource is = new InputSource(uri);
            getXMLReader().parse(is);
            return saxHandler.getRoot();
        }
        catch(RuntimeException re) {
            throw new DigestionException(re);
        }
    }

    // --------------------------------------------------------- Rule Methods

    /**
     * <p>Register a new rule (pattern/action pair).
     *
     * @param pattern Element matching pattern
     * @param action Action to be registered
     */
    public void addRule(String pattern, Action action)
    throws InvalidRuleException {
        saxHandler.addRule(pattern, action);
    }

    // --------------------------------------------------- 
    // Object Stack Methods
    // --------------------------------------------------- 

    /**
     * Set the initial object that will form the root of the tree of
     * objects generated during a parse.
     * <p>
     * Note that this is optional; an ObjectCreateAction associated with the
     * root element of the input document performs the same task.
     *
     * @param object The new object
     */
    public void setInitialObject(Object object) {
        saxHandler.setInitialObject(object);
    }

    /**
     * Returns the root element of the tree of objects created as a result
     * of applying the rules to the input XML.
     * <p>
     * If the digester stack was "primed" by explicitly pushing a root
     * object onto the stack before parsing started, then that root object
     * is returned here.
     * <p>
     * Alternatively, if an Action which creates an object (eg ObjectCreateAction)
     * matched the root element of the xml, then the object created will be
     * returned here.
     * <p>
     * In other cases, the object most recently pushed onto an empty digester
     * stack is returned. This would be a most unusual use of digester, however;
     * one of the previous configurations is much more likely.
     * <p>
     * Note that when using one of the Digester.parse methods, the return
     * value from the parse method is exactly the same as the return value
     * from this method. However when the Digester is being used as a
     * SAXContentHandler, no such return value is available; in this case, this
     * method allows you to access the root object that has been created
     * after parsing has completed.
     *
     * @return the root object that has been created after parsing
     *  or null if the digester has not parsed any XML yet.
     */
    public Object getRoot() {
        return saxHandler.getRoot();
    }

    /**
     * Return the XMLReader to be used for parsing the input document.
     *
     * @exception SAXException if no XMLReader can be instantiated
     */
    public XMLReader getXMLReader() throws SAXException {
        if (reader != null) {
            return reader;
        }

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(validating);
            
            SAXParser parser = factory.newSAXParser();
            reader = parser.getXMLReader();
        } catch (Exception e) {
            Log log = saxHandler.getLogger();
            log.error("Digester.getXMLReader: ", e);
            return null;
        }

        saxHandler.initCallbacks(reader);
        return reader;
    }

    /**
     * Specify the Entity Resolver used to determine the physical location
     * of resources (such as DTDs or schemas) referred to by the input xml.
     * <p>
     * The value <i>null</i> indicates that the SAXHandler object will be
     * used as the entity resolver. In this case, see methods:
     * <ul>
     * <li>{@link #setKnownEntities}</li>
     * <li>{@link #registerKnownEntity}</li>
     * </ul>
     *
     * @param entityResolver a class that implement the 
     * <code>EntityResolver</code> interface, or <i>null</i> to restore
     * the default behaviour.
     */
    public void setEntityResolver(EntityResolver entityResolver){
        saxHandler.setEntityResolver(entityResolver);
    }

    /**
     * Return the Entity Resolver used to determine the physical location
     * of resources (such as DTDs or schemas) referred to by the input xml.
     * <p>
     * The value <i>null</i> indicates that the SAXHandler object will be
     * used as the entity resolver. In this case, see methods:
     * <ul>
     * <li>{@link #setKnownEntities}</li>
     * <li>{@link #registerKnownEntity}</li>
     * </ul>
     *
     * @return the Entity Resolver to be used.
     */
    public EntityResolver getEntityResolver(){
        return saxHandler.getEntityResolver();
    }

    /**
     * Return the error handler which will be used if the xml parser detects
     * errors in the xml input being parsed.
     */
    public ErrorHandler getErrorHandler() {
        return saxHandler.getErrorHandler();
    }

    /**
     * Set the error handler for this Digester.
     *
     * @param errorHandler The new error handler
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        saxHandler.setErrorHandler(errorHandler);
    }
}
