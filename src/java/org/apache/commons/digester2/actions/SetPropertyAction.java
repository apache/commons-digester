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
import org.xml.sax.Attributes;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;

import org.apache.commons.digester2.Context;
import org.apache.commons.digester2.AbstractAction;
import org.apache.commons.digester2.ParseException;

/**
 * An Action which sets a single property on the object on the top of the
 * digester object stack from the body text contained in the matched xml
 * element.
 * <p>
 * The name of the property to set:
 * <ul>
 * <li>can be taken from the value of some xml attribute on the same element.</li>
 * <li>can be the name of the matched xml element. This is useful when the
 *  Action is used with a pattern that can match multiple elements, or
 *  can be used just because it requires less parameters!</li>
 * <li>can be specified directly when the action is created</li>
 * </ul>
 * <p>
 * The text contained in the xml element has all leading and trailing
 * whitespace removed from it before the property setter method is invoked
 * on the target object.
 * <p>
 * Example 1:<br>
 * Given rules:
 * <pre>
 *  digester.addRule("/person", new CreateObjectAction(Person.class));
 *  digester.addRule("/person/property", new SetPropertyAction("target"));
 * </pre>
 * the input
 * <pre>
 *  [person]
 *    [property target='firstName']myname[/property]
 *  [/person]
 * </pre>
 * causes a Person object to be created, then setFirstName("myname") is called.
 * on it.
 * <p>
 * Example 2:<br>
 * Given rules:
 * <pre>
 *  digester.addRule("/person", new CreateObjectAction(Person.class));
 *  digester.addRule("/person/name", new SetPropertyAction());
 * </pre>
 * the input
 * <pre>
 *  [person]
 *    [name]myname[/name]
 *  [/person]
 * </pre>
 * causes a Person object to be created, then setName("myname") is called on it.
 * If you wish to map several child elements onto object properties, then you
 * may wish to consider using the SetNestedPropertiesAction instead.
 * <p>
 * Example 3:<br>
 * Given rules:
 * <pre>
 *  digester.addRule("/person", new CreateObjectAction(Person.class));
 *  digester.addRule("/person/name", new SetPropertyAction("firstName"));
 * </pre>
 * the input
 * <pre>
 *  [person]
 *    [name]myname[/name]
 *  [/person]
 * </pre>
 * causes a Person object to be created, then setFirstName("myname") is called
 * on it. Note that equivalent functionality can be implemented using the
 * CallMethodRule, but that this action is simpler and more efficient for this
 * particular usage.
 * <p>
 * Users of Digester 1.x should not that this class combines the functionality
 * of SetPropertyRule and BeanPropertySetterRule.
 */

public class SetPropertyAction extends AbstractAction {

    // -----------------------------------------------------
    // Instance Variables
    // -----------------------------------------------------

    /**
     * Identify the stack that holds the target property name when
     * the user selected the [property name="xxx"]value[/property] form
     * of this action.
     */
    protected Context.StackId PROPERTY_NAME_STACK
        = new Context.StackId(SetPropertyAction.class, "PropertyName", this);

    /**
     * Set this property on the top object (unless overridden by xml attribute)
     */
    protected String propertyName = null;

    /**
     * Select the target property from this xml attribute, if present.
     */
    protected String xmlAttributeNS = null;
    protected String xmlAttributeName = null;

    // -----------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------

    /**
     * Construct an instance that uses the xml element name to determine
     * which property to set on the target object.
     */
    public SetPropertyAction() {
        this((String)null);
    }

    /**
     * Construct an instance that sets the given property.
     *
     * @param propertyName name of property to set
     */
    public SetPropertyAction(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * Construct an instance that uses the value of the specified xml attribute
     * to select which java property to set. If the xml attribute is not
     * present, and defaultPropertyName is not null, then the specified
     * default property is set. If the default property is also null, then
     * this is regarded as an error.
     *
     * @param xmlAttributeNS is the namespace that the xml attribute is
     * expected to be in. If no namespace is expected, pass null.
     *
     * @param xmlAttributeName is the name of the xml attribute that will
     * contain the target property name. Must be non-null.
     *
     * @param defaultPropertyName is the name of the property to use if
     * there is no xml attribute present with the name specified in
     * xmlAttributeName. If this value is null, then it is considered an
     * error if the matched xml element does not have the expected xml
     * attribute.
     */
    public SetPropertyAction(
    String xmlAttributeNS, String xmlAttributeName,
    String defaultPropertyName) {
        this.xmlAttributeNS = xmlAttributeNS;
        this.xmlAttributeName = xmlAttributeName;
        this.propertyName = defaultPropertyName;
    }

    // ---------------------------------------------------------
    // Public Methods
    // ---------------------------------------------------------

    /**
     * Process the beginning of this element.
     *
     * @param context The object on which all parsing state is stored.
     * @param namespace The xml namespace the current element is in.
     * @param name The local name of the current element.
     * @param attributes The attribute list of the current element
     */
    public void begin(
    Context context,
    String namespace, String elementName,
    Attributes attributes)
    throws ParseException {
        Log log = context.getLogger();
        if (xmlAttributeName != null) {
            // the property to set is determined from an xml attribute
            String realPropertyName =
                attributes.getValue(xmlAttributeNS, xmlAttributeName);

            if (realPropertyName == null) {
                // use default

                if (propertyName == null) {
                    // no default!
                    throw new ParseException(
                        "Element [" + elementName + "]"
                        + " has no xml attribute [" + xmlAttributeName + "]"
                        + " and no default target property.");
                }
                realPropertyName = propertyName;
            }
            context.push(PROPERTY_NAME_STACK, realPropertyName);
        }
    }

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
        Log log = context.getLogger();

        text = text.trim();

        String targetProperty = propertyName;
        if (xmlAttributeName != null) {
            // the target property to set will have been decided in the
            // begin method...
            targetProperty = (String) context.peek(PROPERTY_NAME_STACK);
        } else if (targetProperty == null) {
            // If we don't have a specific property name, use the element name.
            //
            // TODO: implement conversion of xml-hyphenated-names to
            // javaCamelCaseNames. See SetPropertiesAction
            targetProperty = name;
        }

        // Get a reference to the top object
        Object top = context.peek();

        // log some debugging information
        if (log.isDebugEnabled()) {
            log.debug(
                "[ActionBeanPropertySetter]"
                + " path='" + context.getMatchPath() + "'"
                + ": class='" + top.getClass().getName() + "'"
                + ": property '" + targetProperty + "'"
                + " with text '" + text + "'");
        }

        // Force an exception if the property does not exist
        // (BeanUtils.setProperty() silently returns in this case)
        //
        // TODO: check this works correctly for read-only properties
        if (top instanceof DynaBean) {
            DynaProperty desc =
                ((DynaBean) top).getDynaClass().getDynaProperty(targetProperty);
            if (desc == null) {
                throw new ParseException
                    ("DynaBean has no property named " + targetProperty);
            }
        } else /* this is a standard JavaBean */ {
            PropertyDescriptor desc;
            try {
                desc = PropertyUtils.getPropertyDescriptor(top, targetProperty);
            } catch(Exception ex) {
                throw new ParseException(
                    "Unable to access properties for bean of class '"
                    + top.getClass().getName() + "'", ex);
            }
            if (desc == null) {
                throw new ParseException(
                    "Bean of class '" + top.getClass().getName() + "'"
                    + " has no property named '" + targetProperty + "'");
            }
        }

        // Set the property (with conversion as necessary)
        try {
            BeanUtils.setProperty(top, targetProperty, text);
        } catch(Exception ex) {
            throw new ParseException(
                "Unable to set property '" + targetProperty + "' for bean of class '"
                + top.getClass().getName() + "'", ex);
        }
    }

    /**
     * Process the end of this element.
     */
    public void end(Context context, String namespace, String name)
    throws ParseException {
        Log log = context.getLogger();
        if (xmlAttributeName != null) {
            // discard the target property extracted from the xml attribute.
            context.pop(PROPERTY_NAME_STACK);
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
