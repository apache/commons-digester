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

package org.apache.commons.digester3.xmlrules;

/**
 * Object for use with testing call param rules.
 */
public class CallParamTestObject
{

    private String left = "UNSET";

    private String right = "UNSET";

    private String middle = "UNSET";

    public void duo( final String left, final String right )
    {
        this.left = left;
        this.right = right;
    }

    public String getLeft()
    {
        return left;
    }

    public String getMiddle()
    {
        return middle;
    }

    public String getRight()
    {
        return right;
    }

    public void setLeft( final String left )
    {
        this.left = left;
    }

    public void setMiddle( final String middle )
    {
        this.middle = middle;
    }

    public void setRight( final String right )
    {
        this.right = right;
    }

    @Override
    public String toString()
    {
        return "LEFT: " + left + " MIDDLE:" + middle + " RIGHT:" + right;
    }

    public void triple( final String left, final String middle, final String right )
    {
        this.left = left;
        this.right = right;
        this.middle = middle;
    }
}
