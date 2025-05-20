package org.apache.commons.digester3.plugins.strategies;

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

import static org.apache.commons.beanutils.MethodUtils.getAccessibleMethod;

import java.lang.reflect.Method;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.plugins.PluginException;
import org.apache.commons.digester3.plugins.RuleLoader;
import org.apache.commons.logging.Log;

/**
 * A RuleLoader which invokes a static method on a target class, leaving that method to actually instantiate and add new
 * rules to a Digester instance.
 *
 * @since 1.6
 */
public class LoaderFromClass
    extends RuleLoader
{

    /**
     * Find a method on the specified class whose name matches methodName, and whose signature is:
     * {@code  public static void foo(Digester d, String patternPrefix);}.
     *
     * @param rulesClass The target class
     * @param methodName The method name has to be invoked
     * @return The method name has to be invoked, or null if no such method exists.
     * @throws PluginException if any error occurs while discovering the method
     */
    public static Method locateMethod( final Class<?> rulesClass, final String methodName )
        throws PluginException
    {
        final Class<?>[] paramSpec = { Digester.class, String.class };
        return getAccessibleMethod( rulesClass, methodName, paramSpec );
    }

    private final Class<?> rulesClass;

    private final Method rulesMethod;

    /**
     * Constructs a new instance.
     *
     * @param rulesClass The target class
     * @param rulesMethod The method has to be invoked
     */
    public LoaderFromClass( final Class<?> rulesClass, final Method rulesMethod )
    {
        this.rulesClass = rulesClass;
        this.rulesMethod = rulesMethod;
    }

    /**
     * Constructs a new instance.
     *
     * @param rulesClass The target class
     * @param methodName The method name has to be invoked
     * @throws PluginException if input method can't be located inside the given class
     */
    public LoaderFromClass( final Class<?> rulesClass, final String methodName )
        throws PluginException
    {

        final Method method = locateMethod( rulesClass, methodName );

        if ( method == null )
        {
            throw new PluginException( "rule class " + rulesClass.getName() + " does not have method " + methodName
                + " or that method has an invalid signature." );
        }

        this.rulesClass = rulesClass;
        this.rulesMethod = method;
    }

    @Override
    public void addRules( final Digester d, final String path )
        throws PluginException
    {
        final Log log = d.getLogger();
        final boolean debug = log.isDebugEnabled();
        if ( debug )
        {
            log.debug( "LoaderFromClass loading rules for plugin at path [" + path + "]" );
        }

        try
        {
            final Object[] params = { d, path };
            rulesMethod.invoke( null, params );
        }
        catch ( final Exception e )
        {
            throw new PluginException(
                                       "Unable to invoke rules method " + rulesMethod + " on rules class " + rulesClass,
                                       e );
        }
    }

}
