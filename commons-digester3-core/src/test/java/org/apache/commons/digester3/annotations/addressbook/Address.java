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
package org.apache.commons.digester3.annotations.addressbook;

import java.util.Objects;

import org.apache.commons.digester3.annotations.rules.BeanPropertySetter;
import org.apache.commons.digester3.annotations.rules.ObjectCreate;

/**
 * @since 2.1
 */
@ObjectCreate( pattern = "address-book/person/address" )
public class Address
{

    @BeanPropertySetter( pattern = "address-book/person/address/type" )
    private String type;

    @BeanPropertySetter( pattern = "address-book/person/address/street" )
    private String street;

    @BeanPropertySetter( pattern = "address-book/person/address/city" )
    private String city;

    @BeanPropertySetter( pattern = "address-book/person/address/state" )
    private String state;

    @BeanPropertySetter( pattern = "address-book/person/address/zip" )
    private String zip;

    @BeanPropertySetter( pattern = "address-book/person/address/country" )
    private String country;

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
        if ( !Objects.equals(city, other.city) )
        {
            return false;
        }
        if ( !Objects.equals(country, other.country) )
        {
            return false;
        }
        if ( !Objects.equals(state, other.state) )
        {
            return false;
        }
        if ( !Objects.equals(street, other.street) )
        {
            return false;
        }
        if ( !Objects.equals(type, other.type) )
        {
            return false;
        }
        if ( !Objects.equals(zip, other.zip) )
        {
            return false;
        }
        return true;
    }

    public String getCity()
    {
        return city;
    }

    public String getCountry()
    {
        return country;
    }

    public String getState()
    {
        return state;
    }

    public String getStreet()
    {
        return street;
    }

    public String getType()
    {
        return type;
    }

    public String getZip()
    {
        return zip;
    }

    public void setCity( final String city )
    {
        this.city = city;
    }

    public void setCountry( final String country )
    {
        this.country = country;
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

    public void setZip( final String zip )
    {
        this.zip = zip;
    }

    @Override
    public String toString()
    {
        return "Address [city=" + city + ", country=" + country + ", state=" + state + ", street=" + street + ", type="
            + type + ", zip=" + zip + "]";
    }

}
