package org.apache.commons.digester3.annotations.atom;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.digester3.annotations.rules.BeanPropertySetter;
import org.apache.commons.digester3.annotations.rules.CallMethod;
import org.apache.commons.digester3.annotations.rules.ObjectCreate;
import org.apache.commons.digester3.annotations.rules.SetNext;
import org.apache.commons.digester3.annotations.rules.SetProperty;

@ObjectCreate( pattern = "feed" )
public final class Feed
{

    @BeanPropertySetter( pattern = "feed/title" )
    private String title;

    @SetProperty( pattern = "feed/link", attributeName = "href" )
    private URL link;

    @BeanPropertySetter( pattern = "feed/updated" )
    private Date updated;

    private final List<String> authors = new ArrayList<>();

    @BeanPropertySetter( pattern = "feed/id" )
    private String id;

    private final List<Entry> entries = new ArrayList<>();

    @CallMethod( pattern = "feed/author/name", usingElementBodyAsArgument = true )
    public void addAuthor( final String author )
    {
        authors.add( author );
    }

    @SetNext
    public void addEntry( final Entry entry )
    {
        entries.add( entry );
    }

    public List<String> getAuthors()
    {
        return authors;
    }

    public List<Entry> getEntries()
    {
        return entries;
    }

    public String getId()
    {
        return id;
    }

    public URL getLink()
    {
        return link;
    }

    public String getTitle()
    {
        return title;
    }

    public Date getUpdated()
    {
        return updated;
    }

    public void setId( final String id )
    {
        this.id = id;
    }

    public void setLink( final URL link )
    {
        this.link = link;
    }

    public void setTitle( final String title )
    {
        this.title = title;
    }

    public void setUpdated( final Date updated )
    {
        this.updated = updated;
    }

    @Override
    public String toString()
    {
        return "Feed [title=" + title + ", link=" + link + ", updated=" + updated + ", authors=" + authors + ", id="
            + id + ", entries=" + entries + "]";
    }

}
