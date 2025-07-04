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

package org.apache.commons.digester3.plugins;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.apache.commons.digester3.Digester;
import org.junit.jupiter.api.Test;

/**
 * Test cases for plugins with custom rules which include PluginCreateRule instances, allowing recursive datastructures
 * to be processed.
 */

public class TestRecursion
{

    private int countWidgets( final Container c )
    {
        final List<Widget> l = c.getChildren();
        int sum = 0;
        for ( final Widget w : l )
        {
            ++sum;
            if ( w instanceof Container )
            {
                sum += countWidgets( (Container) w );
            }
        }
        return sum;
    }

    @Test
    void testRecursiveRules()
        throws Exception
    {
        // * tests that a rule can declare custom PluginCreateRules
        // that allow it to plug in instances of itself below
        // itself.

        final Digester digester = new Digester();
        final PluginRules rc = new PluginRules();
        digester.setRules( rc );

        final PluginDeclarationRule pdr = new PluginDeclarationRule();
        digester.addRule( "*/plugin", pdr );

        final PluginCreateRule pcr = new PluginCreateRule( Widget.class );
        digester.addRule( "root/widget", pcr );
        digester.addSetNext( "root/widget", "addChild" );

        final Container root = new Container();
        digester.push( root );

        digester.parse( Utils.getInputStream( this, "test6.xml" ) );

        final int nDescendants = countWidgets( root );
        assertEquals( 10, nDescendants );
    }
}
