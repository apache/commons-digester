/* $Id$
 *
 * Copyright 2001-2005 The Apache Software Foundation.
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


package org.apache.commons.digester2;


import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * <p>Default implementation of the <code>RuleManager</code> interface that supports
 * the standard rule matching behavior.  This class can also be used as a
 * base class for specialized <code>RuleManager</code> implementations.</p>
 *
 * <p>The matching policies implemented by this class support two different
 * types of pattern matching rules:</p>
 * <ul>
 * <li><em>Exact Match</em> - A pattern "/a/b/c" exactly matches a
 *     <code>&lt;c&gt;</code> element, nested inside a <code>&lt;b&gt;</code>
 *     element, which is nested inside an <code>&lt;a&gt;</code> element.</li>
 * <li><em>Tail Match</em> - A pattern "a/b" matches a
 *     <code>&lt;b&gt;</code> element, nested inside an <code>&lt;a&gt;</code>
 *      element, no matter how deeply the pair is nested.</li>
 * </ul>
 * <p>
 * ActionManager objects cannot be shared between Digester objects; if you have
 * multiple Digester objects then a different RuleManager object must be
 * created for each one. However the copy() method may be used to duplicate
 * an existing RuleManager instance.
 */

public class DefaultRuleManager extends AbstractRuleManager {

    // -----------------------------------------------------
    // Instance Variables
    // -----------------------------------------------------

    /**
     * List of actions to be returned by getMatchingActions
     * if no rule patterns match the provided path. This list
     * is null unless one or more fallback actions have actually
     * been defined.
     */
    private ArrayList fallbackActions = null;

    /**
     * List of actions to be returned by getMatchingActions
     * regardless of path. This list is null unless one or more
     * mandatory actions have actually been defined.
     */
    private ArrayList mandatoryActions = null;

    /**
     * Map of namespace-prefix to namespace-uri, used only by the
     * addAction() method.
     */
    private HashMap namespaces = new HashMap();

    /**
     * The list of all actions in the cache. This set allows us to
     * iterate over the set of actions to invoke the startParse/finishParse
     * methods.
     */
    private ArrayList actions = new ArrayList(20);

    /**
     * Map of expanded-pattern -> list-of-actions, so that we can
     * find the pattern that matches the current xml element, then
     * return the list of actions.
     */
    private MultiHashMap rules = new MultiHashMap();


    // ---------------------------------------------------------
    // Ctor
    // ---------------------------------------------------------

    /**
     * Default ctor.
     */
    public DefaultRuleManager() {
    }

    /**
     * Returns a clone of this object. The Action objects currently
     * registered are not copied, as Action objects are required to be
     * re-entrant and thread-safe.
     */
    public DefaultRuleManager(DefaultRuleManager manager) {
        if (fallbackActions != null) {
            this.fallbackActions = (ArrayList) fallbackActions.clone();
        }

        if (mandatoryActions != null) {
            this.mandatoryActions = (ArrayList) mandatoryActions.clone();
        }

        this.namespaces = (HashMap) manager.namespaces.clone();
        this.actions = (ArrayList) manager.actions.clone();
        this.rules = (MultiHashMap) manager.rules.clone();
    }


    // ---------------------------------------------------------
    // Public Methods
    // ---------------------------------------------------------

    /**
     * Returns a clone of this object. The Action objects currently
     * registered are not copied, as Action objects are required to be
     * re-entrant and thread-safe.
     */
    public RuleManager copy() {
        return new DefaultRuleManager(this);
    }

    /**
     * This method is called at the start of parsing a new input document.
     * <p>
     * The startParse method is called on each action that has been added
     * to this rulemanager, in the order that they were added. 
     * <p>
     * TODO: build a mapping from element-name to "list of patterns ending
     * in that element name". When we have to do leading-wildcard-matching,
     * therefore, we can omit all the patterns that don't match the last
     * element, which will be a massive optimisation over scanning the entire
     * list of patterns. And we only need to compute this first time, or if
     * new rules have been added in the meantime.
     */
    public void startParse(Context context) throws DigestionException {
        for(Iterator i = actions.iterator(); i.hasNext(); ) {
            Action action = (Action) i.next();
            action.startParse(context);
        }
    }

    /**
     * This method is called after parsing of a new input document has completed.
     * <p>
     * The finishParse method is called on each action that has been added
     * to this rulemanager, in reverse of the order that they were added. 
     * <p>
     * Note that if an error has occurred during parsing, then this method
     * might not be called.
     */
    public void finishParse(Context context) throws DigestionException {
        // Fire "finish" events for all defined actions
        for(int i = actions.size()-1; i>=0; --i) {
            Action action = (Action) actions.get(i);
            action.finishParse(context);
        }
    }

    /**
     * Defines an action that should be returned by method getMatchingActions
     * if no rule pattern matches the specified path.
     * <p>
     * The specified action is appended to the existing list of fallback actions.
     */
    public void addFallbackAction(Action action) {
        if (fallbackActions == null) {
            fallbackActions = new ArrayList(2);
        }
        fallbackActions.add(action);

        // if the action isn't already in the unique-action list, add it
        if (!actions.contains(action)) {
            actions.add(action);
        }
     }

    /**
     * Defines actions that should be returned by method getMatchingActions
     * if no rule pattern matches the specified path.
     * <p>
     * The actions contained in the specified list are appended to the
     * existing list of fallback actions.
     */
     public void addFallbackActions(List actions) {
        if (fallbackActions == null) {
            fallbackActions = new ArrayList(actions);
        } else {
            fallbackActions.addAll(actions);
        }

        // If any action isn't already in the unique-action list, add it.
        // Yes, this is inefficient - but the list is not expected to be
        // long...
        for(Iterator i = actions.iterator(); i.hasNext(); ) {
            Action action = (Action) i.next();

            if (!actions.contains(action)) {
                actions.add(action);
            }
        }
     }

    /**
     * Defines an action that should always be included in the list of actions
     * returned by method getMatchingActions, no matter what path is provided.
     * <p>
     * if no rule pattern matches the specified path. The specified action
     * is appended to the existing list of fallback actions.
     * <p>
     * The mandatory actions (if any) are always returned at the tail of the
     * list of actions returned by getMatchingActions.
     */
    public void addMandatoryAction(Action action) {
         if (mandatoryActions == null) {
             mandatoryActions = new ArrayList(2);
         }
         mandatoryActions.add(action);

         // if the action isn't already in the unique-action list, add it
        if (!actions.contains(action)) {
            actions.add(action);
        }
     }

    /**
     * Defines actions that should always be included in the list of actions
     * returned by method getMatchingActions, no matter what path is provided.
     * <p>
     * The actions contained in the specified list are appended to the
     * existing list of mandatory actions.
     * <p>
     * The mandatory actions (if any) are always returned at the tail of the
     * list of actions returned by getMatchingActions.
     */
    public void addMandatoryActions(List actions) {
         if (mandatoryActions == null) {
             mandatoryActions = new ArrayList(actions);
         } else {
             mandatoryActions.addAll(actions);
         }

        // If any action isn't already in the unique-action list, add it.
        // Yes, this is inefficient - but the list is not expected to be
        // long...
        for(Iterator i = actions.iterator(); i.hasNext(); ) {
            Action action = (Action) i.next();

            if (!actions.contains(action)) {
                actions.add(action);
            }
        }
     }

    /**
     * When rules are added, the pattern is of form
     * <pre>
     *   /prefix:element/prefix:element/prefix:element....
     * </pre>
     * The prefixes must have previously been defined here.
     */
    public void addNamespace(String prefix, String uri) {
        namespaces.put(prefix, uri);
    }

    /**
     * Store the specified Action.
     * <p>
     * The pattern may include namespace-prefixes, in which case the prefixes
     * are expanded to the corresponding namespace URIs using the mapping
     * previously defined in the addNamespace method.
     */
    public void addRule(String pattern, Action action)
    throws InvalidRuleException {
        // if pattern has namespaces, then convert them to URIs.
        String path = patternToPath(namespaces, pattern);

        // if the action isn't already in the unique-action list, add it
        if (!actions.contains(action)) {
            actions.add(action);
        }

        // add the mapping to the multilist
        rules.put(path, action);
    }

    /**
     * Given a string of form "prefix:name/prefix:name", return a string of
     * form "{namespace-uri}name/{namespace-uri}/name".
     */
    private String patternToPath(Map namespaces, String pattern)
    throws InvalidRuleException {
        int nsEndPos = pattern.indexOf(':');
        if (nsEndPos == -1) {
            /* no namespace prefixes present */
            return pattern;
        }

        int currPos = 0;
        StringBuffer out = new StringBuffer();
        while (nsEndPos != -1) {
            int nsStartPos = pattern.lastIndexOf('/', nsEndPos);
            String prefix = pattern.substring(nsStartPos+1, nsEndPos);

            String uri = (String) namespaces.get(prefix);
            if (uri == null) {
                throw new InvalidRuleException(
                    "No namespace for prefix [" + prefix + "]");
            }

            String prePrefix = pattern.substring(currPos, nsStartPos);
            out.append(prePrefix);

            out.append('{');
            out.append(uri);
            out.append('}');

            currPos = nsEndPos + 1;
            nsEndPos = pattern.indexOf(':', nsEndPos+1);
        }
        out.append(pattern.substring(currPos));

        return out.toString();
    }

    /**
     * Return a List of all registered Action instances that match the specified
     * path, or a zero-length List if there are no matches.  If more
     * than one Action instance matches, they <strong>must</strong> be returned
     * in the order originally registered through the <code>add()</code>
     * method.
     *
     * @param path
     */
    public List getMatchingActions(String path) {
        // assert path.startsWith('/');
        List actionList = (List) rules.get(path);

        if ((actionList == null) || (actionList.size() < 1)) {
            /*
             * Ok, there is no absolute path that matches. So what we need to
             * do now is iterate over all the available paths which are not
             * absolute, and see which is the longest match.
             */

            int longestMatch = -1;
            int thisPathLength = path.length();
            Iterator keys = this.rules.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (!key.startsWith("/")) {
                    // yep, this is non-absolute
                    int thisKeyLength = key.length();
                    if ((thisKeyLength > longestMatch) && path.endsWith(key)) {

                        // just check that we aren't trying to match a
                        // pattern of "a/b" against a path of "/gotcha/b"!
                        if (path.charAt(thisPathLength - thisKeyLength - 1) == '/') {
                            actionList = (List) rules.get(key);
                            longestMatch = thisKeyLength;
                        }
                    }
                }
            }
        }

        if (actionList == null) {
            actionList = fallbackActions;
        }

        if (actionList == null) {
            if (mandatoryActions == null) {
                actionList = java.util.Collections.EMPTY_LIST;
            } else {
                actionList = mandatoryActions;
            }
        } else if (mandatoryActions != null) {
            List oldActions = actionList;
            actionList = new ArrayList(oldActions.size() + mandatoryActions.size());
            actionList.addAll(oldActions);
            actionList.addAll(mandatoryActions);
        }

        return actionList;
    }

    /**
     * Return a List of all registered Action instances, or a zero-length List
     * if there are no registered Action instances.
     * <p>
     * The rules are returned in the order they were added. If an Action
     * instance has been added multiple times, then its order is set by the
     * first time it was added.
     */
    public List getActions() {
        return this.actions;
    }
}
