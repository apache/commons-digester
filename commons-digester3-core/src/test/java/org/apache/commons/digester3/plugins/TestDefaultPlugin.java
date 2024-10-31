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

package org.apache.commons.digester3.plugins;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.apache.commons.digester3.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.NoOpLog;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

public class TestDefaultPlugin
{

    @Test
    public void testDefaultPlugins1()
        throws Exception
    {
        // * tests that when a PluginCreateRule is defined with a default
        // class, that the default class is instantiated when no class
        // or id is specified in the XML file.
        final Digester digester = new Digester();
        final PluginRules rc = new PluginRules();
        digester.setRules( rc );

        final PluginCreateRule pcr = new PluginCreateRule( Widget.class, TextLabel.class );
        digester.addRule( "root/widget", pcr );
        digester.addSetNext( "root/widget", "addChild" );

        final Container root = new Container();
        digester.push( root );

        digester.parse( Utils.getInputStream( this, "test2.xml" ) );

        Object child;
        final List<Widget> children = root.getChildren();
        assertNotNull( children );
        assertEquals( 3, children.size() );

        child = children.get( 0 );
        assertNotNull( child );
        assertInstanceOf( TextLabel.class, child );
        final TextLabel label1 = (TextLabel) child;
        assertEquals( "label1", label1.getLabel() );

        child = children.get( 1 );
        assertNotNull( child );
        assertInstanceOf( TextLabel.class, child );
        final TextLabel label2 = (TextLabel) child;
        assertEquals( "label2", label2.getLabel() );

        child = children.get( 2 );
        assertNotNull( child );
        assertInstanceOf( Slider.class, child );
        final Slider slider1 = (Slider) child;
        assertEquals( "slider1", slider1.getLabel() );
    }

    @Test
    public void testDefaultPlugins2()
    {
        // * tests that when there is no default plugin, it is an error
        // not to have one of plugin-class or plugin-id specified
        final Digester digester = new Digester();
        final PluginRules rc = new PluginRules();
        digester.setRules( rc );

        final PluginCreateRule pcr = new PluginCreateRule( Widget.class );
        digester.addRule( "root/widget", pcr );
        digester.addSetNext( "root/widget", "addChild" );

        final Container root = new Container();
        digester.push( root );

        final Log oldLog = digester.getLogger();
        digester.setLogger( new NoOpLog() );
        try
        {
            SAXParseException e = assertThrows( SAXParseException.class, () -> digester.parse( Utils.getInputStream( this, "test2.xml" ) ) );
            assertInstanceOf( PluginInvalidInputException.class, e.getException() );
        }
        finally
        {
            digester.setLogger( oldLog );
        }

    }

    @Test
    public void testDefaultPlugins3()
    {
        // * tests that the default plugin must implement or extend the
        // plugin base class.
        final Digester digester = new Digester();
        final PluginRules rc = new PluginRules();
        digester.setRules( rc );

        final PluginCreateRule pcr = new PluginCreateRule( Widget.class, Object.class );
        digester.addRule( "root/widget", pcr );
        digester.addSetNext( "root/widget", "addChild" );

        final Container root = new Container();
        digester.push( root );

        final Log oldLog = digester.getLogger();
        digester.setLogger( new NoOpLog() );
        try
        {
            SAXParseException e = assertThrows( SAXParseException.class, () -> digester.parse( Utils.getInputStream( this, "test2.xml" ) ) );
            assertInstanceOf( PluginConfigurationException.class, e.getException() );
        }
        finally
        {
            digester.setLogger( oldLog );
        }
    }
}
