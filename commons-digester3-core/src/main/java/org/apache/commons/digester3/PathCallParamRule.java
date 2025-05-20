package org.apache.commons.digester3;

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

import static java.lang.String.format;

import org.xml.sax.Attributes;

/**
 * <p>
 * Rule implementation that saves a parameter containing the {@code Digester} matching path for use by a
 * surrounding {@code CallMethodRule}. This Rule is most useful when making extensive use of wildcards in rule
 * patterns.
 * </p>
 *
 * @since 1.6
 */
public class PathCallParamRule
    extends Rule
{

    /**
     * The zero-relative index of the parameter we are saving.
     */
    protected int paramIndex;

    /**
     * Constructs a "call parameter" rule that will save the body text of this element as the parameter value.
     *
     * @param paramIndex The zero-relative parameter number
     */
    public PathCallParamRule( final int paramIndex )
    {
        this.paramIndex = paramIndex;
    }

    @Override
    public void begin( final String namespace, final String name, final Attributes attributes )
        throws Exception
    {
        final String param = getDigester().getMatch();

        if ( param != null )
        {
            final Object[] parameters = getDigester().peekParams();
            parameters[paramIndex] = param;
        }
    }

    @Override
    public String toString()
    {
        return format( "PathCallParamRule[paramIndex=%s]", paramIndex );
    }

}
