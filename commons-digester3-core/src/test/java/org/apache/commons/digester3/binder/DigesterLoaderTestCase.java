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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.apache.commons.digester3.binder.DigesterLoader.newLoader;

import org.apache.commons.digester3.Digester;
import org.junit.jupiter.api.Test;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

public final class DigesterLoaderTestCase
{

    @Test
    public void testDigester152()
    {
        final Locator expected = new Locator()
        {

            @Override
            public int getColumnNumber()
            {
                // just fake method
                return 0;
            }

            @Override
            public int getLineNumber()
            {
                // just fake method
                return 0;
            }

            @Override
            public String getPublicId()
            {
                // just fake method
                return null;
            }

            @Override
            public String getSystemId()
            {
                // just fake method
                return null;
            }
        };

        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                // do nothing
            }

        } ).setDocumentLocator( expected ).newDigester();

        final Locator actual = digester.getDocumentLocator();

        assertSame( expected, actual );
    }

    /**
     * DIGESTER-151
     */
    @Test
    public void testDigester151()
    {
        final ErrorHandler expected = new ErrorHandler()
        {

            @Override
            public void error( final SAXParseException exception )
            {
                // do nothing
            }

            @Override
            public void fatalError( final SAXParseException exception )
            {
                // do nothing
            }

            @Override
            public void warning( final SAXParseException exception )
            {
                // do nothing
            }

        };

        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                // do nothing
            }

        } ).setErrorHandler( expected ).newDigester();

        final ErrorHandler actual = digester.getErrorHandler();

        assertSame( expected, actual );
    }

    @Test
    public void testDigester155()
    {
        final ClassLoader expected = getClass().getClassLoader();

        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                // do nothing
            }

        } ).setClassLoader( expected ).newDigester();

        final ClassLoader actual = digester.getClassLoader();

        assertSame( expected, actual );
    }

}
