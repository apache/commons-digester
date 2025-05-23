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
import org.apache.commons.digester3.binder.RulesModule;

final class PrefixedRulesBinder
    implements RulesBinder
{

    private final RulesBinder wrappedRulesBinder;

    private final String prefix;

    PrefixedRulesBinder( final RulesBinder wrappedRulesBinder, final String prefix )
    {
        this.wrappedRulesBinder = wrappedRulesBinder;
        this.prefix = prefix;
    }

    @Override
    public void addError( final String messagePattern, final Object... arguments )
    {
        this.wrappedRulesBinder.addError( messagePattern, arguments );
    }

    @Override
    public void addError( final Throwable t )
    {
        this.wrappedRulesBinder.addError( t );
    }

    @Override
    public LinkedRuleBuilder forPattern( String pattern )
    {
        if ( this.prefix != null && !this.prefix.isEmpty() )
        {
            pattern = this.prefix + '/' + pattern;
        }
        return this.wrappedRulesBinder.forPattern( pattern );
    }

    @Override
    public ClassLoader getContextClassLoader()
    {
        return this.wrappedRulesBinder.getContextClassLoader();
    }

    @Override
    public void install( final RulesModule rulesModule )
    {
        this.wrappedRulesBinder.install( rulesModule );
    }

}
