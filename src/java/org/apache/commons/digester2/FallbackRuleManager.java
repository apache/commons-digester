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
import java.util.List;
import java.util.ArrayList;

/**
 * @see DefaultRuleManager
 */
public class FallbackRuleManager extends DefaultRuleManager {

    protected final List fallbackActions;
    protected List unmodifiableFallbackActions;

    public FallbackRuleManager(List fallbackActions) {
        this.fallbackActions = fallbackActions;
        unmodifiableFallbackActions = Collections.unmodifiableList(fallbackActions);
    }
    
    public FallbackRuleManager() {
        this(new ArrayList());
    }
    
    public FallbackRuleManager(FallbackRuleManager manager) {
        this(manager.fallbackActions);
    }
    
    /**
     * @see DefaultRuleManager#copy()
     */
    public RuleManager copy() {
        return new FallbackRuleManager(this);
    }
    
    /**
     * @see DefaultRuleManager#getMatchingActions(String)
     */
    public List getMatchingActions(String path) {
        List actionList = super.getMatchingActions(path);
        if (actionList == Collections.EMPTY_LIST && fallbackActions != null && fallbackActions.size() != 0) {
            return unmodifiableFallbackActions;
        } else {
            return actionList;
        }
    }

    public void addFallbackAction(Action action) {
        fallbackActions.add(action);
        unmodifiableFallbackActions = Collections.unmodifiableList(fallbackActions);
    }

    public boolean removeFallbackAction(Action action) {
        boolean removed = fallbackActions.remove(action);
        unmodifiableFallbackActions = Collections.unmodifiableList(fallbackActions);
        return removed;
    }

    public List getFallbackActions() {
        return unmodifiableFallbackActions;
    }
}
