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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * DIGESTER-155
 */
public final class BinderClassLoaderTestCase
{

    private final BinderClassLoader classLoader = createBinderClassLoader( new ExtendedClassLoader() );

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

    @Test
    public void testGetResource()
    {
        ClassLoader clToAdapt = new ClassLoader()
        {

            @Override
            public URL getResource( String name )
            {
                if ( "xxx".equals( name ) )
                {
                    return super.getResource( "org/apache/commons/digester3/binder/BinderClassLoaderTestCase.class" );
                }
                return super.getResource( name );
            }

        };
        ClassLoader binderCl = createBinderClassLoader( clToAdapt );
        assertNotNull( binderCl.getResource( "xxx" ) );
    }

    @Test
    public void testLoadClass()
        throws Exception
    {
        Class<?> dummyClass1 = Dummy.class;
        Class<?> dummyClass2 = classLoader.loadClass( dummyClass1.getName() );

        assertEquals( dummyClass1.getName(), dummyClass2.getName() );
        assertFalse( dummyClass2.getDeclaredConstructor().newInstance() instanceof Dummy );
        assertNotSame( dummyClass1, dummyClass2 );
        assertNotSame( dummyClass1.getClassLoader(), dummyClass2.getClassLoader() );
        assertSame( classLoader.getAdaptedClassLoader(), dummyClass2.getClassLoader() );
    }

    @Test
    public void testGetPrefixedResource()
        throws Exception
    {
        URL resource = classLoader.getResource( "inmemory:dummyResource" );
        assertNotNull( resource );
        assertEquals( resource.getPath(), "dummyResource" );
        InputStream input = resource.openStream();
        try
        {
            byte[] bytes = toByteArray( input );
            assertArrayEquals( bytes, IN_MEMORY_RESOURCES.get( "dummyResource" ) );
        }
        finally
        {
            input.close();
        }
    }

    private static byte[] toByteArray( InputStream input )
        throws IOException
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream( 512 );
        int n;
        while ( ( n = input.read() ) != -1 )
        {
            result.write( n );
        }
        return result.toByteArray();
    }

    private static final Map<String, byte[]> IN_MEMORY_RESOURCES = new HashMap<String, byte[]>();

    static
    {
        try
        {
            IN_MEMORY_RESOURCES.put( "dummyResource", "Resource data".getBytes( "UTF-8" ) );

            // put bytes of Dummy class
            String dummyClassName = Dummy.class.getName();
            String resourceName = dummyClassName.replace( '.', '/' ) + ".class";
            InputStream input = Dummy.class.getClassLoader().getResourceAsStream( resourceName );
            try
            {
                IN_MEMORY_RESOURCES.put( resourceName, toByteArray( input ) );
            }
            finally
            {
                input.close();
            }
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new ExceptionInInitializerError( e );
        }
        catch ( IOException e )
        {
            throw new ExceptionInInitializerError( e );
        }
    }

    private static class ExtendedClassLoader
        extends ClassLoader
    {

        private final InMemoryURLStreamHandlerFactory streamHandlerFactory = new InMemoryURLStreamHandlerFactory();

        @Override
        protected Class<?> loadClass( String name, boolean resolve )
            throws ClassNotFoundException
        {
            String dummyClassName = Dummy.class.getName();
            if ( dummyClassName.equals( name ) ) {
                Class<?> result = findLoadedClass( name );
                if ( result == null )
                {
                    byte[] byteCode = IN_MEMORY_RESOURCES.get( dummyClassName.replace( '.', '/' ) + ".class" );
                    result = defineClass( name, byteCode, 0, byteCode.length );
                    resolveClass( result );
                }
                return result;
            }
            return super.loadClass( name, resolve );
        }

        @Override
        public URL getResource( String name )
        {
            if ( name.startsWith( "inmemory:" ) )
            {
                try
                {
                    return new URL( null, name, streamHandlerFactory.createURLStreamHandler( "inmemory" ) );
                }
                catch ( MalformedURLException e )
                {
                    throw new RuntimeException( e );
                }
            }
            return super.getResource( name );
        }

        private static class InMemoryURLStreamHandlerFactory
            implements URLStreamHandlerFactory
        {
            public URLStreamHandler createURLStreamHandler( String protocol )
            {
                return new URLStreamHandler()
                {
                    @Override
                    protected URLConnection openConnection( URL u )
                        throws IOException
                    {
                        return new URLConnection( u )
                        {
                            private InputStream inputStream;

                            @Override
                            public void connect()
                                throws IOException
                            {
                                if ( !connected ) {
                                    byte[] data = IN_MEMORY_RESOURCES.get( url.getPath() );
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
    }

    public static class Dummy
    {
    }

}
