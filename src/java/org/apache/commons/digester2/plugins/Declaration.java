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

import java.io.IOException;
import java.util.Properties;
import java.util.List;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.digester2.Context;

/**
 * Represents info on how to load custom digester rules for mapping xml into
 * a class instantiated by a PluginCreateRule.
 * <p>
 * Declaration instances are created by either:
 * <ul>
 * <li> PluginCreateAction from method startParse, in the case where the action
 *  has a default plugin class.
 * <li> PluginCreateAction from method begin, in the case where the input
 *  xml just declares the plugin class on the matching tag, eg
 *  [widget plugin-class="com.acme.widget" ...]
 * <li> PluginDeclarationAction, in the case where the input xml pre-declares
 *  the plugin class, eg [plugin-declaration id="..." class=".." ..../]
 * </ul>
 * <p>
 * Once created, Declaration instances are stored within a
 * PluginDeclarationScope object. When the scope object is deleted, the
 * associated Declarations go with it.
 */

public class Declaration {

    /** The class of the object to be instantiated. */
    private Class pluginClass;

    /** The name of the class of the object to be instantiated. */
    private String pluginClassName;

    /** See {@link #setId}. */ 
    private String id;

    /**
     * Class which is responsible for dynamically loading this
     * plugin's rules on demand.
     */
    private RuleLoader ruleLoader = null;

    //---------------------- constructors ----------------------------------

    /**
     * Create an instance whose configure method will load dynamic rules
     * for a class of the specified name via the classloader returned by
     * method Context.getClassLoader.
     * <p>
     * There can be a difference between this constructor and the one which
     * takes an explicit class if there are multiple different copies of the
     * plugin class in the classpath. If that class provides a static method for
     * adding rules then this constructor will call that method on the class
     * instance returned by the context.getClassLoader, whereas the alternative
     * constructor that takes an explicit class will call the method on the
     * provided class.
     * <p>
     * See {@link #init} for more information.
     *
     * @param properties is expected to be the set of xml attributes present
     *  on the declaration. The RuleFinder classes will inspect the available
     *  properties to help them locate the custom rules associated with the
     *  plugin class. Must not be null.
     */
    public Declaration(Context context, String pluginClassName, Properties properties) 
    throws PluginException {
        this.pluginClassName = pluginClassName;

        try {
            // load the plugin class object
            this.pluginClass =
                context.getClassLoader().loadClass(pluginClassName);
        } catch(ClassNotFoundException cnfe) {
            throw new PluginException(
                "Unable to load class " + pluginClassName, cnfe);
        }

        this.ruleLoader = findLoader(context, pluginClass, properties);
    }

    /**
     * Create an instance whose configure method will load dynamic rules
     * for a class of the specified name via the specified class.
     *
     * @param properties is expected to be the set of xml attributes present
     *  on the declaration. The RuleFinder classes will inspect the available
     *  properties to help them locate the custom rules associated with the
     *  plugin class. Must not be null.
     */
    public Declaration(Context context, Class pluginClass, Properties properties) 
    throws PluginException {
        this.pluginClass = pluginClass;
        this.pluginClassName = pluginClass.getName();
        this.ruleLoader = findLoader(context, pluginClass, properties);
    }

    /**
     * Create an instance where a fully-initialised ruleLoader instance
     * is provided by the caller instead of having the PluginManager
     * "discover" an appropriate one.
     */
    public Declaration(Context context, Class pluginClass, RuleLoader ruleLoader) {
        this.pluginClass = pluginClass;
        this.pluginClassName = pluginClass.getName();
        this.ruleLoader = ruleLoader;
    }

    //---------------------- methods -----------------------------------

    /**
     * Return plugin class associated with this declaration.
     * 
     * @return The pluginClass.
     */
    public Class getPluginClass() {
        return pluginClass;
    }

    /** 
     * The id that the user associated with a particular plugin declaration
     * in the input xml. This id is later used in the input xml to refer
     * back to the original declaration.
     * <p>
     * For plugins declared "in-line", the id is null.
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Return the id associated with this declaration. For plugins
     * declared "inline", null will be returned.
     * 
     * @return The id value. May be null.
     */
    public String getId() {
        return id;
    }

    /**
     * Given a plugin class and some associated properties, scan the
     * list of known RuleFinder instances until one detects a source of
     * custom rules for this plugin (aka a RuleLoader).
     * <p>
     * If no source of custom rules can be found, null is returned.
     *
     * @param context is the current parse context.
     *
     * @param pluginClass is the class whose custom rules are to be located.
     *
     * @param props are any xml attributes included in the declaration which
     *  RuleFinder objects may wish to inspect in order to determine how to
     *  find the custom rules for the pluginClass.
     */
    private static RuleLoader findLoader(
    Context context,
    Class pluginClass, Properties props)
    throws PluginException {

        Log log = LogUtils.getLogger(context);
        boolean debug = log.isDebugEnabled();
        log.debug("scanning ruleFinders to locate loader..");

        PluginConfiguration pluginConfig =
            PluginConfiguration.getInstance(context.getSAXHandler());

        // Iterate over the list of RuleFinders, trying each one
        // until one of them locates a source of custom rules given
        // this specific plugin class and the associated declaration
        // properties.
        List ruleFinders = pluginConfig.getRuleFinders();
        RuleLoader ruleLoader = null;
        try {
            for(Iterator i = ruleFinders.iterator();
                i.hasNext() && ruleLoader == null; ) {

                RuleFinder finder = (RuleFinder) i.next();
                if (debug) {
                    log.debug("checking finder of type " + finder.getClass().getName());
                }
                ruleLoader = finder.findLoader(context, pluginClass, props);
            }
        }
        catch(PluginException e) {
            throw new PluginException(
                "Unable to locate plugin rules for plugin"
                + " with class [" + pluginClass.getName() + "]"
                + ":" + e.getMessage(), e.getCause());
        }
        log.debug("scanned ruleFinders.");

        log.debug(
            "RuleLoader of type [" + ruleLoader.getClass().getName()
            + "] associated with plugin declaration for class"
            + " [" + pluginClass.getClass().getName() + "].");

        return ruleLoader;
    }

    /**
     * Attempt to load custom rules for the plugin class into the current
     * RuleManager associated with the context.
     * <p>
     * This method is expected to be called by PluginCreateAction.
     */

    public void configure(Context context) throws PluginException {
        Log log = context.getLogger();
        boolean debug = log.isDebugEnabled();
        if (debug) {
            log.debug("configure being called!");
        }

        if (ruleLoader != null) {
            // The ruleloader can be null if findLoader returned null, ie
            // none of the registered RuleFinder objects could locate a
            // source of custom rules for the specified class. This
            // probably indicates that something is wrong: maybe we should
            // log a warning here?
            ruleLoader.addRules(context);
        }
    }
}
