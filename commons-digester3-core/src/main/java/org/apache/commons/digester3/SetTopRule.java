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

/**
 * <p>
 * Rule implementation that calls a "set parent" method on the top (child) object, passing the (top-1) (parent) object
 * as an argument.
 * </p>
 * <p>
 * This rule now supports more flexible method matching by default. It is possible that this may break (some) code
 * written against release 1.1.1 or earlier. See {@link #isExactMatch()} for more details.
 * </p>
 */
public class SetTopRule
    extends AbstractMethodRule
{

    /**
     * Constructs a "set top" rule with the specified method name. The method's argument type is assumed to be the class
     * of the child object.
     *
     * @param methodName Method name of the parent method to call
     */
    public SetTopRule( final String methodName )
    {
        super( methodName );
    }

    /**
     * Constructs a "set top" rule with the specified method name.
     *
     * @param methodName Method name of the parent method to call
     * @param paramType Java class of the parent method's argument (if you wish to use a primitive type, specify the
     *            corresponding Java wrapper class instead, such as {@link Boolean} for a
     *            {@code boolean} parameter)
     */
    public SetTopRule( final String methodName, final Class<?> paramType )
    {
        super( methodName, paramType );
    }

    /**
     * Constructs a "set top" rule with the specified method name.
     *
     * @param methodName Method name of the parent method to call
     * @param paramType Java class name of the parent method's argument (if you wish to use a primitive type,
     *                  specify the corresponding Java wrapper class instead, such as {@link Boolean}
     *                  for a {@code boolean} parameter)
     */
    public SetTopRule( final String methodName, final String paramType )
    {
        super( methodName, paramType );
    }

    @Override
    protected Object getChild()
    {
        return getDigester().peek( 1 );
    }

    @Override
    protected Object getParent()
    {
        return getDigester().peek( 0 );
    }

}
