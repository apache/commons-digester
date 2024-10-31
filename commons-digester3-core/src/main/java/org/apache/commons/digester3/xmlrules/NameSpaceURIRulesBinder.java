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

import java.util.Stack;

import org.apache.commons.digester3.binder.LinkedRuleBuilder;
import org.apache.commons.digester3.binder.RulesBinder;
import org.apache.commons.digester3.binder.RulesModule;

/**
 * @since 3.0
 */
final class NameSpaceURIRulesBinder
    implements RulesBinder
{

    // a stack is needed because of includes!!!
    private final Stack<String> namespaceURIs = new Stack<>();

    private final RulesBinder wrappedBinder;

    NameSpaceURIRulesBinder( final RulesBinder wrappedBinder )
    {
        this.wrappedBinder = wrappedBinder;
    }

    @Override
    public void addError( final String messagePattern, final Object... arguments )
    {
        wrappedBinder.addError( messagePattern, arguments );
    }

    @Override
    public void addError( final Throwable t )
    {
        wrappedBinder.addError( t );
    }

    /**
     *
     * @param namespaceURI
     */
    public void addNamespaceURI( final String namespaceURI )
    {
        namespaceURIs.push( namespaceURI );
    }

    @Override
    public LinkedRuleBuilder forPattern( final String pattern )
    {
        return wrappedBinder.forPattern( pattern ).withNamespaceURI( namespaceURIs.peek() );
    }

    @Override
    public ClassLoader getContextClassLoader()
    {
        return wrappedBinder.getContextClassLoader();
    }

    @Override
    public void install( final RulesModule rulesModule )
    {
        wrappedBinder.install( rulesModule );
    }

    /**
     */
    public void removeNamespaceURI()
    {
        namespaceURIs.pop();
    }

}
