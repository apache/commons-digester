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

import static org.apache.commons.digester3.binder.BinderClassLoader.createBinderClassLoader;
import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 * DIGESTER-155
 */
public final class BinderClassLoaderTestCase
{

    private ClassLoader classLoader = createBinderClassLoader( getClass().getClassLoader() );

    @Test
    public void loadBoolean()
        throws Exception
    {
        typeFound( "boolean", boolean.class );
    }

    @Test
    public void loadByte()
        throws Exception
    {
        typeFound( "byte", byte.class );
    }

    @Test
    public void loadShort()
        throws Exception
    {
        typeFound( "short", short.class );
    }

    @Test
    public void loadInt()
        throws Exception
    {
        typeFound( "int", int.class );
    }

    @Test
    public void loadChar()
        throws Exception
    {
        typeFound( "char", char.class );
    }

    @Test
    public void loadLong()
        throws Exception
    {
        typeFound( "long", long.class );
    }

    @Test
    public void loadFloat()
        throws Exception
    {
        typeFound( "float", float.class );
    }

    @Test
    public void loadDouble()
        throws Exception
    {
        typeFound( "double", double.class );
    }

    private void typeFound( String name, Class<?> expected )
        throws Exception
    {
        Class<?> actual = classLoader.loadClass( name );
        assertSame( expected, actual );
    }

}
