/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.digester3;

import static org.apache.commons.digester3.binder.DigesterLoader.newLoader;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.apache.commons.digester3.binder.AbstractRulesModule;
import org.junit.jupiter.api.Test;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Tests for entity resolution and dtd validation
 */
public class DTDValidationTestCase
{

    @Test
    void testDigesterDTDError()
    {
        Digester digester = newLoader( new AbstractRulesModule() {

            @Override
            protected void configure()
            {
                // do nothing
            }

        } )
        .setValidating( true )
        .setErrorHandler( new ErrorHandler()
        {

            @Override
            public void error( final SAXParseException e )
                throws SAXException
            {
                throw e;
            }

            @Override
            public void fatalError( final SAXParseException e )
                throws SAXException
            {
                throw e;
            }

            @Override
            public void warning( final SAXParseException e )
                throws SAXException
            {
                throw e;
            }

        } )
        .newDigester();
        assertThrows( SAXParseException.class, () -> digester.parse( new File( "src/test/resources/org/apache/commons/digester3/document-with-relative-dtd-error.xml" ) ) );
    }

    @Test
    void testDigesterLoaderFeatureDisabled()
        throws Exception
    {
       newLoader( new AbstractRulesModule()
        {

           @Override
            protected void configure()
            {
                // do nothing
            }

        } )
        .setFeature("http://xml.org/sax/features/validation", false)
        .setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        .setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false)
        .setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        .newDigester()
        .parse( new File( "src/test/resources/org/apache/commons/digester3/document-with-relative-dtd-error.xml" ) );
    }

    @Test
    void testDigesterNoDTDValidation()
        throws Exception
    {
        newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                // do nothing
            }

        } )
        .setValidating( false )
        .newDigester()
        .parse( new File( "src/test/resources/org/apache/commons/digester3/document-with-relative-dtd-error.xml" ) );
    }

    @Test
    void testDigesterValidation()
        throws Exception
    {
        newLoader( new AbstractRulesModule()
        {
            @Override
            protected void configure()
            {
                // do nothing
            }
        } )
        .setValidating( true )
        .newDigester()
        .parse( new File( "src/test/resources/org/apache/commons/digester3/document-with-relative-dtd.xml" ) );
    }

}
