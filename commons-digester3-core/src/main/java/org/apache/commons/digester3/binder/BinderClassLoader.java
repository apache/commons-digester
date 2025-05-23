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
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static java.lang.System.getSecurityManager;
import static java.security.AccessController.doPrivileged;

import java.net.URL;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class BinderClassLoader
    extends ClassLoader
{

    private static final Map<String, Class<?>> PRIMITIVE_TYPES;
    static
    {
        final HashMap<String, Class<?>> primitiveTypes = new HashMap<>();
        primitiveTypes.put( "boolean", boolean.class );
        primitiveTypes.put( "byte", byte.class );
        primitiveTypes.put( "short", short.class );
        primitiveTypes.put( "int", int.class );
        primitiveTypes.put( "char", char.class );
        primitiveTypes.put( "long", long.class );
        primitiveTypes.put( "float", float.class );
        primitiveTypes.put( "double", double.class );
        PRIMITIVE_TYPES = Collections.unmodifiableMap( primitiveTypes );
    }

    public static BinderClassLoader createBinderClassLoader( final ClassLoader adaptedClassLoader )
    {
        final PrivilegedAction<BinderClassLoader> action = () -> new BinderClassLoader( adaptedClassLoader );

        if ( getSecurityManager() != null )
        {
            return doPrivileged( action );
        }
        return action.run();
    }

    private BinderClassLoader( final ClassLoader adaptedClassLoader )
    {
        super( adaptedClassLoader );
    }

    public ClassLoader getAdaptedClassLoader()
    {
        return getParent();
    }

    @Override
    public URL getResource( final String name )
    {
        return getAdaptedClassLoader().getResource( name );
    }

    @Override
    protected synchronized Class<?> loadClass( final String name, final boolean resolve )
        throws ClassNotFoundException
    {
        if ( PRIMITIVE_TYPES.containsKey( name ) )
        {
            return PRIMITIVE_TYPES.get( name );
        }
        return getParent().loadClass( name );
    }

}
