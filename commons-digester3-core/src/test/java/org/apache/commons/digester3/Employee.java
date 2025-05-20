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

import java.util.ArrayList;

/**
 * Bean for Digester testing.
 */

public class Employee
{

    private final ArrayList<Address> addresses = new ArrayList<>();

    private String firstName;

    private String lastName;

    // this is to allow testing of primitive conversion
    private int age;

    private boolean active;

    private float salary;

    public Employee()
    {
        this( "My First Name", "My Last Name" );
    }

    public Employee( final String firstName, final String lastName )
    {
        setFirstName( firstName );
        setLastName( lastName );
    }

    public void addAddress( final Address address )
    {
        addresses.add( address );
    }

    public Address getAddress( final String type )
    {
        for ( final Address address : addresses )
        {
            if ( type.equals( address.getType() ) ) {
                return address;
            }
        }
        return null;
    }

    public int getAge()
    {
        return age;
    }

    public String getFirstName()
    {
        return this.firstName;
    }

    public String getLastName()
    {
        return this.lastName;
    }

    public float getSalary()
    {
        return salary;
    }

    public boolean isActive()
    {
        return active;
    }

    public void removeAddress( final Address address )
    {
        addresses.remove( address );
    }

    public void setActive( final boolean active )
    {
        this.active = active;
    }

    public void setAge( final int age )
    {
        this.age = age;
    }

    public void setFirstName( final String firstName )
    {
        this.firstName = firstName;
    }

    public void setLastName( final String lastName )
    {
        this.lastName = lastName;
    }

    public void setSalary( final float salary )
    {
        this.salary = salary;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder( "Employee[" );
        sb.append( "firstName=" );
        sb.append( firstName );
        sb.append( ", lastName=" );
        sb.append( lastName );
        sb.append( "]" );
        return sb.toString();
    }

}
