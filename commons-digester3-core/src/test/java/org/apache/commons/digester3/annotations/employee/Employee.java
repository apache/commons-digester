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
package org.apache.commons.digester3.annotations.employee;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.digester3.annotations.rules.ObjectCreate;
import org.apache.commons.digester3.annotations.rules.SetProperty;

/**
 * @since 2.1
 */
@ObjectCreate( pattern = "employee" )
public class Employee
{

    private final List<Address> addresses = new ArrayList<>();

    @SetProperty( pattern = "employee", attributeName = "name" )
    private String firstName;

    @SetProperty( pattern = "employee", attributeName = "surname" )
    private String lastName;

    public void addAddress( final Address address )
    {
        this.addresses.add( address );
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
        final Employee other = (Employee) obj;
        if ( !Objects.equals(this.addresses, other.getAddresses()) )
        {
            return false;
        }
        if ( !Objects.equals(this.firstName, other.getFirstName()) )
        {
            return false;
        }
        if ( !Objects.equals(this.lastName, other.getLastName()) )
        {
            return false;
        }
        return true;
    }

    public List<Address> getAddresses()
    {
        return this.addresses;
    }

    public String getFirstName()
    {
        return this.firstName;
    }

    public String getLastName()
    {
        return this.lastName;
    }

    public void setFirstName( final String firstName )
    {
        this.firstName = firstName;
    }

    public void setLastName( final String lastName )
    {
        this.lastName = lastName;
    }

    @Override
    public String toString()
    {
        return "Employee [addresses=" + addresses + ", firstName=" + firstName + ", lastName=" + lastName + "]";
    }

}
