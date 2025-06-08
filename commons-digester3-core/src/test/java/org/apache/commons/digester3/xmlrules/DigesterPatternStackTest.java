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

package org.apache.commons.digester3.xmlrules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This test case tests the behavior of DigesterRuleParser.PatternStack, a specialized stack whose toString() method
 * returns a /-separated representation of the stack's elements. The tests ensure that
 * DigesterRuleParser.PatternStack.toString() returns the properly formatted string.
 */
class DigesterPatternStackTest
{

    private final PatternStack patternStack = new PatternStack();

    @BeforeEach
    public void setUp()
    {
        patternStack.clear();
    }

    @Test
    void test1()
    {
        assertEquals( "", patternStack.toString() );
    }

    @Test
    void test2()
    {
        patternStack.push( "A" );
        assertEquals( "A", patternStack.toString() );
        patternStack.pop();
        assertEquals( "", patternStack.toString() );
    }

    @Test
    void test3()
    {
        patternStack.push( "A" );
        patternStack.push( "B" );
        assertEquals( "A/B", patternStack.toString() );

        patternStack.pop();
        assertEquals( "A", patternStack.toString() );
    }

    @Test
    void test4()
    {
        patternStack.push( "" );
        assertEquals( "", patternStack.toString() );

        patternStack.push( "" );
        assertEquals( "", patternStack.toString() );
    }

    @Test
    void test5()
    {
        patternStack.push( "A" );
        assertEquals( "A", patternStack.toString() );

        patternStack.push( "" );
        patternStack.push( "" );
        assertEquals( "A", patternStack.toString() );

    }

    @Test
    void test6()
    {
        patternStack.push( "A" );
        patternStack.push( "B" );
        patternStack.clear();
        assertEquals( "", patternStack.toString() );
    }

    @Test
    void test7()
    {
        patternStack.push( "///" );
        assertEquals( "///", patternStack.toString() );

        patternStack.push( "/" );
        assertEquals( "/////", patternStack.toString() );

        patternStack.pop();
        assertEquals( "///", patternStack.toString() );

        patternStack.pop();
        assertEquals( "", patternStack.toString() );
    }

}
