/* $Id: $
 *
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


package org.apache.commons.digester2.actions;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.logging.Log;

import org.apache.commons.digester2.Context;
import org.apache.commons.digester2.AbstractAction;
import org.apache.commons.digester2.ParseException;

/**
 * <p>Rule implementation that calls a method on the (top-1) (parent)
 * object, passing the top object (child) as an argument.  It is
 * commonly used to establish parent-child relationships.</p>
 */

public class SetNextAction extends AbstractAction {

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a "set next" rule with the specified method name.  The
     * method's argument type is assumed to be the class of the
     * child object.
     *
     * @param methodName Method name of the parent method to call
     */
    public SetNextAction(String methodName) {
        this(methodName, null);
    }

    /**
     * Construct a "set next" rule with the specified method name.
     *
     * @param methodName Method name of the parent method to call
     * @param paramType Java class of the parent method's argument
     *  (if you wish to use a primitive type, specify the corresonding
     *  Java wrapper class instead, such as <code>java.lang.Boolean</code>
     *  for a <code>boolean</code> parameter)
     */
    public SetNextAction(String methodName,
                       String paramType) {
        this.methodName = methodName;
        this.paramType = paramType;
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * The method name to call on the parent object.
     */
    protected String methodName = null;

    /**
     * The Java class name of the parameter type expected by the method.
     */
    protected String paramType = null;

    // --------------------------------------------------------- Public Methods

    /**
     * Process the end of this element.
     */
    public void end(Context context, String namespace, String nme) 
    throws ParseException {
        // Identify the objects to be used
        Object child = context.peek(0);
        Object parent = context.peek(1);
        Log log = context.getLogger();
        if (log.isDebugEnabled()) {
            if (parent == null) {
                log.debug("[SetNextRule]{" + context.getMatchPath() +
                        "} Call [NULL PARENT]." +
                        methodName + "(" + child + ")");
            } else {
                log.debug("[SetNextRule]{" + context.getMatchPath() +
                        "} Call " + parent.getClass().getName() + "." +
                        methodName + "(" + child + ")");
            }
        }

        // Call the specified method
        Class paramTypes[] = new Class[1];
        if (paramType != null) {
            try {
                paramTypes[0] =
                        context.getClassLoader().loadClass(paramType);
            } catch(ClassNotFoundException ex) {
                throw new ParseException(ex);
            }
        } else {
            paramTypes[0] = child.getClass();
        }
        
        try {
            MethodUtils.invokeMethod(parent, methodName,
                new Object[]{ child }, paramTypes);
        } catch(NoSuchMethodException ex) {
            throw new ParseException(ex);
        } catch(IllegalAccessException ex) {
            throw new ParseException(ex);
        } catch(java.lang.reflect.InvocationTargetException ex) {
            throw new ParseException(ex);
        }
    }

    /**
     * Render a printable version of this Rule.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("SetNextRule[");
        sb.append("methodName=");
        sb.append(methodName);
        sb.append(", paramType=");
        sb.append(paramType);
        sb.append("]");
        return (sb.toString());
    }
}
