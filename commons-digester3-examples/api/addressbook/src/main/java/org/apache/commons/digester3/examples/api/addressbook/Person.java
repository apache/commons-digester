package org.apache.commons.digester3.examples.api.addressbook;

/*
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * See Main.java.
 */
public class Person
{

    private int id;

    private String category;

    private String name;

    private final HashMap<String, String> emails = new HashMap<String, String>();

    private final List<Address> addresses = new ArrayList<Address>();

    public void addAddress( final Address addr )
    {
        addresses.add( addr );
    }

    /** We assume only one email of each type... */
    public void addEmail( final String type, final String address )
    {
        emails.put( type, address );
    }

    public void print()
    {
        System.out.println( "Person #" + id );
        System.out.println( "  category=" + category );
        System.out.println( "  name=" + name );

        for (final String type : emails.keySet()) {
            final String address = emails.get( type );

            System.out.println( "  email (type " + type + ") : " + address );
        }

        for (final Address addr : addresses) {
            addr.print( System.out, 2 );
        }
    }

    public void setCategory( final String category )
    {
        this.category = category;
    }

    /**
     * A unique id for this person. Note that the Digester automatically converts the id to an integer.
     */
    public void setId( final int id )
    {
        this.id = id;
    }

    public void setName( final String name )
    {
        this.name = name;
    }

}
