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

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.processing.Filer;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;

/**
 * @since 3.3
 */
final class FilerCodeWriter
    extends CodeWriter
{

    /**
     * Java source file extension.
     */
    private static final String JAVA_FILE_EXTENSION = ".java";

    private final Filer filer;

    public FilerCodeWriter( Filer filer )
    {
        this.filer = filer;
    }

    @Override
    public OutputStream openBinary( JPackage pkg, String fileName )
        throws IOException
    {
        String className = fileName.substring( 0, fileName.length() - JAVA_FILE_EXTENSION.length() );

        return filer.createSourceFile( format( "%s.%s", pkg.name(), className ) ).openOutputStream();
    }

    @Override
    public void close()
        throws IOException
    {
        // do nothing
    }

}
