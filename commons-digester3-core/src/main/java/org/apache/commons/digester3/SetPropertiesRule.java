package org.apache.commons.digester3;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static java.lang.String.format;
import static org.apache.commons.beanutils.BeanUtils.populate;
import static org.apache.commons.beanutils.PropertyUtils.isWriteable;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

/**
 * <p>
 * Rule implementation that sets properties on the object at the top of the stack, based on attributes with
 * corresponding names.
 * </p>
 * <p>
 * This rule supports custom mapping of attribute names to property names. The default mapping for particular attributes
 * can be overridden by using {@link #SetPropertiesRule(String[] attributeNames, String[] propertyNames)}. This allows
 * attributes to be mapped to properties with different names. Certain attributes can also be marked to be ignored.
 * </p>
 */
public class SetPropertiesRule
    extends Rule
{

    private final Map<String, String> aliases = new HashMap<>();

    /**
     * Used to determine whether the parsing should fail if an property specified in the XML is missing from the bean.
     * Default is true for backward compatibility.
     */
    private boolean ignoreMissingProperty = true;

    /**
     * Base constructor.
     */
    public SetPropertiesRule()
    {
        // nothing to set up
    }

    /**
     * Constructor allows attribute to property mapping to be overridden.
     *
     * @param aliases attribute to property mapping
     * @since 3.0
     */
    public SetPropertiesRule( final Map<String, String> aliases )
    {
        if ( aliases != null && !aliases.isEmpty() )
        {
            this.aliases.putAll( aliases );
        }
    }

    /**
     * <p>
     * Convenience constructor overrides the mapping for just one property.
     * </p>
     * <p>
     * For details about how this works, see {@link #SetPropertiesRule(String[] attributeNames, String[] propertyNames)}
     * .
     * </p>
     *
     * @param attributeName map this attribute
     * @param propertyName to a property with this name
     */
    public SetPropertiesRule( final String attributeName, final String propertyName )
    {
        aliases.put( attributeName, propertyName );
    }

    /**
     * <p>
     * Constructor allows attribute to property mapping to be overridden.
     * </p>
     * <p>
     * Two arrays are passed in. One contains the attribute names and the other the property names. The attribute name /
     * property name pairs are match by position In order words, the first string in the attribute name list matches to
     * the first string in the property name list and so on.
     * </p>
     * <p>
     * If a property name is null or the attribute name has no matching property name, then this indicates that the
     * attribute should be ignored.
     * </p>
     * <b>Example One</b>
     * <p>
     * The following constructs a rule that maps the {@code alt-city} attribute to the {@code city} property
     * and the {@code alt-state} to the {@code state} property. All other attributes are mapped as usual using
     * exact name matching.
     * </p>
     * <pre>{@code
     *      SetPropertiesRule(
     *                new String[] {"alt-city", "alt-state"},
     *                new String[] {"city", "state"});
     * }</pre>
     * <b>Example Two</b>
     * <p>
     * The following constructs a rule that maps the {@code class} attribute to the {@code className}
     * property. The attribute {@code ignore-me} is not mapped. All other attributes are mapped as usual using
     * exact name matching.
     * </p>
     * <pre>{@code
     *      SetPropertiesRule(
     *                new String[] {"class", "ignore-me"},
     *                new String[] {"className"});
     * }</pre>
     *
     * @param attributeNames names of attributes to map
     * @param propertyNames names of properties mapped to
     */
    public SetPropertiesRule( final String[] attributeNames, final String[] propertyNames )
    {
        for ( int i = 0, size = attributeNames.length; i < size; i++ )
        {
            String propName = null;
            if ( i < propertyNames.length )
            {
                propName = propertyNames[i];
            }

            aliases.put( attributeNames[i], propName );
        }
    }

    /**
     * Add an additional attribute name to property name mapping. This is intended to be used from the XML rules.
     *
     * @param attributeName the attribute name has to be mapped
     * @param propertyName the target property name
     */
    public void addAlias( final String attributeName, final String propertyName )
    {
        aliases.put( attributeName, propertyName );
    }

    @Override
    public void begin( final String namespace, final String name, final Attributes attributes )
        throws Exception
    {
        // Build a set of attribute names and corresponding values
        final Map<String, String> values = new HashMap<>();

        for ( int i = 0; i < attributes.getLength(); i++ )
        {
            String attributeName = attributes.getLocalName( i );
            if ( "".equals( attributeName ) )
            {
                attributeName = attributes.getQName( i );
            }
            final String value = attributes.getValue( i );

            // alias lookup has complexity O(1)
            if ( aliases.containsKey( attributeName ) )
            {
                attributeName = aliases.get( attributeName );
            }

            if ( getDigester().getLogger().isDebugEnabled() )
            {
                getDigester().getLogger().debug( format( "[SetPropertiesRule]{%s} Setting property '%s' to '%s'",
                                                         getDigester().getMatch(),
                                                         attributeName,
                                                         attributeName ) );
            }

            if ( !ignoreMissingProperty && attributeName != null )
            {
                // The BeanUtils.populate method silently ignores items in
                // the map (ie XML entities) which have no corresponding
                // setter method, so here we check whether each XML attribute
                // does have a corresponding property before calling the
                // BeanUtils.populate method.
                //
                // Yes having the test and set as separate steps is ugly and
                // inefficient. But BeanUtils.populate doesn't provide the
                // functionality we need here, and changing the algorithm which
                // determines the appropriate setter method to invoke is
                // considered too risky.
                //
                // Using two different classes (PropertyUtils vs BeanUtils) to
                // do the test and the set is also ugly; the code paths
                // are different which could potentially lead to trouble.
                // However the BeanUtils/PropertyUtils code has been carefully
                // compared and the PropertyUtils functionality does appear
                // compatible so we'll accept the risk here.

                final Object top = getDigester().peek();
                final boolean test = isWriteable( top, attributeName );
                if ( !test )
                {
                    throw new NoSuchMethodException( "Property " + attributeName + " can't be set" );
                }
            }

            if ( attributeName != null )
            {
                values.put( attributeName, value );
            }
        }

        // Populate the corresponding properties of the top object
        final Object top = getDigester().peek();
        if ( getDigester().getLogger().isDebugEnabled() )
        {
            if ( top != null )
            {
                getDigester().getLogger().debug( format( "[SetPropertiesRule]{%s} Set '%s' properties",
                                                         getDigester().getMatch(),
                                                         top.getClass().getName() ) );
            }
            else
            {
                getDigester().getLogger().debug( format( "[SetPropertiesRule]{%s} Set NULL properties",
                                                         getDigester().getMatch() ) );
            }
        }
        populate( top, values );
    }

    /**
     * <p>
     * Are attributes found in the XML file without matching properties to be ignored?
     * </p>
     * <p>
     * If false, the parsing will interrupt with an {@code NoSuchMethodException} if a property specified in the
     * XML is not found. The default is true.
     * </p>
     *
     * @return true if skipping the unmatched attributes.
     */
    public boolean isIgnoreMissingProperty()
    {
        return this.ignoreMissingProperty;
    }

    /**
     * Sets whether attributes found in the XML file without matching properties should be ignored. If set to false,
     * the parsing will throw an {@code NoSuchMethodException} if an unmatched attribute is found. This allows to trap
     * misspellings in the XML file.
     *
     * @param ignoreMissingProperty false to stop the parsing on unmatched attributes.
     */
    public void setIgnoreMissingProperty( final boolean ignoreMissingProperty )
    {
        this.ignoreMissingProperty = ignoreMissingProperty;
    }

    @Override
    public String toString()
    {
        return format( "SetPropertiesRule[aliases=%s, ignoreMissingProperty=%s]", aliases, ignoreMissingProperty );
    }

}
