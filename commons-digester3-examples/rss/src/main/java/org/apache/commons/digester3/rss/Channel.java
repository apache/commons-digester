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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * <p>Implementation object representing a <strong>channel</strong> in the
 * <em>Rich Site Summary</em> DTD, version 0.91.  This class may be subclassed
 * to further specialize its behavior.</p>
 */
public class Channel
    implements Serializable
{

    /**
     */
    private static final long serialVersionUID = -7358941908590407568L;

    /**
     * The set of items associated with this Channel.
     */
    protected ArrayList<Item> items = new ArrayList<>();

    /**
     * The set of skip days for this channel.
     */
    protected ArrayList<String> skipDays = new ArrayList<>();

    /**
     * The set of skip hours for this channel.
     */
    protected ArrayList<String> skipHours = new ArrayList<>();

    /**
     * The channel copyright (1-100 characters).
     */
    protected String copyright;

    /**
     * The channel description (1-500 characters).
     */
    protected String description;

    /**
     * The channel description file URL (1-500 characters).
     */
    protected String docs;

    /**
     * The image describing this channel.
     */
    protected Image image;

    /**
     * The channel language (2-5 characters).
     */
    protected String language;

    /**
     * The channel last build date (1-100 characters).
     */
    protected String lastBuildDate;

    /**
     * The channel link (1-500 characters).
     */
    protected String link;

    /**
     * The managing editor (1-100 characters).
     */
    protected String managingEditor;

    /**
     * The channel publication date (1-100 characters).
     */
    protected String pubDate;

    /**
     * The channel rating (20-500 characters).
     */
    protected String rating;

    /**
     * The text input description for this channel.
     */
    protected TextInput textInput;

    /**
     * The channel title (1-100 characters).
     */
    protected String title;

    /**
     * The RSS specification version number used to create this Channel.
     */
    protected double version = 0.91;

    /**
     * The webmaster email address (1-100 characters).
     */
    protected String webMaster;

    /**
     * Add an additional item.
     *
     * @param item The item to be added
     */
    public void addItem( final Item item )
    {
        synchronized ( items )
        {
            items.add( item );
        }
    }

    /**
     * Add an additional skip day name.
     *
     * @param skipDay The skip day to be added
     */
    public void addSkipDay( final String skipDay )
    {
        synchronized ( skipDays )
        {
            skipDays.add( skipDay );
        }
    }

    /**
     * Add an additional skip hour name.
     *
     * @param skipHour The skip hour to be added
     */
    public void addSkipHour( final String skipHour )
    {
        synchronized ( skipHours )
        {
            skipHours.add( skipHour );
        }
    }

    /**
     * Gets the items for this channel.
     */
    public Item[] findItems()
    {
        synchronized ( items )
        {
            final Item[] items = new Item[this.items.size()];
            return this.items.toArray( items );
        }
    }

    /**
     * Gets the skip days for this channel.
     */
    public String[] findSkipDays()
    {
        synchronized ( skipDays )
        {
            final String[] skipDays = new String[this.skipDays.size()];
            return this.skipDays.toArray( skipDays );
        }
    }

    /**
     * Gets the skip hours for this channel.
     */
    public String[] findSkipHours()
    {
        synchronized ( skipHours )
        {
            final String[] skipHours = new String[this.skipHours.size()];
            return this.skipHours.toArray( skipHours );
        }
    }

    public String getCopyright()
    {
        return this.copyright;
    }

    public String getDescription()
    {
        return this.description;
    }

    public String getDocs()
    {
        return this.docs;
    }

    public Image getImage()
    {
        return this.image;
    }

    /**
     * Gets the items for this channel.
     */
    public Item[] getItems()
    {
        return findItems();
    }

    public String getLanguage()
    {
        return this.language;
    }

    public String getLastBuildDate()
    {
        return this.lastBuildDate;
    }

    public String getLink()
    {
        return this.link;
    }

    public String getManagingEditor()
    {
        return this.managingEditor;
    }

    public String getPubDate()
    {
        return this.pubDate;
    }

    public String getRating()
    {
        return this.rating;
    }

    /**
     * Gets the skip days for this channel.
     */
    public String[] getSkipDays()
    {
        return findSkipDays();
    }

    /**
     * Gets the skip hours for this channel.
     */
    public String[] getSkipHours()
    {
        return findSkipHours();
    }

    public TextInput getTextInput()
    {
        return this.textInput;
    }

    public String getTitle()
    {
        return this.title;
    }

    public double getVersion()
    {
        return this.version;
    }

    public String getWebMaster()
    {
        return this.webMaster;
    }

    /**
     * Remove an item for this channel.
     *
     * @param item The item to be removed
     */
    public void removeItem( final Item item )
    {
        synchronized ( items )
        {
            items.remove( item );
        }
    }

    /**
     * Remove a skip day for this channel.
     *
     * @param skipDay The skip day to be removed
     */
    public void removeSkipDay( final String skipDay )
    {
        synchronized ( skipDays )
        {
            skipDays.remove( skipDay );
        }
    }

    /**
     * Remove a skip hour for this channel.
     *
     * @param skipHour The skip hour to be removed
     */
    public void removeSkipHour( final String skipHour )
    {
        synchronized ( skipHours )
        {
            skipHours.remove( skipHour );
        }
    }

    /**
     * Render this channel as XML conforming to the RSS 0.91 specification,
     * to the specified output stream, with no indication of character
     * encoding.
     *
     * @param stream The output stream to write to
     */
    public void render( final OutputStream stream )
    {
        try
        {
            render( stream, null );
        }
        catch ( final UnsupportedEncodingException e )
        {
            // Can not happen
        }
    }

    /**
     * Render this channel as XML conforming to the RSS 0.91 specification,
     * to the specified output stream, with the specified character encoding.
     *
     * @param stream The output stream to write to
     * @param encoding The character encoding to declare, or {@code null}
     *  for no declaration
     *
     * @throws UnsupportedEncodingException if the named encoding
     *  is not supported
     */
    public void render( final OutputStream stream, final String encoding )
        throws UnsupportedEncodingException
    {
        PrintWriter pw = null;
        if ( encoding == null )
        {
            pw = new PrintWriter( stream );
        }
        else
        {
            pw = new PrintWriter( new OutputStreamWriter( stream, encoding ) );
        }
        render( pw, encoding );
        pw.flush();
    }

    /**
     * Render this channel as XML conforming to the RSS 0.91 specification,
     * to the specified writer, with no indication of character encoding.
     *
     * @param writer The writer to render output to
     */
    public void render( final PrintWriter writer )
    {
        render( writer, null );
    }

    /**
     * Render this channel as XML conforming to the RSS 0.91 specification,
     * to the specified writer, indicating the specified character encoding.
     *
     * @param writer The writer to render output to
     * @param encoding The character encoding to declare, or {@code null}
     *  for no declaration
     */
    public void render( final PrintWriter writer, final String encoding )
    {
        writer.print( "<?xml version=\"1.0\"" );
        if ( encoding != null )
        {
            writer.print( " encoding=\"" );
            writer.print( encoding );
            writer.print( "\"" );
        }
        writer.println( "?>" );
        writer.println();

        writer.println( "<rss version=\"0.91\">" );
        writer.println();

        writer.println( "  <channel>" );
        writer.println();

        writer.print( "    <title>" );
        writer.print( title );
        writer.println( "</title>" );

        writer.print( "    <description>" );
        writer.print( description );
        writer.println( "</description>" );

        writer.print( "    <link>" );
        writer.print( link );
        writer.println( "</link>" );

        writer.print( "    <language>" );
        writer.print( language );
        writer.println( "</language>" );

        if ( rating != null )
        {
            writer.print( "    <rating>" );
            writer.print( rating );
            writer.println( "</rating>" );
        }

        if ( copyright != null )
        {
            writer.print( "    <copyright>" );
            writer.print( copyright );
            writer.print( "</copyright>" );
        }

        if ( pubDate != null )
        {
            writer.print( "    <pubDate>" );
            writer.print( pubDate );
            writer.println( "</pubDate>" );
        }

        if ( lastBuildDate != null )
        {
            writer.print( "    <lastBuildDate>" );
            writer.print( lastBuildDate );
            writer.println( "</lastBuildDate>" );
        }

        if ( docs != null )
        {
            writer.print( "    <docs>" );
            writer.print( docs );
            writer.println( "</docs>" );
        }

        if ( managingEditor != null )
        {
            writer.print( "    <managingEditor>" );
            writer.print( managingEditor );
            writer.println( "</managingEditor>" );
        }

        if ( webMaster != null )
        {
            writer.print( "    <webMaster>" );
            writer.print( webMaster );
            writer.println( "</webMaster>" );
        }

        writer.println();

        if ( image != null )
        {
            image.render( writer );
            writer.println();
        }

        if ( textInput != null )
        {
            textInput.render( writer );
            writer.println();
        }

        final String[] skipDays = findSkipDays();
        if ( skipDays.length > 0 )
        {
            writer.println( "    <skipDays>" );
            for (final String skipDay : skipDays) {
                writer.print( "      <skipDay>" );
                writer.print( skipDay );
                writer.println( "</skipDay>" );
            }
            writer.println( "    </skipDays>" );
        }

        final String[] skipHours = findSkipHours();
        if ( skipHours.length > 0 )
        {
            writer.println( "    <skipHours>" );
            for (final String skipHour : skipHours) {
                writer.print( "      <skipHour>" );
                writer.print( skipHour );
                writer.println( "</skipHour>" );
            }
            writer.println( "    </skipHours>" );
            writer.println();
        }

        final Item[] items = findItems();
        for (final Item item : items) {
            item.render( writer );
            writer.println();
        }

        writer.println( "  </channel>" );
        writer.println();

        writer.println( "</rss>" );
    }

    /**
     * Render this channel as XML conforming to the RSS 0.91 specification,
     * to the specified writer, with no indication of character encoding.
     *
     * @param writer The writer to render output to
     */
    public void render( final Writer writer )
    {
        render( writer, null );
    }

    /**
     * Render this channel as XML conforming to the RSS 0.91 specification,
     * to the specified writer, indicating the specified character encoding.
     *
     * @param writer The writer to render output to
     * @param encoding The character encoding to declare, or {@code null}
     *  for no declaration
     */
    public void render( final Writer writer, final String encoding )
    {
        final PrintWriter pw = new PrintWriter( writer );
        render( pw, encoding );
        pw.flush();
    }

    public void setCopyright( final String copyright )
    {
        this.copyright = copyright;
    }

    public void setDescription( final String description )
    {
        this.description = description;
    }

    public void setDocs( final String docs )
    {
        this.docs = docs;
    }

    public void setImage( final Image image )
    {
        this.image = image;
    }

    public void setLanguage( final String language )
    {
        this.language = language;
    }

    public void setLastBuildDate( final String lastBuildDate )
    {
        this.lastBuildDate = lastBuildDate;
    }

    public void setLink( final String link )
    {
        this.link = link;
    }

    public void setManagingEditor( final String managingEditor )
    {
        this.managingEditor = managingEditor;
    }

    public void setPubDate( final String pubDate )
    {
        this.pubDate = pubDate;
    }

    public void setRating( final String rating )
    {
        this.rating = rating;
    }

    public void setTextInput( final TextInput textInput )
    {
        this.textInput = textInput;
    }

    public void setTitle( final String title )
    {
        this.title = title;
    }

    public void setVersion( final double version )
    {
        this.version = version;
    }

    public void setWebMaster( final String webMaster )
    {
        this.webMaster = webMaster;
    }

}
