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


import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.logging.Log;
import org.xml.sax.Attributes;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.digester2.Context;
import org.apache.commons.digester2.AbstractAction;
import org.apache.commons.digester2.ParseException;

/**
 * An Action that calls a method on an object on the stack
 * (normally the top/parent object), passing arguments collected from
 * subsequent <code>CallParam...Action</code> actions.
 * <p>
 * Incompatible method parameter types are converted using the
 * <code>org.apache.commons.beanutils.ConvertUtils</code>. library.
 * <p>
 * Note that the target method is invoked when the <i>end</i> of
 * the tag the CallMethodAction fired on is encountered, <i>not</i> when the
 * last parameter becomes available. This implies that rules which fire on
 * tags nested within the one associated with the CallMethodAction will
 * fire before the CallMethodAction invokes the target method. This behaviour is
 * not configurable.
 */

public class CallMethodAction extends AbstractAction {

    // -----------------------------------------------------
    // Instance Variables
    // -----------------------------------------------------

    public static Context.StackId PARAM_STACK 
        = new Context.StackId(CallMethodAction.class, "ParamStack");

    private Context.ItemId PARAM_TYPES
        = new Context.ItemId(CallMethodAction.class, "ParamTypes", this);

    /**
     * location of the target object for the call, relative to the
     * top of the digester object stack. The default value of zero
     * means the target object is the one on top of the stack.
     */
    private int targetOffset;

    /**
     * The method name to call on the parent object.
     */
    protected String methodName;

    /**
     * The number of parameters to collect from CallParam actions.
     */
    protected int paramCount;

    /**
     * The parameter types of the parameters to be collected.
     */
    protected Class paramTypes[] = null;

    /**
     * The names of the classes of the parameters to be collected.
     * This attribute allows creation of the classes to be postponed until
     * the digester is set.
     */
    private String paramClassNames[] = null;

    // -----------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------

    /**
     * Construct an action which will invoke the specified method on the
     * top object on the digester object stack.
     *
     * @param methodName Method name of the parent method to call
     * @param paramCount The number of parameters to collect.
     */
    public CallMethodAction(String methodName, int paramCount) {
        this(0, methodName, paramCount);
    }

    /**
     * Construct an action which will invoke the specified method on whichever
     * object on the digester object stack is at the specified offset.
     *
     * @param targetOffset location of the target object. Positive numbers are
     * relative to the top (newest) entry on the digester object stack.
     * Negative numbers are relative to the bottom (oldest) entry on the stack.
     * Zero implies the top (most recent) object on the stack.
     *
     * @param methodName Method name of the parent method to call
     * @param paramCount The number of parameters to collect.
     */
    public CallMethodAction(int targetOffset, String methodName, int paramCount) {
        this.targetOffset = targetOffset;
        this.methodName = methodName;
        this.paramCount = paramCount;
        this.paramTypes = new Class[paramCount];
        for (int i = 0; i < this.paramTypes.length; i++) {
            this.paramTypes[i] = String.class;
        }
    }

    /**
     * Construct a "call method" rule with the specified method name and
     * parameter types. Before attempting to locate the target method,
     * the values gathered by the CallParam...Action actions are converted
     * to the specified types.
     *
     * @param methodName Method name of the parent method to call
     *
     * @param paramCount The number of parameters to collect, or
     *  zero for a single argument from the body of ths element
     *
     * @param paramTypes The Java class names of the arguments
     *  (if you wish to use a primitive type, specify the corresonding
     *  Java wrapper class instead, such as <code>java.lang.Boolean</code>
     *  for a <code>boolean</code> parameter)
     */
    public CallMethodAction(
    String methodName,
    int paramCount,
    String paramTypes[]) {
        this(0, methodName, paramCount, paramTypes);
    }

    /**
     * Construct a "call method" rule with the specified method name and
     * parameter types.
     *
     * @param targetOffset location of the target object. Positive numbers are
     * relative to the top of the digester object stack. Negative numbers
     * are relative to the bottom of the stack. Zero implies the top
     * object on the stack.
     *
     * @param methodName Method name of the parent method to call
     *
     * @param paramCount The number of parameters to collect.

     * @param paramTypes The Java class names of the arguments
     *  (if you wish to use a primitive type, specify the corresonding
     *  Java wrapper class instead, such as <code>java.lang.Boolean</code>
     *  for a <code>boolean</code> parameter)
     */
    public CallMethodAction(
    int targetOffset,
    String methodName,
    int paramCount,
    String paramTypes[]) {
        this.targetOffset = targetOffset;
        this.methodName = methodName;
        this.paramCount = paramCount;

        // copy the parameter class names into an array
        // the classes will be loaded when the digester is set
        this.paramClassNames = new String[paramTypes.length];
        for (int i = 0; i < this.paramClassNames.length; i++) {
            this.paramClassNames[i] = paramTypes[i];
        }
    }

    /**
     * Construct a "call method" rule with the specified method name and
     * parameter types.
     *
     * @param methodName Method name of the parent method to call
     *
     * @param paramCount The number of parameters to collect.
     *
     * @param paramTypes The Java classes that represent the
     *  parameter types of the method arguments
     *  (if you wish to use a primitive type, specify the corresonding
     *  Java wrapper class instead, such as <code>java.lang.Boolean.TYPE</code>
     *  for a <code>boolean</code> parameter)
     */
    public CallMethodAction(
    String methodName,
    int paramCount,
    Class paramTypes[]) {
        this(0, methodName, paramCount, paramTypes);
    }

    /**
     * Construct a "call method" rule with the specified method name and
     * parameter types.
     *
     * @param targetOffset location of the target object. Positive numbers are
     * relative to the top of the digester object stack. Negative numbers
     * are relative to the bottom of the stack. Zero implies the top
     * object on the stack.
     *
     * @param methodName Method name of the parent method to call
     *
     * @param paramCount The number of parameters to collect.
     *
     * @param paramTypes The Java classes that represent the
     *  parameter types of the method arguments
     *  (if you wish to use a primitive type, specify the corresonding
     *  Java wrapper class instead, such as <code>java.lang.Boolean.TYPE</code>
     *  for a <code>boolean</code> parameter)
     */
    public CallMethodAction(
    int targetOffset,
    String methodName,
    int paramCount,
    Class paramTypes[]) {
        this.targetOffset = targetOffset;
        this.methodName = methodName;
        this.paramCount = paramCount;
        if (paramTypes == null) {
            this.paramTypes = new Class[paramCount];
            for (int i = 0; i < this.paramTypes.length; i++) {
                this.paramTypes[i] = "abc".getClass();
            }
        } else {
            this.paramTypes = new Class[paramTypes.length];
            for (int i = 0; i < this.paramTypes.length; i++) {
                this.paramTypes[i] = paramTypes[i];
            }
        }

    }

    // --------------------------------------------------------- 
    // Public Methods
    // --------------------------------------------------------- 

    /**
     * If needed, this class loads the parameter classes from their names.
     *
     * TODO: Fix this: init method is not allowed to modify object state.
     * What this probably implies is that the paramTypes member needs to
     * be stored on the Context object.
     */
    public void startParse(Context context) throws ParseException
    {
        // if the constructor specified classes using string classnames,
        // then load them via whatever the current classloader is.
        if (paramClassNames != null) {
            ClassLoader classLoader = context.getClassLoader();
            
            Class[] paramTypes = new Class[paramClassNames.length];
            for (int i = 0; i < this.paramClassNames.length; i++) {
                String classname = paramClassNames[i];
                try {
                    paramTypes[i] = classLoader.loadClass(classname);
                } catch (ClassNotFoundException e) {
                    Log log = context.getLogger();
                    
                    String msg = "(ActionCallMethod) Cannot load class " + classname;
                    log.error(msg, e);
                    throw new ParseException(msg, e);
                }
            }
            
            // now cache it on the context for later use
            context.putItem(PARAM_TYPES, paramTypes);
        }
    }

    /**
     * Process the start of this element.
     *
     * @param attributes The attribute list for this element
     */
    public void begin(
    Context context, 
    String namespace, String name, Attributes attributes)
    throws ParseException {
        Parameters parameters = new Parameters(paramCount);
        context.push(PARAM_STACK, parameters);
    }

    /**
     * Process the end of this element.
     */
    public void end(Context context, String namespace, String name)
    throws ParseException {
        Log log = context.getLogger();

        Parameters parameters = (Parameters) context.pop(PARAM_STACK);
        Object paramValues[] = parameters.getValues();
        
        if (log.isTraceEnabled()) {
            for (int i=0,size=paramValues.length;i<size;i++) {
                log.trace("[ActionCallMethod](" + i + ")" + paramValues[i]) ;
            }
        }

        Class[] types = paramTypes;
        if ((types == null) && (paramClassNames != null)) {
            types = (Class[]) context.getItem(PARAM_TYPES);
        }

        if (types != null) {        
            // Convert the datatypes in the paramValues array to the types
            // specified in the constructor (if any).
            //
            // We do the conversion if the param value is a String and
            // the specified paramType is not String. We also do the
            // conversion if the param value is null, as the result of
            // converting a null may be a non-null value. In all other
            // cases, the original value is left alone.
            //
            // TODO: think about whether we should call toString on
            // objects where the source is not a String but the target is.
            // It is probably a fairly rare case..
            
            for (int i = 0; i < paramValues.length; ++i) {
                Object value = paramValues[i];
                if((value==null) ||
                     ((value instanceof String) &&
                       !String.class.isAssignableFrom(types[i]))) {
    
                    paramValues[i] =
                            ConvertUtils.convert((String) value, types[i]);
                }
            }
        }

        // Determine the target object for the method call
        Object target;
        if (targetOffset >= 0) {
            target = context.peek(targetOffset);
        } else {
            target = context.peek(context.getStackSize() + targetOffset );
        }

        if (target == null) {
            StringBuffer sb = new StringBuffer();
            sb.append("[ActionCallMethod]{");
            sb.append(context.getMatchPath());
            sb.append("} Call target is null (");
            sb.append("targetOffset=");
            sb.append(targetOffset);
            sb.append(",stackdepth=");
            sb.append(context.getStackSize());
            sb.append(")");
            throw new ParseException(sb.toString());
        }

        // Invoke the required method on the top object
        if (log.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer("[ActionCallMethod]{");
            sb.append(context.getMatchPath());
            sb.append("} Call ");
            sb.append(target.getClass().getName());
            sb.append(".");
            sb.append(methodName);
            sb.append("(");
            for (int i = 0; i < paramValues.length; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                if (paramValues[i] == null) {
                    sb.append("null");
                } else {
                    sb.append(paramValues[i].toString());
                }
                sb.append("/");
                if (types == null) {
                    sb.append("null");
                } else {
                    sb.append(types[i].getName());
                }
            }
            sb.append(")");
            log.debug(sb.toString());
        }

        try {
            Object result = MethodUtils.invokeMethod(
                    target, methodName,
                    paramValues, types);

            processMethodCallResult(result);
        }
        catch(NoSuchMethodException ex) {
            throw new ParseException(
                "No such method: " + methodName
                + " on object type:" + target.getClass().getName(),
                ex);
        }
        catch(IllegalAccessException ex) {
            throw new ParseException(
                "Unable to access method: " + methodName
                + " on object type:" + target.getClass().getName(),
                ex);
        }
        catch(InvocationTargetException ex) {
            throw new ParseException(
                "Method: " + methodName
                + " on object type:" + target.getClass().getName()
                + " threw an exception when it was invoked.",
                ex);
        }
    }

    /**
     * Subclasses may override this method to perform additional processing of the
     * invoked method's result.
     *
     * @param result the Object returned by the method invoked, possibly null
     */
    protected void processMethodCallResult(Object result) {
        // do nothing
    }

    /**
     * Render a printable version of this Rule.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("ActionCallMethod[");
        sb.append("methodName=");
        sb.append(methodName);
        sb.append(", paramCount=");
        sb.append(paramCount);
        sb.append(", paramTypes={");
        if (paramTypes != null) {
            for (int i = 0; i < paramTypes.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(paramTypes[i].getName());
            }
        }
        sb.append("}");
        sb.append("]");
        return (sb.toString());
    }
}
