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

import org.apache.commons.digester3.binder.LinkedRuleBuilder;
import org.apache.commons.digester3.binder.RulesBinder;
import org.xml.sax.Attributes;

/**
 */
abstract class AbstractXmlMethodRule
    extends AbstractXmlRule
{

    AbstractXmlMethodRule( final RulesBinder targetRulesBinder, final PatternStack patternStack )
    {
        super( targetRulesBinder, patternStack );
    }

    @Override
    protected final void bindRule( final LinkedRuleBuilder linkedRuleBuilder, final Attributes attributes )
        throws Exception
    {
        final String methodName = attributes.getValue( "methodname" );
        final String paramType = attributes.getValue( "paramtype" );
        final String exactMatch = attributes.getValue( "exactMatch" );
        final String fireOnBegin = attributes.getValue( "fireOnBegin" );

        bindRule( linkedRuleBuilder, methodName, paramType, "true".equals( exactMatch ), "true".equals( fireOnBegin ) );
    }

    /**
     * @param methodName
     */
    protected abstract void bindRule( LinkedRuleBuilder linkedRuleBuilder, String methodName, String paramType,
                                      boolean exactMatch, boolean fireOnBegin );

}
