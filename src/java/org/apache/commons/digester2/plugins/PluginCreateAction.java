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

import java.util.Iterator;
import java.util.ListIterator;
import java.util.List;
import java.io.File;

import org.apache.commons.digester2.Context;
import org.apache.commons.digester2.Action;
import org.apache.commons.digester2.AbstractAction;
import org.apache.commons.digester2.RuleManager;
import org.apache.commons.digester2.DefaultRuleManager;
import org.apache.commons.digester2.ParseException;
import org.apache.commons.digester2.DigestionException;
import org.apache.commons.logging.Log;

/**
 * Allows the original rules for parsing the configuration file to define
 * points at which plugins are allowed, by configuring a PluginCreateRule
 * with the appropriate pattern.
 */
public class PluginCreateAction extends AbstractAction {

    /**
     * Simple data structure to store the names of the xml attributes
     * that the xml elements which matches this action will use to
     * tell us what class to instantiate.
     */
    private static class PluginAttrNames {
        String pluginClassAttrNS = null;
        String pluginClassAttr = null;
        String pluginIdAttrNS = null;
        String pluginIdAttr = null;
    }

    private final Context.ItemId PLUGIN_ATTR_NAMES
        = new Context.ItemId(PluginCreateAction.class, "PluginAttrNames", this);

    // See constructors. Note that these contain only values provided by the
    // user. If we default to a per-digester value or to a global default,
    // these member variables remain unaltered. This is because Actions are
    // forbidden to change any member variables during a parse.
    private String pluginClassAttrNS;
    private String pluginClassAttr;
    private String pluginIdAttrNS;
    private String pluginIdAttr;

    /** A base class that any plugin must derive from. */
    private Class baseClass = null;

    /**
     * Info about optional default plugin to be used if no plugin-id is
     * specified in the input data. This can simplify the syntax where one
     * particular plugin is usually used.
     */
    private Declaration defaultPlugin;

    //-------------------- constructors -------------------------------------

    /**
     * Create a plugin rule where the user <i>must</i> specify a plugin-class
     * or plugin-id.
     *
     * @param baseClass is the class which any specified plugin <i>must</i> be
     * descended from.
     */
    public PluginCreateAction(Class baseClass) {
        this.baseClass = baseClass;
    }

    /**
     * Create a plugin rule where the user <i>may</i> specify a plugin.
     * If the user doesn't specify a plugin, then the default class specified
     * in this constructor is used.
     *
     * @param baseClass is the class which any specified plugin <i>must</i> be
     * descended from.
     * @param dfltPluginClass is the class which will be used if the user
     * doesn't specify any plugin-class or plugin-id. This class will have
     * custom rules installed for it just like a declared plugin.
     */
    public PluginCreateAction(Class baseClass, Class dfltPluginClass) {
        this.baseClass = baseClass;
        if (dfltPluginClass != null) {
            defaultPlugin = new Declaration(dfltPluginClass);
        }
    }

    /**
     * Create a plugin rule where the user <i>may</i> specify a plugin.
     * If the user doesn't specify a plugin, then the default class specified
     * in this constructor is used.
     *
     * @param baseClass is the class which any specified plugin <i>must</i> be
     * descended from.
     * @param dfltPluginClass is the class which will be used if the user
     * doesn't specify any plugin-class or plugin-id. This class will have
     * custom rules installed for it just like a declared plugin.
     * @param dfltPluginRuleLoader is a RuleLoader instance which knows how
     * to load the custom rules associated with this default plugin.
     */
    public PluginCreateAction(Class baseClass, Class dfltPluginClass,
                    RuleLoader dfltPluginRuleLoader) {

        this.baseClass = baseClass;
        if (dfltPluginClass != null) {
            defaultPlugin =
                new Declaration(dfltPluginClass, dfltPluginRuleLoader);
        }
    }

    //------------------- properties ---------------------------------------

    /**
     * Sets the xml attribute which the input xml uses to indicate to a
     * PluginCreateRule which class should be instantiated.
     * <p>
     * See {@link PluginRules#setPluginClassAttribute} for more info.
     */
    public void setPluginClassAttribute(String namespaceUri, String attrName) {
        pluginClassAttrNS = namespaceUri;
        pluginClassAttr = attrName;
    }

    /**
     * Sets the xml attribute which the input xml uses to indicate to a
     * PluginCreateRule which plugin declaration is being referenced.
     * <p>
     * See {@link PluginRules#setPluginIdAttribute} for more info.
     */
    public void setPluginIdAttribute(String namespaceUri, String attrName) {
        pluginIdAttrNS = namespaceUri;
        pluginIdAttr = attrName;
    }

    //------------------- methods --------------------------------------------

    /**
     * Invoked before parsing begins on a document.
     * <p>
     * We ensure the default plugin class is loaded into memory, and
     * that it does indeed implement the declared base class for this
     * plugin point. We then ensure any custom rules for the default
     * plugin have been located, though we don't add them to a RuleManager
     * yet.
     *
     *
     * @exception PluginConfigurationException
     */
    public void startParse(Context context)
    throws PluginException {
        // TODO: determine whether there will be problems with using
        // wildcard patterns with a PluginCreateAction, or with adding
        // the same instance multiple times..

        Log log = LogUtils.getLogger(context);
        boolean debug = log.isDebugEnabled();
        if (debug) {
            log.debug("PluginCreateAction.beginParse");
        }

        if (baseClass == null) {
            baseClass = Object.class;
        }

        PluginDeclarationScope pds  =
            (PluginDeclarationScope) context.getItem(
                PluginDeclarationScope.PLUGIN_DECL_SCOPE);

        if (defaultPlugin != null) {
            // check default class is valid. We can't do this until the parse
            // begins, as we need to load the baseClass and plugin class via
            // the classloader associated with the context.
            if (!baseClass.isAssignableFrom(defaultPlugin.getPluginClass())) {
                throw new PluginException(
                     "Default class [" +
                     defaultPlugin.getPluginClass().getName() +
                     "] does not inherit from [" +
                     baseClass.getName() + "].");
            }

            // initialise the plugin declaration, which means a RuleLoader
            // will figure out where the custom rules for the default plugin
            // class are...
            defaultPlugin.init(context, pds);
        }

        PluginAttrNames pluginAttrNames = createPluginAttrNames(context);
        // and now we've done all that work, cache the info in the context
        // as instance-specific data.
        context.putItem(PLUGIN_ATTR_NAMES, pluginAttrNames);
    }

    /**
     * Invoked when the Digester matches this rule against an xml element.
     * <p>
     * A new instance of the target class is created, and pushed onto the
     * stack. A new "private" PluginRuleManager object is then created and
     * set as the current RuleManager object. Any custom rules associated with
     * the plugin class are then loaded into that new RuleManager object.
     * Finally, any custom rules that are associated with the current pattern
     * (such as SetPropertiesAction) have their begin methods executed.
     *
     * @param namespace
     * @param name
     * @param attributes
     *
     * @throws ClassNotFoundException
     * @throws PluginInvalidInputException
     * @throws PluginConfigurationException
     */
    public void begin(
    Context context,
    String namespace, String name,
    org.xml.sax.Attributes attributes)
    throws ParseException {
        String path = context.getMatchPath();

        Log log = context.getLogger();
        boolean debug = log.isDebugEnabled();
        if (debug) {
            log.debug("PluginCreateRule.begin" +
                  " match=[" + path + "]");
        }

        // load any custom rules associated with the plugin
        PluginDeclarationScope pds = PluginDeclarationScope.getInstance(context);

        // and get the cached info calculated at startParse time...
        PluginAttrNames pluginAttrNames = (PluginAttrNames)
            context.getItem(PLUGIN_ATTR_NAMES);
            
        Declaration currDeclaration = null;

        String pluginClassName = attributes.getValue(
            pluginAttrNames.pluginClassAttrNS,
            pluginAttrNames.pluginClassAttr);

        String pluginId = attributes.getValue(
            pluginAttrNames.pluginIdAttrNS,
            pluginAttrNames.pluginIdAttr);

        if (pluginClassName != null) {
            // The user is using a plugin "inline", ie without a previous
            // explicit declaration. If they have used the same plugin class
            // before, we have already gone to the effort of creating a
            // Declaration object, so retrieve it. If there is no existing
            // declaration object for this class, then create one.

            currDeclaration = pds.getDeclarationByClass(pluginClassName);

            if (currDeclaration == null) {
                currDeclaration = new Declaration(pluginClassName);
                try {
                    currDeclaration.init(context, pds);
                } catch(PluginException pwe) {
                    throw new PluginInvalidInputException(
                        pwe.getMessage(), pwe.getCause());
                }
                pds.addDeclaration(currDeclaration);
            }
        } else if (pluginId != null) {
            currDeclaration = pds.getDeclarationById(pluginId);

            if (currDeclaration == null) {
                throw new PluginInvalidInputException(
                    "Plugin id [" + pluginId + "] is not defined.");
            }
        } else if (defaultPlugin != null) {
            currDeclaration = defaultPlugin;
        } else {
            throw new PluginInvalidInputException(
                "No plugin class specified for element " + path);
        }

        // get the class of the user plugged-in type
        Class pluginClass = currDeclaration.getPluginClass();

        // create a new RuleManager object and effectively push it onto a
        // stack of rules objects. The stack is actually a linked list;
        // using the PluginRuleManager constructor below causes the new
        // instance to link to the previous head-of-stack, then calling
        // context.setRules() makes the new instance the new head-of-stack.
        RuleManager oldRuleManager = context.getRuleManager();
        RuleManager delegateRuleManager = new DefaultRuleManager();
        PluginRuleManager newRuleManager
            = new PluginRuleManager(oldRuleManager, delegateRuleManager, path);
        context.setRuleManager(newRuleManager);

        if (debug) {
            log.debug("PluginCreateRule.begin: installing new plugin: " +
                "oldrules=" + oldRuleManager.toString() +
                ", newrules=" + newRuleManager.toString());
        }

        // load up the custom rules
        currDeclaration.configure(context);

        // create an instance of the plugin class
        try {
            Object instance = pluginClass.newInstance();
            context.push(instance);
        } catch(InstantiationException ex) {
            throw new ParseException(
                "Unable to instantiate class [" + pluginClass.getName() + "]",
                ex);
        } catch(IllegalAccessException ex) {
            throw new ParseException(
                "Not permitted to instantiate class [" + pluginClass.getName() + "]",
                ex);
        }
        
        if (debug) {
            log.debug(
                "PluginCreateAction.begin" +
                " match=[" + context.getMatchPath() + "]" +
                " pushed instance of plugin [" + pluginClass.getName() + "]");
        }

        // and now we have to fire any custom rules which would have
        // been matched by the same path that matched this rule, had
        // they been loaded at that time.
        List actions;
        try {
            actions = newRuleManager.getMatchingActions(path);
        } catch(DigestionException ex) {
            throw new ParseException(
                "Unable to get matching actions from class " 
                + this.getClass().getName(),
                ex);
        }
        fireBeginMethods(context, actions, namespace, name, attributes);
    }

    /**
     * Process the body text of this element.
     *
     * @param text The body text of this element
     */
    public void body(Context context, String namespace, String name, String text)
    throws ParseException {

        // While this class itself has no work to do in the body method,
        // we do need to fire the body methods of all dynamically-added
        // rules matching the same path as this rule. During begin, we had
        // to manually execute the dynamic rules' begin methods because they
        // didn't exist in the digester's Rules object when the match begin.
        // So in order to ensure consistent ordering of rule execution, the
        // PluginRules class deliberately avoids returning any such rules
        // in later calls to the match method, instead relying on this
        // object to execute them at the appropriate time.
        //
        // Note that this applies only to rules matching exactly the path
        // which is also matched by this PluginCreateRule.

        String path = context.getMatchPath();
        RuleManager newRuleManager = context.getRuleManager();
        List actions;
        try {
            actions = newRuleManager.getMatchingActions(context.getMatchPath());
        } catch(DigestionException ex) {
            throw new ParseException(
                "Unable to get matching actions from class " 
                + this.getClass().getName(),
                ex);
        }
        fireBodyMethods(context, actions, namespace, name, text);
    }

    /**
     * Invoked by the digester when the closing tag matching this Rule's
     * pattern is encountered.
     *
     * @param namespace Description of the Parameter
     * @param name Description of the Parameter
     * @exception Exception Description of the Exception
     *
     * @see #begin
     */
    public void end(Context context, String namespace, String name)
    throws ParseException {

        // see body method for more info
        String path = context.getMatchPath();
        PluginRuleManager newRuleManager
            = (PluginRuleManager) context.getRuleManager();

        List actions;
        try {
            actions = newRuleManager.getMatchingActions(context.getMatchPath());
        } catch(DigestionException ex) {
            throw new ParseException(
                "Unable to get matching actions from class " 
                + this.getClass().getName(),
                ex);
        }
        fireEndMethods(context, actions, namespace, name);

        // pop the stack of PluginRuleManager instances, which
        // discards all custom rules associated with this plugin
        context.setRuleManager(newRuleManager.getParent());

        // and get rid of the instance of the plugin class from the
        // digester object stack.
        context.pop();
    }

    /**
     * Duplicate the processing that the Digester does when firing the
     * begin methods of rules. It would be really nice if the Digester
     * class provided a way for this functionality to just be invoked
     * directly.
     */
    public void fireBeginMethods(
    Context context,
    List actions,
    String namespace, String name,
    org.xml.sax.Attributes list)
    throws ParseException {
        Log log = context.getLogger();
        boolean debug = log.isDebugEnabled();
        for (int i = 0; i < actions.size(); i++) {
            try {
                Action action = (Action) actions.get(i);
                if (debug) {
                    log.debug("  Fire begin() for " + action);
                }
                action.begin(context, namespace, name, list);
            } catch (PluginException e) {
                throw e;
            }
        }
    }

    /**
     * Duplicate the processing that the Digester does when firing the
     * body methods of rules. It would be really nice if the Digester
     * class provided a way for this functionality to just be invoked
     * directly.
     */
    private void fireBodyMethods(
    Context context,
    List actions,
    String namespaceURI, String name,
    String text) throws ParseException {

        if (actions.size() > 0) {
            Log log = context.getLogger();
            boolean debug = log.isDebugEnabled();
            for (int i = 0; i < actions.size(); i++) {
                try {
                    Action action = (Action) actions.get(i);
                    if (debug) {
                        log.debug("  Fire body() for " + action);
                    }
                    action.body(context, namespaceURI, name, text);
                } catch (ParseException e) {
                    throw e;
                }
            }
        }
    }

    /**
     * Duplicate the processing that the Digester does when firing the
     * end methods of rules. It would be really nice if the Digester
     * class provided a way for this functionality to just be invoked
     * directly.
     */
    public void fireEndMethods(
    Context context,
    List actions,
    String namespaceURI, String name)
    throws ParseException {

        // Fire "end" events for all relevant rules in reverse order
        Log log = context.getLogger();
        boolean debug = log.isDebugEnabled();
        for (int i = 0; i < actions.size(); i++) {
            int j = (actions.size() - i) - 1;
            try {
                Action action = (Action) actions.get(j);
                if (debug) {
                    log.debug("  Fire end() for " + action);
                }
                action.end(context, namespaceURI, name);
            } catch (ParseException e) {
                throw e;
            }
        }
    }

    /**
     * Determine what xml attributews are expected to be present on the matched
     * xml element in order to tell us which plugin class to load.
     */
    private PluginAttrNames createPluginAttrNames(Context context) {
        Log log = context.getLogger();
        boolean debug = log.isDebugEnabled();

        PluginConfiguration pc = PluginConfiguration.getInstance(context);

        PluginAttrNames pluginAttrNames = new PluginAttrNames();

        if (pluginClassAttr ==  null) {
            // the user hasn't set explicit xml attr names on this action,
            // so fetch the per-saxhandler default values
            pluginAttrNames.pluginClassAttrNS = pc.getPluginClassAttrNS();
            if (pluginAttrNames.pluginClassAttrNS == null) {
                pluginAttrNames.pluginClassAttrNS = pc.DFLT_PLUGIN_CLASS_ATTR_NS;
            }

            pluginAttrNames.pluginClassAttr = pc.getPluginClassAttr();
            if (pluginAttrNames.pluginClassAttr == null) {
                pluginAttrNames.pluginClassAttr = pc.DFLT_PLUGIN_CLASS_ATTR;
            }

            if (debug) {
                log.debug(
                    "init: pluginClassAttr set to values ["
                    + "ns=" + pluginAttrNames.pluginClassAttrNS
                    + ", name=" + pluginAttrNames.pluginClassAttr + "]");
            }
        } else {
            // ok, we use the values specified in setPluginClassAttribute method.
            pluginAttrNames.pluginClassAttrNS = pluginClassAttrNS;
            pluginAttrNames.pluginClassAttr = pluginClassAttr;
            if (debug) {
                log.debug(
                    "init: pluginClassAttr set to action-specific values ["
                    + "ns=" + pluginClassAttrNS
                    + ", name=" + pluginClassAttr + "]");
            }
        }

        // what xml attributes are expected to be present on the matched
        // xml element in order to tell us which preceding plugin declaration
        // to use?
        if (pluginIdAttr ==  null) {
            // the user hasn't set explicit xml attr names on this rule,
            // so fetch the default values
            pluginAttrNames.pluginIdAttrNS = pc.getPluginIdAttrNS();
            if (pluginAttrNames.pluginIdAttrNS == null) {
                pluginAttrNames.pluginIdAttrNS = pc.DFLT_PLUGIN_ID_ATTR_NS;
            }

            pluginAttrNames.pluginIdAttr = pc.getPluginIdAttr();
            if (pluginAttrNames.pluginIdAttr == null) {
                pluginAttrNames.pluginIdAttr = pc.DFLT_PLUGIN_ID_ATTR;
            }

            if (debug) {
                log.debug(
                    "init: pluginIdAttr set to values ["
                    + "ns=" + pluginAttrNames.pluginIdAttrNS
                    + ", name=" + pluginAttrNames.pluginIdAttr + "]");
            }
        } else {
            // ok, we use the values specified in setPluginIdAttribute method.
            pluginAttrNames.pluginIdAttrNS = pluginIdAttrNS;
            pluginAttrNames.pluginIdAttr = pluginIdAttr;
            if (debug) {
                log.debug(
                    "init: pluginIdAttr set to rule-specific values ["
                    + "ns=" + pluginIdAttrNS
                    + ", name=" + pluginIdAttr + "]");
            }
        }

        return pluginAttrNames;
    }
}
