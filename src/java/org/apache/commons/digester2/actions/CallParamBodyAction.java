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
 * Action which saves the body text of the current xml element as a parameter
 * to a method invoked by a CallMethodRule.
 */

public class CallParamBodyAction extends AbstractAction {

    /**
     * The zero-relative index of the parameter we are saving.
     */
    protected int paramIndex = 0;
    
    // ---------------------------------------------------------
    // Constructor
    //
    // TODO: support attributes
    //   "trim" --> trim the body before passing as parameter?
    //   "emptyAsNull" --> pass null if the body is an empty string
    // ---------------------------------------------------------

    /**
     * Construct a "call parameter" rule that will pass the body text
     * of the matching xml element as the parameter value.
     *
     * @param paramIndex The zero-relative parameter number
     */
    public CallParamBodyAction(int paramIndex) {
        this.paramIndex = paramIndex;
    }

    // ---------------------------------------------------------
    // Public Methods
    // ---------------------------------------------------------

    /**
     * Process the start of this element.
     *
     * @param attributes The attribute list for this element
     */
    public void body(
    Context context,
    String namespace, String name, 
    String text)
    throws ParseException {
        Log log = context.getLogger();
        if (log.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer("[CallParamBodyAction]{");
            sb.append(context.getMatchPath());
            sb.append("} saving body text value [");
            sb.append(text);
            sb.append("]");
            log.debug(sb.toString());
        }

        Parameters params = (Parameters) context.peek(CallMethodAction.PARAM_STACK);
        params.put(paramIndex, text);
    }

    /**
     * Render a printable version of this Rule.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("CallParamBodyAction[");
        sb.append("paramIndex=");
        sb.append(paramIndex);
        return sb.toString();
    }
}
