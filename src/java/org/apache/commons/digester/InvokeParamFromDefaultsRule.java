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
 * <p>Rule implementation that can be used to force a method to be called
 * even when some of its parameter values aren't available, by providing
 * default values for them.</p>
 */

public class InvokeParamFromDefaultsRule extends InvokeParamRule {

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a "call parameter" rule that will replace any missing
     * parameter values with default values.
     *
     * The special value InvokeParamRule.NO_DEFAULT can be used in the
     * defaults array to specifiy that there is no default value whatsoever
     * (not even null) for that particular parameter. In that case, the
     * target method will not fire if that parameter has not explicitly been
     * given a value by some InvokeParamRule instance.
     *
     * Null values in the defaults array is converted to the appropriate type
     * for passing to the target method. For all reference types, null is 
     * passed. For all value types, the "default" value for that primitive type
     * is passed (eg false for booleans, 0 for ints).
     *
     * @param defaults is the default values to assign to parameters.
     */
    public InvokeParamFromDefaultsRule(Object[] defaults) {
        this.defaults = defaults;
    }

    /**
     * Construct a "call parameter" rule that will replace any missing
     * parameter values with default values.
     *
     * @param owner is the rule which this object sets a parameter value for.
     * This value may be null, in which case the owner is automatically
     * determined as specified in {@link InvokeParamRule#findAssociatedRule}.
     *
     * @param defaults is the default values to assign to parameters.
     */
    public InvokeParamFromDefaultsRule(InvokeMethodRule owner, Object[] defaults) {
        super(owner);
        this.defaults = defaults;
    }
    
    // ----------------------------------------------------- Instance Variables

    /**
     * The default values for each parameter.
     */
    protected Object[] defaults;

    // --------------------------------------------------------- Public Methods

    /**
     * Ensure the method is invoked.
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
        owner.setDefaults(defaults);
    }

    /**
     * Render a printable version of this Rule.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("InvokeParamFromDefaultsRule");
        return (sb.toString());
    }
}
