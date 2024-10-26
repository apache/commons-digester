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

import org.apache.commons.digester3.binder.AbstractRulesModule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ErrorHandlerTest
{

    @Test( expected = SAXException.class )
    public void testCustomErrorHandler() throws Exception
    {

        final ErrorHandler customErrorHandler = new ErrorHandler()
        {
            final Log log = LogFactory.getLog( this.getClass() );

            @Override
            public void error( final SAXParseException e )
                throws SAXException
            {
                log.error( "Custom Error Handler" );
            }

            @Override
            public void fatalError( final SAXParseException e )
                throws SAXException
            {
                log.fatal( "Custom Fatal Error Handler" );
            }

            @Override
            public void warning( final SAXParseException arg0 )
                throws SAXException
            {
                log.warn( "Custom Warn Handler" );
            }
        };

        Digester digester = newLoader( new AbstractRulesModule()
        {
            @Override
            protected void configure()
            {
                forPattern( "employee" ).createObject().ofType( Employee.class );
                forPattern( "employee/firstName" ).setBeanProperty().extractPropertyNameFromAttribute( "name" );
            }
        } ).newDigester();
        digester.setErrorHandler( customErrorHandler );
        digester.parse( getClass().getResource( "Test-digester-172-wrong.xml" ) );
    }

    @Test( expected = SAXException.class )
    public void testCustomErrorHandlerWithStack() throws Exception
    {

        final ErrorHandler customErrorHandler = new ErrorHandler()
        {
            final Log log = LogFactory.getLog( this.getClass() );

            @Override
            public void error( final SAXParseException e )
                throws SAXException
            {
                log.error( "Custom Error Handler", e );
            }

            @Override
            public void fatalError( final SAXParseException e )
                throws SAXException
            {
                log.fatal( "Custom Fatal Error Handler", e );
            }

            @Override
            public void warning( final SAXParseException arg0 )
                throws SAXException
            {
                log.warn( "Custom Warn Handler" );
            }
        };

        Digester digester = newLoader( new AbstractRulesModule()
        {
            @Override
            protected void configure()
            {
                forPattern( "employee" ).createObject().ofType( Employee.class );
                forPattern( "employee/firstName" ).setBeanProperty().extractPropertyNameFromAttribute( "name" );
            }
        } ).newDigester();
        digester.setErrorHandler( customErrorHandler );
        digester.parse( getClass().getResource( "Test-digester-172-wrong.xml" ) );
    }

    @Test( expected = SAXException.class )
    public void testNoCustomErrorHandler() throws Exception
    {
        newLoader( new AbstractRulesModule()
        {
            @Override
            protected void configure()
            {
                forPattern( "employee" ).createObject().ofType( Employee.class );
                forPattern( "employee/firstName" ).setBeanProperty().extractPropertyNameFromAttribute( "name" );
            }
        } ).newDigester().parse( getClass().getResource( "Test-digester-172-wrong.xml" ) );
    }
}
