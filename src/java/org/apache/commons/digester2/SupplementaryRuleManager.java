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


import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @see DefaultRuleManager
 */

public class SupplementaryRuleManager extends FallbackRuleManager {

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
    
    protected final List supplementaryActions;
    protected final Map path2ActionsMap = new HashMap();
    
    public SupplementaryRuleManager(List supplementaryActions) {
        this.supplementaryActions = supplementaryActions;
    }
    
    public SupplementaryRuleManager(List supplementaryActions, List fallbackActions) {
        super(fallbackActions);
        this.supplementaryActions = supplementaryActions;
    }
    
    public SupplementaryRuleManager() {
        this(new ArrayList());
    }
    
    public SupplementaryRuleManager(SupplementaryRuleManager manager) {
        super(manager);
        this.supplementaryActions = manager.supplementaryActions;
    }
    
    /**
     * @see DefaultRuleManager#copy()
     */
    public RuleManager copy() {
        return new SupplementaryRuleManager(this);
    }
    
    public void addRule(String pattern, Action action) throws InvalidRuleException {
        super.addRule(pattern, action);
        invalidateCache();
    }

    public void addFallbackAction(Action action) {
        super.addFallbackAction(action);
        invalidateCache();
    }

    public void addSupplementaryAction(Action action) {
        supplementaryActions.add(action);
        invalidateCache();
    }

    /**
     * @see DefaultRuleManager#getMatchingActions(String)
     */
    public List getMatchingActions(String path) {
        
        List completeList = (List) path2ActionsMap.get(path);
        if (completeList != null) {
            return completeList;
        } 
        
        List actionList = super.getMatchingActions(path);
        if (supplementaryActions.size() != 0) {
            if (actionList == Collections.EMPTY_LIST) {
                completeList = Collections.unmodifiableList(supplementaryActions);
            } else {
                completeList = new ReadOnlyConcatList(actionList, supplementaryActions);
            }
        } else {
            completeList = actionList;
        }

        path2ActionsMap.put(path, completeList);
        return completeList;
    }

    protected void invalidateCache() {
        path2ActionsMap.clear();
    }
    
    protected static class ReadOnlyConcatList extends AbstractList {

        final List left;
        final List right;
        final int border;

        ReadOnlyConcatList(List left, List right) {
            this.left = left;
            this.right = right;
            this.border = left.size();
        }

        public Object get(int index) {
            if (index >= border) {
                return right.get(index - border);
            } else {
                return left.get(index);
            }
        }

        public int size() {
            return left.size() + right.size();
        }

    }
}