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
 * An Action which sets a property on the object on the top of the
 * digester object stack to the body text from the matched element.
 * <p>
 * The name of the property to set:
 * <ul>
 * <li>can be specified when the action is created, or</li>
 * <li>can be the name of the matched xml element. This is useful when the
 *  Action is used with a pattern that can match multiple elements, or
 *  can be used just because it requires less parameters!</li>
 * </ul>
 * <p>
 * The text contained in the xml element has all leading and trailing
 * whitespace removed from it before the property setter method is invoked
 * on the target object.
 * <p>
 * Example:<br>
 * Given rules:
 * <pre>
 *  digester.addRule("/person", new CreateObjectAction(Person.class));
 *  digester.addRule("/person/name", new BeanPropertySetterAction());
 * </pre>
 * the input
 * <pre>
 *  [person]
 *    [name]myname[/name]
 *  [/person]
 * </pre>
 * causes a Person object to be created, then setName("myname") is called on it.
 * <p>
 * If you wish to map several child elements onto object properties, then you
 * may wish to consider using the SetNestedPropertiesAction instead.
 */

public class BeanPropertySetterAction extends AbstractAction {

    // ----------------------------------------------------- Instance Variables

    /**
     * Set this property on the top object.
     */
    protected String propertyName = null;

    /**
     * The identifier of the context "scratch stack" used to store the
     * body text passed to the body method, so that it can be used in
     * the end method. This stack is per-action-instance, so that other
     * instances of this class can't ever interfere with the data on this
     * stack.
     */
    protected final Context.StackId BODY_TEXT_STACK =
        new Context.StackId(BeanPropertySetterAction.class, "bodytext", this);

    // -----------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------

    /**
     * Construct an instance that uses the xml element name to determine
     * which property to set on the target object.
     */
    public BeanPropertySetterAction() {
        this((String)null);
    }

    /**
     * Construct an instance that sets the given property.
     *
     * @param propertyName name of property to set
     */
    public BeanPropertySetterAction(String propertyName) {
        this.propertyName = propertyName;
    }

    // ---------------------------------------------------------
    // Public Methods
    // ---------------------------------------------------------

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

        context.push(BODY_TEXT_STACK, text.trim());
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

        String bodyText = (String) context.pop(BODY_TEXT_STACK);

        String property = propertyName;
        if (property == null) {
            // If we don't have a specific property name, use the element name.
            //
            // TODO: implement conversion of xml-hyphenated-names to
            // javaCamelCaseNames. See SetPropertiesAction
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
