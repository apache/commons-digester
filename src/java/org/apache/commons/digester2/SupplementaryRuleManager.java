/* $Id: DefaultRuleManager.java 153050 2005-02-09 12:12:28Z skitching $
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
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


/**
 * @see DefaultRuleManager
 */

public class SupplementaryRuleManager extends DefaultRuleManager {

    public static boolean matches(String path, String pathToMatch) {
        if (pathToMatch.charAt(0) == '/') {
            // absolute
            return path.equals(pathToMatch);
        } else {
            // relative
            // XXX looks wrong but protects a match of 
            // "a/b" against a path of "/gotcha/b", but
            // still allows
            // "a/b" to match against "/a/b"
            return path.endsWith("/" + pathToMatch);
        }
    }
    
    /** 
     * Checks if this path matches any of the paths given. This means we iterate through 
     * <code>pathsToMatch</code> and match every entry to this path.
     */
    public static boolean matchsAny(String path, String[] pathsToMatch) {
        for (int i = 0; i < pathsToMatch.length; i++) {
            if (matches(path, pathsToMatch[i]))
                return true;
        }
        return false;
    }
    
    protected final Action supplementaryAction;
    protected final Action fallbackAction;
    
    protected final List fallbackList = new ArrayList();
    
    public SupplementaryRuleManager(Action supplementaryAction) {
        this(supplementaryAction, null);
    }
    
    public SupplementaryRuleManager(Action supplementaryAction, Action fallbackAction) {
        
        if (fallbackAction == null && supplementaryAction == null) {
            throw new IllegalArgumentException(
                    "Both parameters set to null makes no sense. Use DefaultRuleManager instead.");
        }

        this.supplementaryAction = supplementaryAction;
        this.fallbackAction = fallbackAction;

        if (fallbackAction != null) {
            fallbackList.add(fallbackAction);
        }

        if (supplementaryAction != null) {
            fallbackList.add(supplementaryAction);
        }
    }
    
    public SupplementaryRuleManager(SupplementaryRuleManager manager) {
        this(manager.supplementaryAction, manager.fallbackAction);
        this.namespaces = (HashMap) manager.namespaces.clone();
        this.actions = (ArrayList) manager.actions.clone();
        this.rules = (MultiHashMap) manager.rules.clone();
    }
    
    /**
     * @see DefaultRuleManager#copy()
     */
    public RuleManager copy() {
        return new SupplementaryRuleManager(this);
    }
    
    /**
     * @see DefaultRuleManager#getMatchingActions(String)
     */
    public List getMatchingActions(String path) {
        List actionList = super.getMatchingActions(path);
        if (actionList == Collections.EMPTY_LIST) {
            return fallbackList;
        }
        if (supplementaryAction != null) {
            actionList.add(supplementaryAction);
        }
        return actionList;
    }

    /**
     * @return Returns the fallbackAction.
     */
    public Action getFallbackAction() {
        return fallbackAction;
    }

    /**
     * @return Returns the supplementaryAction.
     */
    public Action getSupplementaryAction() {
        return supplementaryAction;
    }
}
