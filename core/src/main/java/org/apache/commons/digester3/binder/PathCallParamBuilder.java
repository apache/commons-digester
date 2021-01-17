package org.apache.commons.digester3.binder;

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

import org.apache.commons.digester3.PathCallParamRule;

/**
 * Builder chained when invoking {@link LinkedRuleBuilder#callParamPath()}.
 *
 * @since 3.0
 */
public final class PathCallParamBuilder
    extends AbstractBackToLinkedRuleBuilder<PathCallParamRule>
{

    private int paramIndex;

    PathCallParamBuilder( final String keyPattern, final String namespaceURI, final RulesBinder mainBinder,
                          final LinkedRuleBuilder mainBuilder )
    {
        super( keyPattern, namespaceURI, mainBinder, mainBuilder );
    }

    /**
     * Sets the zero-relative parameter number.
     *
     * @param paramIndex The zero-relative parameter number
     * @return this builder instance
     */
    public PathCallParamBuilder ofIndex( final int paramIndex )
    {
        if ( paramIndex < 0 )
        {
            reportError( "callParamPath().ofIndex( int )", "negative index argument not allowed" );
        }

        this.paramIndex = paramIndex;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PathCallParamRule createRule()
    {
        return new PathCallParamRule( paramIndex );
    }

}
