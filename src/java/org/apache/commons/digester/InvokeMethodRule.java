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

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.logging.Log;

import org.xml.sax.Attributes;


/**
 * <p>Rule implementation that calls a method on an object on the stack
 * (normally the top/parent object), passing arguments collected from 
 * subsequent <code>InvokeParamRule</code> rules or from the body of this
 * element. The method invocation is performed as soon as all the required 
 * parameter values have been set by associated InvokeParamRule rules.
 *
 * <p>Incompatible method parameter types are converted 
 * using <code>org.apache.commons.beanutils.ConvertUtils</code>.
 * </p>
 *
 * <p>The {@link CallMethodRule} class behaves similarly to this one, but
 * that class invokes the target method only when the end tag of the matching
 * xml element is encountered which can make its behaviour unintuitive at
 * times. The API of this class is also somewhat cleaner. For these reasons,
 * this class should be used in preference to CallMethodRule where possible. 
 * </p> 
 *
 * <h3>Default Parameter Processing</h3>
 * <p>
 * Normally, the target method will only be invoked if all of the associated
 * InvokeParamRule objects actually fire, and find their param data. So if
 * an InvokeParamFromBodyRule is used to set <i>any</i> of the parameters to a 
 * method, but the matching tag it is supposed to extract the text from isn't 
 * present in the input, then the target method isn't invoked. Or if an
 * InvokeParamFromAttrRule is used to set <i>any</i> of the parameters to a
 * method, but the matching tag doesn't exist, or it does exist but that
 * attribute isn't present, then the method isn't invoked.
 * <p>
 * If it desired to invoke the method even when some parameters are not
 * available, then an InvokeParamFromDefaultsRule should be added with
 * the same pattern as the InvokeMethodRule, to ensure that the invocation
 * executes.
 */

public class InvokeMethodRule extends Rule {

    // ----------------------------------------------------------- Constructors


    /**
     * Construct an "invoke method" rule with the specified method name.  
     * All parameter values assigned via InvokeMethodRule rules
     * will be automatically converted to the types declared for the target
     * method's parameters using the standard commons MethodUtils functionality.
     * <p>
     * <strong>Beware!</strong> The paramCount parameter here accepts
     * different values than the equivalent parameter on class CallMethodRule.
     *
     * @param methodName Method name of the parent method to call
     * @param paramCount The number of parameters to collect. If zero, then
     * the target method is invoked when the matching start tag is encountered, 
     * with no parameters. The value -1 is special, and indicates that the 
     * target method is to be invoked with one parameter, being the body text 
     * of this element.
     */
    public InvokeMethodRule(String methodName, int paramCount) {
        this(0, methodName, paramCount, (Class[])null);
    }

    /**
     * Construct an "invoke method" rule with the specified method name.  
     * All parameter values assigned via InvokeMethodRule rules
     * will be automatically converted to the types declared for the target
     * method's parameters using the standard commons MethodUtils functionality.
     *
     * @param targetOffset location of the target object. Positive numbers are
     * relative to the top of the digester object stack. Negative numbers 
     * are relative to the bottom of the stack. Zero implies the top
     * object on the stack.
     * @param methodName Method name of the parent method to call
     * @param paramCount The number of parameters to collect. If zero, then
     * the target method is invoked when the matching start tag is encountered, 
     * with no parameters. The value -1 is special, and indicates that the 
     * target method is to be invoked with one parameter, being the body text 
     * of this element.
     */
    public InvokeMethodRule(int targetOffset,
                          String methodName,
                          int paramCount) {

        this(targetOffset, methodName, paramCount, (Class[])null);
    }

    /**
     * Construct an "invoke method" rule with the specified method name.
     * All parameter values assigned via InvokeMethodRule rules
     * will be automatically converted to the types specified in the
     * paramTypes array using the ConvertUtils functionality, before the
     * target method is invoked.
     *
     * @param methodName Method name of the parent method to call
     * @param paramCount The number of parameters to collect. If zero, then
     * the target method is invoked when the matching start tag is encountered, 
     * with no parameters. The value -1 is special, and indicates that the 
     * target method is to be invoked with one parameter, being the body text 
     * of this element.
     * @param paramTypeNames The Java class names of the arguments
     *  (if you wish to use a primitive type, specify the corresonding
     *  Java wrapper class instead, such as <code>java.lang.Boolean</code>
     *  for a <code>boolean</code> parameter). This parameter may be null,
     *  in which case the parameters will be converted to whatever types
     *  are declared for the target method's parameters. If this parameter
     *  is non-null, then its length is expected to be equal to the paramCount 
     *  value. 
     */
    public InvokeMethodRule(String methodName,
                            int paramCount, 
                            String paramTypeNames[]) {
        this(0, methodName, paramCount, paramTypeNames);
    }

    /**
     * Construct a "call method" rule with the specified method name and
     * parameter types. If <code>paramCount</code> is set to zero the rule
     * will use the body of this element as the single argument of the
     * method, unless <code>paramTypes</code> is null or empty, in this
     * case the rule will call the specified method with no arguments.
     *
     * @param targetOffset location of the target object. Positive numbers are
     * relative to the top of the digester object stack. Negative numbers 
     * are relative to the bottom of the stack. Zero implies the top
     * object on the stack.
     * @param methodName Method name of the parent method to call
     * @param paramCount The number of parameters to collect. If zero, then
     * the target method is invoked when the matching start tag is encountered, 
     * with no parameters. The value -1 is special, and indicates that the 
     * target method is to be invoked with one parameter, being the body text 
     * of this element.
     * @param paramTypeNames The Java class names of the arguments
     *  (if you wish to use a primitive type, specify the corresonding
     *  Java wrapper class instead, such as <code>java.lang.Boolean</code>
     *  for a <code>boolean</code> parameter). This parameter may be null,
     *  in which case the parameters will be converted to whatever types
     *  are declared for the target method's parameters. If this parameter
     *  is non-null, then its length is expected to be equal to the paramCount 
     *  value.
     */
    public InvokeMethodRule(int targetOffset,
                            String methodName,
                            int paramCount, 
                            String paramTypeNames[]) {

        this.targetOffset = targetOffset;
        this.methodName = methodName;
        this.paramCount = paramCount;
        
        if (paramTypeNames != null) {
            // copy array for safety
            this.paramTypeNames = new String[paramTypeNames.length];
            System.arraycopy(
                paramTypeNames, 0, 
                this.paramTypeNames, 0, 
                paramTypeNames.length);
        }
    }


    /**
     * Construct a "call method" rule with the specified method name and
     * parameter types. If <code>paramCount</code> is set to zero the rule
     * will use the body of this element as the single argument of the
     * method, unless <code>paramTypes</code> is null or empty, in this
     * case the rule will call the specified method with no arguments.
     *
     * @param methodName Method name of the parent method to call
     * @param paramCount The number of parameters to collect, or
     *  zero for a single argument from the body of ths element
     * @param paramTypes The Java classes that represent the
     *  parameter types of the method arguments
     *  (if you wish to use a primitive type, specify the corresonding
     *  Java wrapper class instead, such as <code>java.lang.Boolean.TYPE</code>
     *  for a <code>boolean</code> parameter).  If this parameter
     *  is non-null, then its length is expected to be equal to the paramCount 
     *  value.
     */
    public InvokeMethodRule(String methodName,
                            int paramCount, 
                            Class paramTypes[]) {
        this(0, methodName, paramCount, paramTypes);
    }

    /**
     * Construct a "call method" rule with the specified method name and
     * parameter types. If <code>paramCount</code> is set to zero the rule
     * will use the body of this element as the single argument of the
     * method, unless <code>paramTypes</code> is null or empty, in this
     * case the rule will call the specified method with no arguments.
     *
     * @param targetOffset location of the target object. Positive numbers are
     * relative to the top of the digester object stack. Negative numbers 
     * are relative to the bottom of the stack. Zero implies the top
     * object on the stack.
     * @param methodName Method name of the parent method to call
     * @param paramCount The number of parameters to collect, or
     *  zero for a single argument from the body of ths element
     * @param paramTypes The Java classes that represent the
     *  parameter types of the method arguments
     *  (if you wish to use a primitive type, specify the corresonding
     *  Java wrapper class instead, such as <code>java.lang.Boolean.TYPE</code>
     *  for a <code>boolean</code> parameter). If this parameter
     *  is non-null, then its length is expected to be equal to the paramCount 
     *  value.
     */
    public InvokeMethodRule(int targetOffset,
                            String methodName,
                            int paramCount, 
                            Class paramTypes[]) {

        this.targetOffset = targetOffset;
        this.methodName = methodName;
        this.paramCount = paramCount;

        if (paramTypes != null) {
            // copy array for safety
            this.paramTypes = new Class[paramTypes.length];
            System.arraycopy(
                paramTypes, 0, 
                this.paramTypes, 0, 
                paramTypes.length);
        }
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * A stack of MethodParam objects. If the InvokeMethodRule instance
     * is not associated with a wildcard pattern, then this stack is
     * never deeper than 1 element. When a single InvokeMethodRule
     * instance is associated with wildcards such that it can fire in
     * a "nested" manner, then this stack is used.
     */
    protected ArrayStack paramStack = new ArrayStack();
    
    /** 
     * location of the target object for the call, relative to the
     * top of the digester object stack. The default value of zero
     * means the target object is the one on top of the stack.
     */
    protected int targetOffset = 0;

    /**
     * The method name to call on the parent object.
     */
    protected String methodName = null;


    /**
     * The number of parameters to collect from <code>MethodParam</code> rules.
     * If this value is zero, a single parameter will be collected from the
     * body of this element.
     */
    protected int paramCount = 0;


    /**
     * The parameter types of the parameters to be collected.
     */
    protected Class paramTypes[] = null;

    /**
     * The names of the classes of the parameters to be collected.
     * This attribute allows creation of the classes to be postponed 
     * until the digester is set.
     */
    protected String paramTypeNames[] = null;
    
    /**
     * Due to the inability of some methods to throw exception on error,
     * this object can be set by them to an exception object, and this
     * object will be thrown by some other method, as soon as possible.
     */
    protected Exception initException = null;
    
    // --------------------------------------------------------- Public Methods
    
    /**
     * Set the associated digester.
     * If needed, this class loads the parameter classes from their names.
     */
    public void setDigester(Digester digester)
    {
        // call superclass
        super.setDigester(digester);
        
        // if necessary, load parameter classes
        if (paramTypeNames != null) {
            paramTypes = new Class[paramTypeNames.length];
            for (int i = 0; i < paramTypeNames.length; ++i) {
                String typeName = paramTypeNames[i];
                try {
                    paramTypes[i] =
                            digester.getClassLoader().loadClass(typeName);
                } catch (ClassNotFoundException e) {
                    // use the digester log
                    digester.getLogger().error(
                        "(InvokeMethodRule) Cannot load class " + 
                        paramTypeNames[i], e);

                    paramTypes = null;
                    initException = new Exception(
                        "Cannot load class " + paramTypeNames[i], e);
                        
                    break;
                }
            }
        }
    }

    /**
     * Process the start of this element.
     *
     * @param attributes The attribute list for this element
     */
    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {

        if (initException != null) {
            // some earlier error was detected, but an exception couldn't
            // be thrown at that point; we can throw it here.
            throw initException;
        }
        
        if (paramCount == 0) {
            // invoke immediately; no parameters are required for the method.
            Object target = getObjectFromStack(digester, targetOffset);
            doInvocation(target, null);
        }
        else if (paramCount > 0) {
            // create object to gather params
            Object target = getObjectFromStack(digester, targetOffset);
            MethodParams mp =
                new MethodParams(this, target, paramCount);

            paramStack.push(mp);
        }
        // else special case of paramCount == -1: pass bodytext as param.
        // see body method of this class.
    }


    /**
     * Process the body text of this element.
     *
     * @param bodyText The body text of this element
     */
    public void body(String namespace, String name, String bodyText)
        throws Exception {

        // the method call is only performed if bodytext is not
        // null, just like methods are only called if all their
        // associated InvokeMethodParam rules fire and find their
        // data.
        
        if ((paramCount == -1)  && (bodyText != null)) {
            Object params[] = new Object[1];
            params[0] = bodyText.trim();
            Object target = getObjectFromStack(digester, targetOffset); 
            doInvocation(target, params);
        }
    }

    /**
     * Process the end of this element.
     */
    public void end(String namespace, String name)
        throws Exception {

        if (paramCount > 0) {
            paramStack.pop();
        }
    }

    /**
     * Actually invoke the method on the target object with the
     * available params.
     */
    public void doInvocation(
    Object target, 
    Object[] params) 
    throws Exception {
        Log log = digester.getLogger();
        boolean debug = log.isDebugEnabled();
        boolean trace = log.isTraceEnabled();
        
        if (trace) {
            StringBuffer buf = new StringBuffer();
            buf.append("[InvokeMethodRule]");
            if (params == null) {
                buf.append(" no params.");
            } else {
                for (int i=0,size=params.length;i<size;i++) {
                    buf.append(" param #" + i + "=[" + params[i] + "]");
                }
            }
            log.trace(buf.toString());
        }

        if (target == null) {
            log.debug("null target!");
            StringBuffer sb = new StringBuffer();
            sb.append("[InvokeMethodRule]{");
            sb.append(digester.match);
            sb.append("} Call target is null (");
            sb.append("targetOffset=");
            sb.append(targetOffset);
            sb.append(",stackdepth=");
            sb.append(digester.getCount());
            sb.append(")");
            throw new org.xml.sax.SAXException(sb.toString());
        }
        
        // log the param info
        if (debug) {
            log.debug("dumping invocation info...");
            StringBuffer sb = new StringBuffer("[InvokeMethodRule]{");
            sb.append(digester.match);
            sb.append("} Call ");
            sb.append(target.getClass().getName());
            sb.append(".");
            sb.append(methodName);
            sb.append("(");
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    if (params[i] == null) {
                        sb.append("null");
                    } else {
                        sb.append(params[i].toString());
                    }
                    sb.append("/");
                    if (paramTypes == null) {
                        sb.append("unknown");
                    } else if (paramTypes[i] == null) {
                        sb.append("null");
                    } else {
                        sb.append(paramTypes[i].getName());
                    }
                }
            }
            sb.append(")");
            log.debug(sb.toString());
        }
        
        // If the user explicitly specified that the parameters should
        // be of particular types, then convert all the objects in the
        // params array (which will normally be Strings) to the desired 
        // types. If the user didn't specify param types, then the
        // MethodUtils method used later will implicitly do conversions
        // to the method's declared types for us.
        
        // We only do the conversion if the param value is a String and
        // the specified paramType is not String.
        if (paramTypes != null) {
            for (int i = 0; i < paramTypes.length; i++) {
                if(
                    params[i] == null ||
                     (params[i] instanceof String && 
                       !String.class.isAssignableFrom(paramTypes[i]))) {
                    
                    params[i] =
                        ConvertUtils.convert(
                            (String) params[i], paramTypes[i]);
                }
            }
        }

        // Invoke the required method
        Object result;
        if (paramTypes == null) {
            result = MethodUtils.invokeMethod(target, methodName, params);
        } else {
            result = MethodUtils.invokeMethod(target, methodName,
                params, paramTypes);
        }
        
        processMethodCallResult(result);
        
        log.debug("doInvocation complete.");
    }

    /**
     * Clean up after parsing is complete.
     */
    public void finish() throws Exception {
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

        StringBuffer sb = new StringBuffer("InvokeMethodRule[");
        sb.append("methodName=");
        sb.append(methodName);
        sb.append(", paramCount=");
        sb.append(paramCount);
        sb.append(", paramTypes=");
            
        if (paramTypes == null) {
            sb.append("null");
        } else {
            sb.append("{");
            for (int i = 0; i < paramTypes.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(paramTypes[i].getName());
            }
            sb.append("}");
        }
        sb.append("]");
        return (sb.toString());

    }
    
    /**
     * Called during parsing to assign an explicit value to one of the
     * parameters that will later be passed to a method invocation. If
     * this parameter is the last one needed in order to make the method
     * invocation, then the method will immediately be invoked.
     */
    public void setParam(int index, Object value) throws Exception {
        Log log = digester.getLogger();
        boolean debug = log.isDebugEnabled();
        if (debug) {
            log.debug("setting param " + index + " to value [" + value + "]");
        }
        
        MethodParams params = (MethodParams) paramStack.peek();
        params.setParam(index, value);
        
        if (debug) {
            digester.getLogger().debug("set param " + index);
        }
    }
 
    /**
     * This method can be called during parsing to assign values to parameters
     * that have not yet been given explicit values. Note that this usually
     * causes the target method to fire. It should be called only after all
     * InvokeParamRule instances have fired. See InvokeParamFromDefaultsRule
     * for the canonical use of this method.
     */
    public void setDefaults(Object[] defaults) throws Exception {
        Log log = digester.getLogger();
        log.debug("setting param defaults");
        MethodParams params = (MethodParams) paramStack.peek();
        params.setDefaults(defaults);
        log.debug("set param defaults");
    }
    
    /**
     * Retrieve an object from the digester stack at the specified offset.
     * Unlike the Digester#peek(int) method, this method allows -ve offsets,
     * which are relative to the <i>base</i> of the stack, not the top.
     * An offset of -1 is the root object on the stack.
     */
    private static Object getObjectFromStack(Digester d, int offset) {
        if (offset >= 0) {
            return d.peek(offset);
        }
        
        int depth = d.getCount();
        return d.peek(depth + offset);
    }
}
