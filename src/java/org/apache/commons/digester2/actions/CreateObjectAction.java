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


import org.xml.sax.Attributes;
import org.apache.commons.logging.Log;


import org.apache.commons.digester2.Context;
import org.apache.commons.digester2.AbstractAction;
import org.apache.commons.digester2.ParseException;

/**
 * Rule implementation that creates a new object and pushes it
 * onto the object stack.  When the element is complete, the
 * object will be popped
 */

public class CreateObjectAction extends AbstractAction {

    // ----------------------------------------------------------- Constructors

    /**
     * Construct an object create rule with the specified class name.
     *
     * @param className Java class name of the object to be created
     */
    public CreateObjectAction(String className) {

        this(className, (String) null);

    }


    /**
     * Construct an object create rule with the specified class.
     *
     * @param clazz Java class name of the object to be created
     */
    public CreateObjectAction(Class clazz) {

        this(clazz.getName(), (String) null);

    }


    /**
     * Construct an object create rule with the specified class name and an
     * optional attribute name containing an override.
     *
     * @param className Java class name of the object to be created
     * @param attributeName Attribute name which, if present, contains an
     *  override of the class name to create
     */
    public CreateObjectAction(String className,
                            String attributeName) {

        this.className = className;
        this.attributeName = attributeName;

    }


    /**
     * Construct an object create rule with the specified class and an
     * optional attribute name containing an override.
     *
     * @param attributeName Attribute name which, if present, contains an
     * @param clazz Java class name of the object to be created
     *  override of the class name to create
     */
    public CreateObjectAction(String attributeName,
                            Class clazz) {

        this(clazz.getName(), attributeName);

    }

    // ----------------------------------------------------- Instance Variables


    /**
     * The attribute containing an override class name if it is present.
     */
    protected String attributeName = null;


    /**
     * The Java class name of the object to be created.
     */
    protected String className = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Process the beginning of this element.
     *
     * @param attributes The attribute list of this element
     */
    public void begin(Context context, String namespace, String name, Attributes attributes) 
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
