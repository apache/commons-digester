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

package org.apache.commons.digester3.plugins;

import org.apache.commons.digester3.Digester;

public class Slider
    implements Widget
{
    // define different rules on this class
    public static void addRangeRules( final Digester digester, final String pattern )
    {
        // note: deliberately no addSetProperties rule
        final Class<?>[] paramtypes = { Integer.class, Integer.class };
        digester.addCallMethod( pattern + "/range", "setRange", 2, paramtypes );
        digester.addCallParam( pattern + "/range", 0, "min" );
        digester.addCallParam( pattern + "/range", 1, "max" );
    }

    // define rules on this class
    public static void addRules( final Digester digester, final String pattern )
    {
        digester.addSetProperties( pattern );

        final Class<?>[] paramtypes = { Integer.class };
        digester.addCallMethod( pattern + "/min", "setMin", 0, paramtypes );
        digester.addCallMethod( pattern + "/max", "setMax", 0, paramtypes );
    }

    private String label = "nolabel";

    private int min;

    private int max;

    public Slider()
    {
    }

    public String getLabel()
    {
        return label;
    }

    public int getMax()
    {
        return max;
    }

    public int getMin()
    {
        return min;
    }

    public void setLabel( final String label )
    {
        this.label = label;
    }

    public void setMax( final int max )
    {
        this.max = max;
    }

    public void setMin( final int min )
    {
        this.min = min;
    }

    public void setRange( final int min, final int max )
    {
        this.min = min;
        this.max = max;
    }
}
