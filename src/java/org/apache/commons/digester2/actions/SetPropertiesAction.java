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
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;

import org.apache.commons.digester2.Context;
import org.apache.commons.digester2.AbstractAction;
import org.apache.commons.digester2.ParseException;

/**
 * <p>An Action that sets properties on the object at the top of the
 * stack, based on attributes with corresponding names.</p>
 *
 * <p>By default, any xml attribute xyz='value' causes a call to property-setter
 * method setXyz(value) on the top object on the stack. If the target method
 * does not take a String parameter, then the BeanUtils library attempts
 * to convert the String value of the xml attribute to the required type.</p>
 *
 * <p>Custom mapping of attribute names to property names can also be done.
 * The default mapping for particular attributes can be overridden by using
 * {@link #ActionSetProperties(String[] attributeNames, String[] propertyNames)}.
 * This allows attributes to be mapped to properties with different names.
 * Certain attributes can also be marked to be ignored, by specifying the
 * target property to be null.</p>
 *
 * <p>XML Attributes that are not in the default namespace are ignored.</p>
 */

public class SetPropertiesAction extends AbstractAction {

    private HashMap customMap = null;

    // -----------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------

    /**
     * Base constructor.
     */
    public SetPropertiesAction() {
        // nothing to set up
    }

    /**
     * <p>Constructor which allows the default attribute->property mapping to
     * be overriden.</p>
     *
     * <p>The keys of the map are xml attribute names, and the associated values
     * are the java property name to map that attribute to. If the value
     * associated with an attribute name is null then the attibute will be
     * ignored.</p>
     *
     * <h5>Example</h5>
     * <p> The following constructs a rule that maps the <code>class</code>
     * attribute to the <code>className</code> property. The attribute
     * <code>ignore</code> is not mapped, and will therefore be ignored rather
     * than be passed to a setIgnore method (the default behaviour). All other
     * attributes are mapped as usual using exact name matching.
     * <code><pre>
     *      HashMap map = new HashMap();
     *      map.put("class", "className");
     *      map.put("ignore", null);
     *      Action action = new SetPropertiesAction(map);
     * </pre></code></p>
     *
     * <p>See also {@link #addAlias} which allows the custom mapping to be
     * modified after the SetPropertiesAction has been constructed.</p>
     *
     * @param customMap is a map. The map is copied, so future changes to
     * the map will not affect this object. 
     */
    public SetPropertiesAction(Map customMap) {
        this.customMap = new HashMap(customMap);
    }

    /**
     * <p>Convenience constructor which overrides the default attribute->property
     * mapping for just one property.</p>
     *
     * <p>For details about how this works, see
     *   {@link #ActionSetProperties(Map customMappings)}.</p>
     *
     * @param attributeName map this attribute. Must not be null.
     * @param propertyName to a property with this name. May be null.
     */
    public SetPropertiesAction(String attributeName, String propertyName) {
        customMap = new HashMap(1);
        customMap.put(attributeName, propertyName);
    }

    /**
     * <p>Constructor which allows the default attribute->property mapping to
     * be overriden.</p>
     *
     * <p>Two arrays are passed in. One contains the attribute names and the
     * other the property names. The attribute name / property name pairs are
     * matched by position In order words, the first string in the attribute
     * name list maps to the first string in the property name list and so on.
     * </p>
     *
     * <p>If a property name is null or the attribute name has no matching
     * property name (ie the property array is shorter than the attribute array)
     * then the attibute will be ignored.</p>
     *
     * <h5>Example One</h5>
     * <p> The following constructs a rule that maps the <code>alt-city</code>
     * attribute to the <code>city</code> property and the <code>alt-state</code>
     * to the <code>state</code> property.
     * All other attributes are mapped as usual using exact name matching.
     * <code><pre>
     *      Action action = new SetPropertiesAction(
     *                new String[] {"alt-city", "alt-state"},
     *                new String[] {"city", "state"});
     * </pre></code>
     *
     * <h5>Example Two</h5>
     * <p> The following constructs a rule that maps the <code>class</code>
     * attribute to the <code>className</code> property. The attribute
     * <code>ignore</code> is not mapped, and will therefore be ignored rather
     * than be passed to a setIgnore method (the default behaviour). All other
     * attributes are mapped as usual using exact name matching.
     * <code><pre>
     *      Action action = new SetPropertiesAction(
     *                new String[] {"class", "ignore"},
     *                new String[] {"className"});
     * </pre></code>
     *
     * @param attributeNames names of attributes to map
     * @param propertyNames names of properties mapped to
     */
    public SetPropertiesAction(String[] attributeNames, String[] propertyNames) {
        int nAttributes = attributeNames.length;
        int nProperties = propertyNames.length;

        customMap = new HashMap(nAttributes);

        for(int i=0; i<nAttributes; ++i) {
            if (i < nProperties) {
                customMap.put(attributeNames[i], propertyNames[i]);
            } else {
                customMap.put(attributeNames[i], null);
            }
        }
    }

    // ---------------------------------------------------------
    // Public Methods
    // ---------------------------------------------------------

    /**
     * <p>Add an additional attribute name to property name mapping.
     * This is particularly useful for the xmlrules optional module.</p>
     *
     * <p>See {@link #SetPropertiesAction(Map customMap)}.
     */
    public void addAlias(String attributeName, String propertyName) {
        if (customMap == null) {
            customMap = new HashMap();
        }
        customMap.put(attributeName, propertyName);
    }

    // ---------------------------------------------------------
    // Action Methods
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

        // Build a set of property names and corresponding values
        HashMap values = new HashMap();

        // for each xml attribute
        for (int i = 0; i < attributes.getLength(); i++) {
            if (attributes.getURI(i).length() > 0) {
                // currently we ignore any attributes with namespaces
                if (log.isDebugEnabled()) {
                    log.debug(
                        "[SetProperties]{" + context.getMatchPath() +
                        "} Ignoring namespaced xml attribute "
                        + attributes.getLocalName(i));
                }
            } else {
                String attrName = attributes.getLocalName(i);
                if ("".equals(attrName)) {
                    attrName = attributes.getQName(i);
                }
                String value = attributes.getValue(i);

                String propName;

                // We'll now check for custom mappings. Note that propName
                // can be set to null by this...
                if ((customMap != null) && customMap.containsKey(attrName)) {
                    propName = customMap.get(attrName).toString();
                } else {
                    // if attrName contains a hyphen, it will be converted
                    // to camelCase, otherwise we just try to use the
                    // unmodified attrName as the propName.
                    propName = convertHyphenatedToCamelCase(attrName);
                }

                if (propName != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("[SetProperties]{" + context.getMatchPath() +
                                "} Setting property '" + propName + "' to '" +
                                value + "'");
                    }
    
                    values.put(propName, value);
                }
            }
        }

        // Populate the corresponding properties of the top object
        Object target = context.peek();
        if (log.isDebugEnabled()) {
            if (target != null) {
                log.debug("[ActionSetProperties]{" + context.getMatchPath() +
                                   "} Set " + target.getClass().getName() +
                                   " properties");
            } else {
                log.debug("[ActionSetProperties]{" + context.getMatchPath() +
                                   "} Set NULL properties");
            }
        }
        
        try {
            BeanUtils.populate(target, values);
        } catch(IllegalAccessException ex) {
            throw new ParseException(ex);
        } catch(java.lang.reflect.InvocationTargetException ex) {
            throw new ParseException(ex);
        }
    }


    // ---------------------------------------------------------
    // Private Methods
    // ---------------------------------------------------------

    /**
     * <p>This method is intended to convert xml hypenated names
     * into javabean names.</p>
     *
     * <p>Traditionally, xml element and attribute names that are 
     * formed from multiple words use hyphens to separate the words,
     * for example a-long-attribute-name. However the Java tradition
     * is to use camel-case, eg aLongAttributeName. This method
     * converts from the xml to the java convention.</p>
     *
     * <p>If src contains no hyphens then a reference to the same object
     * is returned.</p>
     *
     * <p>If src ends in a hyphen, then it is stripped.</p>
     *
     * @param src is the string to be converted. Must not be null.
     */
    private String convertHyphenatedToCamelCase(String src) {
        // check whether there is a hyphen in this string or not 
        int firstPos = src.indexOf("-");
        if (firstPos == -1) {
            return src;
        }
        
        int srcLen = src.length();
        StringBuffer buf = new StringBuffer(srcLen);
        
        // bulk append up until the first hyphen, as we already know
        // where it is.
        buf.append(src.substring(0, firstPos));
        
        // now it is easiest to simply step through char-by-char until
        // the end of the string.
        boolean cap = true;
        for(int i=firstPos+1; i<srcLen; ++i) {
            char c = src.charAt(i);
            if (c == '-') {
                cap = true;
            } else if (cap) {
                buf.append(Character.toUpperCase(c));
                cap = false;
            } else {
                buf.append(c);
            }
        }
        
        return buf.toString();
    }

    // ---------------------------------------------------------
    // Other Methods
    // ---------------------------------------------------------

    /**
     * Render a printable version of this Rule.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("SetPropertiesAction[");
        sb.append("]");
        return (sb.toString());
    }
}
