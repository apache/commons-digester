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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for an {@link Attributes} object which expands any "variables" referenced in the attribute value via
 * ${foo} or similar. This is only done when something actually asks for the attribute value, thereby imposing no
 * performance penalty if the attribute is not used.
 *
 * @since 1.6
 */
public class VariableAttributes
    implements Attributes
{

    // list of mapped attributes.
    private final ArrayList<String> values = new ArrayList<>( 10 );

    private Attributes attrs;

    private VariableExpander expander;

    // plain proxy methods follow : nothing interesting :-)

    @Override
    public int getIndex( final String qName )
    {
        return attrs.getIndex( qName );
    }

    @Override
    public int getIndex( final String uri, final String localPart )
    {
        return attrs.getIndex( uri, localPart );
    }

    @Override
    public int getLength()
    {
        return attrs.getLength();
    }

    @Override
    public String getLocalName( final int index )
    {
        return attrs.getLocalName( index );
    }

    @Override
    public String getQName( final int index )
    {
        return attrs.getQName( index );
    }

    @Override
    public String getType( final int index )
    {
        return attrs.getType( index );
    }

    @Override
    public String getType( final String qName )
    {
        return attrs.getType( qName );
    }

    @Override
    public String getType( final String uri, final String localName )
    {
        return attrs.getType( uri, localName );
    }

    @Override
    public String getURI( final int index )
    {
        return attrs.getURI( index );
    }

    @Override
    public String getValue( final int index )
    {
        if ( index >= values.size() )
        {
            // Expand the values array with null elements, so the later
            // call to set(index, s) works ok.
            //
            // Unfortunately, there is no easy way to set the size of
            // an arraylist; we must repeatedly add null elements to it.
            values.ensureCapacity( index + 1 );
            for ( int i = values.size(); i <= index; ++i )
            {
                values.add( null );
            }
        }

        String s = values.get( index );

        if ( s == null )
        {
            // we have never been asked for this value before.
            // get the real attribute value and perform substitution
            // on it.
            s = attrs.getValue( index );
            if ( s != null )
            {
                s = expander.expand( s );
                values.set( index, s );
            }
        }

        return s;
    }

    @Override
    public String getValue( final String qName )
    {
        final int index = attrs.getIndex( qName );
        if ( index == -1 )
        {
            return null;
        }
        return getValue( index );
    }

    @Override
    public String getValue( final String uri, final String localName )
    {
        final int index = attrs.getIndex( uri, localName );
        if ( index == -1 )
        {
            return null;
        }
        return getValue( index );
    }

    /**
     * Specify which attributes class this object is a proxy for.
     *
     * @param attrs The attributes where variables have to be expanded.
     * @param expander The variables expander instance.
     */
    public void init( final Attributes attrs, final VariableExpander expander )
    {
        this.attrs = attrs;
        this.expander = expander;

        // I hope this doesn't release the memory for this array; for
        // efficiency, this should just mark the array as being size 0.
        values.clear();
    }

}
