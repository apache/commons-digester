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

package org.apache.commons.digester3;

public class AlphaBean
    implements Nameable
{
    private String name = "ALPHA";

    private Nameable child;

    private Nameable parent;

    public AlphaBean()
    {
    }

    public AlphaBean( final String name )
    {
        setName( name );
    }

    public Nameable getChild()
    {
        return child;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public Nameable getParent()
    {
        return parent;
    }

    public void setChild( final Nameable child )
    {
        this.child = child;
    }

    @Override
    public void setName( final String name )
    {
        this.name = name;
    }

    public void setParent( final Nameable parent )
    {
        this.parent = parent;
    }

    @Override
    public String toString()
    {
        return "[AlphaBean] name=" + name + " child=" + child;
    }
}
