package org.apache.commons.digester3.xmlrules;

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

import java.util.Stack;

/**
 */
final class PatternStack
    extends Stack<String>
{

    private static final long serialVersionUID = 1L;

    @Override
    public String toString()
    {
        final StringBuilder strBuilder = new StringBuilder();
        for ( String elem : this )
        {
            if ( !elem.isEmpty() )
            {
                if ( strBuilder.length() > 0 )
                {
                    strBuilder.append( '/' );
                }
                strBuilder.append( elem );
            }
        }
        return strBuilder.toString();
    }

}
