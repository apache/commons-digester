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

import org.apache.commons.digester3.Rule;
import org.apache.commons.digester3.binder.SetPropertiesBuilder;
import org.xml.sax.Attributes;

/**
 */
final class SetPropertiesIgnoreRule
    extends Rule
{

    @Override
    public void begin( final String namespace, final String name, final Attributes attributes )
        throws Exception
    {
        final String attributeName = attributes.getValue( "attr-name" );

        final SetPropertiesBuilder builder = getDigester().peek();
        builder.ignoreAttribute( attributeName );
    }

}
