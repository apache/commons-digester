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

import org.apache.commons.digester3.CallParamRule;

/**
 * Builder chained when invoking {@link LinkedRuleBuilder#callParam()}.
 *
 * @since 3.0
 */
public final class CallParamBuilder
    extends AbstractBackToLinkedRuleBuilder<CallParamRule>
{

    private int paramIndex;

    private int stackIndex;

    private boolean fromStack;

    private String attributeName;

    CallParamBuilder( final String keyPattern, final String namespaceURI, final RulesBinder mainBinder, final LinkedRuleBuilder mainBuilder )
    {
        super( keyPattern, namespaceURI, mainBinder, mainBuilder );
    }

    /**
     * Sets the zero-relative parameter number.
     *
     * @param paramIndex The zero-relative parameter number
     * @return this builder instance
     */
    public CallParamBuilder ofIndex( final int paramIndex )
    {
        if ( paramIndex < 0 )
        {
            reportError( "callParam().ofIndex( int )", "negative index argument not allowed" );
        }

        this.paramIndex = paramIndex;
        return this;
    }

    /**
     * Sets the attribute from which to save the parameter value.
     *
     * @param attributeName The attribute from which to save the parameter value
     * @return this builder instance
     */
    public CallParamBuilder fromAttribute( /* @Nullable */final String attributeName )
    {
        this.attributeName = attributeName;
        return this;
    }

    /**
     * Flags the parameter to be set from the stack.
     *
     * @param fromStack the parameter flag to be set from the stack
     * @return this builder instance
     */
    public CallParamBuilder fromStack( final boolean fromStack )
    {
        this.fromStack = fromStack;
        return this;
    }

    /**
     * Sets the position of the object from the top of the stack.
     *
     * @param stackIndex The position of the object from the top of the stack
     * @return this builder instance
     */
    public CallParamBuilder withStackIndex( final int stackIndex )
    {
        this.stackIndex = stackIndex;
        this.fromStack = true;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CallParamRule createRule()
    {
        CallParamRule rule;

        if ( fromStack )
        {
            rule = new CallParamRule( paramIndex, stackIndex );
        }
        else
        {
            rule = new CallParamRule( paramIndex );
        }

        rule.setAttributeName( attributeName );

        return rule;
    }

}
