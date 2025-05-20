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

import java.util.Objects;

import org.apache.commons.digester3.annotations.rules.BeanPropertySetter;
import org.apache.commons.digester3.annotations.rules.ObjectCreate;
import org.apache.commons.digester3.annotations.rules.SetProperty;
import org.apache.commons.digester3.annotations.rules.SetTop;

/**
 * @since 2.1
 */
@ObjectCreate( pattern = "employee/address" )
public class Address
{

    @BeanPropertySetter( pattern = "employee/address/city" )
    private String city;

    @BeanPropertySetter( pattern = "employee/address/state" )
    private String state;

    @BeanPropertySetter( pattern = "employee/address/street" )
    private String street;

    @SetProperty( pattern = "employee/address", attributeName = "place" )
    private String type;

    @BeanPropertySetter( pattern = "employee/address/zip-code" )
    private String zipCode;

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
        final Address other = (Address) obj;
        if ( !Objects.equals(this.city, other.getCity()) )
        {
            return false;
        }
        if ( !Objects.equals(this.state, other.getState()) )
        {
            return false;
        }
        if ( !Objects.equals(this.street, other.getStreet()) )
        {
            return false;
        }
        if ( !Objects.equals(this.type, other.getType()) )
        {
            return false;
        }
        if ( !Objects.equals(this.zipCode, other.getZipCode()) )
        {
            return false;
        }
        return true;
    }

    public String getCity()
    {
        return this.city;
    }

    public String getState()
    {
        return this.state;
    }

    public String getStreet()
    {
        return this.street;
    }

    public String getType()
    {
        return this.type;
    }

    public String getZipCode()
    {
        return this.zipCode;
    }

    public void setCity( final String city )
    {
        this.city = city;
    }

    @SetTop( pattern = "employee/address" )
    public void setEmployee( final Employee employee )
    {
        employee.addAddress( this );
    }

    public void setState( final String state )
    {
        this.state = state;
    }

    public void setStreet( final String street )
    {
        this.street = street;
    }

    public void setType( final String type )
    {
        this.type = type;
    }

    public void setZipCode( final String zipCode )
    {
        this.zipCode = zipCode;
    }

    @Override
    public String toString()
    {
        return "Address [city=" + city + ", state=" + state + ", street=" + street + ", type=" + type + ", zipCode="
            + zipCode + "]";
    }

}
