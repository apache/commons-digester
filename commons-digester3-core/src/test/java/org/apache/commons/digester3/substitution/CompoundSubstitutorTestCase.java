package org.apache.commons.digester3.substitution;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.digester3.Substitutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

public final class CompoundSubstitutorTestCase
{

    private static final class SubstitutorStub
        extends Substitutor
    {

        private final String newBodyText;

        private final String uri;

        private final String localName;

        private final String type;

        private final String value;

        public SubstitutorStub( final String bodyText, final String uri, final String localName, final String type, final String value )
        {
            this.newBodyText = bodyText;
            this.uri = uri;
            this.localName = localName;
            this.type = type;
            this.value = value;
        }

        /**
         * @see org.apache.commons.digester3.Substitutor#substitute(org.xml.sax.Attributes)
         */
        @Override
        public Attributes substitute( final Attributes attributes )
        {
            final AttributesImpl attribs = new AttributesImpl( attributes );
            attribs.addAttribute( uri, localName, uri + ":" + localName, type, value );
            return attribs;
        }

        /**
         * @see org.apache.commons.digester3.Substitutor#substitute(String)
         */
        @Override
        public String substitute( final String bodyText )
        {
            return newBodyText;
        }

    }

    private Attributes attrib;

    private String bodyText;

    private boolean areEqual( final Attributes a, final Attributes b )
    {
        if ( a.getLength() != b.getLength() )
        {
            return false;
        }

        boolean success = true;
        for ( int i = 0; i < a.getLength() && success; i++ )
        {
            success = a.getLocalName( i ).equals( b.getLocalName( i ) )
                    && a.getQName( i ).equals( b.getQName( i ) )
                    && a.getType( i ).equals( b.getType( i ) )
                    && a.getURI( i ).equals( b.getURI( i ) )
                    && a.getValue( i ).equals( b.getValue( i ) );
        }

        return success;
    }

    @BeforeEach
    public void setUp()
    {
        final AttributesImpl aImpl = new AttributesImpl();
        aImpl.addAttribute( "", "b", ":b", "", "bcd" );
        aImpl.addAttribute( "", "c", ":c", "", "cde" );
        aImpl.addAttribute( "", "d", ":d", "", "def" );

        attrib = aImpl;
        bodyText = "Amazing Body Text!";
    }

    @Test
    public void testChaining()
    {
        final Substitutor a = new SubstitutorStub( "XYZ", "", "a", "", "abc" );
        final Substitutor b = new SubstitutorStub( "STU", "", "b", "", "bcd" );

        final Substitutor test = new CompoundSubstitutor( a, b );

        final AttributesImpl attribFixture = new AttributesImpl( attrib );
        attribFixture.addAttribute( "", "a", ":a", "", "abc" );
        attribFixture.addAttribute( "", "b", ":b", "", "bcd" );

        assertTrue( areEqual( test.substitute( attrib ), attribFixture ) );
        assertEquals( "STU", test.substitute( bodyText ) );
    }

    @Test
    public void testConstructors()
    {
        assertThrows( IllegalArgumentException.class, () -> new CompoundSubstitutor( null, null ) );

        final Substitutor a = new SubstitutorStub( "XYZ", "", "a", "", "abc" );

        assertThrows( IllegalArgumentException.class, () -> new CompoundSubstitutor( a, null ) );

        assertThrows( IllegalArgumentException.class, () -> new CompoundSubstitutor( null, a ) );
    }

}
