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
 * <p>Rule implementation that selects an object from the digester object
 * stack for use as a parameter by a surrounding <code>InvokeMethodRule<code>.
 * </p>
 */

public class InvokeParamFromStackRule extends InvokeParamRule {

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a "call parameter" rule that will save the value of the
     * object on the top of the digester stack.
     *
     * @param paramIndex The zero-relative parameter number
     */
    public InvokeParamFromStackRule(int paramIndex) {
        this(null, paramIndex, 0);
    }

    /**
     * Construct a "call parameter" rule that will save the value of the
     * object on the digester stack at the specified offset.
     *
     * @param paramIndex The zero-relative parameter number
     * @param stackIndex the index of the object which will be passed as a parameter. 
     * The zeroth object is the top of the stack, 1 is the next object down and so on.
     */
    public InvokeParamFromStackRule(int paramIndex, int stackIndex) {
        this(null, paramIndex, stackIndex);
    }

    /**
     * Construct a "call parameter" rule that will save the value of the
     * object on the digester stack at the specified offset.
     *
     * @param owner is the rule which this object sets a parameter value for.
     * This value may be null, in which case the owner is automatically
     * determined as specified in {@link InvokeParamRule#findAssociatedRule}.
     * @param paramIndex The zero-relative parameter number
     * @param stackIndex the index of the object which will be passed as a parameter. 
     * The zeroth object is the top of the stack, 1 is the next object down and so on.
     */
     public InvokeParamFromStackRule(InvokeMethodRule owner, int paramIndex, 
                    int stackIndex) {
        super(owner);
        this.paramIndex = paramIndex;
        this.stackIndex = stackIndex;
    }

    // ----------------------------------------------------- Instance Variables


    /**
     * The offset of the source attribute on the stack.
     */
    protected int stackIndex = 0;


    /**
     * The zero-relative index of the parameter we are saving.
     */
    protected int paramIndex = 0;

    // --------------------------------------------------------- Public Methods

    /**
     * Process the start of this element.
     *
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just 
     *   the element name otherwise
     * @param attributes The attribute list of this element
     */
    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {

        Object param = digester.peek(stackIndex);
        InvokeMethodRule owner = getOwner();
        owner.setParam(paramIndex, param);
    }

    /**
     * Render a printable version of this Rule.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("InvokeParamFromStackRule[");
        sb.append("paramIndex=");
        sb.append(paramIndex);
        sb.append(", stackIndex=");
        sb.append(stackIndex);
        sb.append("]");
        return (sb.toString());

    }
}
