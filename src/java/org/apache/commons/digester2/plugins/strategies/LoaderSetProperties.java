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

import org.apache.commons.logging.Log;

import org.apache.commons.digester2.Context;
import org.apache.commons.digester2.InvalidRuleException;
import org.apache.commons.digester2.actions.SetPropertiesAction;
import org.apache.commons.digester2.plugins.RuleLoader;
import org.apache.commons.digester2.plugins.PluginException;

/**
 * A RuleLoader which creates a single SetPropertiesRule and adds it to the
 * digester when its addRules() method is invoked.
 * <p>
 * This loader ensures that any xml attributes on the plugin tag get
 * mapped to equivalent properties on a javabean. This allows JavaBean
 * classes to be used as plugins without any requirement to create custom
 * plugin rules.
 */

public class LoaderSetProperties extends RuleLoader {
    
    /**
     * Just add a SetPropertiesRule at the specified path.
     */
    public void addRules(Context context) 
    throws PluginException {
        String path = context.getMatchPath();

        Log log = context.getLogger();
        boolean debug = log.isDebugEnabled();
        if (debug) {
            log.debug(
                "LoaderSetProperties loading rules for plugin at path [" 
                + path + "]");
        }

        try {
            context.getRuleManager().addRule(null, new SetPropertiesAction());
        } catch(InvalidRuleException ex) {
            throw new PluginException(ex);
        }
    }
}

