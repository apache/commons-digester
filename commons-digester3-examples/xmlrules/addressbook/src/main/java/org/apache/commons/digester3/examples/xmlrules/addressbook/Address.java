package org.apache.commons.digester3.examples.xmlrules.addressbook;

/*
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

import java.io.PrintStream;

/**
 * See Main.java.
 */
public class Address
{

    private String type;

    private String street;

    private String city;

    private String state;

    private String zip;

    private String country;

    /**
     * Returns the value of city.
     */
    public String getCity()
    {
        return city;
    }

    /**
     * Returns the value of country.
     */
    public String getCountry()
    {
        return country;
    }

    /**
     * Returns the value of state.
     */
    public String getState()
    {
        return state;
    }

    /**
     * Returns the value of street.
     */
    public String getStreet()
    {
        return street;
    }

    /**
     * Returns the value of type.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Returns the value of zip.
     */
    public String getZip()
    {
        return zip;
    }

    public void print( final PrintStream out, int indentAmount )
    {
        final StringBuilder indentStr = new StringBuilder( indentAmount );
        for ( ; indentAmount > 0; --indentAmount )
        {
            indentStr.append( ' ' );
        }

        out.print( indentStr );
        out.print( "address type: " );
        out.println( type );

        out.print( indentStr );
        out.println( "  " + street );

        out.print( indentStr );
        out.println( "  " + city + " " + state + " " + zip );

        out.print( indentStr );
        out.println( "  " + country );
    }

    /**
     * Sets the value of city.
     *
     * @param city The value to assign to city.
     */
    public void setCity( final String city )
    {
        this.city = city;
    }

    /**
     * Sets the value of country.
     *
     * @param country The value to assign to country.
     */
    public void setCountry( final String country )
    {
        this.country = country;
    }

    /**
     * Sets the value of state.
     *
     * @param state The value to assign to state.
     */
    public void setState( final String state )
    {
        this.state = state;
    }

    /**
     * Sets the value of street.
     *
     * @param street The value to assign to street.
     */
    public void setStreet( final String street )
    {
        this.street = street;
    }

    /**
     * Sets the value of type.
     *
     * @param type The value to assign to type.
     */
    public void setType( final String type )
    {
        this.type = type;
    }

    /**
     * Sets the value of zip.
     *
     * @param zip The value to assign to zip.
     */
    public void setZip( final String zip )
    {
        this.zip = zip;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( " address (type " + type + ")\n" );
        sb.append( "       " + street + "\n" );
        sb.append( "       " + city + " " + state + " " + zip + "\n" );
        sb.append( "       " + country + "\n" );
        return sb.toString();
    }

}
