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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

/**
 * <p>
 * Test Case for the Digester class. These tests exercise the individual methods of a Digester, but do not attempt to
 * process complete documents.
 * </p>
 */
public class DigesterTestCase
{

    /** Utility class for method testStackAction */
    private static final class TrackingStackAction
        implements StackAction
    {
        public ArrayList<String> events = new ArrayList<>();

        @Override
        public Object onPop( final Digester d, final String stackName, final Object o )
        {
            final String msg = "pop:" + stackName + ":" + o.toString();
            events.add( msg );
            final String str = o.toString();
            if ( str.startsWith( "replpop" ) )
            {
                return new String( str );
            }
            return o;
        }

        @Override
        public Object onPush( final Digester d, final String stackName, final Object o )
        {
            final String msg = "push:" + stackName + ":" + o.toString();
            events.add( msg );

            final String str = o.toString();
            if ( str.startsWith( "replpush" ) )
            {
                return new String( str );
            }
            return o;
        }
    }

    /**
     * The set of public identifiers, and corresponding resource names, for the versions of the DTDs that we know about.
     * There <strong>MUST</strong> be an even number of Strings in this array.
     */
    protected static final String[] registrations = { "-//Netscape Communications//DTD RSS 0.9//EN",
        "/org/apache/commons/digester3/rss/rss-0.9.dtd", "-//Netscape Communications//DTD RSS 0.91//EN",
        "/org/apache/commons/digester3/rss/rss-0.91.dtd", };

    /**
     * The digester instance we will be processing.
     */
    protected Digester digester;

    /**
     * Sets up instance variables required by this test case.
     */
    @BeforeEach
    public void setUp()
    {

        digester = new Digester();
        digester.setRules( new RulesBase() );

    }

    /**
     * Tear down instance variables required by this test case.
     */
    @AfterEach
    public void tearDown()
    {

        digester = null;

    }

    @Test
    public void testBasicSubstitution()
        throws Exception
    {
        final class TestSubRule
            extends Rule
        {
            public String body;

            public Attributes attributes;

            @Override
            public void begin( final String namespace, final String name, final Attributes attributes )
            {
                this.attributes = new AttributesImpl( attributes );
            }

            @Override
            public void body( final String namespace, final String name, final String text )
            {
                this.body = text;
            }
        }

        final TestSubRule tsr = new TestSubRule();
        final Digester digester = new Digester();
        digester.addRule( "alpha/beta", tsr );

        // it's not easy to transform dirty harry into the mighty circus - but let's give it a try
        final String xml =
            "<?xml version='1.0'?><alpha><beta forname='Dirty' surname='Harry'>Do you feel luck punk?</beta></alpha>";
        InputSource in = new InputSource( new StringReader( xml ) );

        digester.parse( in );

        assertEquals( "Do you feel luck punk?", tsr.body, "Unsubstituted body text" );
        assertEquals( 2, tsr.attributes.getLength(), "Unsubstituted number of attributes" );
        assertEquals( "Dirty", tsr.attributes.getValue( "forname" ), "Unsubstituted forname attribute value" );
        assertEquals( "Harry", tsr.attributes.getValue( "surname" ), "Unsubstituted surname attribute value" );

        digester.setSubstitutor( new Substitutor()
        {
            @Override
            public Attributes substitute( final Attributes attributes )
            {
                final AttributesImpl results = new AttributesImpl();
                results.addAttribute( "", "python", "python", "CDATA", "Cleese" );
                return results;
            }

            @Override
            public String substitute( final String bodyText )
            {
                return "And now for something completely different...";
            }
        } );

        // now transform into the full monty
        in = new InputSource( new StringReader( xml ) );
        digester.parse( in );

        assertEquals( "And now for something completely different...", tsr.body, "Substituted body text" );
        assertEquals( 1, tsr.attributes.getLength(), "Substituted number of attributes" );
        assertEquals( "Cleese", tsr.attributes.getValue( "", "python" ), "Substituted python attribute value" );
    }

    /**
     * Test the Digester.getRoot method.
     */
    @Test
    public void testGetRoot()
        throws Exception
    {
        final Digester digester = new Digester();
        digester.addRule( "root", new ObjectCreateRule( TestBean.class ) );

        final String xml = "<root/>";
        final InputSource in = new InputSource( new StringReader( xml ) );

        digester.parse( in );

        final Object root = digester.getRoot();
        assertNotNull( root, "root object not retrieved" );
        assertInstanceOf( TestBean.class, root, "root object not a TestRule instance" );
    }

    /** Tests that values are stored independently */
    @Test
    public void testNamedIndependence()
    {
        final String testStackOneName = "org.apache.commons.digester3.tests.testNamedIndependenceOne";
        final String testStackTwoName = "org.apache.commons.digester3.tests.testNamedIndependenceTwo";
        final Digester digester = new Digester();
        digester.push( testStackOneName, "Tweedledum" );
        digester.push( testStackTwoName, "Tweedledee" );
        assertEquals( "Tweedledum", digester.pop( testStackOneName ), "Popped value one:" );
        assertEquals( "Tweedledee", digester.pop( testStackTwoName ), "Popped value two:" );
    }

    /** Tests for isEmpty */
    @Test
    public void testNamedStackIsEmpty()
    {
        final String testStackName = "org.apache.commons.digester3.tests.testNamedStackIsEmpty";
        final Digester digester = new Digester();
        assertTrue( digester.isEmpty( testStackName ), "A named stack that has no object pushed onto it yet should be empty" );

        digester.push( testStackName, "Some test value" );
        assertFalse( digester.isEmpty( testStackName ), "A named stack that has an object pushed onto it should be not empty" );

        digester.peek( testStackName );
        assertFalse( digester.isEmpty( testStackName ), "Peek should not effect whether the stack is empty" );

        digester.pop( testStackName );
        assertTrue( digester.isEmpty( testStackName ), "A named stack that has it's last object popped is empty" );
    }

    /** Tests the push-peek-pop cycle for a named stack */
    @Test
    public void testNamedStackPushPeekPop()
    {
        final BigDecimal archimedesAveragePi = new BigDecimal( "3.1418" );
        final String testStackName = "org.apache.commons.digester3.tests.testNamedStackPushPeekPop";
        final Digester digester = new Digester();
        assertTrue( digester.isEmpty( testStackName ), "Stack starts empty:" );
        digester.push( testStackName, archimedesAveragePi );
        assertEquals( archimedesAveragePi, digester.peek( testStackName ), "Peeked value:" );
        assertEquals( archimedesAveragePi, digester.pop( testStackName ), "Popped value:" );
        assertTrue( digester.isEmpty( testStackName ), "Stack ends empty:" );

        digester.push( testStackName, "1" );
        digester.push( testStackName, "2" );
        digester.push( testStackName, "3" );

        assertEquals( "1", digester.peek( testStackName, 2 ), "Peek#1" );
        assertEquals( "2", digester.peek( testStackName, 1 ), "Peek#2" );
        assertEquals( "3", digester.peek( testStackName, 0 ), "Peek#3" );
        assertEquals( "3", digester.peek( testStackName ), "Peek#3a" );

        // peek beyond stack
        assertThrows( EmptyStackException.class, () -> digester.peek( testStackName, 3 ), "Peek#4 failed to throw an exception." );

        // peek a nonexistent named stack
        assertThrows( EmptyStackException.class, () -> digester.peek( "no.such.stack", 0 ), "Peeking a non-existent stack failed to throw an exception." );
    }

    /**
     * Test {@code null} parsing. (should lead to {@code IllegalArgumentException}s)
     */
    @Test
    public void testNullFileParse()
    {
        assertThrows( IllegalArgumentException.class, () -> digester.parse( ( File ) null ) );
    }

    @Test
    public void testNullInputSourceParse()
    {
        assertThrows( IllegalArgumentException.class, () -> digester.parse( ( InputSource ) null ) );
    }

    @Test
    public void testNullInputStreamParse()
    {
        assertThrows( IllegalArgumentException.class, () -> digester.parse( ( InputStream ) null ) );
    }

    @Test
    public void testNullReaderParse()
    {
        assertThrows( IllegalArgumentException.class, () -> digester.parse( ( Reader ) null ) );
    }

    @Test
    public void testNullStringParse()
    {
        assertThrows( IllegalArgumentException.class, () -> digester.parse( ( String ) null ) );
    }

    @Test
    public void testNullURLParse()
    {
        assertThrows( IllegalArgumentException.class, () -> digester.parse( ( URL ) null ) );
    }

    @Test
    public void testOnceAndOnceOnly()
        throws Exception
    {

        final class TestConfigureDigester
            extends Digester
        {
            public int called;

            public TestConfigureDigester()
            {
            }

            @Override
            protected void initialize()
            {
                called++;
            }
        }

        final TestConfigureDigester digester = new TestConfigureDigester();

        final String xml = "<?xml version='1.0'?><document/>";
        digester.parse( new StringReader( xml ) );

        assertEquals( 1, digester.called, "Initialize should be called once and only once" );
    }

    /** Tests popping named stack not yet pushed */
    @Test
    public void testPopNamedStackNotPushed()
    {
        String testStackName = "org.apache.commons.digester3.tests.testPopNamedStackNotPushed";
        Digester digester = new Digester();
        assertThrows( EmptyStackException.class, () -> digester.pop( testStackName ) );
    }

    /** Tests peeking named stack not yet pushed */
    @Test
    public void testPeekNamedStackNotPushed()
    {
        String testStackName = "org.apache.commons.digester3.tests.testPopNamedStackNotPushed";
        Digester digester = new Digester();
        assertThrows( EmptyStackException.class, () -> digester.peek( testStackName ) );
    }

    /**
     * Test the basic property getters and setters.
     */
    @Test
    public void testProperties()
    {

        assertNull( digester.getErrorHandler(), "Initial error handler is null" );
        digester.setErrorHandler( digester );
        assertSame( digester.getErrorHandler(), digester, "Set error handler is digester" );
        digester.setErrorHandler( null );
        assertNull( digester.getErrorHandler(), "Reset error handler is null" );

        assertFalse( digester.getNamespaceAware(), "Initial namespace aware is false" );
        digester.setNamespaceAware( true );
        assertTrue( digester.getNamespaceAware(), "Set namespace aware is true" );
        digester.setNamespaceAware( false );
        assertFalse( digester.getNamespaceAware(), "Reset namespace aware is false" );

        assertFalse( digester.getValidating(), "Initial validating is false" );
        digester.setValidating( true );
        assertTrue( digester.getValidating(), "Set validating is true" );
        digester.setValidating( false );
        assertFalse( digester.getValidating(), "Reset validating is false" );

    }

    /**
     * Test registration of URLs for specified public identifiers.
     */
    @Test
    public void testRegistrations()
    {

        Map<String, URL> map = digester.getRegistrations();
        assertEquals( 0, map.size(), "Initially zero registrations" );
        int n = 0;
        for ( int i = 0; i < registrations.length; i += 2 )
        {
            final URL url = this.getClass().getResource( registrations[i + 1] );
            if ( url != null )
            {
                digester.register( registrations[i], url );
                n++;
            }
        }
        map = digester.getRegistrations();
        assertEquals( n, map.size(), "Registered two URLs" );

        final int[] count = new int[n];
        for ( int i = 0; i < n; i++ ) {
            count[i] = 0;
        }
        for ( final String key : map.keySet() )
        {
            for ( int i = 0; i < n; i++ )
            {
                if ( key.equals( registrations[i * 2] ) )
                {
                    count[i]++;
                    break;
                }
            }
        }
        for ( int i = 0; i < n; i++ ) {
            assertEquals( 1, count[i], "Count for key " + registrations[i * 2] );
        }

    }

    /**
     * Basic test for rule creation and matching.
     */
    @Test
    public void testRules()
    {

        assertEquals( 0, digester.getRules().match( null, "a", null, null ).size(), "Initial rules list is empty" );
        digester.addSetProperties( "a" );
        assertEquals( 1, digester.getRules().match( null, "a", null, null ).size(), "Add a matching rule" );
        digester.addSetProperties( "b" );
        assertEquals( 1, digester.getRules().match( null, "a", null, null ).size(), "Add a non-matching rule" );
        digester.addSetProperties( "a/b" );
        assertEquals( 1, digester.getRules().match( null, "a", null, null ).size(), "Add a non-matching nested rule" );
        digester.addSetProperties( "a/b" );
        assertEquals( 2, digester.getRules().match( null, "a/b", null, null ).size(), "Add a second matching rule" );

    }

    /**
     * <p>
     * Test matching rules in {@link RulesBase}.
     * </p>
     * <p>
     * Tests:
     * </p>
     * <ul>
     * <li>exact match</li>
     * <li>tail match</li>
     * <li>longest pattern rule</li>
     * </ul>
     */
    @Test
    public void testRulesBase()
    {

        assertEquals( 0, digester.getRules().rules().size(), "Initial rules list is empty" );

        // We're going to set up
        digester.addRule( "a/b/c/d", new TestRule( "a/b/c/d" ) );
        digester.addRule( "*/d", new TestRule( "*/d" ) );
        digester.addRule( "*/c/d", new TestRule( "*/c/d" ) );

        // Test exact match
        assertEquals( 1, digester.getRules().match( null, "a/b/c/d", null, null ).size(), "Exact match takes precedence 1" );
        assertEquals( "a/b/c/d", ( ( TestRule ) digester.getRules().match( null, "a/b/c/d", null, null ).iterator().next() ).getIdentifier(), "Exact match takes precedence 2" );

        // Test wildcard tail matching
        assertEquals( 1, digester.getRules().match( null, "a/b/d", null, null ).size(), "Wildcard tail matching rule 1" );
        assertEquals( "*/d", ( ( TestRule ) digester.getRules().match( null, "a/b/d", null, null ).iterator().next() ).getIdentifier(), "Wildcard tail matching rule 2" );

        // Test the longest matching pattern rule
        assertEquals( 1, digester.getRules().match( null, "x/c/d", null, null ).size(), "Longest tail rule 1" );
        assertEquals( "*/c/d", ( ( TestRule ) digester.getRules().match( null, "x/c/d", null, null ).iterator().next() ).getIdentifier(), "Longest tail rule 2" );

    }

    /**
     * Test custom StackAction subclasses.
     */
    @Test
    public void testStackAction()
    {
        final TrackingStackAction action = new TrackingStackAction();

        final Object obj1 = "obj1";
        final Object obj2 = "obj2";
        final Object obj3 = "replpop.obj3";
        final Object obj4 = "replpush.obj4";

        final Object obj8 = "obj8";
        final Object obj9 = "obj9";

        final Digester d = new Digester();
        d.setStackAction( action );

        assertEquals( 0, action.events.size() );
        d.push( obj1 );
        d.push( obj2 );
        d.push( obj3 );
        d.push( obj4 );

        assertNotNull( d.peek( 0 ) );
        // for obj4, a copy should have been pushed
        assertNotSame( obj4, d.peek( 0 ) );
        assertEquals( obj4, d.peek( 0 ) );
        // for obj3, replacement only occurs on pop
        assertSame( obj3, d.peek( 1 ) );
        assertSame( obj2, d.peek( 2 ) );
        assertSame( obj1, d.peek( 3 ) );

        final Object obj4a = d.pop();
        final Object obj3a = d.pop();
        final Object obj2a = d.pop();
        final Object obj1a = d.pop();

        assertNotSame( obj4, obj4a );
        assertEquals( obj4, obj4a );
        assertNotSame( obj3, obj4a );
        assertEquals( obj3, obj3a );
        assertSame( obj2, obj2a );
        assertSame( obj1, obj1a );

        d.push( "stack1", obj8 );
        d.push( "stack1", obj9 );
        final Object obj9a = d.pop( "stack1" );
        final Object obj8a = d.pop( "stack1" );

        assertSame( obj8, obj8a );
        assertSame( obj9, obj9a );

        assertEquals( 12, action.events.size() );
        assertEquals( "push:null:obj1", action.events.get( 0 ) );
        assertEquals( "push:null:obj2", action.events.get( 1 ) );
        assertEquals( "push:null:replpop.obj3", action.events.get( 2 ) );
        assertEquals( "push:null:replpush.obj4", action.events.get( 3 ) );
        assertEquals( "pop:null:replpush.obj4", action.events.get( 4 ) );
        assertEquals( "pop:null:replpop.obj3", action.events.get( 5 ) );
        assertEquals( "pop:null:obj2", action.events.get( 6 ) );
        assertEquals( "pop:null:obj1", action.events.get( 7 ) );

        assertEquals( "push:stack1:obj8", action.events.get( 8 ) );
        assertEquals( "push:stack1:obj9", action.events.get( 9 ) );
        assertEquals( "pop:stack1:obj9", action.events.get( 10 ) );
        assertEquals( "pop:stack1:obj8", action.events.get( 11 ) );
    }

    /**
     * Test the basic stack mechanisms.
     */
    @Test
    public void testStackMethods()
    {

        Object value;

        // New stack must be empty
        assertEquals( 0, digester.getCount(), "New stack is empty" );
        value = digester.peek();
        assertNull( value, "New stack peek() returns null" );
        value = digester.pop();
        assertNull( value, "New stack pop() returns null" );

        // Test pushing and popping activities
        digester.push( "First Item" );
        assertEquals( 1, digester.getCount(), "Pushed one item size" );
        value = digester.peek();
        assertNotNull( value, "Peeked first item is not null" );
        assertEquals( "First Item", value, "Peeked first item value" );

        digester.push( "Second Item" );
        assertEquals( 2, digester.getCount(), "Pushed two items size" );
        value = digester.peek();
        assertNotNull( value, "Peeked second item is not null" );
        assertEquals( "Second Item", value, "Peeked second item value" );

        value = digester.pop();
        assertEquals( 1, digester.getCount(), "Popped stack size" );
        assertNotNull( value, "Popped second item is not null" );
        assertEquals( "Second Item", value, "Popped second item value" );
        value = digester.peek();
        assertNotNull( value, "Remaining item is not null" );
        assertEquals( "First Item", value, "Remaining item value" );
        assertEquals( 1, digester.getCount(), "Remaining stack size" );

        // Cleared stack is empty
        digester.push( "Dummy Item" );
        digester.clear();
        assertEquals( 0, digester.getCount(), "Cleared stack is empty" );
        value = digester.peek();
        assertNull( value, "Cleared stack peek() returns null" );
        value = digester.pop();
        assertNull( value, "Cleared stack pop() returns null" );

    }
}
