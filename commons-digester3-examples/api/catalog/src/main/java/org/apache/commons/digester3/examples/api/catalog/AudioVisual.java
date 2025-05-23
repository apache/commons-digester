package org.apache.commons.digester3.examples.api.catalog;

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

/**
 *  See Main.java.
 */
public class AudioVisual
    implements Item
{

    private int yearMade;

    private String category;

    private String name;

    private String desc;

    private Integer runtime;

    private String type;

    @Override
    public void print()
    {
        System.out.println( "AudioVisual:" );
        System.out.println( "  type=" + type );
        System.out.println( "  yearMade=" + yearMade );
        System.out.println( "  category=" + category );
        System.out.println( "  name=" + name );
        System.out.println( "  desc=" + desc );
        System.out.println( "  runtime=" + runtime );
    }

    public void setCategory( final String category )
    {
        this.category = category;
    }

    public void setDesc( final String desc )
    {
        this.desc = desc;
    }

    public void setName( final String name )
    {
        this.name = name;
    }

    // note: digester can convert a string in the XML file to an Integer
    public void setRuntime( final Integer runtime )
    {
        this.runtime = runtime;
    }

    public void setType( final String type )
    {
        this.type = type;
    }

    // note: digester can convert a string in the XML file to an int.
    public void setYearMade( final int yearMade )
    {
        this.yearMade = yearMade;
    }

}
