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

import java.io.InputStream;
import java.util.Properties;
import java.lang.reflect.Method;

import org.apache.commons.digester2.Context;
import org.apache.commons.digester2.plugins.RuleFinder;
import org.apache.commons.digester2.plugins.RuleLoader;
import org.apache.commons.digester2.plugins.PluginException;

/**
 * A rule-finding algorithm which looks for a method with a specific name
 * on the plugin class.
 */

public class FinderFromDfltMethod extends RuleFinder {
    public static String DFLT_METHOD_NAME = "addRules";

    private String methodName;
    
    /** See {@link #findLoader}. */
    public FinderFromDfltMethod() { 
        this(DFLT_METHOD_NAME);
    }

    /**
     * Create a rule-finder which invokes a specific method on the plugin
     * class whenever dynamic rules for a plugin need to be loaded. See the 
     * findRules method for more info.
     *
     * @param methodName must be non-null.
     */
    public FinderFromDfltMethod(String methodName) { 
        this.methodName = methodName;
    }
    
    /**
     * If there exists on the plugin class a method with name matching the 
     * constructor's methodName value then locate the appropriate Method on 
     * the plugin class and return an object encapsulating that info.
     * <p>
     * If there is no matching method then just return null.
     * <p>
     * The returned object (when non-null) will invoke the target method
     * on the plugin class whenever its addRules method is invoked. The
     * target method is expected to have the following prototype:
     * <code> public static void xxxxx(RuleManager rm, String patternPrefix); </code>
     */
    public RuleLoader findLoader(
    Context context, Class pluginClass, Properties p)
    throws PluginException {

        Method rulesMethod = LoaderFromClass.locateMethod(pluginClass, methodName);
        if (rulesMethod == null) {
            return null;
        }
        
        return new LoaderFromClass(pluginClass, rulesMethod);
    }
}

