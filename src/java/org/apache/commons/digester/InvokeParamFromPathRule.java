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
 * <p>Rule implementation that passes the name of the current xml element or
 * the path to the current xml element as a parameter to an InvokeMethodRule.</p>
 *
 * <p>This Rule is most useful when using rule that allow extensive use of 
 * wildcards.</p>
 */

public class InvokeParamFromPathRule extends InvokeParamRule {

    // ----------------------------------------------------------- Constructors

    /**
     * Constructor for an instance which passes the full path to the
     * current xml element as a parameter.
     *
     * @param paramIndex The zero-relative parameter number
     */
    public InvokeParamFromPathRule(int paramIndex) {
        this(null, paramIndex, true);
    }
 
    /**
     * Constructor.
     *
     * @param paramIndex The zero-relative parameter number
     * @param passFullPath indicates whether the string passed to the
     * invoked method should contain a string of form "foo/bar/baz" or
     * just the name of the matched element, "baz".
     */
    public InvokeParamFromPathRule(int paramIndex, boolean passFullPath) {
        this(null, paramIndex, passFullPath);
    }
 
    /**
     * Constructor.
     *
     * @param owner is the rule which this object sets a parameter value for.
     * This value may be null, in which case the owner is automatically
     * determined as specified in {@link InvokeParamRule#findAssociatedRule}.
     * @param paramIndex The zero-relative parameter number
     * @param passFullPath indicates whether the string passed to the
     * invoked method should contain a string of form "foo/bar/baz" or
     * just the name of the matched element, "baz".
     */
    public InvokeParamFromPathRule(InvokeMethodRule owner, int paramIndex, 
                boolean passFullPath) {

        super(owner);
        this.paramIndex = paramIndex;
        this.passFullPath = passFullPath;
    }
 
    // ----------------------------------------------------- Instance Variables

    /**
     * The zero-relative index of the parameter we are saving.
     */
    protected int paramIndex = 0;

    /**
     * Whether to pass the full path or just the last component.
     */
    protected boolean passFullPath = true;

    // --------------------------------------------------------- Public Methods


    /**
     * Process the start of this element.
     *
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just 
     *   the element name otherwise
     * @param attributes The attribute list for this element

     */
    public void begin(String namespace, String name, Attributes attributes) throws Exception {

        String param = getDigester().getMatch();
        
        if(param != null) {
            if (!passFullPath) {
                int start = param.lastIndexOf('/');
                if (start != -1) {
                    param = param.substring(start+1);
                }
            }
            InvokeMethodRule owner = getOwner();
            owner.setParam(paramIndex, param);
        }
        
    }

    /**
     * Render a printable version of this Rule.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("PathCallParamRule[");
        sb.append("paramIndex=");
        sb.append(paramIndex);
        sb.append("]");
        return (sb.toString());

    }
}
