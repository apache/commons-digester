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

import static org.junit.Assert.assertSame;
import static org.apache.commons.digester3.binder.DigesterLoader.newLoader;

import org.apache.commons.digester3.Digester;
import org.junit.Test;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public final class DigesterLoaderTestCase
{

    /**
     * DIGESTER-151
     */
    @Test
    public void digester151()
    {
        ErrorHandler expected = new ErrorHandler()
        {

            public void warning( SAXParseException exception )
                throws SAXException
            {
                // do nothing
            }

            public void fatalError( SAXParseException exception )
                throws SAXException
            {
                // do nothing
            }

            public void error( SAXParseException exception )
                throws SAXException
            {
                // do nothing
            }

        };

        Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                // do nothing
            }

        } ).setErrorHandler( expected ).newDigester();

        ErrorHandler actual = digester.getErrorHandler();

        assertSame( expected, actual );
    }

    @Test
    public void digeser152()
    {
        Locator expected = new Locator()
        {

            public String getSystemId()
            {
                // just fake method
                return null;
            }

            public String getPublicId()
            {
                // just fake method
                return null;
            }

            public int getLineNumber()
            {
                // just fake method
                return 0;
            }

            public int getColumnNumber()
            {
                // just fake method
                return 0;
            }
        };

        Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                // do nothing
            }

        } ).setDocumentLocator( expected ).newDigester();

        Locator actual = digester.getDocumentLocator();

        assertSame( expected, actual );
    }

    @Test
    public void digester155()
    {
        ClassLoader expected = getClass().getClassLoader();

        Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                // do nothing
            }

        } ).setClassLoader( expected ).newDigester();

        ClassLoader actual = digester.getClassLoader();

        assertSame( expected, actual );
    }

}
