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

import java.util.List;

/**
 * Public interface defining a collection of Action instances (and corresponding
 * matching patterns) plus an implementation of a matching policy that selects
 * the Actions that match a particular pattern of nested elements discovered
 * during parsing.
 *  <p>
 * Terminology:
 * <ul>
 * <li>Pattern: a string which may possibly have "wildcard" matching in it,
 *  for example "a", which matches any "a" element anywhere in the document,
 *  or "a/b" which matches any "b" element which is a child of an "a" element
 *  anywhere in the document.
 *</li>
 * <li>Path: a string which represents the absolute path from the root element
 *  of the document to a particular xml element.
 * </ul>
 * <p>
 * <strong>IMPORTANT NOTE</strong>: Anyone implementing a custom RuleManager is
 * strongly encouraged to subclass AbstractRuleManager rather than implement this
 * interface directly. Digester minor releases (2.x -> 2.y) guarantee that
 * subclasses of AbstractRuleManager will not be broken. However the RuleManager
 * interface <i>may</i> change in minor releases, which will break any class
 * which implements this interface directly.
 */

public interface RuleManager {

    /**
     * Returns a new instance with the same type as the concrete object this
     * method is invoked on, complete with contained Actions and patterns. Note
     * that the new RuleManager instance may contain references to the same
     * Action instances as the old one, as Action instances are expected to be
     * stateless and therefore can be safely shared between RuleManagers.
     */
    public RuleManager copy();

    /**
     * Invoked before parsing each input document, this method gives the
     * RuleManager opportunity to do per-parse initialisation if required.
     */
    public void startParse(Context context) throws DigestionException;

    /**
     * Invoked after parsing each input document, this method gives the
     * RuleManager the opportunity to do per-parse cleanup if required.
     */
     public void finishParse(Context context) throws DigestionException;

    /**
     * Defines an action that should be returned by method getMatchingActions
     * if no rule pattern matches the specified path.
     * <p>
     * The specified action is appended to the existing list of fallback actions.
     */
    public void addFallbackAction(Action action);

    /**
     * Defines actions that should be returned by method getMatchingActions
     * if no rule pattern matches the specified path.
     * <p>
     * The actions contained in the specified list are appended to the
     * existing list of fallback actions.
     */
    public void addFallbackActions(List actions);

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
    public void addMandatoryAction(Action action);

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
    public void addMandatoryActions(List actions);

    /**
     * Define a mapping between xml element prefix and namespace uri
     * for use when rule patterns contain namespace prefixes.
     */
    public void addNamespace(String prefix, String uri);

    /**
     * Cause the specified Action to be invoked whenever an xml element
     * is encountered in the input which matches the specified pattern.
     * <p>
     * If the pattern contains any namespace prefixes, eg "/myns:item",
     * then an exception will be thrown unless that prefix has previously
     * been defined via a call to method addNamespace.
     * <p>
     * Note that it is permitted for the same Action to be added multiple
     * times with different associated patterns.
     */
    public void addRule(String pattern, Action action) throws InvalidRuleException;

    /**
     * Return a List of all registered Action instances, or a zero-length List
     * if there are no registered Action instances.
     * <p>
     * The rules are returned in the order they were added. If an Action
     * instance has been added multiple times, then its order is set by the
     * first time it was added.
     * <p>
     * This list includes fallback and mandatory actions (if any).
     */
    public List getActions();

    /**
     * Return a List of all registered Action instances whose associated
     * pattern matches the specified path. If more than one rule matches, then
     * the associated Action instance are <strong>guaranteed</strong> to be
     * returned in the order originally registered through the
     * <code>addRule()</code> method.
     * <p>
     * If no rule's pattern matches the path, then the fallback actions
     * (if any) will be returned.
     * <p>
     * If there are any mandatory actions defined, then these will always be
     * appended to the list of actions that have matched (including any
     * fallback actions).
     * <p>
     * If there are no matching rules, no fallback actions, and no mandatory
     * actions then an empty list is returned.
     * <p>
     * Note that the returned list should be treated as <i>unmodifiable</i>.
     * Attempting to do so will result in undefined behaviour.
     *
     * @param path is a string of form
     * <pre>/{namespace}elementName/{namespace}elementName"</pre>
     * identifying the path from the root of the input document to the element
     * for which the caller wants the set of matching Action objects. If an
     * element has no namespace, then the {} part is omitted.
     */
    public List getMatchingActions(String path) throws DigestionException;
}
