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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Comparator;

import org.apache.commons.digester2.Context;
import org.apache.commons.digester2.Action;
import org.apache.commons.digester2.RuleManager;
import org.apache.commons.digester2.AbstractRuleManager;
import org.apache.commons.digester2.DigestionException;
import org.apache.commons.digester2.InvalidRuleException;
import org.apache.commons.logging.Log;

/**
 * A custom digester RuleManager which contains its own rules, but can also
 * return rules from a "parent" RuleManager.
 * <p>
 * Plugged-in classes have custom parsing rules associated with them. These
 * rules need to be added to the existing set of rules during the scope of
 * the plugged-in class, and then discarded when the plugged-in class goes
 * out of scope.
 * <p>
 * In order to implement this behaviour, the following occurs when a 
 * PluginCreateAction's begin method fires:
 * <ul>
 *  <li>An instance of this class is created
 *  <li>Any custom rules associated with the plugged-in class are added to the
 *    new instance PluginRuleManager instance
 *  <li>The new PluginRuleManager instance is made the "current" RuleManager.
 * </li>
 * <p>
 * As the SAXHandler continues parsing of the xml within the plugged-in class,
 * any calls it makes to the current RuleManager cause the custom rules to be
 * returned.
 * <p>
 * When the PluginCreateAction's end method fires, the previous RuleManager
 * is restored, effectively discarding those custom rules that are no longer
 * relevant.
 * <p>
 * Rather than implement RuleManager functionality directly, this class
 * holds a reference to a "real" rulemanager, and just forwards any calls
 * to that delegate. This avoids having to implement full RuleManager
 * functionality here. More importantly, it allows an instance of this
 * class to support various RuleManager matching behaviours, simply by having
 * the delegate rulemanager be of the appropriate class.
 */

public class PluginRuleManager extends AbstractRuleManager {
                                               
    /** 
     * The rulemanager implementation that holds rules defined before the
     * PluginRuleManager was created. This object might be a PluginRuleManager
     * itself, in which case it has a parentRuleManager, etc. Eventually the
     * chain will lead back to the original rule manager associated with the
     * SAXHandler.
     */
    private RuleManager parentRuleManager;
    
    /** 
     * The rulemanager implementation that we are using to avoid having
     * to actually implement the logic of a RuleManager here. This is never a
     * PluginRuleManager, but rather an instance that implements whatever
     * matching behaviour the plugged-in class wants for its custom rules.
     */
    private RuleManager delegateRuleManager;
    
    /**
     * The path below which this rulemanager object has responsibility.
     * For paths shorter than or equal the mountpoint, the parent's 
     * match is called.
     */
    private String mountPoint;
    
    // ------------------------------------------------------------- 
    // Constructor
    // ------------------------------------------------------------- 
    
    /**
     * Constructs an instance with the specified parent RuleManager.
     * <p>
     * One of these is created each time a PluginCreateAction's begin method 
     * fires, in order to manage the custom rules associated with whatever 
     * concrete plugin class the user has specified.
     *
     * @param parent must be non-null, and is expected to be the value returned
     *  by context.getRuleManager().
     *
     * @param delegate must be non-null, and is expected to be some new object
     *  which implements RuleManager, eg a DefaultRuleManager.
     *  
     * @param mountPoint is the digester match path for the element 
     * matching a PluginCreateRule which caused this "nested parsing scope"
     * to begin. This is expected to be equal to context.getMatchPath().
     */
     PluginRuleManager(
     RuleManager parent, 
     RuleManager delegate,
     String mountPoint)
     throws PluginException {
        this.parentRuleManager = parent;
        this.delegateRuleManager = delegate;
        this.mountPoint = mountPoint;
    }
    
    // --------------------------------------------------------- 
    // Properties
    // --------------------------------------------------------- 

    /**
     * Return the rulemanager that was current before this one took over.
     */
    public RuleManager getParent() {
        return parentRuleManager;
    }
    
    // --------------------------------------------------------- 
    // AbstractRuleManager methods
    // --------------------------------------------------------- 

    /**
     * Always throws UnsupportedOperationException; there should never be
     * any reason to copy one of these objects.
     */
     public RuleManager copy() {
         throw new UnsupportedOperationException(
            "PluginRuleManager.copy is not supported.");
     }

    /**
     * Invokes the startParse method on any rules that have been added
     * specifically to this RuleManager.
     * <p>
     * This method should be called after custom rules have been added to
     * this object, so that the actions get the expected lifecycle called.
     */
    public void startParse(Context context) throws DigestionException {
        delegateRuleManager.startParse(context);

        // We deliberately don't call parent.startParse, as the actions within
        // that rulemanager should already have had their startParse methods
        // invoked.
    }

    /**
     * Invokes the finishParse method on any rules that have been added
     * specifically to this RuleManager.
     * <p>
     * This method should be called just before this instance is discarded,
     * so that the actions get the expected lifecycle called. The endParse
     * method is <i>not</i> invoked on the parent RuleManager, as the actions
     * stored within it are still in use; their endParse methods will be
     * called later.
     */
    public void finishParse(Context context) throws DigestionException {
        delegateRuleManager.finishParse(context);

        // We deliberately don't call parent.startParse, as the actions within
        // that rulemanager should already have had their startParse methods
        // invoked.
     }

    /** See {@link RuleManager#addFallbackAction}. */
    public void addFallbackAction(Action action) {
        delegateRuleManager.addFallbackAction(action);
    }

    /** See {@link RuleManager#addFallbackActions}. */
    public void addFallbackActions(List actions) {
        delegateRuleManager.addFallbackActions(actions);
    }

    /** See {@link RuleManager#addMandatoryAction}. */
    public void addMandatoryAction(Action action) {
        delegateRuleManager.addMandatoryAction(action);
    }

    /** See {@link RuleManager#addMandatoryActions}. */
    public void addMandatoryActions(List actions) {
       delegateRuleManager.addMandatoryActions(actions);
    }

    /** See {@link RuleManager#addNamespace}. */
    public void addNamespace(String prefix, String uri) {
       delegateRuleManager.addNamespace(prefix, uri);
    }

    /**
     * Add a custom rule.
     * <p>
     * Note that this does hard-wire an assumption that the concrete
     * RuleManager this instance is delegating to accepts the "canonical path"
     * as a valid pattern prefix, and treats paths starting with a leading
     * slash as absolute. 
     *
     * See {@link RuleManager#addRule}.
     */
    public void addRule(String pattern, Action action)
    throws InvalidRuleException {
        delegateRuleManager.addRule(pattern, action);
    }

    /**
     * This method always throws an exception. It is not obvious whether this
     * method should return just the actions registered with it, or include
     * the actions registered with the parent rulemanager too. And in any case
     * it is not expected that this method will ever need to be invoked on a
     * instance of this class, so it's safer to disallow this operation.
     */
    public List getActions() {
        throw new UnsupportedOperationException(
            "PluginRuleManager.getActions is not supported");
    }

    /**
     * If the path specified is below the mount-point for this rulemanager,
     * then only actions that were added to this rulemanager are returned,
     * as the custom rules for a plugged-in class is expected to be 
     * "stand-alone".
     * <p>
     * In other words, once a plugin has been entered, only custom rules
     * associated with that action are returned, even if the parent rulemanager
     * has some matching rules. This includes fallback and mandatory actions.
     * <p>
     *
     * @param path is a string of form
     * <pre>/{namespace}elementName/{namespace}elementName"</pre>
     * identifying the path from the root of the input document to the element
     * for which the caller wants the set of matching Action objects. If an
     * element has no namespace, then the {} part is omitted.
     */
    public List getMatchingActions(String path) throws DigestionException {
        // As we don't have access to a context object here, we have to do
        // the following which effectively disables logging. Damn, we need a
        // better approach to logging...
        Log log = LogUtils.getLogger(null);
        boolean debug = log.isDebugEnabled();
        
        if (debug) {
            log.debug(
                "Matching path [" + path +
                "] on rulemanager object " + this.toString());
        }

        return delegateRuleManager.getMatchingActions(path);
    }
}
