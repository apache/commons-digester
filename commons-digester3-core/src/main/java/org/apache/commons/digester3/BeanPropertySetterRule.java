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
import static org.apache.commons.beanutils.BeanUtils.setProperty;
import static org.apache.commons.beanutils.PropertyUtils.getPropertyDescriptor;

import java.beans.PropertyDescriptor;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.xml.sax.Attributes;

/**
 * <p>
 * Rule implements sets a bean property on the top object to the body text.
 * </p>
 * <p>
 * The property set:
 * </p>
 * <ul>
 * <li>can be specified when the rule is created</li>
 * <li>or can match the current element when the rule is called.</li>
 * </ul>
 * <p>
 * Using the second method and the {@link ExtendedBaseRules} child match pattern, all the child elements can be
 * automatically mapped to properties on the parent object.
 * </p>
 */
public class BeanPropertySetterRule
    extends Rule
{

    /**
     * Sets this property on the top object.
     */
    private String propertyName;

    /**
     * Extract the property name from attribute
     */
    private String propertyNameFromAttribute;

    /**
     * The body text used to set the property.
     */
    private String bodyText;

    /**
     * <p>
     * Constructs rule that automatically sets a property from the body text.
     * <p>
     * This construct creates a rule that sets the property on the top object named the same as the current element.
     */
    public BeanPropertySetterRule()
    {
        this( null );
    }

    /**
     * <p>
     * Constructs rule that sets the given property from the body text.
     * </p>
     *
     * @param propertyName name of property to set
     */
    public BeanPropertySetterRule( final String propertyName )
    {
        this.propertyName = propertyName;
    }

    @Override
    public void begin( final String namespace, final String name, final Attributes attributes )
        throws Exception
    {
        if ( propertyNameFromAttribute != null )
        {
            propertyName = attributes.getValue( propertyNameFromAttribute );

            getDigester().getLogger().warn( format( "[BeanPropertySetterRule]{%s} Attribute '%s' not found in matching element '%s'",
                                                    getDigester().getMatch(), propertyNameFromAttribute, name ) );
        }
    }

    @Override
    public void body( final String namespace, final String name, final String text )
        throws Exception
    {
        // log some debugging information
        if ( getDigester().getLogger().isDebugEnabled() )
        {
            getDigester().getLogger().debug( format( "[BeanPropertySetterRule]{%s} Called with text '%s'",
                                                     getDigester().getMatch(),
                                                     text ) );
        }

        bodyText = text.trim();
    }

    @Override
    public void end( final String namespace, final String name )
        throws Exception
    {
        String property = propertyName;

        if ( property == null )
        {
            // If we don't have a specific property name,
            // use the element name.
            property = name;
        }

        // Get a reference to the top object
        final Object top = getDigester().peek();

        // log some debugging information
        if ( getDigester().getLogger().isDebugEnabled() )
        {
            getDigester().getLogger().debug( format( "[BeanPropertySetterRule]{%s} Set %s property %s with text %s",
                                                     getDigester().getMatch(),
                                                     top.getClass().getName(),
                                                     property,
                                                     bodyText ) );
        }

        // Force an exception if the property does not exist
        // (BeanUtils.setProperty() silently returns in this case)
        if ( top instanceof DynaBean )
        {
            final DynaProperty desc = ( (DynaBean) top ).getDynaClass().getDynaProperty( property );
            if ( desc == null )
            {
                throw new NoSuchMethodException( "Bean has no property named " + property );
            }
        }
        else
        /* this is a standard JavaBean */
        {
            final PropertyDescriptor desc = getPropertyDescriptor( top, property );
            if ( desc == null )
            {
                throw new NoSuchMethodException( "Bean has no property named " + property );
            }
        }

        // Set the property (with conversion as necessary)
        setProperty( top, property, bodyText );
    }

    @Override
    public void finish()
        throws Exception
    {
        bodyText = null;
    }

    /**
     * Returns the body text used to set the property.
     *
     * @return The body text used to set the property
     */
    protected String getBodyText()
    {
        return bodyText;
    }

    /**
     * Returns the property name associated to this setter rule.
     *
     * @return The property name associated to this setter rule
     */
    public String getPropertyName()
    {
        return propertyName;
    }

    /**
     * Sets the attribute name from which the property name has to be extracted.
     *
     * @param propertyNameFromAttribute the attribute name from which the property name has to be extracted.
     * @since 3.0
     */
    public void setPropertyNameFromAttribute( final String propertyNameFromAttribute )
    {
        this.propertyNameFromAttribute = propertyNameFromAttribute;
    }

    @Override
    public String toString()
    {
        return format( "BeanPropertySetterRule[propertyName=%s]", propertyName );
    }

}
