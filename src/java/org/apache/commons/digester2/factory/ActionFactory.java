/* $Id: ActionFactory.java,v 1.107 2004/09/18 09:51:03 skitching Exp $
 *
 * Copyright 2004 The Apache Software Foundation.
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

package org.apache.commons.digester2.factory;

import org.apache.commons.digester2.Action;
import org.apache.commons.digester2.RuleManager;
import org.apache.commons.digester2.Digester;
import org.apache.commons.digester2.InvalidRuleException;

import org.apache.commons.digester2.actions.*;

/**
 * <p>A convenience class for creating new instances of the various Action
 * classes bundled with the Digester distribution and adding them to a
 * Digester or a RuleManager. Note that this class is entirely optional; 
 * actions can equally well be created via direct instantiation and
 * added via Digester.addRule or RuleManager.addRule methods. </p>
 */

public class ActionFactory {


    // --------------------------------------------------------- Constructors


    /**
     * Construct a new ActionFactory. Equivalent to
     * <pre>
     * ActionFactory(digester.getRuleManager())
     * </pre>
     */
    public ActionFactory(Digester digester) {
        this.target = digester.getRuleManager();
    }


    /**
     * Construct a new ActionFactory.
     */
    public ActionFactory(RuleManager ruleManager) {
        this.target = ruleManager;
    }


    // --------------------------------------------------- Instance Variables


    /**
     * The destination RuleManager for created rules.
     */
    protected RuleManager target;


    // ------------------------------------------------------------- Properties

    /**
     * <p>Register a new rule, ie (pattern, action) pair.</p>
     *
     * @param pattern Element matching pattern
     * @param action Action to be registered
     */
    public Action addRule(String pattern, Action action) 
    throws InvalidRuleException {
        target.addRule(pattern, action);
        return action;
    }

    /**
     * Add a "create object" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param className Java class name to be created
     * @see CreateObjectAction
     */
    public void addCreateObject(String pattern, String className)
    throws InvalidRuleException {
        addRule(pattern,
                new CreateObjectAction(className));
    }


    /**
     * Add a "create object" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param clazz Java class to be created
     * @see CreateObjectAction
     */
    public void addCreateObject(String pattern, Class clazz)
    throws InvalidRuleException {
        addRule(pattern,
                new CreateObjectAction(clazz));
    }


    /**
     * Add a "create object" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param className Default Java class name to be created
     * @param attributeName Attribute name that optionally overrides
     *  the default Java class name to be created
     * @see CreateObjectAction
     */
    public void addCreateObject(
    String pattern, 
    String className, 
    String attributeName)
    throws InvalidRuleException {
        addRule(pattern,
                new CreateObjectAction(className, attributeName));
    }


    /**
     * Add a "create object" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param clazz Default Java class to be created
     * @param attributeName Attribute name that optionally overrides
     *  the default Java class name to be created
     *
     * @see CreateObjectAction
     */
    public void addCreateObject(
    String pattern, 
    Class clazz,
    String attributeName) 
    throws InvalidRuleException {
        addRule(pattern,
                new CreateObjectAction(clazz, attributeName));
    }

    /**
     * Add a "set properties" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @see SetPropertiesAction
     */
    public void addSetProperties(String pattern)
    throws InvalidRuleException {
        addRule(pattern,
                new SetPropertiesAction());
    }

    /**
     * Add a "set properties" rule with a single overridden parameter.
     * See {@link SetPropertiesAction#SetPropertiesAction(String attributeName, String propertyName)}
     *
     * @param pattern Element matching pattern
     * @param attributeName map this attribute
     * @param propertyName to this property
     * @see SetPropertiesAction
     */
    public void addSetProperties(
    String pattern, 
    String attributeName, 
    String propertyName)
    throws InvalidRuleException {
        addRule(pattern,
                new SetPropertiesAction(attributeName, propertyName));
    }

    /**
     * Add a "set properties" rule with overridden parameters.
     * See {@link SetPropertiesAction#SetPropertiesAction(String [] attributeNames, String [] propertyNames)}
     *
     * @param pattern Element matching pattern
     * @param attributeNames names of attributes with custom mappings
     * @param propertyNames property names these attributes map to
     * @see SetPropertiesAction
     */
    public void addSetProperties(
    String pattern, 
    String [] attributeNames, 
    String [] propertyNames)
    throws InvalidRuleException {
        addRule(pattern,
                new SetPropertiesAction(attributeNames, propertyNames));
    }

    /**
     * Add a "bean property setter" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @see BeanPropertySetterAction
     */
    public Action addBeanPropertySetter(String pattern)
    throws InvalidRuleException {
        Action action = new BeanPropertySetterAction();
        return addRule(pattern, action);
    }

    /**
     * Add a "bean property setter" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param propertyName Name of property to set
     * @see BeanPropertySetterAction
     */
    public Action addBeanPropertySetter(String pattern, String propertyName) 
    throws InvalidRuleException {
        Action action = new BeanPropertySetterAction(propertyName);
        return addRule(pattern, action);
    }

    /**
     * Add a "set next" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param methodName Method name to call on the parent element
     * @see SetNextAction
     */
    public void addSetNext(String pattern, String methodName)
    throws InvalidRuleException {
        addRule(pattern,
                new SetNextAction(methodName));
    }

    /**
     * Add a "set next" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param methodName Method name to call on the parent element
     * @param paramType Java class name of the expected parameter type
     *  (if you wish to use a primitive type, specify the corresonding
     *  Java wrapper class instead, such as <code>java.lang.Boolean</code>
     *  for a <code>boolean</code> parameter)
     * @see SetNextAction
     */
    public void addSetNext(
    String pattern, 
    String methodName,
    String paramType)
    throws InvalidRuleException {
        addRule(pattern,
                new SetNextAction(methodName, paramType));
    }

    /**
     * Add an "call method" rule for a method which accepts no arguments.
     *
     * @param pattern Element matching pattern
     * @param methodName Method name to be called
     * @see CallMethodAction
     */
    public Action addCallMethod(String pattern, String methodName)
    throws InvalidRuleException {
        Action action = new CallMethodAction(methodName, 0); 
        return addRule(pattern, action);
    }

    /**
     * Add an "call method" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param methodName Method name to be called
     * @param paramCount Number of expected parameters (or zero
     *  for a single parameter from the body of this element)
     * @see CallMethodAction
     */
    public Action addCallMethod(String pattern, String methodName, int paramCount)
    throws InvalidRuleException {
        Action action = new CallMethodAction(methodName, paramCount);
        return addRule(pattern, action);
    }

    /**
     * Add an "call method" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param methodName Method name to be called
     * @param paramCount Number of expected parameters.
     * @param paramTypes Set of Java class names for the types
     *  of the expected parameters
     *  (if you wish to use a primitive type, specify the corresonding
     *  Java wrapper class instead, such as <code>java.lang.Boolean</code>
     *  for a <code>boolean</code> parameter)
     * @see CallMethodAction
     */
    public Action addCallMethod(
    String pattern, String methodName,
    int paramCount, String paramTypes[]) 
    throws InvalidRuleException {
        Action action = new CallMethodAction(methodName, paramCount, paramTypes);
        return addRule(pattern, action);
    }


    /**
     * Add an "call method" rule for the specified parameters.
     *
     * @param pattern Element matching pattern
     * @param methodName Method name to be called
     * @param paramCount Number of expected parameters (or zero
     *  for a single parameter from the body of this element)
     * @param paramTypes The Java class names of the arguments
     *  (if you wish to use a primitive type, specify the corresonding
     *  Java wrapper class instead, such as <code>java.lang.Boolean</code>
     *  for a <code>boolean</code> parameter)
     * @see CallMethodAction
     */
    public Action addCallMethod(
    String pattern, String methodName,
    int paramCount, Class paramTypes[])
    throws InvalidRuleException {
        return addRule(pattern,
                new CallMethodAction(
                                    methodName,
                                    paramCount, 
                                    paramTypes));

    }

    /**
     * Set a call parameter from the body of the matched element.
     *
     * @param pattern Element matching pattern
     * @param paramIndex Zero-relative parameter index to set
     * @see CallParamAction
     */
    public Action addCallParamBody(String pattern, int paramIndex)
    throws InvalidRuleException {
        Action action = new CallParamBodyAction(paramIndex);
        return addRule(pattern, action);
    }

    /**
     * Set a call parameter from an xml attribute of the matched element.
     *
     * @param pattern Element matching pattern
     * @param paramIndex Zero-relative parameter index to set
     * @param attributeName Attribute whose value is used.
     * @see CallParamAction
     */
    public void addCallParamAttribute(
    String pattern, 
    int paramIndex, 
    String attributeName)
    throws InvalidRuleException {
        addRule(pattern,
                new CallParamAttributeAction(paramIndex, attributeName));
    }

    /**
     * Set a call parameter from an object on the digester object stack.
     *
     * @param paramIndex The zero-relative parameter number
     * @param stackIndex set the call parameter to the stackIndex'th object
     * down the stack, where 0 is the top of the stack, 1 the next element 
     * down and so on
     * @see CallMethodAction
     */    
    public void addCallParamFromStack(String pattern, int paramIndex, int stackIndex)
    throws InvalidRuleException {
         addRule(pattern,
                new CallParamFromStackAction(paramIndex, stackIndex));
    }
}
