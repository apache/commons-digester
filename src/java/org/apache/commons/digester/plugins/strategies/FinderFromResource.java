/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
 
package org.apache.commons.digester.plugins.strategies;

import java.util.Properties;
import java.io.InputStream;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.plugins.RuleFinder;
import org.apache.commons.digester.plugins.RuleLoader;
import org.apache.commons.digester.plugins.PluginException;

/**
 * A rule-finding algorithm which expects the user to specify a resource
 * name (ie a file in the classpath). The file is expected to contain Digester
 * rules in xmlrules format.
 */

public class FinderFromResource  implements RuleFinder {
    /** 
     * Name of xml attribute on the plugin declaration which is used
     * to configure rule-loading for that declaration. 
     */
    public static String DFLT_RESOURCE_ATTR = "resource";
    
    /** See {@link #findLoader}. */
    private String resourceAttr;
    
    /** Constructor. */
    public FinderFromResource() {
        this(DFLT_RESOURCE_ATTR);
    }

    /** See {@link #findLoader}. */
    public FinderFromResource(String resourceAttr) { 
        this.resourceAttr = resourceAttr;
    }
    
    /**
     * If there exists a property with the name matching constructor param
     * resourceAttr, then load that file, run it through the xmlrules
     * module and return an object encapsulating those rules.
     * <p>
     * If there is no matching property provided, then just return null.
     * <p>
     * The returned object (when non-null) will add the selected rules to
     * the digester whenever its addRules method is invoked.
     */
    public RuleLoader findLoader(Digester d, Class pluginClass, Properties p)
                        throws PluginException {

        String ruleResource = p.getProperty(resourceAttr);
        if (ruleResource == null) {
            // nope, user hasn't requested dynamic rules to be loaded
            // from a specific file.
            return null;
        }
        
        InputStream is = 
            pluginClass.getClassLoader().getResourceAsStream(
                ruleResource);
        if (is == null) {
            throw new PluginException(
                "Resource " + ruleResource + " not found.");
        }
        
        // the rest is not yet implemented
        throw new PluginException(
            "FinderFromResource not implemented.");
            
        /*
        RuleLoader loader = new LoaderFromStream(is);
        is.close();
        return loader;
        */
    }
}

