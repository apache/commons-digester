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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.apache.commons.digester3.binder.DigesterLoader.newLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.xmlrules.FromXmlRulesModule;
import org.junit.Before;
import org.junit.Test;

/**
 * Test.
 */
public class Digester163TestCase
{

    public static final int MAX_THREADS = 4;

    private DigesterLoader loader = null;

    @Before
    public void before()
    {
        loader = newLoader( new FromXmlRulesModule()
        {

            @Override
            protected void loadRules()
            {
                loadXMLRules( getClass().getResourceAsStream( "rules.xml" ) );
            }

        } );
    }

    @Test
    public void test()
        throws InterruptedException
    {
        ThreadPoolExecutor executor = new ThreadPoolExecutor( MAX_THREADS,
                                                              MAX_THREADS,
                                                              Long.MAX_VALUE,
                                                              TimeUnit.NANOSECONDS,
                                                              new LinkedBlockingQueue<Runnable>() );
        final LinkedBlockingQueue<Exception> exceptions = new LinkedBlockingQueue<Exception>();
        for ( int i = 0; i < MAX_THREADS * 2; i++ )
        {
            executor.submit( new Runnable()
            {

                public void run()
                {

                    Digester dig = null;
                    InputStream in = null;
                    try
                    {
                        dig = loader.newDigester();
                        in = Digester163TestCase.class.getResourceAsStream( "test.xml" );
                        Entity et = dig.parse( in );
                        assertEquals( "Author 1", et.getAuthor() );
                    }
                    catch ( Exception e )
                    {
                        exceptions.add( e );
                    }
                    finally
                    {
                        if ( in != null )
                        {
                            try
                            {
                                in.close();
                            }
                            catch ( IOException e )
                            {
                                // close quietly
                            }
                        }
                    }

                }
            } );
        }

        while ( !executor.awaitTermination( 10, TimeUnit.MILLISECONDS ) )
        {
            if ( executor.getQueue().isEmpty() )
            {
                executor.shutdown();
            }
            if ( executor.isTerminated() )
            {
                break;
            }
        }

        Exception e = exceptions.poll();
        if ( e != null )
        {
            e.printStackTrace();
            fail( "Throwable caught -> " + e.getMessage() != null ? e.getMessage() : "" );
        }

    }
}
