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

/**
 * Bean for Digester testing.
 */

public class Address
{

    private String city;

    private String state;

    private String street;

    private String type;

    private String zipCode;

    public Address()
    {
        this( "My Street", "My City", "US", "MyZip" );
    }

    public Address( final String street, final String city, final String state, final String zipCode )
    {
        setStreet( street );
        setCity( city );
        setState( state );
        setZipCode( zipCode );
    }

    public String getCity()
    {
        return ( this.city );
    }

    public String getState()
    {
        return ( this.state );
    }

    public String getStreet()
    {
        return ( this.street );
    }

    public String getType()
    {
        return ( this.type );
    }

    public String getZipCode()
    {
        return ( this.zipCode );
    }

    public void setCity( final String city )
    {
        this.city = city;
    }

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
        final StringBuilder sb = new StringBuilder( "Address[" );
        sb.append( "street=" );
        sb.append( street );
        sb.append( ", city=" );
        sb.append( city );
        sb.append( ", state=" );
        sb.append( state );
        sb.append( ", zipCode=" );
        sb.append( zipCode );
        sb.append( "]" );
        return ( sb.toString() );
    }

}
