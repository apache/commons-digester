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

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester3.binder.AbstractRulesModule;
import org.junit.jupiter.api.Test;
import org.xml.sax.Locator;

/**
 * Tests that StackAction can be used to track the source location of objects created from input XML stream.
 */

public class LocationTrackerTestCase
{

    private static final class LocationTracker
        implements StackAction
    {
        public Map<Object, String> locations = new HashMap<>();

        @Override
        public <T> T onPop( final Digester d, final String stackName, final T o )
        {
            return o;
        }

        @Override
        public <T> T onPush( final Digester d, final String stackName, final T o )
        {
            if ( stackName == null )
            {
                // we only care about the real object stack

                // note that a Locator object can also provide
                // publicId and systemId info.
                final Locator l = d.getDocumentLocator();
                final StringBuilder locn = new StringBuilder();
                locn.append( "line=" );
                locn.append( l.getLineNumber() );
                locations.put( o, locn.toString() );
            }
            return o;
        }
    }

    @Test
    void testAll()
        throws Exception
    {
        final String TEST_XML =
            "<?xml version='1.0'?>\n" + "<box id='root'>\n" + "  <subBox id='box1'/>\n" + "  <ignoreme/>\n"
                + "  <subBox id='box2'/> <subBox id='box3'/>\n" + "</box>";

        final LocationTracker locnTracker = new LocationTracker();

        final Digester digester = newLoader( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "box" ).createObject().ofType( Box.class )
                    .then()
                    .setProperties();
                forPattern( "box/subBox" ).createObject().ofType( Box.class )
                    .then()
                    .setProperties()
                    .then()
                    .setNext( "addChild" );
            }

        })
        .setStackAction( locnTracker )
        .newDigester();

        final Box root = digester.parse( new StringReader( TEST_XML ) );
        assertNotNull( root );
        final List<Box> children = root.getChildren();
        assertEquals( 3, children.size() );
        final Box box1 = children.get( 0 );
        final Box box2 = children.get( 1 );
        final Box box3 = children.get( 2 );

        assertEquals( "line=2", locnTracker.locations.get( root ) );
        assertEquals( "line=3", locnTracker.locations.get( box1 ) );
        assertEquals( "line=5", locnTracker.locations.get( box2 ) );
        assertEquals( "line=5", locnTracker.locations.get( box3 ) );
    }
}
