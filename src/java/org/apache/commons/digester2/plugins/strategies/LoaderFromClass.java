/* $Id$
 *
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.digester2.plugins.strategies;

import java.lang.reflect.Method;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.logging.Log;

import org.apache.commons.digester2.Context;
import org.apache.commons.digester2.RuleManager;
import org.apache.commons.digester2.plugins.RuleLoader;
import org.apache.commons.digester2.plugins.PluginException;

/**
 * A RuleLoader which invokes a static method on a target class, leaving that
 * method to actually instantiate and add new rules to a Digester instance.
 */

public class LoaderFromClass extends RuleLoader {

    private Class rulesClass;
    private Method rulesMethod;

    /** Constructor. */
    public LoaderFromClass(Class rulesClass, Method rulesMethod) {
        this.rulesClass = rulesClass;
        this.rulesMethod = rulesMethod;
    }

    /** Constructor. */
    public LoaderFromClass(Class rulesClass, String methodName)
                throws PluginException {

        Method method = locateMethod(rulesClass, methodName);

        if (method == null) {
            throw new PluginException(
                "rule class " + rulesClass.getName()
                + " does not have method " + methodName
                + " or that method has an invalid signature.");
        }

        this.rulesClass = rulesClass;
        this.rulesMethod = method;
    }

    /**
     * Just invoke the target method.
     */
    public void addRules(Context context) throws PluginException {
        String path = context.getMatchPath();

        Log log = context.getLogger();
        boolean debug = log.isDebugEnabled();
        if (debug) {
            log.debug(
                "LoaderFromClass loading rules for plugin at path ["
                + path + "]");
        }

        try {
            Object[] params = {context.getRuleManager(), path};
            Object none = rulesMethod.invoke(null, params);
        } catch (Exception e) {
            throw new PluginException(
                "Unable to invoke rules method " + rulesMethod
                + " on rules class " + rulesClass, e);
        }
    }

    /**
     * Find a method on the specified class whose name matches methodName,
     * and whose signature is:
     * <code> public static void foo(RuleManager rm, String patternPrefix);</code>.
     *
     * @return null if no such method exists.
     */
    public static Method locateMethod(Class rulesClass, String methodName)
    throws PluginException {

        Class[] paramSpec = { RuleManager.class, String.class };
        Method rulesMethod = MethodUtils.getAccessibleMethod(
            rulesClass, methodName, paramSpec);

        return rulesMethod;
    }
}

