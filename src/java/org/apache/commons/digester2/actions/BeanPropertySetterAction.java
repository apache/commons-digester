/* $Id: ActionBeanPropertySetter.java,v 1.20 2004/05/10 06:30:06 skitching Exp $
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


import java.beans.PropertyDescriptor;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;

import org.apache.commons.digester2.Context;
import org.apache.commons.digester2.AbstractAction;
import org.apache.commons.digester2.ParseException;

/**
 * <p> Action which sets a bean property on the top object to the body text.</p>
 *
 * <p> The property set:</p>
 * <ul><li>can be specified when the rule is created</li>
 * <li>or can match the current element when the rule is called.</li></ul>
 *
 * <p> Using the second method and the {@link ExtendedRuleManager} child match
 * pattern, all the child elements can be automatically mapped to properties
 * on the parent object.</p>
 */

public class BeanPropertySetterAction extends AbstractAction {

    // ----------------------------------------------------------- Constructors

    /**
     * <p>Construct instance that sets the given property from the body text.</p>
     *
     * @param propertyName name of property to set
     */
    public BeanPropertySetterAction(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * <p>Construct instance that automatically sets a property from the body text.
     *
     * <p> This construct creates an action that sets the property
     * on the top object named the same as the current element.
     */
    public BeanPropertySetterAction() {
        this((String)null);
    }
    
    // ----------------------------------------------------- Instance Variables

    /**
     * Set this property on the top object.
     */
    protected String propertyName = null;

    /**
     * The body text used to set the property.
     */
    protected String bodyText = null;

    // --------------------------------------------------------- Public Methods

    /**
     * Process the body text of this element.
     *
     * @param context is the current parse context.
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the element has no namespace
     * @param name the local name of the element.
     * @param text The text of the body of this element
     */
    public void body(Context context, String namespace, String name, String text)
        throws ParseException {

        // log some debugging information
        Log log = context.getLogger();
        if (log.isDebugEnabled()) {
            log.debug(
                "[ActionBeanPropertySetter] Called with text '" + text + "'"
                + " at path '" + context.getMatchPath() + "'");
        }

        bodyText = text.trim();
    }

    /**
     * Process the end of this element.
     *
     * @param context is the current parse context.
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the element has no namespace
     * @param name the local name of the element.
     *
     * @exception NoSuchMethodException if the bean does not
     *  have a writeable property of the specified name
     */
    public void end(Context context, String namespace, String name) 
        throws ParseException {

        String property = propertyName;

        if (property == null) {
            // If we don't have a specific property name,
            // use the element name.
            property = name;
        }

        // Get a reference to the top object
        Object top = context.peek();

        // log some debugging information
        Log log = context.getLogger();
        if (log.isDebugEnabled()) {
            log.debug(
                "[ActionBeanPropertySetter]"
                + " path='" + context.getMatchPath() + "'"
                + ": class='" + top.getClass().getName() + "'"
                + ": property '" + property + "'" 
                + " with text '" + bodyText + "'");
        }

        // Force an exception if the property does not exist
        // (BeanUtils.setProperty() silently returns in this case)
        if (top instanceof DynaBean) {
            DynaProperty desc =
                ((DynaBean) top).getDynaClass().getDynaProperty(property);
            if (desc == null) {
                throw new ParseException
                    ("DynaBean has no property named " + property);
            }
        } else /* this is a standard JavaBean */ {
            PropertyDescriptor desc;
            try {
                desc = PropertyUtils.getPropertyDescriptor(top, property);
            } catch(Exception ex) {
                throw new ParseException(
                    "Unable to access properties for bean of class '"
                    + top.getClass().getName() + "'", ex);
            }
            if (desc == null) {
                throw new ParseException(
                    "Bean of class '" + top.getClass().getName() + "'"
                    + " has no property named '" + property + "'");
            }
        }

        // Set the property (with conversion as necessary)
        try {
            BeanUtils.setProperty(top, property, bodyText);
        } catch(Exception ex) {
            throw new ParseException(
                "Unable to set property '" + property + "' for bean of class '"
                + top.getClass().getName() + "'", ex);
        }
    }

    /**
     * Init before parsing commences (just in case a previous parse has
     * failed, leaving garbage).
     */
    public void startParse(Context context) throws ParseException {
        bodyText = null;
    }

    /**
     * Clean up after parsing is complete.
     */
    public void finishParse(Context context) throws ParseException {
        bodyText = null;
    }

    /**
     * Render a printable version of this Actuib.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("ActionBeanPropertySetter[");
        sb.append("propertyName=");
        sb.append(propertyName);
        sb.append("]");
        return (sb.toString());
    }
}
