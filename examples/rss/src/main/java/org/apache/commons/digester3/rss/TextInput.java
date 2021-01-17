package org.apache.commons.digester3.rss;

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

import java.io.PrintWriter;
import java.io.Serializable;


/**
 * <p>Implementation object representing a <strong>textinput</strong> in the
 * <em>Rich Site Summary</em> DTD, version 0.91.  This class may be subclassed
 * to further specialize its behavior.</p>
 */

public class TextInput implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -147615076863607237L;

    // ------------------------------------------------------------- Properties

    /**
     * The text input description (1-100 characters).
     */
    protected String description;

    public String getDescription()
    {
        return ( this.description );
    }

    public void setDescription( final String description )
    {
        this.description = description;
    }

    /**
     * The text input link (1-500 characters).
     */
    protected String link;

    public String getLink()
    {
        return ( this.link );
    }

    public void setLink( final String link )
    {
        this.link = link;
    }

    /**
     * The text input field name (1-100 characters).
     */
    protected String name;

    public String getName()
    {
        return ( this.name );
    }

    public void setName( final String name )
    {
        this.name = name;
    }

    /**
     * The text input submit button label (1-100 characters).
     */
    protected String title;

    public String getTitle()
    {
        return ( this.title );
    }

    public void setTitle( final String title )
    {
        this.title = title;
    }

    // -------------------------------------------------------- Package Methods

    /**
     * Render this channel as XML conforming to the RSS 0.91 specification,
     * to the specified writer.
     *
     * @param writer The writer to render output to
     */
    void render( final PrintWriter writer )
    {
        writer.println( "    <textinput>" );

        writer.print( "      <title>" );
        writer.print( title );
        writer.println( "</title>" );

        writer.print( "      <description>" );
        writer.print( description );
        writer.println( "</description>" );

        writer.print( "      <name>" );
        writer.print( name );
        writer.println( "</name>" );

        writer.print( "      <link>" );
        writer.print( link );
        writer.println( "</link>" );

        writer.println( "    </textinput>" );
    }

}
