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

import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Iterator;

import org.apache.commons.digester2.Context;

import org.apache.commons.logging.Log;

/**
 * Provides a location to store plugin declarations.
 * <p>
 * Plugin declarations are created by either:
 * <ul>
 * <li> PluginCreateAction from method startParse time, in the case where a 
 *  plugin has a default plugin class.
 * <li> PluginCreateAction from method begin, in the case where the input
 *  xml just declares the plugin class on the matching tag, eg
 *  [widget plugin-class="com.acme.widget" ...]
 * <li> PluginDeclarationAction, in the case where the input xml pre-declares
 *  the plugin class, eg [plugin-declaration id="..." class=".." ..../]
 * </ul>
 * <p>
 * When plugin declarations are encountered in the input xml, they
 * should "shadow" (temporarily override) declarations made within
 * parent elements. And when the element within which the declaration
 * occurred ends, all information about those declarations should be
 * discarded, causing:
 * <ol>
 * <li>any references to those declarations after they have gone out
 *  of scope to be errors, and
 * <li>any declarations previously "shadowed" to be "unshadowed"
 * </ol>
 * <p>
 * This is implemented by having a stack of instances of this class. When
 * declarations occur in a new scope, a new instance of this class is pushed
 * onto the corresponding stack. Lookups for declarations are always performed
 * on the top object on the stack, so the most "recent" declarations are seen.
 * If a declaration is not found on the top object, it delegates the lookup to
 * the previous object, etc. And when a declaration scope ends, the stack is
 * popped, thereby discarding the out-of-scope plugin declarations.
 * <p>
 * Note that there is a problem with the current implementation of declaration
 * scoping. The problem is not in this class, but in the code which decides 
 * when to push and pop new instances of this class. Simply, declaration scopes
 * should last until the end of the parent xml element containing the 
 * declaration (not the end of the declaration element itself) - but Digester 
 * doesn't provide any hooks to allow detection of the end of the parent 
 * element. The current solution, therefore, is to regard a scope as starting 
 * when a PluginCreateAction fires. This does at least separate the scope of 
 * declarations within a plugin from those "above" the plugin point which is 
 * the most important issue.
 * <p>
 * At some future time, if digester provides the facility to perform actions 
 * associated with "the parent tag", then the PluginDeclarationScope stack
 * can be decoupled from the firing of PluginCreateRule instances. 
 */

public class PluginDeclarationScope {

    public static final Context.ItemId PLUGIN_DECL_SCOPE
        = new Context.ItemId(PluginDeclarationScope.class, "PluginDecls");
        
    /** Map of classname->Declaration */
    private HashMap declarationsByClass = new HashMap();

    /** Map of id->Declaration  */
    private HashMap declarationsById = new HashMap();

    /** the parent manager to which this one may delegate lookups. */
    private PluginDeclarationScope parent;
    
    // ---------------------------------------------------------------------
    // Static Methods
    // ---------------------------------------------------------------------

    /**
     * Extract the current (top) PluginDeclarationScope object from the 
     * provided context. If one hasn't been created yet, then do so.
     * <p>
     * This is similar to a singleton method, except that there is a
     * PluginDeclarationScope per context.
     * <p>
     * Note that instead of using a "scratch stack" in the context, we
     * just use a "scratch item" which returns the most-recent
     * PluginDeclarationScope. As each of these objects contains a reference
     * to its "parent" scope, this is effectively a linked-list-stack.
     */
    public static PluginDeclarationScope getInstance(Context context) {
        PluginDeclarationScope pds = 
            (PluginDeclarationScope) context.getItem(PLUGIN_DECL_SCOPE);
        if (pds == null) {
            pds = new PluginDeclarationScope();
            context.putItem(PLUGIN_DECL_SCOPE, pds);
        }

        return pds;
    }
    
    //------------------- constructors ---------------------------------------

    /** Construct a "root" PluginDeclarationScope, ie one with no parent. */
    public PluginDeclarationScope() {
    }

    /** 
     * Construct a "child" PluginDeclarationScope. When declarations are added 
     * to a "child", they are stored within the child and do not modify the
     * parent, so when the child goes out of scope, those declarations
     * disappear. When asking a "child" to retrieve a declaration, it 
     * delegates the search to its parent if it does not hold a matching
     * entry itself.
     * <p>
     * @param parent must be non-null.
     */
    public PluginDeclarationScope(PluginDeclarationScope parent) {
        this.parent = parent;
    }
    
    //------------------- methods --------------------------------------------

    /**
     * Add the declaration to the set of known declarations.
     * <p>
     * TODO: somehow get a reference to a Digester object
     * so that we can really log here. Currently, all
     * logging is disabled from this method.
     *
     * @param decl an object representing a plugin class.
     */
    public void addDeclaration(Declaration decl) {
        Log log = LogUtils.getLogger(null);
        boolean debug = log.isDebugEnabled();
        
        Class pluginClass = decl.getPluginClass();
        String id = decl.getId();
        
        declarationsByClass.put(pluginClass.getName(), decl);
            
        if (id != null) {
            declarationsById.put(id, decl);
            if (debug) {
                log.debug(
                    "Indexing plugin-id [" + id + "]" +
                    " -> class [" + pluginClass.getName() + "]");
            }
        }
    }

    /**
     * Return the declaration object with the specified class.
     * If no such plugin is known, null is returned.
     */
    public Declaration getDeclarationByClass(String className) {
        Declaration decl = 
            (Declaration) declarationsByClass.get(className);
            
        if ((decl == null) && (parent != null)) {
            decl = parent.getDeclarationByClass(className);
        }

        return decl;
    }

    /**
     * Return the declaration object with the specified id.
     * If no such plugin is known, null is returned.
     *
     *@param id Description of the Parameter
     *@return The declaration value
     */
    public Declaration getDeclarationById(String id) {
        Declaration decl = (Declaration) declarationsById.get(id);

        if ((decl == null) && (parent != null)) {
            decl = parent.getDeclarationById(id);
        }

        return decl;
    }
}
