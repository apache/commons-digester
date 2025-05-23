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
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static java.lang.String.format;

import java.util.Formatter;
import java.util.Stack;

import org.xml.sax.Attributes;

/**
 * <p>
 * Rule implementation that uses an {@link ObjectCreationFactory} to create a new object which it pushes onto the object
 * stack. When the element is complete, the object will be popped.
 * </p>
 * <p>
 * This rule is intended in situations where the element's attributes are needed before the object can be created. A
 * common scenario is for the ObjectCreationFactory implementation to use the attributes as parameters in a call to
 * either a factory method or to a non-empty constructor.
 */
public class FactoryCreateRule
    extends Rule
{

    /** Should exceptions thrown by the factory be ignored? */
    private final boolean ignoreCreateExceptions;

    /** Stock to manage */
    private Stack<Boolean> exceptionIgnoredStack;

    /**
     * The attribute containing an override class name if it is present.
     */
    protected String attributeName;

    /**
     * The Java class name of the ObjectCreationFactory to be created. This class must have a no-arguments constructor.
     */
    protected String className;

    /**
     * The object creation factory we will use to instantiate objects as required based on the attributes specified in
     * the matched XML element.
     */
    protected ObjectCreationFactory<?> creationFactory;

    /**
     * <p>
     * Constructs a factory create rule that will use the specified class to create an {@link ObjectCreationFactory}
     * which will then be used to create an object and push it on the stack.
     * </p>
     * <p>
     * Exceptions thrown during the object creation process will be propagated.
     * </p>
     *
     * @param clazz Java class name of the object creation factory class
     */
    public FactoryCreateRule( final Class<? extends ObjectCreationFactory<?>> clazz )
    {
        this( clazz, false );
    }

    /**
     * Constructs a factory create rule that will use the specified class to create an {@link ObjectCreationFactory}
     * which will then be used to create an object and push it on the stack.
     *
     * @param clazz Java class name of the object creation factory class
     * @param ignoreCreateExceptions if true, exceptions thrown by the object creation factory will be ignored.
     */
    public FactoryCreateRule( final Class<? extends ObjectCreationFactory<?>> clazz, final boolean ignoreCreateExceptions )
    {
        this( clazz, null, ignoreCreateExceptions );
    }

    /**
     * <p>
     * Constructs a factory create rule that will use the specified class (possibly overridden by the specified attribute
     * if present) to create an {@link ObjectCreationFactory}, which will then be used to instantiate an object instance
     * and push it onto the stack.
     * </p>
     * <p>
     * Exceptions thrown during the object creation process will be propagated.
     * </p>
     *
     * @param clazz Default Java class name of the factory class
     * @param attributeName Attribute name which, if present, contains an override of the class name of the object
     *            creation factory to create.
     */
    public FactoryCreateRule( final Class<? extends ObjectCreationFactory<?>> clazz, final String attributeName )
    {
        this( clazz, attributeName, false );
    }

    /**
     * Constructs a factory create rule that will use the specified class (possibly overridden by the specified attribute
     * if present) to create an {@link ObjectCreationFactory}, which will then be used to instantiate an object instance
     * and push it onto the stack.
     *
     * @param clazz Default Java class name of the factory class
     * @param attributeName Attribute name which, if present, contains an override of the class name of the object
     *            creation factory to create.
     * @param ignoreCreateExceptions if true, exceptions thrown by the object creation factory will be ignored.
     */
    public FactoryCreateRule( final Class<? extends ObjectCreationFactory<?>> clazz, final String attributeName,
                              final boolean ignoreCreateExceptions )
    {
        this( clazz.getName(), attributeName, ignoreCreateExceptions );
    }

    /**
     * <p>
     * Constructs a factory create rule using the given, already instantiated, {@link ObjectCreationFactory}.
     * </p>
     * <p>
     * Exceptions thrown during the object creation process will be propagated.
     * </p>
     *
     * @param creationFactory called on to create the object.
     */
    public FactoryCreateRule( final ObjectCreationFactory<?> creationFactory )
    {
        this( creationFactory, false );
    }

    /**
     * Constructs a factory create rule using the given, already instantiated, {@link ObjectCreationFactory}.
     *
     * @param creationFactory called on to create the object.
     * @param ignoreCreateExceptions if true, exceptions thrown by the object creation factory will be ignored.
     */
    public FactoryCreateRule( final ObjectCreationFactory<?> creationFactory, final boolean ignoreCreateExceptions )
    {
        this.creationFactory = creationFactory;
        this.ignoreCreateExceptions = ignoreCreateExceptions;
    }

    /**
     * <p>
     * Constructs a factory create rule that will use the specified class name to create an {@link ObjectCreationFactory}
     * which will then be used to create an object and push it on the stack.
     * </p>
     * <p>
     * Exceptions thrown during the object creation process will be propagated.
     * </p>
     *
     * @param className Java class name of the object creation factory class
     */
    public FactoryCreateRule( final String className )
    {
        this( className, false );
    }

    /**
     * Constructs a factory create rule that will use the specified class name to create an {@link ObjectCreationFactory}
     * which will then be used to create an object and push it on the stack.
     *
     * @param className Java class name of the object creation factory class
     * @param ignoreCreateExceptions if true, exceptions thrown by the object creation factory will be ignored.
     */
    public FactoryCreateRule( final String className, final boolean ignoreCreateExceptions )
    {
        this( className, null, ignoreCreateExceptions );
    }

    /**
     * <p>
     * Constructs a factory create rule that will use the specified class name (possibly overridden by the specified
     * attribute if present) to create an {@link ObjectCreationFactory}, which will then be used to instantiate an
     * object instance and push it onto the stack.
     * </p>
     * <p>
     * Exceptions thrown during the object creation process will be propagated.
     * </p>
     *
     * @param className Default Java class name of the factory class
     * @param attributeName Attribute name which, if present, contains an override of the class name of the object
     *            creation factory to create.
     */
    public FactoryCreateRule( final String className, final String attributeName )
    {
        this( className, attributeName, false );
    }

    /**
     * Constructs a factory create rule that will use the specified class name (possibly overridden by the specified
     * attribute if present) to create an {@link ObjectCreationFactory}, which will then be used to instantiate an
     * object instance and push it onto the stack.
     *
     * @param className Default Java class name of the factory class
     * @param attributeName Attribute name which, if present, contains an override of the class name of the object
     *            creation factory to create.
     * @param ignoreCreateExceptions if true, exceptions thrown by the object creation factory will be ignored.
     */
    public FactoryCreateRule( final String className, final String attributeName, final boolean ignoreCreateExceptions )
    {
        this.className = className;
        this.attributeName = attributeName;
        this.ignoreCreateExceptions = ignoreCreateExceptions;
    }

    @Override
    public void begin( final String namespace, final String name, final Attributes attributes )
        throws Exception
    {
        if ( ignoreCreateExceptions )
        {
            if ( exceptionIgnoredStack == null )
            {
                exceptionIgnoredStack = new Stack<>();
            }

            try
            {
                final Object instance = getFactory( attributes ).createObject( attributes );

                if ( getDigester().getLogger().isDebugEnabled() )
                {
                    getDigester().getLogger().debug( format( "[FactoryCreateRule]{%s} New %s",
                                                             getDigester().getMatch(),
                                                             instance == null ? "null object"
                                                                             : instance.getClass().getName() ) );
                }
                getDigester().push( instance );
                exceptionIgnoredStack.push( Boolean.FALSE );

            }
            catch ( final Exception e )
            {
                // log message and error
                if ( getDigester().getLogger().isInfoEnabled() )
                {
                    getDigester().getLogger().info( format( "[FactoryCreateRule]{%s} Create exception ignored: %s",
                                                            getDigester().getMatch(),
                                                            e.getMessage() == null ? e.getClass().getName()
                                                                            : e.getMessage() ) );
                    if ( getDigester().getLogger().isDebugEnabled() )
                    {
                        getDigester().getLogger().debug( "[FactoryCreateRule] Ignored exception:", e );
                    }
                }
                exceptionIgnoredStack.push( Boolean.TRUE );
            }

        }
        else
        {
            final Object instance = getFactory( attributes ).createObject( attributes );

            if ( getDigester().getLogger().isDebugEnabled() )
            {
                getDigester().getLogger().debug( format( "[FactoryCreateRule]{%s} New %s",
                                                         getDigester().getMatch(),
                                                         instance == null ? "null object"
                                                                         : instance.getClass().getName() ) );
            }
            getDigester().push( instance );
        }
    }

    @Override
    public void end( final String namespace, final String name )
        throws Exception
    {
        // check if object was created
        // this only happens if an exception was thrown and we're ignoring them
        if ( ignoreCreateExceptions
                        && exceptionIgnoredStack != null
                        && !exceptionIgnoredStack.empty()
                        && exceptionIgnoredStack.pop().booleanValue() )
        {
            // creation exception was ignored
            // nothing was put onto the stack
            if ( getDigester().getLogger().isTraceEnabled() )
            {
                getDigester().getLogger().trace( format( "[FactoryCreateRule]{%s} No creation so no push so no pop",
                                                         getDigester().getMatch() ) );
            }
            return;
        }

        final Object top = getDigester().pop();
        if ( getDigester().getLogger().isDebugEnabled() )
        {
            getDigester().getLogger().debug( format( "[FactoryCreateRule]{%s} Pop %s",
                                                     getDigester().getMatch(),
                                                     top.getClass().getName() ) );
        }
    }

    @Override
    public void finish()
        throws Exception
    {
        if ( attributeName != null )
        {
            creationFactory = null;
        }
    }

    /**
     * Gets an instance of our associated object creation factory, creating one if necessary.
     *
     * @param attributes Attributes passed to our factory creation element
     * @return An instance of our associated object creation factory, creating one if necessary
     * @throws Exception if any error occurs
     */
    protected ObjectCreationFactory<?> getFactory( final Attributes attributes )
        throws Exception
    {
        if ( creationFactory == null )
        {
            String realClassName = className;
            if ( attributeName != null )
            {
                final String value = attributes.getValue( attributeName );
                if ( value != null )
                {
                    realClassName = value;
                }
            }
            if ( getDigester().getLogger().isDebugEnabled() )
            {
                getDigester().getLogger().debug( format( "[FactoryCreateRule]{%s} New factory %s",
                                                         getDigester().getMatch(), realClassName ) );
            }
            final Class<?> clazz = getDigester().getClassLoader().loadClass( realClassName );
            creationFactory = (ObjectCreationFactory<?>) clazz.newInstance();
            creationFactory.setDigester( getDigester() );
        }
        return creationFactory;
    }

    @Override
    public String toString()
    {
        final Formatter formatter = new Formatter().format( "FactoryCreateRule[className=%s, attributeName=%s",
                                                      className, attributeName );
        if ( creationFactory != null )
        {
            formatter.format( ", creationFactory=%s", creationFactory );
        }
        formatter.format( "]" );
        return formatter.toString();
    }

}
