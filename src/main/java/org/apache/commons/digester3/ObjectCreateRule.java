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

import static java.lang.System.arraycopy;
import static java.lang.String.format;
import static org.apache.commons.beanutils.ConstructorUtils.getAccessibleConstructor;
import static org.apache.commons.beanutils.ConvertUtils.convert;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Rule implementation that creates a new object and pushes it onto the object stack. When the element is complete, the
 * object will be popped
 */
public class ObjectCreateRule
    extends Rule
{
    private static class DeferredConstructionCallback implements MethodInterceptor
    {
        Constructor<?> constructor;
        Object[] constructorArgs;
        ArrayList<RecordedInvocation> invocations = new ArrayList<RecordedInvocation>();
        Object delegate;

        DeferredConstructionCallback( Constructor<?> constructor, Object[] constructorArgs )
        {
            this.constructor = constructor;
            this.constructorArgs = constructorArgs;
        }

        public Object intercept( Object obj, Method method, Object[] args, MethodProxy proxy )
            throws Throwable
        {
            boolean hasDelegate = delegate != null;
            if ( !hasDelegate )
            {
                invocations.add( new RecordedInvocation( method, args ) );
            }
            if ( hasDelegate )
            {
                return proxy.invoke( delegate, args );
            }
            return proxy.invokeSuper( obj, args );
        }

        void establishDelegate()
            throws Exception
        {
            convertTo( constructor.getParameterTypes(), constructorArgs );
            delegate = constructor.newInstance( constructorArgs );
            for ( RecordedInvocation invocation : invocations )
            {
                invocation.getInvokedMethod().invoke( delegate, invocation.getArguments() );
            }
            constructor = null;
            constructorArgs = null;
            invocations = null;
        }
    }

    private static class ProxyManager
    {
        private final Class<?> clazz;
        private final Constructor<?> constructor;
        private final Object[] templateConstructorArguments;
        private final Digester digester;
        private final boolean hasDefaultConstructor;
        private Factory factory;

        ProxyManager( Class<?> clazz, Constructor<?> constructor, Object[] constructorArguments, Digester digester )
        {
            this.clazz = clazz;
            hasDefaultConstructor = getAccessibleConstructor( clazz, new Class[0] ) != null;
            this.constructor = constructor;
            Class<?>[] argTypes = constructor.getParameterTypes();
            templateConstructorArguments = new Object[argTypes.length];
            if ( constructorArguments == null )
            {
                for ( int i = 0; i < templateConstructorArguments.length; i++ )
                {
                    if ( argTypes[i].equals( boolean.class ) )
                    {
                        templateConstructorArguments[i] = Boolean.FALSE;
                        continue;
                    }
                    if ( argTypes[i].isPrimitive() )
                    {
                        templateConstructorArguments[i] = convert( "0", argTypes[i] );
                        continue;
                    }
                    templateConstructorArguments[i] = null;
                }
            }
            else
            {
                if ( constructorArguments.length != argTypes.length )
                {
                    throw new IllegalArgumentException(
                        format( "wrong number of constructor arguments specified: %s instead of %s",
                        constructorArguments.length, argTypes.length ) );
                }
                arraycopy( constructorArguments, 0, templateConstructorArguments, 0, constructorArguments.length );
            }
            convertTo( argTypes, templateConstructorArguments );
            this.digester = digester;
        }

        Object createProxy()
        {
            Object[] constructorArguments = new Object[templateConstructorArguments.length];
            arraycopy( templateConstructorArguments, 0, constructorArguments, 0, constructorArguments.length );
            digester.pushParams( constructorArguments );

            DeferredConstructionCallback callback =
                new DeferredConstructionCallback( constructor, constructorArguments );

            Object result;

            if ( factory == null )
            {
                Enhancer enhancer = new Enhancer();
                enhancer.setSuperclass( clazz );
                enhancer.setCallback( callback );
                enhancer.setClassLoader( digester.getClassLoader() );
                enhancer.setInterceptDuringConstruction( false );
                if ( hasDefaultConstructor )
                {
                    result = enhancer.create();
                }
                else
                {
                    result = enhancer.create( constructor.getParameterTypes(), constructorArguments );
                }
                factory = (Factory) result;
                return result;
            }

            if ( hasDefaultConstructor )
            {
                result = factory.newInstance( callback );
            }
            else
            {
                result = factory.newInstance( constructor.getParameterTypes(),
                    constructorArguments, new Callback[] { callback } );
            }
            return result;
        }

        void finalize( Object proxy )
            throws Exception
        {
            digester.popParams();
            ( (DeferredConstructionCallback) ( (Factory) proxy ).getCallback( 0 ) ).establishDelegate();
        }
    }

    // ----------------------------------------------------------- Constructors

    /**
     * Construct an object create rule with the specified class name.
     *
     * @param className Java class name of the object to be created
     */
    public ObjectCreateRule( String className )
    {
        this( className, (String) null );
    }

    /**
     * Construct an object create rule with the specified class.
     *
     * @param clazz Java class name of the object to be created
     */
    public ObjectCreateRule( Class<?> clazz )
    {
        this( clazz.getName(), (String) null );
        this.clazz = clazz;
    }

    /**
     * Construct an object create rule with the specified class name and an optional attribute name containing an
     * override.
     *
     * @param className Java class name of the object to be created
     * @param attributeName Attribute name which, if present, contains an override of the class name to create
     */
    public ObjectCreateRule( String className, String attributeName )
    {
        this.className = className;
        this.attributeName = attributeName;
    }

    /**
     * Construct an object create rule with the specified class and an optional attribute name containing an override.
     *
     * @param attributeName Attribute name which, if present, contains an
     * @param clazz Java class name of the object to be created override of the class name to create
     */
    public ObjectCreateRule( String attributeName, Class<?> clazz )
    {
        this( clazz.getName(), attributeName );
        this.clazz = clazz;
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * The attribute containing an override class name if it is present.
     */
    protected String attributeName = null;

    /**
     * The Java class of the object to be created.
     */
    protected Class<?> clazz = null;

    /**
     * The Java class name of the object to be created.
     */
    protected String className = null;

    /**
     * The constructor argument types.
     *
     * @since 3.2
     */
    private Class<?>[] constructorArgumentTypes;

    /**
     * The explictly specified default constructor arguments which may be overridden by CallParamRules.
     *
     * @since 3.2
     */
    private Object[] defaultConstructorArguments;

    /**
     * Helper object for managing proxies.
     *
     * @since 3.2
     */
    private ProxyManager proxyManager;

    // --------------------------------------------------------- Public Methods

    /**
     * Allows users to specify constructor argument types.
     *
     * @param constructorArgumentTypes the constructor argument types
     * @since 3.2
     */
    public void setConstructorArgumentTypes( Class<?>... constructorArgumentTypes )
    {
        if ( constructorArgumentTypes == null )
        {
            throw new IllegalArgumentException( "Parameter 'constructorArgumentTypes' must not be null" );
        }

        this.constructorArgumentTypes = constructorArgumentTypes;
    }

    /**
     * Allows users to specify default constructor arguments.  If a default/no-arg constructor is not available
     * for the target class, these arguments will be used to create the proxy object.  For any argument
     * not supplied by a {@link CallParamRule}, the corresponding item from this array will be used
     * to construct the final object as well.
     *
     * @param constructorArguments the default constructor arguments.
     * @since 3.2
     */
    public void setDefaultConstructorArguments( Object... constructorArguments )
    {
        if ( constructorArguments == null )
        {
            throw new IllegalArgumentException( "Parameter 'constructorArguments' must not be null" );
        }

        this.defaultConstructorArguments = constructorArguments;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void begin( String namespace, String name, Attributes attributes )
        throws Exception
    {
        Class<?> clazz = this.clazz;

        if ( clazz == null )
        {
            // Identify the name of the class to instantiate
            String realClassName = className;
            if ( attributeName != null )
            {
                String value = attributes.getValue( attributeName );
                if ( value != null )
                {
                    realClassName = value;
                }
            }
            if ( getDigester().getLogger().isDebugEnabled() )
            {
                getDigester().getLogger().debug( format( "[ObjectCreateRule]{%s} New '%s'",
                                                         getDigester().getMatch(),
                                                         realClassName ) );
            }

            // Instantiate the new object and push it on the context stack
            clazz = getDigester().getClassLoader().loadClass( realClassName );
        }
        Object instance;
        if ( constructorArgumentTypes == null || constructorArgumentTypes.length == 0 )
        {
            if ( getDigester().getLogger().isDebugEnabled() )
            {
                getDigester()
                    .getLogger()
                    .debug( format( "[ObjectCreateRule]{%s} New '%s' using default empty constructor",
                                    getDigester().getMatch(),
                                    clazz.getName() ) );
            }

            instance = clazz.newInstance();
        }
        else
        {
            if ( proxyManager == null )
            {
                Constructor<?> constructor = getAccessibleConstructor( clazz, constructorArgumentTypes );

                if ( constructor == null )
                {
                    throw new SAXException(
                                   format( "[ObjectCreateRule]{%s} Class '%s' does not have a construcor with types %s",
                                           getDigester().getMatch(),
                                           clazz.getName(),
                                           Arrays.toString( constructorArgumentTypes ) ) );
                }
                proxyManager = new ProxyManager( clazz, constructor, defaultConstructorArguments, getDigester() );
            }
            instance = proxyManager.createProxy();
        }
        getDigester().push( instance );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void end( String namespace, String name )
        throws Exception
    {
        Object top = getDigester().pop();

        if ( proxyManager != null )
        {
            proxyManager.finalize( top );
        }

        if ( getDigester().getLogger().isDebugEnabled() )
        {
            getDigester().getLogger().debug( format( "[ObjectCreateRule]{%s} Pop '%s'",
                                                     getDigester().getMatch(),
                                                     top.getClass().getName() ) );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return format( "ObjectCreateRule[className=%s, attributeName=%s]", className, attributeName );
    }

    private static void convertTo( Class<?>[] types, Object[] array )
    {
        if ( array.length != types.length )
        {
            throw new IllegalArgumentException();
        }
        // this piece of code is adapted from CallMethodRule
        for ( int i = 0; i < array.length; i++ )
        {
            // convert nulls and convert stringy parameters for non-stringy param types
            if ( array[i] == null
                    || ( array[i] instanceof String && !String.class.isAssignableFrom( types[i] ) ) )
            {
                array[i] = convert( (String) array[i], types[i] );
            }
        }
    }

}
