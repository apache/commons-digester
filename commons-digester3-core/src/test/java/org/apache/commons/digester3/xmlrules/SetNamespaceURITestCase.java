package org.apache.commons.digester3.xmlrules;

import static org.apache.commons.digester3.binder.DigesterLoader.newLoader;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;
import java.util.Date;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.digester3.Digester;
import org.junit.jupiter.api.Test;

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

public final class SetNamespaceURITestCase
{

    @Test
    public void testAtomWithNamespaceParse()
        throws Exception
    {
        // Drive commons-beanutils how to convert dates
        final DateConverter dateConverter = new DateConverter();
        dateConverter.setPatterns( new String[] { "yyyy-MM-dd'T'HH:mm" } );
        ConvertUtils.register( dateConverter, Date.class );

        final URL rules = getClass().getResource( "atom-rules.xml" );
        final URL input = getClass().getResource( "atom-content.xml" );

        final Digester digester = newLoader( new FromXmlRulesModule()
        {

            @Override
            protected void loadRules()
            {
                loadXMLRules( rules );
            }

        } )
        .setNamespaceAware( true )
        .newDigester();
        final Feed feed = digester.parse( input.openStream() );
        assertNotNull( feed );
    }

}
