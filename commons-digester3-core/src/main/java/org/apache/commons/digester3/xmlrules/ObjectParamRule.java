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

import static java.lang.Integer.parseInt;
import static org.apache.commons.beanutils.ConvertUtils.convert;

import org.apache.commons.digester3.binder.LinkedRuleBuilder;
import org.apache.commons.digester3.binder.ObjectParamBuilder;
import org.apache.commons.digester3.binder.RulesBinder;
import org.xml.sax.Attributes;

/**
 * @since 3.2
 */
final class ObjectParamRule
    extends AbstractXmlRule
{

    /**
     * @param targetRulesBinder
     * @param patternStack
     */
    ObjectParamRule( final RulesBinder targetRulesBinder, final PatternStack patternStack )
    {
        super( targetRulesBinder, patternStack );
    }

    @Override
    protected void bindRule( final LinkedRuleBuilder linkedRuleBuilder, final Attributes attributes )
        throws Exception
    {
        // create callparamrule
        final String paramNumber = attributes.getValue( "paramnumber" );
        final String attributeName = attributes.getValue( "attrname" );
        final String type = attributes.getValue( "type" );
        final String value = attributes.getValue( "value" );

        final int paramIndex = parseInt( paramNumber );

        // create object instance
        final Class<?> clazz = getDigester().getClassLoader().loadClass( type );
        Object param;
        if ( value != null )
        {
            param = convert( value, clazz );
        }
        else
        {
            param = clazz.newInstance();
        }

        final ObjectParamBuilder<?> builder = linkedRuleBuilder.objectParam( param ).ofIndex( paramIndex );

        if ( attributeName != null )
        {
            builder.matchingAttribute( attributeName );
        }
    }

}
