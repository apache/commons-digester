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
package org.apache.commons.digester3.annotations.rss;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.digester3.annotations.rules.BeanPropertySetter;
import org.apache.commons.digester3.annotations.rules.ObjectCreate;
import org.apache.commons.digester3.annotations.rules.SetNext;

/**
 * @since 2.1
 */
@ObjectCreate( pattern = "rss/channel" )
public final class Channel
{

    private final List<Item> items = new ArrayList<>();

    @BeanPropertySetter( pattern = "rss/channel/title" )
    private String title;

    @BeanPropertySetter( pattern = "rss/channel/link" )
    private String link;

    @BeanPropertySetter( pattern = "rss/channel/description" )
    private String description;

    @BeanPropertySetter( pattern = "rss/channel/language" )
    private String language;

    private Image image;

    @SetNext
    public void addItem( final Item item )
    {
        this.items.add( item );
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
        final Channel other = (Channel) obj;
        if ( !Objects.equals(description, other.description) )
        {
            return false;
        }
        if ( !Objects.equals(image, other.image) )
        {
            return false;
        }
        if ( !Objects.equals(items, other.items) )
        {
            return false;
        }
        if ( !Objects.equals(language, other.language) )
        {
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
        return true;
    }

    public String getDescription()
    {
        return description;
    }

    public Image getImage()
    {
        return image;
    }

    public List<Item> getItems()
    {
        return items;
    }

    public String getLanguage()
    {
        return language;
    }

    public String getLink()
    {
        return link;
    }

    public String getTitle()
    {
        return title;
    }

    public void setDescription( final String description )
    {
        this.description = description;
    }

    @SetNext
    public void setImage( final Image image )
    {
        this.image = image;
    }

    public void setLanguage( final String language )
    {
        this.language = language;
    }

    public void setLink( final String link )
    {
        this.link = link;
    }

    public void setTitle( final String title )
    {
        this.title = title;
    }

    @Override
    public String toString()
    {
        return "Channel [description=" + description + ", image=" + image + ", items=" + items + ", language="
            + language + ", link=" + link + ", title=" + title + "]";
    }

}
