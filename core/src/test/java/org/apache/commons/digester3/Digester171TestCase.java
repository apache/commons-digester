package org.apache.commons.digester3;

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

import static org.apache.commons.digester3.binder.DigesterLoader.newLoader;

import java.io.File;

import org.apache.commons.digester3.binder.AbstractRulesModule;
import org.junit.Test;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class Digester171TestCase
{

    @Test
    public void testNoErrorHandler()
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
        .setFeature( "http://xml.org/sax/features/validation", true )
        .newDigester()
        .parse( new File( "src/test/resources/org/apache/commons/digester3/document-with-relative-dtd-error.xml" ) );
    }

    @Test( expected = SAXParseException.class )
    public void testDefaultThrowingErrorHandler()
        throws Exception
    {
        ErrorHandler customErrorHandler = new DefaultThrowingErrorHandler();

        newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                // do nothing
            }

        } )
        .setFeature( "http://xml.org/sax/features/validation", true )
        .setErrorHandler( customErrorHandler )
        .newDigester()
        .parse( new File( "src/test/resources/org/apache/commons/digester3/document-with-relative-dtd-error.xml" ) );
    }

}
