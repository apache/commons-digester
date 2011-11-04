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

import static java.lang.String.format;

import java.util.Map;

/**
 *
 *
 * @since 3.2
 */
public final class ConstructorArgumentTypeBinder
{

    private final ObjectCreateBuilder parent;

    private final Map<String, Class<?>> constructorArguments;

    private final ClassLoader classLoader;

    private final String attibuteName;

    ConstructorArgumentTypeBinder( ObjectCreateBuilder parent,
                                   Map<String, Class<?>> constructorArguments,
                                   String attibuteName,
                                   ClassLoader classLoader )
    {
        this.parent = parent;
        this.constructorArguments = constructorArguments;
        this.attibuteName = attibuteName;
        this.classLoader = classLoader;
    }

    public <T> ObjectCreateBuilder ofType( Class<T> type )
    {
        if ( type == null )
        {
            parent.reportError( format( "createObject().addConstructorArgument( %s ).ofType( Class<T> )", attibuteName ),
                                "NULL attibute name not allowed" );
            return parent;
        }

        constructorArguments.put( attibuteName, type );
        return parent;
    }

    public <T> ObjectCreateBuilder ofType( String type )
    {
        if ( type == null )
        {
            parent.reportError( format( "createObject().addConstructorArgument( %s ).ofType( String )", attibuteName ),
                                "NULL attibute name not allowed" );
            return parent;
        }

        try
        {
            constructorArguments.put( attibuteName, classLoader.loadClass( type ) );
        }
        catch ( ClassNotFoundException e )
        {
            parent.reportError( format( "createObject().addConstructorArgument( %s ).ofType( String )", attibuteName ),
                                format( "class '%s' cannot be load", type ) );
        }

        return parent;
    }

}
