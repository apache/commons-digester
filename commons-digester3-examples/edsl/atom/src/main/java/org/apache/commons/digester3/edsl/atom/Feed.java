package org.apache.commons.digester3.edsl.atom;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

public final class Feed
{

    private String title;

    private URL link;

    private Date updated;

    private final List<String> authors = new ArrayList<String>();

    private String id;

    private final List<Entry> entries = new ArrayList<Entry>();

    public void addAuthor( final String author )
    {
        authors.add( author );
    }

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
