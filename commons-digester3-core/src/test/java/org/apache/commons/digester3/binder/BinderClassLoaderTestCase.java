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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * DIGESTER-155
 */
public final class BinderClassLoaderTestCase
{

    public static class Dummy
    {
    }

    private static final class ExtendedClassLoader
        extends ClassLoader
    {

        private static final class InMemoryURLStreamHandlerFactory
            implements URLStreamHandlerFactory
        {
            @Override
            public URLStreamHandler createURLStreamHandler( final String protocol )
            {
                return new URLStreamHandler()
                {
                    @Override
                    protected URLConnection openConnection( final URL u )
                    {
                        return new URLConnection( u )
                        {
                            private InputStream inputStream;

                            @Override
                            public void connect()
                            {
                                if ( !connected ) {
                                    final byte[] data = IN_MEMORY_RESOURCES.get( url.getPath() );
                                    if ( data != null )
                                    {
                                        inputStream = new ByteArrayInputStream( data );
                                    }
                                    connected = true;
                                }
                            }

                            @Override
                            public InputStream getInputStream()
                                throws IOException
                            {
                                connect();
                                if ( inputStream == null )
                                {
                                    throw new FileNotFoundException( url.getPath() );
                                }
                                return inputStream;
                            }
                        };
                    }
                };
            }
        }

        private final InMemoryURLStreamHandlerFactory streamHandlerFactory = new InMemoryURLStreamHandlerFactory();

        @Override
        public URL getResource( final String name )
        {
            if ( name.startsWith( "inmemory:" ) )
            {
                try
                {
                    return new URL( null, name, streamHandlerFactory.createURLStreamHandler( "inmemory" ) );
                }
                catch ( final MalformedURLException e )
                {
                    throw new RuntimeException( e );
                }
            }
            return super.getResource( name );
        }

        @Override
        protected Class<?> loadClass( final String name, final boolean resolve )
            throws ClassNotFoundException
        {
            final String dummyClassName = Dummy.class.getName();
            if ( dummyClassName.equals( name ) ) {
                Class<?> result = findLoadedClass( name );
                if ( result == null )
                {
                    final byte[] byteCode = IN_MEMORY_RESOURCES.get( dummyClassName.replace( '.', '/' ) + ".class" );
                    result = defineClass( name, byteCode, 0, byteCode.length );
                    resolveClass( result );
                }
                return result;
            }
            return super.loadClass( name, resolve );
        }
    }

    private static final Map<String, byte[]> IN_MEMORY_RESOURCES = new HashMap<>();

    static
    {
        try
        {
            IN_MEMORY_RESOURCES.put( "dummyResource", "Resource data".getBytes(StandardCharsets.UTF_8) );

            // put bytes of Dummy class
            final String dummyClassName = Dummy.class.getName();
            final String resourceName = dummyClassName.replace( '.', '/' ) + ".class";
            try ( InputStream input = Dummy.class.getClassLoader().getResourceAsStream( resourceName ) )
            {
                IN_MEMORY_RESOURCES.put( resourceName, toByteArray( input ) );
            }
        }
        catch ( final IOException e )
        {
            throw new ExceptionInInitializerError( e );
        }
    }

    private static byte[] toByteArray( final InputStream input )
        throws IOException
    {
        final ByteArrayOutputStream result = new ByteArrayOutputStream( 512 );
        int n;
        while ( ( n = input.read() ) != -1 )
        {
            result.write( n );
        }
        return result.toByteArray();
    }

    private final BinderClassLoader classLoader = createBinderClassLoader( new ExtendedClassLoader() );

    @Test
    public void testGetPrefixedResource()
        throws Exception
    {
        final URL resource = classLoader.getResource( "inmemory:dummyResource" );
        assertNotNull( resource );
        assertEquals( resource.getPath(), "dummyResource" );
        try ( InputStream input = resource.openStream() )
        {
            final byte[] bytes = toByteArray( input );
            assertArrayEquals( bytes, IN_MEMORY_RESOURCES.get( "dummyResource" ) );
        }
    }

    @Test
    public void testGetResource()
    {
        final ClassLoader clToAdapt = new ClassLoader()
        {

            @Override
            public URL getResource( final String name )
            {
                if ( "xxx".equals( name ) )
                {
                    return super.getResource( "org/apache/commons/digester3/binder/BinderClassLoaderTestCase.class" );
                }
                return super.getResource( name );
            }

        };
        final ClassLoader binderCl = createBinderClassLoader( clToAdapt );
        assertNotNull( binderCl.getResource( "xxx" ) );
    }

    @Test
    public void testLoadBoolean()
        throws Exception
    {
        typeFound( "boolean", boolean.class );
    }

    @Test
    public void testLoadByte()
        throws Exception
    {
        typeFound( "byte", byte.class );
    }

    @Test
    public void testLoadChar()
        throws Exception
    {
        typeFound( "char", char.class );
    }

    @Test
    public void testLoadClass()
        throws Exception
    {
        final Class<?> dummyClass1 = Dummy.class;
        final Class<?> dummyClass2 = classLoader.loadClass( dummyClass1.getName() );

        assertEquals( dummyClass1.getName(), dummyClass2.getName() );
        assertFalse( dummyClass2.getDeclaredConstructor().newInstance() instanceof Dummy );
        assertNotSame( dummyClass1, dummyClass2 );
        assertNotSame( dummyClass1.getClassLoader(), dummyClass2.getClassLoader() );
        assertSame( classLoader.getAdaptedClassLoader(), dummyClass2.getClassLoader() );
    }

    @Test
    public void testLoadDouble()
        throws Exception
    {
        typeFound( "double", double.class );
    }

    @Test
    public void testLoadFloat()
        throws Exception
    {
        typeFound( "float", float.class );
    }

    @Test
    public void testLoadInt()
        throws Exception
    {
        typeFound( "int", int.class );
    }

    @Test
    public void testLoadLong()
        throws Exception
    {
        typeFound( "long", long.class );
    }

    @Test
    public void testLoadShort()
        throws Exception
    {
        typeFound( "short", short.class );
    }

    private void typeFound( final String name, final Class<?> expected )
        throws Exception
    {
        final Class<?> actual = classLoader.loadClass( name );
        assertSame( expected, actual );
    }

}
