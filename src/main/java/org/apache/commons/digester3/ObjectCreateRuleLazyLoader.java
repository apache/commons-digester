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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.apache.commons.beanutils.ConvertUtils.convert;

import java.lang.reflect.Constructor;

import net.sf.cglib.proxy.LazyLoader;

final class ObjectCreateRuleLazyLoader
    implements LazyLoader
{

    private final Constructor<?> constructor;

    private final Class<?>[] paramTypes;

    private final Object[] parameters;

    public ObjectCreateRuleLazyLoader( Constructor<?> constructor,
                                  Class<?>[] paramTypes,
                                  Object[] parameters )
    {
        this.constructor = constructor;
        this.paramTypes = paramTypes;
        this.parameters = parameters;
    }

    /**
     * {@inheritDoc}
     */
    public Object loadObject()
        throws Exception
    {
        // this piece of code is borrowed from CallMethodRule
        Object[] paramValues = new Object[paramTypes.length];
        for ( int i = 0; i < paramTypes.length; i++ )
        {
            // convert nulls and convert stringy parameters
            // for non-stringy param types
            if ( parameters[i] == null
                || ( parameters[i] instanceof String && !String.class.isAssignableFrom( paramTypes[i] ) ) )
            {
                paramValues[i] = convert( (String) parameters[i], paramTypes[i] );
            }
            else
            {
                paramValues[i] = parameters[i];
            }
        }

        return constructor.newInstance( paramValues );
    }

}
