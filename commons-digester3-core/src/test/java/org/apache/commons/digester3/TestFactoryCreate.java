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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xml.sax.Attributes;

/**
 * Test case for factory create rules.
 */
public class TestFactoryCreate
{
    @ParameterizedTest
    @ValueSource( booleans = { true, false } )
    public void testFactoryCreateRule( boolean ignoreCreateExceptions )
        throws Exception
    {

        // test passing object create
        Digester digester = new Digester();
        ObjectCreationFactoryTestImpl factory = new ObjectCreationFactoryTestImpl();
        digester.addFactoryCreate( "root", factory, ignoreCreateExceptions );
        String xml = "<?xml version='1.0' ?><root one='good' two='bad' three='ugly'><element/></root>";
        digester.parse( new StringReader( xml ) );

        assertTrue( factory.called, "Object create not called(1)[" + ignoreCreateExceptions + "]" );
        assertEquals( "good", factory.attributes.getValue( "one" ), "Attribute not passed (1)[" + ignoreCreateExceptions + "]" );
        assertEquals( "bad", factory.attributes.getValue( "two" ), "Attribute not passed (2)[" + ignoreCreateExceptions + "]" );
        assertEquals( "ugly", factory.attributes.getValue( "three" ), "Attribute not passed (3)[" + ignoreCreateExceptions + "]" );

        digester = new Digester();
        digester.addFactoryCreate( "root", "org.apache.commons.digester3.ObjectCreationFactoryTestImpl",
                                   ignoreCreateExceptions );
        digester.addSetNext( "root", "add" );
        xml = "<?xml version='1.0' ?><root one='good' two='bad' three='ugly'><element/></root>";
        List<ObjectCreationFactoryTestImpl> list = new ArrayList<>();
        digester.push( list );
        digester.parse( new StringReader( xml ) );

        assertEquals( 1, list.size(), "List should contain only the factory object" );
        factory = list.get( 0 );
        assertTrue( factory.called, "Object create not called(2)[" + ignoreCreateExceptions + "]" );
        assertEquals( "good", factory.attributes.getValue( "one" ), "Attribute not passed (4)[" + ignoreCreateExceptions + "]" );
        assertEquals( "bad", factory.attributes.getValue( "two" ), "Attribute not passed (5)[" + ignoreCreateExceptions + "]" );
        assertEquals( "ugly", factory.attributes.getValue( "three" ), "Attribute not passed (6)[" + ignoreCreateExceptions + "]" );

        digester = new Digester();
        digester.addFactoryCreate( "root", "org.apache.commons.digester3.ObjectCreationFactoryTestImpl", "override",
                                   ignoreCreateExceptions );
        digester.addSetNext( "root", "add" );
        xml = "<?xml version='1.0' ?><root one='good' two='bad' three='ugly'><element/></root>";
        list = new ArrayList<>();
        digester.push( list );
        digester.parse( new StringReader( xml ) );

        assertEquals( 1, list.size(), "List should contain only the factory object" );
        factory = list.get( 0 );
        assertTrue( factory.called, "Object create not called(3)[" + ignoreCreateExceptions + "]" );
        assertEquals( "good", factory.attributes.getValue( "one" ), "Attribute not passed (7)[" + ignoreCreateExceptions + "]" );
        assertEquals( "bad", factory.attributes.getValue( "two" ), "Attribute not passed (8)[" + ignoreCreateExceptions + "]" );
        assertEquals( "ugly", factory.attributes.getValue( "three" ), "Attribute not passed (8)[" + ignoreCreateExceptions + "]" );

        digester = new Digester();
        digester.addFactoryCreate( "root", "org.apache.commons.digester3.ObjectCreationFactoryTestImpl", "override",
                                   ignoreCreateExceptions );
        digester.addSetNext( "root", "add" );
        xml =
            "<?xml version='1.0' ?><root one='good' two='bad' three='ugly' "
                + " override='org.apache.commons.digester3.OtherTestObjectCreationFactory' >" + "<element/></root>";
        list = new ArrayList<>();
        digester.push( list );
        digester.parse( new StringReader( xml ) );

        assertEquals( 1, list.size(), "List should contain only the factory object" );
        factory = list.get( 0 );
        assertEquals( factory.getClass().getName(), "org.apache.commons.digester3.OtherTestObjectCreationFactory", "Attribute Override Failed (1)" );
        assertTrue( factory.called, "Object create not called(4)[" + ignoreCreateExceptions + "]" );
        assertEquals( "good", factory.attributes.getValue( "one" ), "Attribute not passed (10)[" + ignoreCreateExceptions + "]" );
        assertEquals( "bad", factory.attributes.getValue( "two" ), "Attribute not passed (11)[" + ignoreCreateExceptions + "]" );
        assertEquals( "ugly", factory.attributes.getValue( "three" ), "Attribute not passed (12)[" + ignoreCreateExceptions + "]" );

        digester = new Digester();
        digester.addFactoryCreate( "root", ObjectCreationFactoryTestImpl.class, "override", ignoreCreateExceptions );
        digester.addSetNext( "root", "add" );
        xml = "<?xml version='1.0' ?><root one='good' two='bad' three='ugly'><element/></root>";
        list = new ArrayList<>();
        digester.push( list );
        digester.parse( new StringReader( xml ) );

        assertEquals( 1, list.size(), "List should contain only the factory object" );
        factory = list.get( 0 );
        assertTrue( factory.called, "Object create not called(5)[" + ignoreCreateExceptions + "]" );
        assertEquals( "good", factory.attributes.getValue( "one" ), "Attribute not passed (13)[" + ignoreCreateExceptions + "]" );
        assertEquals( "bad", factory.attributes.getValue( "two" ), "Attribute not passed (14)[" + ignoreCreateExceptions + "]" );
        assertEquals( "ugly", factory.attributes.getValue( "three" ), "Attribute not passed (15)[" + ignoreCreateExceptions + "]" );

        digester = new Digester();
        digester.addFactoryCreate( "root", ObjectCreationFactoryTestImpl.class, "override", ignoreCreateExceptions );
        digester.addSetNext( "root", "add" );
        xml =
            "<?xml version='1.0' ?><root one='good' two='bad' three='ugly' "
                + " override='org.apache.commons.digester3.OtherTestObjectCreationFactory' >" + "<element/></root>";
        list = new ArrayList<>();
        digester.push( list );
        digester.parse( new StringReader( xml ) );

        assertEquals( 1, list.size(), "List should contain only the factory object" );
        factory = list.get( 0 );
        assertInstanceOf( OtherTestObjectCreationFactory.class, factory, "Attribute Override Failed (2)" );
        assertTrue( factory.called, "Object create not called(6)[" + ignoreCreateExceptions + "]" );
        assertEquals( "good", factory.attributes.getValue( "one" ), "Attribute not passed (16)[" + ignoreCreateExceptions + "]" );
        assertEquals( "bad", factory.attributes.getValue( "two" ), "Attribute not passed (17)[" + ignoreCreateExceptions + "]" );
        assertEquals( "ugly", factory.attributes.getValue( "three" ), "Attribute not passed (18)[" + ignoreCreateExceptions + "]" );
    }

    @ParameterizedTest
    @ValueSource( booleans = { true, false } )
    public void testPropagateException( boolean ignoreCreateExceptions )
    {

        // only used with this method
        final class ThrowExceptionCreateRule
            extends AbstractObjectCreationFactory<Object>
        {
            @Override
            public Object createObject( final Attributes attributes )
            {
                throw new RuntimeException();
            }
        }

        // now for the tests
        final String xml = "<?xml version='1.0' ?><root><element/></root>";

        // test default - which is to propagate the exception
        final Digester digester = new Digester();
        digester.addFactoryCreate( "root", new ThrowExceptionCreateRule(), ignoreCreateExceptions );
        try
        {

            digester.parse( new StringReader( xml ) );
            if ( !ignoreCreateExceptions )
            {
                fail( "Exception should be propagated from create rule" );
            }

        }
        catch ( final Exception e )
        {
            if ( ignoreCreateExceptions )
            {
                fail( "Exception should not be propagated" );
            }
        }
    }
}
