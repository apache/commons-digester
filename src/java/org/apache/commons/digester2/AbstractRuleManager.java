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
 * Provides a base implementation for custom RuleManagers (ie classes that
 * match input xml elements to lists of Actions to be executed).
 * <p>
 * Note that extending this abstract class rather than directly implementing
 * the RuleManager interface provides much better "forward compatibility".
 * Digester minor releases (2.x -> 2.y) guarantee not to break any classes that
 * subclass this abstract class. However no such guarantee exists for classes
 * that directly implement the RuleManager interface.
 */

public abstract class AbstractRuleManager implements RuleManager {

    /**
     * Returns a new instance with the same type as the concrete object this
     * method is invoked on, complete with contained Actions and patterns. Note
     * that the new RuleManager instance may contain references to the same
     * Action instances as the old one, as Action instances are expected to be
     * stateless and therefore can be safely shared between RuleManagers.
     */
    public abstract RuleManager copy();

    /**
     * Invoked before parsing each input document, this method gives the
     * RuleManager the opportunity to do per-parse initialisation if required.
     */
    public void startParse(Context context) throws DigestionException {}

    /**
     * Invoked after parsing each input document, this method gives the
     * RuleManager and the managed Action objects the opportunity to do
     * per-parse cleanup if required.
     */
     public void finishParse(Context context) throws DigestionException {}

    /**
     * Defines an action that should be returned by method getMatchingActions
     * if no rule pattern matches the specified path.
     * <p>
     * The specified action is appended to the existing list of fallback actions.
     */
    public abstract void addFallbackAction(Action action);

    /**
     * Defines actions that should be returned by method getMatchingActions
     * if no rule pattern matches the specified path.
     * <p>
     * The actions contained in the specified list are appended to the
     * existing list of fallback actions.
     */
    public abstract void addFallbackActions(List actions);

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
    public abstract void addMandatoryAction(Action action);

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
    public abstract void addMandatoryActions(List actions);

    /**
     * Define a mapping between xml element prefix and namespace uri
     * for use when rule patterns contain namespace prefixes.
     */
    public abstract void addNamespace(String prefix, String uri);

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
    public abstract void addRule(String pattern, Action action)
    throws InvalidRuleException;

    /**
     * Return a List of all registered Action instances, or a zero-length List
     * if there are no registered Action instances.
     * <p>
     * The rules are returned in the order they were added. If an Action
     * instance has been added multiple times, then its order is set by the
     * first time it was added.
     */
    public abstract List getActions();

    /**
     * Return a List of all registered Action instances that match the specified
     * nesting pattern, or a zero-length List if there are no matches.  If more
     * than one Action instance matches, they <strong>must</strong> be returned
     * in the order originally registered through the <code>addRule()</code>
     * method.
     *
     * @param path is a string of form
     * <pre>/{namespace}elementName/{namespace}elementName"</pre>
     * identifying the path from the root of the input document to the element
     * for which the caller wants the set of matching Action objects. If an
     * element has no namespace, then the {} part is omitted.
     */
    public abstract List getMatchingActions(String path)
        throws DigestionException;
}
