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

import org.xml.sax.Attributes;
import org.apache.commons.logging.Log;

import org.apache.commons.digester2.Context;
import org.apache.commons.digester2.AbstractAction;
import org.apache.commons.digester2.ParseException;

/**
 * An Action that creates a new object and pushes it onto the object stack.
 * When the element is complete, the object will be popped.
 * <p>
 * Normally a SetNextAction or CallMethodAction is used in partnership with
 * this action to create a reference from some other object to this newly
 * created object before it is popped from the object stack.
 */

public class CreateObjectAction extends AbstractAction {

    // ----------------------------------------------------- 
    // Instance Variables
    // ----------------------------------------------------- 

    /**
     * The Java class name of the object to be created.
     */
    protected String className = null;

    /**
     * The attribute containing an override class name if it is present.
     */
    protected String attributeName = null;

    // ----------------------------------------------------------- 
    // Constructors
    // ----------------------------------------------------------- 

    /**
     * Construct a "create object" action which will create an instance 
     * of the specified class.
     * <p>
     * The classloader used to load the class will be the value returned
     * from the SAXHandler.getClassLoader method. By default this is the same
     * classloader which was used to load the Digester classes. For simple
     * applications the default is fine, but applications running in more
     * complex environments may need to use the following methods to control
     * exactly which classloader is used:
     * <ul>
     *  <li>setExplicitClassLoader on Digester or SAXHandler classes
     *  <li>setUseContextClassLoader on Digester or SAXHandler classes
     * </ul>
     *
     * @param className Java class name of the object to be created
     */
    public CreateObjectAction(String className) {
        this.className = className;
    }

    /**
     * Construct a "create object" action which will create an instance
     * of the specified class.
     * <p>
     * Note that this is exactly equivalent to calling
     * <code>new CreateObjectAction(clazz.getClassName())<code>.
     * Note in particular that the classloader associated with the specified
     * Class object is ignored; when the rule fires the new instance will
     * be created from a Class object which has been loaded via the classloader
     * returned from saxHandler.getClassLoader. See method 
     * {@link #CreateObjectAction(String className)} for more information.
     *
     * @param clazz Java class of the object to be created
     */
    public CreateObjectAction(Class clazz) {
        this.className = clazz.getClassName();
    }

    /**
     * Construct a "create object" action which will create an instance of
     * the specified class unless an xml attribute is present with the
     * specified name. If such an attribute is present, then the string value
     * of that attribute is expected to be a java class name, and an instance
     * of that class will be created instead.
     * <p>
     * TODO: allow namespaced attributes to be used. Currently, only 
     * non-namespaces attributes may be used to override class names.
     *
     * @param className Java class name of the object to be created
     * @param attributeName Attribute name which, if present, contains an
     *  override of the class name to create
     */
    public CreateObjectAction(String className, String attributeName) {
        this.className = className;
        this.attributeName = attributeName;
    }

    /**
     * Construct a "create object" action which will create an instance of
     * the specified class unless an xml attribute is present with the
     * specified name. If such an attribute is present, then the string value
     * of that attribute is expected to be a java class name, and an instance
     * of that class will be created instead.
     * <p>
     * TODO: allow namespaced attributes to be used. Currently, only 
     * non-namespaces attributes may be used to override class names.
     *
     * @param clazz Java class name of the object to be created
     *  override of the class name to create
     * @param attributeName Attribute name which, if present, contains an
     */
    public CreateObjectAction(Class clazz, String attributeName) {
        this.className = clazz.getClassName();
        this.attributeName = attributeName;
    }

    // --------------------------------------------------------- 
    // Public Methods
    // --------------------------------------------------------- 

    /**
     * Process the beginning of this element.
     *
     * @param context is the parsing context currently being used
     * @param namespace is the xml namespace the current element is in
     * @param name is the name of the current xml element
     * @param attributes is the attribute list of the current element
     */
    public void begin(
    Context context, 
    String namespace, String name, Attributes attributes) 
    throws ParseException {
        // Identify the name of the class to instantiate
        String realClassName = className;
        if (attributeName != null) {
            String value = attributes.getValue(attributeName);
            if (value != null) {
                realClassName = value;
            }
        }
        
        Log log = context.getLogger();
        if (log.isDebugEnabled()) {
            log.debug("[CreateObjectAction]{" + context.getMatchPath() +
                    "}New " + realClassName);
        }

        // Instantiate the new object and push it on the context stack
        try {
            Class clazz = context.getClassLoader().loadClass(realClassName);
            Object instance = clazz.newInstance();
            context.push(instance);
        } catch(Exception ex) {
            throw new ParseException(
                "Unable to create instance of class '" + realClassName + "'",
                ex);
        }
    }


    /**
     * Process the end of this element.
     */
    public void end(Context context, String namespace, String name) 
    throws ParseException {
        Object top = context.pop();
        Log log = context.getLogger();
        if (log.isDebugEnabled()) {
            log.debug("[CreateObjectAction]{" + context.getMatchPath() +
                    "} Pop " + top.getClass().getName());
        }
    }

    /**
     * Render a printable version of this Rule.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("CreateObjectAction[");
        sb.append("className=");
        sb.append(className);
        sb.append(", attributeName=");
        sb.append(attributeName);
        sb.append("]");
        return (sb.toString());
    }
}
