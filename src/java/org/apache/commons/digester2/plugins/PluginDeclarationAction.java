/* $Id$
 *
 * Copyright 2003-2005 The Apache Software Foundation.
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

package org.apache.commons.digester2.plugins;

import java.util.Properties;

import org.apache.commons.digester2.Action;
import org.apache.commons.digester2.AbstractAction;
import org.apache.commons.digester2.Context;
import org.apache.commons.digester2.ParseException;
import org.apache.commons.beanutils.MethodUtils;

import org.apache.commons.logging.Log;

/**
 * A digester Action which allows the user to pre-declare a class which is to
 * be referenced later at a plugin point by a PluginCreateAction.
 * <p>
 * Normally, a PluginDeclarationAction is added to a Digester instance with
 * the pattern "/{root}/plugin" or "plugin" where {root} is the name of 
 * the root tag in the input document.
 */

public class PluginDeclarationAction extends AbstractAction {

    //------------------- constructors ---------------------------------------

    /** constructor  */
    public PluginDeclarationAction() {
    }

    //------------------- methods --------------------------------------------

    /**
     * Invoked upon reading a tag defining a plugin declaration. The tag
     * must have the following mandatory attributes:
     * <ul>
     *   <li> id </li>
     *   <li> class </li>
     * </ul>
     *
     *@param namespace The xml namespace in which the xml element which
     * triggered this rule resides.
     *@param name The name of the xml element which triggered this rule.
     *@param attributes The set of attributes on the xml element which
     * triggered this rule.
     *@exception java.lang.Exception
     */

    public void begin(
    Context context, 
    String namespace, String name,
    org.xml.sax.Attributes attributes)
    throws ParseException {

        // copy all the attribute values into a properties object so that
        // the plugin finder strategies can access the properties later.
        int nAttrs = attributes.getLength();
        Properties props = new Properties();
        for(int i=0; i<nAttrs; ++i) {
            String key = attributes.getLocalName(i);
            if ((key == null) || (key.length() == 0)) {
                key = attributes.getQName(i);
            }
            String value = attributes.getValue(i);
            props.setProperty(key, value);
        }
        
        try {
            declarePlugin(context, props);
        } catch(PluginInvalidInputException ex) {
            throw new PluginInvalidInputException(
                "Error on element [" + context.getMatchPath() + 
                "]: " + ex.getMessage());
        }
    }
    
    /**
     * Creates a Declaration object to represent this implicit or explicit
     * declaration of a plugin, and store it away in the PluginManager object
     * associated with the current context so it can be retrieved later when
     * a PluginCreateAction fires.
     * <p>
     * Note that this (static) method is called directly from the
     * PluginCreateAction class if an "inline declaration" is found, ie where
     * the xml element that triggers the PluginCreateAction also provides the
     * necessary declaration information.
     */
    public static void declarePlugin(Context context, Properties props)
    throws PluginException {
        
        Log log = context.getLogger();
        boolean debug = log.isDebugEnabled();
        
        String id = props.getProperty("id");
        String pluginClassName = props.getProperty("class");
        
        if (id == null) {
            throw new PluginInvalidInputException(
                "mandatory attribute id not present on plugin declaration");
        }

        if (pluginClassName == null) {
            throw new PluginInvalidInputException(
                "mandatory attribute class not present on plugin declaration");
        }

        Declaration newDecl = new Declaration(context, pluginClassName, props);
        newDecl.setId(id);

        PluginDeclarationScope pds = PluginDeclarationScope.getInstance(context); 
        pds.addDeclaration(newDecl);
    }
}

