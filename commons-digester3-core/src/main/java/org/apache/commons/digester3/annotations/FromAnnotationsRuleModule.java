package org.apache.commons.digester3.annotations;

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

import static org.apache.commons.digester3.annotations.utils.AnnotationUtils.getAnnotationsArrayValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.commons.digester3.Rule;
import org.apache.commons.digester3.annotations.reflect.MethodArgument;
import org.apache.commons.digester3.binder.AbstractRulesModule;

/**
 * {@link org.apache.commons.digester3.binder.RulesModule} implementation that allows loading rules from
 * annotated classes.
 *
 * @since 3.0
 */
public abstract class FromAnnotationsRuleModule
    extends AbstractRulesModule
{

    private static final String JAVA_PACKAGE = "java";

    private static final AnnotationHandlerFactory DEFAULT_HANDLER_FACTORY = new DefaultAnnotationHandlerFactory();

    private AnnotationHandlerFactory annotationHandlerFactory = DEFAULT_HANDLER_FACTORY;

    private WithMemoryRulesBinder rulesBinder;

    /**
     * Scan the input Class, looking for Digester rules expressed via annotations, and binds them.
     *
     * @param type the type has to be analyzed
     * @see DigesterRule
     */
    protected final void bindRulesFrom( final Class<?> type )
    {
        if ( type == null || type.getPackage().getName().startsWith( JAVA_PACKAGE )
            || rulesBinder.isAlreadyBound( type ) )
        {
            return;
        }

        // TYPE
        visitElements( type );

        if ( !type.isInterface() )
        {
            // CONSTRUCTOR
            visitElements( type::getDeclaredConstructors );

            // FIELD
            visitElements( type::getDeclaredFields );
        }

        // METHOD
        visitElements( type::getDeclaredMethods );

        rulesBinder.markAsBound( type );
        bindRulesFrom( type.getSuperclass() );
    }

    @Override
    protected final void configure()
    {
        if ( rulesBinder == null )
        {
            rulesBinder = new WithMemoryRulesBinder( rulesBinder() );
        }

        try
        {
            configureRules();
        }
        finally
        {
            rulesBinder = null;
        }
    }

    /**
     * Configures a {@link org.apache.commons.digester3.binder.RulesBinder} via the exposed methods.
     */
    protected abstract void configureRules();

    /**
     * Handles the current visited element and related annotation, invoking the
     * right handler putting the rule provider in the rule set.
     *
     * @param annotation the current visited annotation.
     * @param element the current visited element.
     */
    @SuppressWarnings( "unchecked" )
    private <A extends Annotation, E extends AnnotatedElement, R extends Rule> void handle( final A annotation, final E element )
    {
        final Class<?> annotationType = annotation.annotationType();

        // check if it is one of the @*.List annotation
        if ( annotationType.isAnnotationPresent( DigesterRuleList.class ) )
        {
            final Annotation[] annotations = getAnnotationsArrayValue( annotation );
            if ( annotations != null && annotations.length > 0 )
            {
                // if it is an annotations array, process them
                for ( final Annotation ptr : annotations )
                {
                    handle( ptr, element );
                }
            }
        }
        else if ( annotationType.isAnnotationPresent( DigesterRule.class ) )
        {
            final DigesterRule digesterRule = annotationType.getAnnotation( DigesterRule.class );

            // the default behavior if the handler is not specified
            final Class<? extends AnnotationHandler<Annotation, AnnotatedElement>> handlerType =
                (Class<? extends AnnotationHandler<Annotation, AnnotatedElement>>) digesterRule.handledBy();
            try
            {
                final AnnotationHandler<Annotation, AnnotatedElement> handler =
                    annotationHandlerFactory.newInstance( handlerType );

                // run!
                handler.handle( annotation, element, this.rulesBinder );
            }
            catch ( final Exception e )
            {
                rulesBinder.addError( e );
            }
        }
    }

    /**
     * Allows users plug a different {@link AnnotationHandlerFactory} to create {@link AnnotationHandler} instances.
     *
     * @param annotationHandlerFactory A custom {@link AnnotationHandlerFactory} to create
     *        {@link AnnotationHandler} instances
     */
    protected final void useAnnotationHandlerFactory( final AnnotationHandlerFactory annotationHandlerFactory )
    {
        if ( annotationHandlerFactory == null )
        {
            throw new IllegalArgumentException( "Argument 'annotationHandlerFactory' must be not null" );
        }

        this.annotationHandlerFactory = annotationHandlerFactory;
    }

    /**
     * Allows users to switch back to the default {@link AnnotationHandlerFactory} implementation.
     */
    protected final void useDefaultAnnotationHandlerFactory()
    {
        useAnnotationHandlerFactory( DEFAULT_HANDLER_FACTORY );
    }

    /**
     *
     *
     * @param annotatedElements
     */
    private void visitElements( final AnnotatedElement... annotatedElements )
    {
        for ( final AnnotatedElement element : annotatedElements )
        {
            for ( final Annotation annotation : element.getAnnotations() )
            {
                handle( annotation, element );
            }

            if ( element instanceof Constructor || element instanceof Method )
            {
                Annotation[][] parameterAnnotations;
                Class<?>[] parameterTypes;

                if ( element instanceof Constructor )
                {
                    // constructor args
                    final Constructor<?> constructor = (Constructor<?>) element;
                    parameterAnnotations = constructor.getParameterAnnotations();
                    parameterTypes = constructor.getParameterTypes();
                }
                else
                {
                    // method args
                    final Method method = (Method) element;
                    parameterAnnotations = method.getParameterAnnotations();
                    parameterTypes = method.getParameterTypes();
                }

                for ( int i = 0; i < parameterTypes.length; i++ )
                {
                    visitElements( new MethodArgument( i, parameterTypes[i], parameterAnnotations[i] ) );
                }
            }
        }
    }

    /**
     *
     *
     * @param <AE>
     * @param action
     */
    private <AE extends AnnotatedElement> void visitElements( final PrivilegedAction<AE[]> action )
    {
        AE[] annotatedElements;
        if ( System.getSecurityManager() != null )
        {
            annotatedElements = AccessController.doPrivileged( action );
        }
        else
        {
            annotatedElements = action.run();
        }
        visitElements( annotatedElements );
    }

}
