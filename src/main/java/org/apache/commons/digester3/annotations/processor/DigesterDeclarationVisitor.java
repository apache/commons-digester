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

import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.ConstructorDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.EnumConstantDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.declaration.ExecutableDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.util.DeclarationVisitor;

/**
 * {@code DeclarationVisitor} implementation for Digester rules handling.
 *
 * @since 3.3
 */
@SuppressWarnings( "restriction" )
final class DigesterDeclarationVisitor
    implements DeclarationVisitor
{

    private final Messager messager;

    DigesterDeclarationVisitor( Messager messager )
    {
        this.messager = messager;
    }

    /**
     * {@inheritDoc}
     */
    public void visitAnnotationTypeDeclaration( AnnotationTypeDeclaration declaration )
    {

    }

    /**
     * {@inheritDoc}
     */
    public void visitAnnotationTypeElementDeclaration( AnnotationTypeElementDeclaration declaration )
    {

    }

    /**
     * {@inheritDoc}
     */
    public void visitClassDeclaration( ClassDeclaration declaration )
    {

    }

    /**
     * {@inheritDoc}
     */
    public void visitConstructorDeclaration( ConstructorDeclaration declaration )
    {

    }

    /**
     * {@inheritDoc}
     */
    public void visitDeclaration( Declaration declaration )
    {

    }

    /**
     * {@inheritDoc}
     */
    public void visitEnumConstantDeclaration( EnumConstantDeclaration declaration )
    {

    }

    /**
     * {@inheritDoc}
     */
    public void visitEnumDeclaration( EnumDeclaration declaration )
    {

    }

    /**
     * {@inheritDoc}
     */
    public void visitExecutableDeclaration( ExecutableDeclaration declaration )
    {

    }

    /**
     * {@inheritDoc}
     */
    public void visitFieldDeclaration( FieldDeclaration declaration )
    {

    }

    /**
     * {@inheritDoc}
     */
    public void visitInterfaceDeclaration( InterfaceDeclaration declaration )
    {

    }

    /**
     * {@inheritDoc}
     */
    public void visitMemberDeclaration( MemberDeclaration declaration )
    {

    }

    /**
     * {@inheritDoc}
     */
    public void visitMethodDeclaration( MethodDeclaration declaration )
    {

    }

    /**
     * {@inheritDoc}
     */
    public void visitPackageDeclaration( PackageDeclaration declaration )
    {

    }

    /**
     * {@inheritDoc}
     */
    public void visitParameterDeclaration( ParameterDeclaration declaration )
    {

    }

    /**
     * {@inheritDoc}
     */
    public void visitTypeDeclaration( TypeDeclaration declaration )
    {

    }

    /**
     * {@inheritDoc}
     */
    public void visitTypeParameterDeclaration( TypeParameterDeclaration declaration )
    {

    }

}
