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

import org.xml.sax.Attributes;

/**
 * <p>Rule implementation that sets a parameter to be passed to an object by
 * an <code>InvokeMethodRule<code>.</p>
 *
 * <p>This parameter is an arbitrary object provided by the caller.</p>
 */

public class InvokeParamFromObjectRule extends InvokeParamRule {
    
    // ----------------------------------------------------------- Constructors
    
    /**
     * Construct a "call parameter" rule that will save the given Object as
     * the parameter value.
     *
     * @param paramIndex The zero-relative parameter number
     * @param param the parameter to pass along
     */
    public InvokeParamFromObjectRule(int paramIndex, Object param) {
        this (null, paramIndex, param);
    }

    /**
     * Construct a "call parameter" rule that will save the given Object as
     * the parameter value.
     *
     * @param owner is the rule which this object sets a parameter value for.
     * This value may be null, in which case the owner is automatically
     * determined as specified in {@link InvokeParamRule#findAssociatedRule}.
     * @param paramIndex The zero-relative parameter number
     * @param param the parameter to pass along
     */
    public InvokeParamFromObjectRule(InvokeMethodRule owner,
                int paramIndex, Object param) {
        super(owner);
        this.paramIndex = paramIndex;
        this.param = param;
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * The zero-relative index of the parameter we are saving.
     */
    protected int paramIndex;

    /**
     * The parameter we wish to pass to the method call
     */
    protected Object param;


    // --------------------------------------------------------- Public Methods

    /**
     * Process the start of this element.
     *
     * @param attributes The attribute list for this element
     */
    public void begin(String namespace, String name,
                      Attributes attributes) throws Exception {
        InvokeMethodRule owner = getOwner();
        owner.setParam(paramIndex, param);
    }

    /**
     * Render a printable version of this Rule.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("InvokeParamFromObjectRule[");
        sb.append("paramIndex=");
        sb.append(paramIndex);
        sb.append(", param=");
        sb.append(param);
        sb.append("]");
        return (sb.toString());
    }
}
