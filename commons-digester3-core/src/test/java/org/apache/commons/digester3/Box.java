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

import java.util.LinkedList;
import java.util.List;

/**
 * Simple class for use in unit tests. A box has an ID, and can have multiple boxes within it.
 */
public class Box
{
    private String id;

    private final List<Box> children = new LinkedList<Box>();

    public Box()
    {
    }

    public void addChild( final Box child )
    {
        this.children.add( child );
    }

    public List<Box> getChildren()
    {
        return children;
    }

    public String getId()
    {
        return id;
    }

    /**
     * Return a string containing this object's name value, followed by the names of all child objects (and their
     * children etc) in pre-order sequence. Each name is separated by a space from the preceding one.
     */
    public String getIds()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( this.id );
        for ( final Box child : children )
        {
            buf.append( " " );
            buf.append( child.getIds() );
        }
        return buf.toString();
    }

    public void setId( final String id )
    {
        this.id = id;
    }

    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( "[Box] id=" );
        buf.append( id );
        buf.append( " nchildren=" );
        buf.append( children.size() );

        for ( final Box child : children )
        {
            buf.append( "  " );
            buf.append( child.toString() );
        }
        return buf.toString();
    }
}
