/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.commons.digester3.binder.AbstractRulesModule;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

/**
 * <p>
 * Tests for the {@code ObjectParamRuleTestCase}
 */
public class ObjectParamRuleTestCase
{

    private final StringBuilder sb =
        new StringBuilder().append( "<arraylist><A/><B/><C/><D desc=\"the fourth\"/><E/></arraylist>" );

    /**
     * Test method calls with the ObjectParamRule rule. It should be possible to pass any subclass of Object as a
     * parameter, provided that either the element or the element + attribute has been matched.
     */
    @Test
    public void testBasic()
        throws SAXException, IOException
    {
        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "arraylist" ).createObject().ofType( ArrayList.class );
                forPattern( "arraylist/A" ).callMethod( "add" ).withParamCount( 1 )
                    .then()
                    .objectParam( -9 );
                forPattern( "arraylist/B" ).callMethod( "add" ).withParamCount( 1 )
                    .then()
                    .objectParam( 3.14159f );
                forPattern( "arraylist/C" ).callMethod( "add" ).withParamCount( 1 )
                    .then()
                    .objectParam( 999999999L );
                forPattern( "arraylist/D" ).callMethod( "add" ).withParamCount( 1 )
                    .then()
                    .objectParam( "foobarbazbing" ).matchingAttribute( "desc" );
                forPattern( "arraylist/E" ).callMethod( "add" ).withParamCount( 1 )
                    .then()
                    .objectParam( "ignore" ).matchingAttribute( "nonexistentattribute" );
            }

        }).newDigester();

        // Parse it and obtain the ArrayList
        final ArrayList<?> al = digester.parse( new StringReader( sb.toString() ) );
        assertNotNull( al );
        assertEquals( al.size(), 4 );
        assertTrue( al.contains( -9 ) );
        assertTrue( al.contains( 3.14159f ) );
        assertTrue( al.contains( 999999999L ) );
        assertTrue( al.contains( "foobarbazbing" ) );
        assertFalse( al.contains( "ignore" ) );
    }

}
