/* $Id$
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


package org.apache.commons.digester2.actions;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.logging.Log;

import org.apache.commons.digester2.Context;
import org.apache.commons.digester2.AbstractAction;
import org.apache.commons.digester2.ParseException;

/**
 * An Action that builds relationships between objects on the digester
 * object stack, usually parent/child relationships.
 * <p>
 * The default behaviour calls a method on the parent (top-1) object, passing 
 * the child (top) object as an argument. The parent object is then expected
 * to store a reference to that child object for later use.
 * <p>
 * Providing non-default values for sourceOffset and targetOffset to the
 * constructor of this class produce actions that can (for example):
 * <ul>
 * <li> pass the parent (top-1) object to a method on the child (top) object, or
 * <li> pass the child (top) object to a method on the object at the root of 
 *  the digester object stack.
 * </ul>
 * <p>
 * For users of the Digester 1.x series, this action is equivalent to the
 * SetNextRule, SetTopRule and SetRootRule classes.
 */

public class LinkObjectsAction extends AbstractAction {

    // ----------------------------------------------------- 
    // Instance Variables
    // ----------------------------------------------------- 

    /**
     * The offset on the digester object stack of the object that will be
     * passed as a parameter.
     */
    protected int sourceOffset;
    
    /**
     * The offset on the digester object stack of the object on which the
     * method will be invoked.
     */
    protected int targetOffset;
    
    /**
     * The method name to call on the parent object.
     */
    protected String methodName = null;

    /**
     * The Java class name of the parameter type expected by the method.
     * Normally this is null, in which case the paramType used is the
     * concrete type of the object being passed.
     */
    protected String paramType = null;

    // ----------------------------------------------------------- 
    // Constructors
    // ----------------------------------------------------------- 

    /**
     * Construct an action which invokes the specified method name on the
     * parent (top-1) object, passing the child (top) object.
     *
     * @param methodName Method name of the parent method to call
     */
    public LinkObjectsAction(String methodName) {
        this(0, 1, methodName, null);
    }

    /**
     * Construct an action which invokes the specified method name on the
     * parent (top-1) object, passing the child (top) object, and that
     * the standard type-conversion facilities should be applied to the
     * child object first.
     * <p>
     * Note that when type-conversion is applied, this generally means that
     * the object passed to the parent is <i>not</i> actually a reference to
     * the object on top of the stack, but instead a reference to some new
     * object derived from it. This implies that other actions which operate
     * on the top object on the stack (ie the child) might not affect the
     * object passed to the target method.
     *
     * @param methodName Method name of the parent method to call
     * @param paramType Java class of the parent method's argument
     *  (if you wish to use a primitive type, specify the corresonding
     *  Java wrapper class instead, such as <code>java.lang.Boolean</code>
     *  for a <code>boolean</code> parameter). The value null may be
     *  passed to indicate that no special type override will be done.
     */
    public LinkObjectsAction(String methodName, String paramType) {
        this(0, 1, methodName, paramType);
    }


    /**
     * Construct an action which invokes the specified method name on the
     * specified target object passing the specified source object.
     * <p>
     * A stack offset of zero indicates the top (newest) object on the
     * stack. Positive values are used to specify objects relative to the
     * top of the stack. A stack offset of -1 indicates the oldest (root)
     * object on the stack, and increasingly negative values are used to 
     * specify objects relative to the root of the stack.
     *
     * @param sourceOffset is the offset on the digester stack of the
     *  object that should be passed as a parameter.
     * 
     * @param targetOffset is the offset on the digester stack of the
     *  objct that should have a method invoked on it.
     *
     * @param methodName Method name of the method to call on the target object.
     *
     * @param paramType Java class of the target method's argument
     *  (if you wish to use a primitive type, specify the corresonding
     *  Java wrapper class instead, such as <code>java.lang.Boolean</code>
     *  for a <code>boolean</code> parameter). The value null may be
     *  passed to indicate that no special type override will be done.
     */
    public LinkObjectsAction(
    int sourceOffset, int targetOffset,
    String methodName, String paramType) {
        this.sourceOffset = sourceOffset;
        this.targetOffset = targetOffset;
        this.methodName = methodName;
        this.paramType = paramType;
    }

    // --------------------------------------------------------- 
    // Public Methods
    // --------------------------------------------------------- 

    /**
     * Process the end of this element.
     */
    public void end(Context context, String namespace, String name) 
    throws ParseException {
        Log log = context.getLogger();

        // Identify the objects to be used
        Object sourceObject = context.peek(sourceOffset);
        Object targetObject = context.peek(targetOffset);

        if (log.isDebugEnabled()) {
            if (targetObject == null) {
                log.debug("[LinkObjectsAction]{" + context.getMatchPath() +
                        "} Call [NULL TARGET]." +
                        methodName + "(" + sourceObject + ")");
            } else {
                log.debug("[LinkObjectsAction]{" + context.getMatchPath() +
                        "} Call " + targetObject.getClass().getName() + "." +
                        methodName + "(" + sourceObject + ")");
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
            paramTypes[0] = sourceObject.getClass();
        }
        
        try {
            MethodUtils.invokeMethod(targetObject, methodName,
                new Object[]{ sourceObject }, paramTypes);
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
        StringBuffer sb = new StringBuffer("LinkObjectsAction[");
        sb.append("sourceOffset=");
        sb.append(sourceOffset);
        sb.append(", targetOffset=");
        sb.append(targetOffset);
        sb.append(", methodName=");
        sb.append(methodName);
        sb.append(", paramType=");
        sb.append(paramType);
        sb.append("]");
        return sb.toString();
    }
}
