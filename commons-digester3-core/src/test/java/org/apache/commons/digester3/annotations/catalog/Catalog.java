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
package org.apache.commons.digester3.annotations.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.digester3.annotations.rules.ObjectCreate;
import org.apache.commons.digester3.annotations.rules.SetNext;

/**
 * @since 2.1
 */
@ObjectCreate.List( @ObjectCreate( pattern = "catalog" ) )
public final class Catalog
{

    private final List<Item> items = new ArrayList<>();

    @SetNext( { AudioVisual.class, Book.class } )
    public void addItem( final Item item )
    {
        this.items.add( item );
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final Catalog other = (Catalog) obj;
        if ( !Objects.equals(this.items, other.getItems()) )
        {
            return false;
        }
        return true;
    }

    public List<Item> getItems()
    {
        return this.items;
    }

    public void print()
    {
        System.out.println( "This catalog has " + this.items.size() + " items" );

        for ( final Item item : this.items )
        {
            item.print();
        }
    }

    @Override
    public String toString()
    {
        return "Catalog [items=" + items + "]";
    }

}
