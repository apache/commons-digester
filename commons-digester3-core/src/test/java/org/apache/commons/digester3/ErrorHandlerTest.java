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
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.apache.commons.digester3.binder.DigesterLoader.newLoader;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.digester3.binder.AbstractRulesModule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class ErrorHandlerTest
{

    @Test
    void testCustomErrorHandler()
    {

        final ErrorHandler customErrorHandler = new ErrorHandler()
        {
            final Log log = LogFactory.getLog( this.getClass() );

            @Override
            public void error( final SAXParseException e )
            {
                log.error( "Custom Error Handler" );
            }

            @Override
            public void fatalError( final SAXParseException e )
            {
                log.fatal( "Custom Fatal Error Handler" );
            }

            @Override
            public void warning( final SAXParseException arg0 )
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
        assertThrows( SAXException.class, () -> digester.parse( getClass().getResource( "Test-digester-172-wrong.xml" ) ) );
    }

    @Test
    void testCustomErrorHandlerWithStack()
    {

        final ErrorHandler customErrorHandler = new ErrorHandler()
        {
            final Log log = LogFactory.getLog( this.getClass() );

            @Override
            public void error( final SAXParseException e )
            {
                log.error( "Custom Error Handler", e );
            }

            @Override
            public void fatalError( final SAXParseException e )
            {
                log.fatal( "Custom Fatal Error Handler", e );
            }

            @Override
            public void warning( final SAXParseException arg0 )
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
        assertThrows( SAXException.class, () -> digester.parse( getClass().getResource( "Test-digester-172-wrong.xml" ) ) );
    }

    @Test
    void testNoCustomErrorHandler()
    {
        Digester digester = newLoader( new AbstractRulesModule()
        {
            @Override
            protected void configure()
            {
                forPattern( "employee" ).createObject().ofType( Employee.class );
                forPattern( "employee/firstName" ).setBeanProperty().extractPropertyNameFromAttribute( "name" );
            }
        } ).newDigester();
        assertThrows( SAXException.class, () -> digester.parse( getClass().getResource( "Test-digester-172-wrong.xml" ) ) );
    }
}
