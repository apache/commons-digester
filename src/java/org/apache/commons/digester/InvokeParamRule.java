/*
 * Copyright 2001-2004 The Apache Software Foundation.
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


package org.apache.commons.digester;


import java.util.Iterator;
import java.util.List;

import org.xml.sax.Attributes;


/**
 * <p>Abstract base for all classes which configure parameters for use by
 * an InvokeMethodRule instance.</p>
 */

public abstract class InvokeParamRule extends Rule {

    /**
     * A unique "constant" used in default parameter lists to indicate
     * that there is no default value. Null cannot be used for this
     * purpose, as null is a valid default value for a parameter.
     */
    public static final Object NO_DEFAULT = new Object();
    
    /**
     * The rule instance that this rule sets params for.
     */
    private InvokeMethodRule owner;

    // ----------------------------------------------------------- Constructors

    /**
     * Constructor which requires this object to automatically determine
     * which InvokeMethodRule instance it is associated with; see
     * {@link #findAssociatedRule} for more info.
     */
     public InvokeParamRule() {
     }

    /**
     * Constructor which specifies the rule which this param is associated
     * with.
     */
     public InvokeParamRule(InvokeMethodRule owner) {
         this.owner = owner;
     }

    // --------------------------------------------------------- Public Methods

    /**
     * Specify the digester instance that this rule is associated with.
     * 
     * <p>This method also causes this object to determine which 
     * InvokeMethodRule instance it is associated with, provided that
     * wasn't specified in this object's constructor. See {@link
     * #findAssociatedRule} for more info.</p>
     */
    public void setDigester(Digester d) {
        super.setDigester(d);
        
        if (owner == null) {
            owner = findAssociatedRule(getDigester(), this);
        }
        
        if (owner == null) {
            digester.getLogger().error("oops - cannot find associated rule.");
            // throw new Exception("No InvokeMethodRule preceding param rule.");
        }
    }
    
    /**
     * Returns the InvokeMethodRule instance that this parameter is associated
     * with. If this is the first time this method has been called, then
     */
    public InvokeMethodRule getOwner() {
        return owner;
    }

    /**
     * This method is invoked by kinds of InvokeParamRule to determine
     * which InvokeMethodRule they are associated with. This is done by
     * scanning the complete rules list for the digester's rules engine
     * and looking for the InvokeMethodRule instance which precedes
     * the specified param rule. In other words, the order in which
     * param rules are added to the digester determines which InvokeMethodRule
     * instance they are associated with, without the user having the
     * inconvenience of actually having to pass a specific InvokeMethodRule
     * instance as the associated instance.
     */
    public static InvokeMethodRule findAssociatedRule(Digester d, Rule paramRule) {
        List allRules = d.getRules().rules();
        InvokeMethodRule invokeRule = null;
        for(Iterator i = allRules.iterator(); i.hasNext();) {
            Rule r = (Rule) i.next();
            if (r == paramRule) {
                return invokeRule;
            } else if (r instanceof InvokeMethodRule) {
                invokeRule = (InvokeMethodRule) r;
            }
        }
        return null;
    }
}
