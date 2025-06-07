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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

/**
 * Test case for WithDefaultsRulesWrapper
 */
public class WithDefaultsRulesWrapperTestCase
{

    @Test
    void testClear()
    {
        // test clear wrapped
        final WithDefaultsRulesWrapper rules = new WithDefaultsRulesWrapper( new RulesBase() );
        rules.add( "alpha", new TestRule( "Tom" ) );
        rules.add( "alpha", new TestRule( "Dick" ) );
        rules.add( "alpha", new TestRule( "Harry" ) );

        assertNotNull( rules.rules(), "Rules should not be null" );
        assertEquals( 3, rules.rules().size(), "Wrong number of rules registered (1)" );
        rules.clear();
        assertEquals( 0, rules.rules().size(), "Clear Failed (1)" );

        // mixed
        rules.add( "alpha", new TestRule( "Tom" ) );
        rules.add( "alpha", new TestRule( "Dick" ) );
        rules.add( "alpha", new TestRule( "Harry" ) );
        rules.addDefault( new TestRule( "Roger" ) );
        assertEquals( 4, rules.rules().size(), "Wrong number of rules registered (2)" );
        rules.clear();
        assertEquals( 0, rules.rules().size(), "Clear Failed (2)" );

        rules.addDefault( new TestRule( "Roger" ) );
        assertEquals( 1, rules.rules().size(), "Wrong number of rules registered (3)" );
        rules.clear();
        assertEquals( 0, rules.rules().size(), "Clear Failed (3)" );
    }

    @Test
    void testMatch()
    {
        // test no defaults
        final WithDefaultsRulesWrapper rules = new WithDefaultsRulesWrapper( new RulesBase() );
        rules.add( "alpha", new TestRule( "Tom" ) );
        rules.add( "alpha", new TestRule( "Dick" ) );
        rules.add( "alpha", new TestRule( "Harry" ) );
        rules.addDefault( new TestRule( "Roger" ) );
        rules.addDefault( new TestRule( "Rabbit" ) );

        List<Rule> matches = rules.match( "", "alpha", null, null );
        assertEquals( 3, matches.size(), "Wrong size (1)" );
        assertEquals( "Tom", ( ( TestRule ) matches.get( 0 ) ).getIdentifier(), "Wrong order (1)" );
        assertEquals( "Dick", ( ( TestRule ) matches.get( 1 ) ).getIdentifier(), "Wrong order (2)" );
        assertEquals( "Harry", ( ( TestRule ) matches.get( 2 ) ).getIdentifier(), "Wrong order (3)" );

        matches = rules.match( "", "not-alpha", null, null );
        assertEquals( 2, matches.size(), "Wrong size (2)" );
        assertEquals( "Roger", ( ( TestRule ) matches.get( 0 ) ).getIdentifier(), "Wrong order (4)" );
        assertEquals( "Rabbit", ( ( TestRule ) matches.get( 1 ) ).getIdentifier(), "Wrong order (5)" );
    }

    @Test
    void testRules()
    {
        // test rules
        final WithDefaultsRulesWrapper rules = new WithDefaultsRulesWrapper( new RulesBase() );
        rules.add( "alpha", new TestRule( "Tom" ) );
        rules.add( "alpha", new TestRule( "Dick" ) );
        rules.addDefault( new TestRule( "Roger" ) );
        rules.add( "alpha", new TestRule( "Harry" ) );

        assertNotNull( rules.rules(), "Rules should not be null" );
        assertEquals( "Tom", ( ( TestRule ) rules.rules().get( 0 ) ).getIdentifier(), "Wrong order (1)" );
        assertEquals( "Dick", ( ( TestRule ) rules.rules().get( 1 ) ).getIdentifier(), "Wrong order (2)" );
        assertEquals( "Roger", ( ( TestRule ) rules.rules().get( 2 ) ).getIdentifier(), "Wrong order (3)" );
        assertEquals( "Harry", ( ( TestRule ) rules.rules().get( 3 ) ).getIdentifier(), "Wrong order (4)" );
    }
}
