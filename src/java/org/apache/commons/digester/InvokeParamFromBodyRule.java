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
 * <p>Rule implementation that selects the body text within an xml element
 * for use as a parameter by a surrounding <code>InvokeMethodRule<code>.</p>
 */

public class InvokeParamFromBodyRule extends InvokeParamRule {

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a "call parameter" rule that will save the value of the
     * matching xml element's body text as the parameter value.
     *
     * @param paramIndex The zero-relative parameter number
     */
    public InvokeParamFromBodyRule(int paramIndex) {

        this.paramIndex = paramIndex;
    }

    /**
     * Construct a "call parameter" rule that will save the value of the
     * matching xml element's body text as the parameter value.
     *
     * @param owner is the rule which this object sets a parameter value for.
     * This value may be null, in which case the owner is automatically
     * determined as specified in {@link InvokeParamRule#findAssociatedRule}.
     * @param paramIndex The zero-relative parameter number
     */
    public InvokeParamFromBodyRule(InvokeMethodRule owner, int paramIndex) {

        super(owner);
        this.paramIndex = paramIndex;
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * The zero-relative index of the parameter we are saving.
     */
    protected int paramIndex = 0;

    // --------------------------------------------------------- Public Methods

    /**
     * Process the accumulated body text of this element.
     *
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just 
     *   the element name otherwise
     * @param bodyText The accumulated text within this element.
     */
    public void body(String namespace, String name, String bodyText)
        throws Exception {

        InvokeMethodRule owner = getOwner();
        owner.setParam(paramIndex, bodyText.trim());
    }

    /**
     * Render a printable version of this Rule.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("InvokeParamFromBodyRule[");
        sb.append("paramIndex=");
        sb.append(paramIndex);
        sb.append("]");
        return (sb.toString());

    }
}
