package org.apache.commons.digester3.annotations.processor;

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

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

import java.util.Collection;
import java.util.Set;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessors;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

/**
 * {@link DigesterAnnotationProcessor} factory.
 *
 * @since 3.3
 */
@SuppressWarnings( "restriction" )
public final class DigesterAnnotationProcessorFactory
    implements AnnotationProcessorFactory
{

    /**
     * {@inheritDoc}
     */
    public AnnotationProcessor getProcessorFor( Set<AnnotationTypeDeclaration> declarations,
                                                AnnotationProcessorEnvironment environment )
    {
        if ( declarations.isEmpty() )
        {
            return AnnotationProcessors.NO_OP;
        }
        return new DigesterAnnotationProcessor( environment );
    }

    /**
     * {@inheritDoc}
     */
    public Collection<String> supportedAnnotationTypes()
    {
        return singleton( "org.apache.commons.digester3.annotations.rules.*" );
    }

    /**
     * {@inheritDoc}
     */
    public Collection<String> supportedOptions()
    {
        return emptyList();
    }

}
