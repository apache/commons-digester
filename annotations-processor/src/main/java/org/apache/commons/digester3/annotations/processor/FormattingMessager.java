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

import static java.lang.String.format;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.MANDATORY_WARNING;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.Diagnostic.Kind.OTHER;
import static javax.tools.Diagnostic.Kind.WARNING;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic.Kind;

/**
 * Utility class to reduce the boilerplate code of writing messages using the {@link Messager}.
 *
 * @since 3.3
 */
final class FormattingMessager
{

    private final Messager messager;

    public FormattingMessager( Messager messager )
    {
        this.messager = messager;
    }

    public void error( String pattern, Object...args )
    {
        message( ERROR, pattern, args );
    }

    public void mandatoryWarning( String pattern, Object...args )
    {
        message( MANDATORY_WARNING, pattern, args );
    }

    public void note( String pattern, Object...args )
    {
        message( NOTE, pattern, args );
    }

    public void other( String pattern, Object...args )
    {
        message( OTHER, pattern, args );
    }

    public void warning( String pattern, Object...args )
    {
        message( WARNING, pattern, args );
    }

    private void message( Kind kind, String pattern, Object...args )
    {
        messager.printMessage( kind, format( pattern, args ) );
    }

}
