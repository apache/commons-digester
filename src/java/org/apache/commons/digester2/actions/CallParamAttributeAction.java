/* $Id$
 *
 * Copyright 2005 The Apache Software Foundation.
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


package org.apache.commons.digester2.actions;

import org.xml.sax.Attributes;

import org.apache.commons.digester2.ArrayStack;
import org.apache.commons.logging.Log;

import org.apache.commons.digester2.Context;
import org.apache.commons.digester2.AbstractAction;
import org.apache.commons.digester2.ParseException;

/**
 * Action which saves an xml attribute as a parameter value for a method
 * invoked by a CallMethodRule.
 */

public class CallParamAttributeAction extends AbstractAction {

    /**
     * The zero-relative index of the parameter we are saving.
     */
    protected int paramIndex = 0;

    /**
     * The attribute from which to save the parameter value
     */
    protected String attributeName;

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    /**
     * Construct a "call parameter" rule that will save the value of the
     * specified attribute as the parameter value.
     *
     * @param paramIndex The zero-relative parameter number
     * @param attributeName The name of the attribute to save
     */
    public CallParamAttributeAction(int paramIndex, String attributeName) {
        this.paramIndex = paramIndex;
        this.attributeName = attributeName;
    }

    // ---------------------------------------------------------
    // Public Methods
    // ---------------------------------------------------------

    /**
     * Process the start of this element.
     *
     * @param attributes The attribute list for this element
     */
    public void begin(
    Context context,
    String namespace, String name, Attributes attributes)
    throws ParseException {
        Object paramValue = attributes.getValue(attributeName);

        Log log = context.getLogger();
        if (log.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer("[CallParamAttributeAction]{");
            sb.append(context.getMatchPath());
            sb.append("} saving xml attribute [");
            sb.append(attributeName);
            sb.append("] value [");
            sb.append(paramValue);
            sb.append("]");
            log.debug(sb.toString());
        }

        Parameters params = (Parameters) context.peek(CallMethodAction.PARAM_STACK);
        params.put(paramIndex, paramValue);
    }

    /**
     * Render a printable version of this Rule.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("CallParamAttributeAction[");
        sb.append("paramIndex=");
        sb.append(paramIndex);
        sb.append(", attributeName=");
        sb.append(attributeName);
        sb.append("]");
        return sb.toString();
    }
}
