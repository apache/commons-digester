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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.StringTokenizer;

import org.apache.commons.digester3.binder.LinkedRuleBuilder;
import org.apache.commons.digester3.binder.ObjectCreateBuilder;
import org.apache.commons.digester3.binder.RulesBinder;
import org.xml.sax.Attributes;

/**
 */
final class ObjectCreateRule
    extends AbstractXmlRule
{

    ObjectCreateRule( final RulesBinder targetRulesBinder, final PatternStack patternStack )
    {
        super( targetRulesBinder, patternStack );
    }

    @Override
    protected void bindRule( final LinkedRuleBuilder linkedRuleBuilder, final Attributes attributes )
        throws Exception
    {
        final ObjectCreateBuilder builder = linkedRuleBuilder.createObject()
            .ofType( attributes.getValue( "classname" ) )
            .ofTypeSpecifiedByAttribute( attributes.getValue( "attrname" ) );

        final String paramTypesStr = attributes.getValue( "paramtypes" );
        if ( paramTypesStr != null && !paramTypesStr.isEmpty() )
        {
            final StringTokenizer tokens = new StringTokenizer( paramTypesStr, " \t\n\r," );
            final String[] paramTypeNames = new String[tokens.countTokens()];
            int counter = 0;
            while ( tokens.hasMoreTokens() )
            {
                paramTypeNames[counter++] = tokens.nextToken();
            }
            builder.usingConstructor( paramTypeNames );
        }
        final String paramsStr = attributes.getValue( "params" );
        if ( paramsStr != null && !paramsStr.isEmpty() )
        {
            final StringTokenizer tokens = new StringTokenizer( paramTypesStr, " \t\n\r," );
            final Object[] params = new Object[tokens.countTokens()];
            int counter = 0;
            while ( tokens.hasMoreTokens() )
            {
                params[counter++] = tokens.nextToken();
            }
            builder.usingDefaultConstructorArguments( params );
        }
    }

}
