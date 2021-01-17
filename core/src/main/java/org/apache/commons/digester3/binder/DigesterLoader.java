package org.apache.commons.digester3.binder;

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

import static org.apache.commons.digester3.binder.BinderClassLoader.createBinderClassLoader;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.RuleSet;
import org.apache.commons.digester3.Rules;
import org.apache.commons.digester3.RulesBase;
import org.apache.commons.digester3.StackAction;
import org.apache.commons.digester3.Substitutor;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * This class manages the creation of Digester instances from digester rules modules.
 */
public final class DigesterLoader
{

    /**
     * The default head when reporting an errors list.
     */
    private static final String HEADING = "Digester creation errors:%n%n";

    /**
     * Creates a new {@link DigesterLoader} instance given one or more {@link RulesModule} instance.
     *
     * @param rulesModules The modules containing the {@code Rule} binding
     * @return A new {@link DigesterLoader} instance
     */
    public static DigesterLoader newLoader( final RulesModule... rulesModules )
    {
        if ( rulesModules == null || rulesModules.length == 0 )
        {
            throw new DigesterLoadingException( "At least one RulesModule has to be specified" );
        }
        return newLoader( Arrays.asList( rulesModules ) );
    }

    /**
     * Creates a new {@link DigesterLoader} instance given a collection of {@link RulesModule} instance.
     *
     * @param rulesModules The modules containing the {@code Rule} binding
     * @return A new {@link DigesterLoader} instance
     */
    public static DigesterLoader newLoader( final Iterable<RulesModule> rulesModules )
    {
        if ( rulesModules == null )
        {
            throw new DigesterLoadingException( "RulesModule has to be specified" );
        }

        return new DigesterLoader( rulesModules );
    }

    /**
     * The concrete {@link RulesBinder} implementation.
     */
    private final DefaultRulesBinder rulesBinder = new DefaultRulesBinder();

    /**
     * The URLs of entityValidator that have been registered, keyed by the public
     * identifier that corresponds.
     */
    private final Map<String, URL> entityValidator = new HashMap<String, URL>();

    /**
     * The SAXParserFactory to create new default {@link Digester} instances.
     */
    private final SAXParserFactory factory = SAXParserFactory.newInstance();

    private final Iterable<RulesModule> rulesModules;

    /**
     * The class loader to use for instantiating application objects.
     * If not specified, the context class loader, or the class loader
     * used to load Digester itself, is used, based on the value of the
     * <code>useContextClassLoader</code> variable.
     */
    private BinderClassLoader classLoader;

    /**
     * An optional class that substitutes values in attributes and body text. This may be null and so a null check is
     * always required before use.
     */
    private Substitutor substitutor;

    /**
     * The EntityResolver used by the SAX parser. By default it use this class
     */
    private EntityResolver entityResolver;

    /**
     * Object which will receive callbacks for every pop/push action on the default stack or named stacks.
     */
    private StackAction stackAction;

    /**
     * The executor service to run asynchronous parse method.
     * @since 3.1
     */
    private ExecutorService executorService;

    /**
     * The application-supplied error handler that is notified when parsing warnings, errors, or fatal errors occur.
     * @since 3.2
     */
    private ErrorHandler errorHandler;

    /**
     * The Locator associated with our parser.
     * @since 3.2
     */
    private Locator locator;

    /**
     * Creates a new {@link DigesterLoader} instance given a collection of {@link RulesModule} instance.
     *
     * @param rulesModules The modules containing the {@code Rule} binding
     */
    private DigesterLoader( final Iterable<RulesModule> rulesModules )
    {
        this.rulesModules = rulesModules;
        setUseContextClassLoader( true );
    }

    /**
     * Determine whether to use the Context ClassLoader (the one found by
     * calling <code>Thread.currentThread().getContextClassLoader()</code>)
     * to resolve/load classes that are defined in various rules.  If not
     * using Context ClassLoader, then the class-loading defaults to
     * using the calling-class' ClassLoader.
     *
     * @param useContextClassLoader determines whether to use Context ClassLoader.
     * @return This loader instance, useful to chain methods.
     */
    public DigesterLoader setUseContextClassLoader( final boolean useContextClassLoader )
    {
        if ( useContextClassLoader )
        {
            setClassLoader( Thread.currentThread().getContextClassLoader() );
        }
        else
        {
            setClassLoader( getClass().getClassLoader() );
        }
        return this;
    }

    /**
     * Set the class loader to be used for instantiating application objects when required.
     *
     * @param classLoader the class loader to be used for instantiating application objects when required.
     * @return This loader instance, useful to chain methods.
     */
    public DigesterLoader setClassLoader( final ClassLoader classLoader )
    {
        if ( classLoader == null )
        {
            throw new IllegalArgumentException( "Parameter 'classLoader' cannot be null" );
        }

        this.classLoader = createBinderClassLoader( classLoader );

        rulesBinder.initialize( this.classLoader );
        for ( final RulesModule rulesModule : rulesModules )
        {
            rulesModule.configure( rulesBinder );
        }

        return this;
    }

    /**
     * Sets the <code>Substitutor</code> to be used to convert attributes and body text.
     *
     * @param substitutor the Substitutor to be used to convert attributes and body text
     *        or null if not substitution of these values is to be performed.
     * @return This loader instance, useful to chain methods.
     */
    public DigesterLoader setSubstitutor( final Substitutor substitutor )
    {
        this.substitutor = substitutor;
        return this;
    }

    /**
     * Set the "namespace aware" flag for parsers we create.
     *
     * @param namespaceAware The new "namespace aware" flag
     * @return This loader instance, useful to chain methods.
     */
    public DigesterLoader setNamespaceAware( final boolean namespaceAware )
    {
        factory.setNamespaceAware( namespaceAware );
        return this;
    }

    /**
     * Return the "namespace aware" flag for parsers we create.
     *
     * @return true, if the "namespace aware" flag for parsers we create, false otherwise.
     */
    public boolean isNamespaceAware()
    {
        return factory.isNamespaceAware();
    }

    /**
     * Set the XInclude-aware flag for parsers we create. This additionally
     * requires namespace-awareness.
     *
     * @param xIncludeAware The new XInclude-aware flag
     * @return This loader instance, useful to chain methods.
     * @see #setNamespaceAware(boolean)
     */
    public DigesterLoader setXIncludeAware( final boolean xIncludeAware )
    {
        factory.setXIncludeAware( xIncludeAware );
        return this;
    }

    /**
     * Return the XInclude-aware flag for parsers we create;
     *
     * @return true, if the XInclude-aware flag for parsers we create is set,
     *         false otherwise
     */
    public boolean isXIncludeAware()
    {
        return factory.isXIncludeAware();
    }

    /**
     * Set the {@code DOCTYPE} validation parser flag and should not be used when using schemas.
     *
     * @param validating The new validating parser flag.
     * @return This loader instance, useful to chain methods.
     * @see javax.xml.parsers.SAXParserFactory#setValidating(boolean)
     */
    public DigesterLoader setValidating( final boolean validating )
    {
        factory.setValidating( validating );
        return this;
    }

    /**
     * Return the {@code DOCTYPE} validation parser flag.
     *
     * @return true, if the validating parser flag is set, false otherwise
     */
    public boolean isValidating()
    {
        return this.factory.isValidating();
    }

    /**
     * Set the XML Schema to be used when parsing.
     *
     * @param schema The {@link Schema} instance to use.
     * @return This loader instance, useful to chain methods.
     */
    public DigesterLoader setSchema( final Schema schema )
    {
        factory.setSchema( schema );
        return this;
    }

    /**
     * Sets a flag indicating whether the requested feature is supported by the underlying implementation of
     * <code>org.xml.sax.XMLReader</code>.
     * 
     * @see org.apache.commons.digester3.Digester#setFeature(String, boolean)
     * @param feature Name of the feature to set the status for
     * @param value The new value for this feature
     * @return This loader instance, useful to chain methods.
     * @throws ParserConfigurationException if a parser configuration error occurs
     * @throws SAXNotRecognizedException if the property name is not recognized
     * @throws SAXNotSupportedException if the property name is recognized but not supported
     * @since 3.3
     */
    public DigesterLoader setFeature( final String feature, final boolean value )
        throws SAXNotRecognizedException, SAXNotSupportedException, ParserConfigurationException
    {
        factory.setFeature(feature, value);
        return this;
    }

    /**
     * <p>Register the specified DTD URL for the specified public identifier.
     * This must be called before the first call to <code>parse()</code>.
     * </p><p>
     * <code>Digester</code> contains an internal <code>EntityResolver</code>
     * implementation. This maps <code>PUBLICID</code>'s to URLs
     * (from which the resource will be loaded). A common use case for this
     * method is to register local URLs (possibly computed at runtime by a
     * classloader) for DTDs. This allows the performance advantage of using
     * a local version without having to ensure every <code>SYSTEM</code>
     * URI on every processed xml document is local. This implementation provides
     * only basic functionality. If more sophisticated features are required,
     * using {@link #setEntityResolver(EntityResolver)} to set a custom resolver is recommended.
     * </p><p>
     * <strong>Note:</strong> This method will have no effect when a custom
     * <code>EntityResolver</code> has been set. (Setting a custom
     * <code>EntityResolver</code> overrides the internal implementation.)
     * </p>
     * @param publicId Public identifier of the DTD to be resolved
     * @param entityURL The URL to use for reading this DTD
     * @return This loader instance, useful to chain methods.
     */
    public DigesterLoader register( final String publicId, final URL entityURL )
    {
        entityValidator.put( publicId, entityURL );
        return this;
    }

    /**
     * <p>Convenience method that registers the string version of an entity URL
     * instead of a URL version.</p>
     *
     * @param publicId Public identifier of the entity to be resolved
     * @param entityURL The URL to use for reading this entity
     * @return This loader instance, useful to chain methods.
     */
    public DigesterLoader register( final String publicId, final String entityURL )
    {
        try
        {
            return register( publicId, new URL( entityURL ) );
        }
        catch ( final MalformedURLException e )
        {
            throw new IllegalArgumentException( "Malformed URL '" + entityURL + "' : " + e.getMessage() );
        }
    }

    /**
     * Return the set of DTD URL registrations, keyed by public identifier.
     *
     * @return the set of DTD URL registrations.
     */
    public Map<String, URL> getRegistrations()
    {
        return Collections.unmodifiableMap( this.entityValidator );
    }

    /**
     * Set the <code>EntityResolver</code> used by SAX when resolving public id and system id. This must be called
     * before the first call to <code>parse()</code>.
     *
     * @param entityResolver a class that implement the <code>EntityResolver</code> interface.
     * @return This loader instance, useful to chain methods.
     */
    public DigesterLoader setEntityResolver( final EntityResolver entityResolver )
    {
        this.entityResolver = entityResolver;
        return this;
    }

    /**
     * Sets the Object which will receive callbacks for every pop/push action on the default stack or named stacks.
     *
     * @param stackAction the Object which will receive callbacks for every pop/push action on the default stack
     *        or named stacks.
     * @return This loader instance, useful to chain methods.
     */
    public DigesterLoader setStackAction( final StackAction stackAction )
    {
        this.stackAction = stackAction;
        return this;
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
     * @return This loader instance, useful to chain methods.
     * @since 3.1
     */
    public DigesterLoader setExecutorService( final ExecutorService executorService )
    {
        this.executorService = executorService;
        return this;
    }

    /**
     * Return the error handler for this Digester.
     *
     * @return the error handler for this Digester.
     * @since 3.2
     */
    public ErrorHandler getErrorHandler()
    {
        return ( this.errorHandler );
    }

    /**
     * Set the error handler for this Digester.
     *
     * @param errorHandler The new error handler
     * @return This loader instance, useful to chain methods.
     * @since 3.2
     */
    public DigesterLoader setErrorHandler( final ErrorHandler errorHandler )
    {
        this.errorHandler = errorHandler;
        return this;
    }

    /**
     * Gets the document locator associated with our parser.
     *
     * @return the Locator supplied by the document parser
     * @since 3.2
     */
    public Locator getDocumentLocator()
    {
        return locator;
    }

    /**
     * Sets the document locator associated with our parser.
     *
     * @param locator the document locator associated with our parser.
     * @return This loader instance, useful to chain methods.
     * @since 3.2
     */
    public DigesterLoader setDocumentLocator( final Locator locator )
    {
        this.locator = locator;
        return this;
    }

    /**
     * Creates a new {@link Digester} instance that relies on the default {@link Rules} implementation.
     *
     * @return a new {@link Digester} instance
     */
    public Digester newDigester()
    {
        return this.newDigester( new RulesBase() );
    }

    /**
     * Creates a new {@link Digester} instance that relies on the custom user define {@link Rules} implementation
     *
     * @param rules The custom user define {@link Rules} implementation
     * @return a new {@link Digester} instance
     */
    public Digester newDigester( final Rules rules )
    {
        try
        {
            return this.newDigester( this.factory.newSAXParser(), rules );
        }
        catch ( final ParserConfigurationException e )
        {
            throw new DigesterLoadingException( "SAX Parser misconfigured", e );
        }
        catch ( final SAXException e )
        {
            throw new DigesterLoadingException( "An error occurred while initializing the SAX Parser", e );
        }
    }

    /**
     * Creates a new {@link Digester} instance that relies on the given {@code SAXParser}
     * and the default {@link Rules} implementation.
     *
     * @param parser the user defined {@code SAXParser}
     * @return a new {@link Digester} instance
     */
    public Digester newDigester( final SAXParser parser )
    {
        return newDigester( parser, new RulesBase() );
    }

    /**
     * Creates a new {@link Digester} instance that relies on the given {@code SAXParser}
     * and custom user define {@link Rules} implementation.
     *
     * @param parser The user defined {@code SAXParser}
     * @param rules The custom user define {@link Rules} implementation
     * @return a new {@link Digester} instance
     */
    public Digester newDigester( final SAXParser parser, final Rules rules )
    {
        if ( parser == null )
        {
            throw new DigesterLoadingException( "SAXParser must be not null" );
        }

        try
        {
            return this.newDigester( parser.getXMLReader(), rules );
        }
        catch ( final SAXException e )
        {
            throw new DigesterLoadingException( "An error occurred while creating the XML Reader", e );
        }
    }

    /**
     * Creates a new {@link XMLReader} instance that relies on the given {@code XMLReader}
     * and the default {@link Rules} implementation.
     *
     * <b>WARNING</b> Input {@link XMLReader} will be linked to built Digester instance so it is recommended
     * to <b>NOT</b> share same {@link XMLReader} instance to produce the Digester.
     *
     * @param reader The user defined {@code XMLReader}
     * @return a new {@link Digester} instance
     */
    public Digester newDigester( final XMLReader reader )
    {
        return this.newDigester( reader, new RulesBase() );
    }

    /**
     * Creates a new {@link XMLReader} instance that relies on the given {@code XMLReader}
     * and custom user define {@link Rules} implementation.
     *
     * <b>WARNING</b> Input {@link XMLReader} and {@link Rules} will be linked to built Digester instance,
     * so it is recommended to <b>NOT</b> share same {@link XMLReader} and {@link Rules} instance to produce the Digester.
     *
     * @param reader The user defined {@code XMLReader}
     * @param rules The custom user define {@link Rules} implementation
     * @return a new {@link Digester} instance
     */
    public Digester newDigester( final XMLReader reader, final Rules rules )
    {
        if ( reader == null )
        {
            throw new DigesterLoadingException( "XMLReader must be not null" );
        }
        if ( rules == null )
        {
            throw new DigesterLoadingException( "Impossible to create a new Digester with null Rules" );
        }

        final Digester digester = new Digester( reader );
        // the ClassLoader adapter is no needed anymore
        digester.setClassLoader( classLoader.getAdaptedClassLoader() );
        digester.setRules( rules );
        digester.setSubstitutor( substitutor );
        digester.registerAll( entityValidator );
        digester.setEntityResolver( entityResolver );
        digester.setStackAction( stackAction );
        digester.setNamespaceAware( isNamespaceAware() );
        digester.setExecutorService( executorService );
        digester.setErrorHandler( errorHandler );
        digester.setDocumentLocator( locator );

        addRules( digester );

        return digester;
    }

    /**
     * Add rules to an already created Digester instance, analyzing the digester annotations in the target class.
     *
     * @param digester the Digester instance reference.
     */
    public void addRules( final Digester digester )
    {
        final RuleSet ruleSet = createRuleSet();
        ruleSet.addRuleInstances( digester );
    }

    /**
     * Creates a new {@link RuleSet} instance based on the current configuration.
     *
     * @return A new {@link RuleSet} instance based on the current configuration.
     */
    public RuleSet createRuleSet()
    {
        if ( rulesBinder.hasError() )
        {
            final Formatter fmt = new Formatter().format( HEADING );
            int index = 1;

            for ( final ErrorMessage errorMessage : rulesBinder.getErrors() )
            {
                fmt.format( "%s) %s%n", index++, errorMessage.getMessage() );

                final Throwable cause = errorMessage.getCause();
                if ( cause != null )
                {
                    final StringWriter writer = new StringWriter();
                    cause.printStackTrace( new PrintWriter( writer ) );
                    fmt.format( "Caused by: %s", writer.getBuffer() );
                }

                fmt.format( "%n" );
            }

            if ( rulesBinder.errorsSize() == 1 )
            {
                fmt.format( "1 error" );
            }
            else
            {
                fmt.format( "%s errors", rulesBinder.errorsSize() );
            }

            throw new DigesterLoadingException( fmt.toString() );
        }

        return rulesBinder.getFromBinderRuleSet();
    }

}
