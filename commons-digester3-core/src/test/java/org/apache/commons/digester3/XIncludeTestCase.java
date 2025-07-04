/*
 *
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.digester3.binder.AbstractRulesModule;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Tests for XInclude aware parsing.
 * </p>
 */
public class XIncludeTestCase
{

    /**
     * Gets an appropriate InputStream for the specified test file (which must be inside our current package.
     *
     * @param name Name of the test file we want
     * @throws IOException if an input/output error occurs
     */
    protected InputStream getInputStream( final String name )
    {

        return this.getClass().getResourceAsStream( "/org/apache/commons/digester3/" + name );

    }

    /**
     * Test XInclude.
     */
    @Test
    void testXInclude() throws Exception
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "employee" ).createObject().ofType( Employee.class );
                forPattern( "employee/firstName" ).callMethod( "setFirstName" ).usingElementBodyAsArgument();
                forPattern( "employee/lastName" ).callMethod( "setLastName" ).usingElementBodyAsArgument();

                forPattern( "employee/address" ).createObject().ofType( Address.class )
                    .then()
                    .setNext( "addAddress" );
                forPattern( "employee/address/type" ).callMethod( "setType" ).usingElementBodyAsArgument();
                forPattern( "employee/address/city" ).callMethod( "setCity" ).usingElementBodyAsArgument();
                forPattern( "employee/address/state" ).callMethod( "setState" ).usingElementBodyAsArgument();
            }

        })
        .setNamespaceAware( true )
        .setXIncludeAware( true )
        .newDigester();

        // Parse our test input
        final Employee employee = digester.parse( getInputStream( "Test12.xml" ) );
        assertNotNull( employee, "failed to parse an employee" );

        // Test basics
        assertEquals( "First Name", employee.getFirstName() );
        assertEquals( "Last Name", employee.getLastName() );

        // Test includes have been processed
        final Address ha = employee.getAddress( "home" );
        assertNotNull( ha );
        assertEquals( "Home City", ha.getCity() );
        assertEquals( "HS", ha.getState() );
        final Address oa = employee.getAddress( "office" );
        assertNotNull( oa );
        assertEquals( "Office City", oa.getCity() );
        assertEquals( "OS", oa.getState() );

    }

}
