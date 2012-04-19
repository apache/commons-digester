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

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.AbstractElementVisitor6;

/**
 * Scope of that visitor is collecting the right sequence of patterns/rules
 * that will be rendered in form of chained methods in the generated RulesModule.
 *
 * @since 3.3
 */
final class DigesterElementVisitor
    extends AbstractElementVisitor6<Void, TypeElement>
{

    private final FormattingMessager messager;

    public DigesterElementVisitor( FormattingMessager messager )
    {
        this.messager = messager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void visitExecutable( ExecutableElement method, TypeElement annotation )
    {
        messager.note( "visiting @%s on method %s", annotation, method );
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void visitPackage( PackageElement pkg, TypeElement annotation )
    {
        // not needed to handle packages in this version
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void visitType( TypeElement clazz, TypeElement annotation )
    {
        System.out.println( ">>>>>>>>>" + clazz.getAnnotationMirrors() );
        messager.note( "visiting @%s on class %s", annotation, clazz );
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void visitTypeParameter( TypeParameterElement methodArgument, TypeElement annotation )
    {
        messager.note( "visiting @%s on methodArgument %s", annotation, methodArgument );
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void visitVariable( VariableElement field, TypeElement annotation )
    {
        messager.note( "visiting @%s on class member %s", annotation, field );
        return null;
    }

}
