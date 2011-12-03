package org.apache.commons.digester3.annotations.handlers;

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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;

import org.apache.commons.digester3.annotations.AnnotationHandler;
import org.apache.commons.digester3.annotations.rules.ObjectCreate;
import org.apache.commons.digester3.binder.ObjectCreateBuilder;
import org.apache.commons.digester3.binder.RulesBinder;

/**
 * {@link ObjectCreateHandler} handler.
 *
 * @since 3.0
 */
public final class ObjectCreateHandler
    implements AnnotationHandler<ObjectCreate, AnnotatedElement>
{

    /**
     * {@inheritDoc}
     */
    public void handle( ObjectCreate annotation, AnnotatedElement element, RulesBinder rulesBinder )
    {
        Class<?> type = null;
        if ( element instanceof Class<?> )
        {
            type = (Class<?>) element;
        }
        else if ( element instanceof Constructor<?> )
        {
            type = ( (Constructor<?>) element ).getDeclaringClass();
        }
        else
        {
            rulesBinder.addError( "Misplaced @ObjectCreate annotation to %s, Class and Constructor only supported",
                                  element );
            return;
        }

        ObjectCreateBuilder builder = rulesBinder
                .forPattern( annotation.pattern() )
                .withNamespaceURI( annotation.namespaceURI() )
                .createObject()
                .ofType( type )
                .ofTypeSpecifiedByAttribute( annotation.attributeName() != null ? annotation.attributeName() : null );

        if ( element instanceof Constructor<?> )
        {
            Constructor<?> method = (Constructor<?>) element;
            builder.usingConstructor( method.getParameterTypes() );
        }
    }

}
