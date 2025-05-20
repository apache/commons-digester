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
package org.apache.commons.digester3.annotations.rss;

import java.util.Objects;

import org.apache.commons.digester3.annotations.rules.BeanPropertySetter;
import org.apache.commons.digester3.annotations.rules.ObjectCreate;

/**
 * @since 2.1
 */
@ObjectCreate( pattern = "rss/channel/image" )
public final class Image
{

    @BeanPropertySetter( pattern = "rss/channel/image/description" )
    private String description;

    @BeanPropertySetter( pattern = "rss/channel/image/width" )
    private int width;

    @BeanPropertySetter( pattern = "rss/channel/image/height" )
    private int height;

    @BeanPropertySetter( pattern = "rss/channel/image/link" )
    private String link;

    @BeanPropertySetter( pattern = "rss/channel/image/title" )
    private String title;

    @BeanPropertySetter( pattern = "rss/channel/image/url" )
    private String url;

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
        final Image other = (Image) obj;
        if ( !Objects.equals(description, other.description) )
        {
            return false;
        }
        if ( height != other.height ) {
            return false;
        }
        if ( !Objects.equals(link, other.link) )
        {
            return false;
        }
        if ( !Objects.equals(title, other.title) )
        {
            return false;
        }
        if ( !Objects.equals(url, other.url) )
        {
            return false;
        }
        if ( width != other.width ) {
            return false;
        }
        return true;
    }

    public String getDescription()
    {
        return description;
    }

    public int getHeight()
    {
        return height;
    }

    public String getLink()
    {
        return link;
    }

    public String getTitle()
    {
        return title;
    }

    public String getUrl()
    {
        return url;
    }

    public int getWidth()
    {
        return width;
    }

    public void setDescription( final String description )
    {
        this.description = description;
    }

    public void setHeight( final int height )
    {
        this.height = height;
    }

    public void setLink( final String link )
    {
        this.link = link;
    }

    public void setTitle( final String title )
    {
        this.title = title;
    }

    public void setUrl( final String url )
    {
        this.url = url;
    }

    public void setWidth( final int width )
    {
        this.width = width;
    }

    @Override
    public String toString()
    {
        return "Image [description=" + description + ", height=" + height + ", link=" + link + ", title=" + title
            + ", url=" + url + ", width=" + width + "]";
    }

}
