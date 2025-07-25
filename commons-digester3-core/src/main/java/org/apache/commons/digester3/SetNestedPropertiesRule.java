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
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.apache.commons.beanutils.BeanUtils.setProperty;
import static org.apache.commons.beanutils.PropertyUtils.getPropertyDescriptor;

import static java.lang.String.format;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.logging.Log;
import org.xml.sax.Attributes;

/**
 * <p>
 * Rule implementation that sets properties on the object at the top of the stack, based on child elements with names
 * matching properties on that object.
 * </p>
 * <p>
 * Example input that can be processed by this rule:
 * </p>
 *
 * <pre>
 *   [widget]
 *    [height]7[/height]
 *    [width]8[/width]
 *    [label]Hello, world[/label]
 *   [/widget]
 * </pre>
 * <p>
 * For each child element of [widget], a corresponding setter method is located on the object on the top of the digester
 * stack, the body text of the child element is converted to the type specified for the (sole) parameter to the setter
 * method, then the setter method is invoked.
 * </p>
 * <p>
 * This rule supports custom mapping of XML element names to property names. The default mapping for particular elements
 * can be overridden by using {@link #SetNestedPropertiesRule(String[] elementNames, String[] propertyNames)}. This
 * allows child elements to be mapped to properties with different names. Certain elements can also be marked to be
 * ignored.
 * </p>
 * <p>
 * A very similar effect can be achieved using a combination of the {@code BeanPropertySetterRule} and the
 * {@code ExtendedBaseRules} rules manager; this {@code Rule}, however, works fine with the default
 * {@code RulesBase} rules manager.
 * </p>
 * <p>
 * Note that this rule is designed to be used to set only "primitive" bean properties, eg String, int, boolean. If some
 * of the child XML elements match ObjectCreateRule rules (ie cause objects to be created) then you must use one of the
 * more complex constructors to this rule to explicitly skip processing of that XML element, and define a SetNextRule
 * (or equivalent) to handle assigning the child object to the appropriate property instead.
 * </p>
 * <p>
 * <strong>Implementation Notes</strong>
 * </p>
 * <p>
 * This class works by creating its own simple Rules implementation. When begin is invoked on this rule, the digester's
 * current rules object is replaced by a custom one. When end is invoked for this rule, the original rules object is
 * restored. The digester rules objects therefore behave in a stack-like manner.
 * </p>
 * <p>
 * For each child element encountered, the custom Rules implementation ensures that a special AnyChildRule instance is
 * included in the matches returned to the digester, and it is this rule instance that is responsible for setting the
 * appropriate property on the target object (if such a property exists). The effect is therefore like a
 * "trailing wildcard pattern". The custom Rules implementation also returns the matches provided by the underlying
 * Rules implementation for the same pattern, so other rules are not "disabled" during processing of a
 * SetNestedPropertiesRule.
 * </p>
 * <p>
 * TODO: Optimize this class. Currently, each time begin is called, new AnyChildRules and AnyChildRule objects are
 * created. It should be possible to cache these in normal use (though watch out for when a rule instance is invoked
 * re-entrantly!).
 * </p>
 *
 * @since 1.6
 */
public class SetNestedPropertiesRule
    extends Rule
{

    private final class AnyChildRule
        extends Rule
    {

        private String currChildElementName;

        @Override
        public void begin( final String namespaceURI, final String name, final Attributes attributes )
            throws Exception
        {
            currChildElementName = name;
        }

        @Override
        public void body( final String namespace, final String name, String text )
            throws Exception
        {
            String propName = currChildElementName;
            if ( elementNames.containsKey( currChildElementName ) )
            {
                // override propName
                propName = elementNames.get( currChildElementName );
                if ( propName == null )
                {
                    // user wants us to ignore this element
                    return;
                }
            }

            final boolean debug = log.isDebugEnabled();

            if ( debug )
            {
                log.debug( "[SetNestedPropertiesRule]{" + getDigester().getMatch() + "} Setting property '" + propName
                    + "' to '" + text + "'" );
            }

            // Populate the corresponding properties of the top object
            final Object top = getDigester().peek();
            if ( debug )
            {
                if ( top != null )
                {
                    log.debug( "[SetNestedPropertiesRule]{" + getDigester().getMatch() + "} Set "
                        + top.getClass().getName() + " properties" );
                }
                else
                {
                    log.debug( "[SetPropertiesRule]{" + getDigester().getMatch() + "} Set NULL properties" );
                }
            }

            if ( trimData )
            {
                text = text.trim();
            }

            if ( !allowUnknownChildElements )
            {
                // Force an exception if the property does not exist
                // (BeanUtils.setProperty() silently returns in this case)
                if ( top instanceof DynaBean )
                {
                    final DynaProperty desc = ( (DynaBean) top ).getDynaClass().getDynaProperty( propName );
                    if ( desc == null )
                    {
                        throw new NoSuchMethodException( "Bean has no property named " + propName );
                    }
                }
                else
                /* this is a standard JavaBean */
                {
                    final PropertyDescriptor desc = getPropertyDescriptor( top, propName );
                    if ( desc == null )
                    {
                        throw new NoSuchMethodException( "Bean has no property named " + propName );
                    }
                }
            }

            try
            {
                setProperty( top, propName, text );
            }
            catch ( final NullPointerException e )
            {
                log.error( "NullPointerException: top=" + top + ",propName=" + propName + ",value=" + text + "!" );
                throw e;
            }
        }

        @Override
        public void end( final String namespace, final String name )
            throws Exception
        {
            currChildElementName = null;
        }
    }

    /** Private Rules implementation */
    private final class AnyChildRules
        implements Rules
    {
        private String matchPrefix;

        private Rules decoratedRules;

        private final List<Rule> rules = new ArrayList<>( 1 );

        private final AnyChildRule rule;

        AnyChildRules( final AnyChildRule rule )
        {
            this.rule = rule;
            rules.add( rule );
        }

        @Override
        public void add( final String pattern, final Rule rule )
        {
        }

        @Override
        public void clear()
        {
        }

        @Override
        public Digester getDigester()
        {
            return null;
        }

        @Override
        public String getNamespaceURI()
        {
            return null;
        }

        public Rules getOldRules()
        {
            return decoratedRules;
        }

        public void init( final String prefix, final Rules rules )
        {
            matchPrefix = prefix;
            decoratedRules = rules;
        }

        @Override
        public List<Rule> match( final String namespaceURI, final String matchPath, final String name, final Attributes attributes )
        {
            final List<Rule> match = decoratedRules.match( namespaceURI, matchPath, name, attributes );

            if ( matchPath.startsWith( matchPrefix ) && matchPath.indexOf( '/', matchPrefix.length() ) == -1 )
            {

                // The current element is a direct child of the element
                // specified in the init method, so we want to ensure that
                // the rule passed to this object's constructor is included
                // in the returned list of matching rules.

                if ( match == null || match.isEmpty() )
                {
                    // The "real" rules class doesn't have any matches for
                    // the specified path, so we return a list containing
                    // just one rule: the one passed to this object's
                    // constructor.
                    return rules;
                }
                // The "real" rules class has rules that match the current
                // node, so we return this list *plus* the rule passed to
                // this object's constructor.
                //
                // It might not be safe to modify the returned list,
                // so clone it first.
                final LinkedList<Rule> newMatch = new LinkedList<>( match );
                newMatch.addLast( rule );
                return newMatch;
            }
            return match;
        }

        @Override
        public List<Rule> rules()
        {
            // This is not actually expected to be called during normal
            // processing.
            //
            // There is only one known case where this is called; when a rule
            // returned from AnyChildRules.match is invoked and throws a
            // SAXException then method Digester.endDocument will be called
            // without having "uninstalled" the AnyChildRules instance. That
            // method attempts to invoke the "finish" method for every Rule
            // instance - and thus needs to call rules() on its Rules object,
            // which is this one. Actually, java 1.5 and 1.6beta2 have a
            // bug in their XML implementation such that endDocument is not
            // called after a SAXException, but other parsers (eg Aelfred)
            // do call endDocument. Here, we therefore need to return the
            // rules registered with the underlying Rules object.
            log.debug( "AnyChildRules.rules invoked." );
            return decoratedRules.rules();
        }

        @Override
        public void setDigester( final Digester digester )
        {
        }

        @Override
        public void setNamespaceURI( final String namespaceURI )
        {
        }
    }

    private Log log;

    private boolean trimData = true;

    private boolean allowUnknownChildElements;

    private final HashMap<String, String> elementNames = new HashMap<>();

    /**
     * Base constructor, which maps every child element into a bean property with the same name as the XML element.
     * <p>
     * It is an error if a child XML element exists but the target Java bean has no such property (unless
     * {@link #setAllowUnknownChildElements(boolean)} has been set to true).
     * </p>
     */
    public SetNestedPropertiesRule()
    {
        // nothing to set up
    }

    /**
     * Constructor which allows element to property mapping to be overridden.
     *
     * @param elementNames names of elements to properties to map
     * @since 3.0
     */
    public SetNestedPropertiesRule( final Map<String, String> elementNames )
    {
        if ( elementNames != null && !elementNames.isEmpty() )
        {
            this.elementNames.putAll( elementNames );
        }
    }

    /**
     * <p>
     * Convenience constructor which overrides the default mappings for just one property.
     * </p>
     * <p>
     * For details about how this works, see
     * {@link #SetNestedPropertiesRule(String[] elementNames, String[] propertyNames)}.
     * </p>
     *
     * @param elementName is the child XML element to match
     * @param propertyName is the Java bean property to be assigned the value of the specified XML element. This may be
     *            null, in which case the specified XML element will be ignored.
     */
    public SetNestedPropertiesRule( final String elementName, final String propertyName )
    {
        elementNames.put( elementName, propertyName );
    }

    /**
     * <p>
     * Constructor which allows element to property mapping to be overridden.
     * </p>
     * <p>
     * Two arrays are passed in. One contains XML element names and the other Java bean property names. The element name
     * / property name pairs are matched by position; in order words, the first string in the element name array
     * corresponds to the first string in the property name array and so on.
     * </p>
     * <p>
     * If a property name is null or the XML element name has no matching property name due to the arrays being of
     * different lengths then this indicates that the XML element should be ignored.
     * </p>
     * <strong>Example One</strong>
     * <p>
     * The following constructs a rule that maps the {@code alt-city} element to the {@code city} property and
     * the {@code alt-state} to the {@code state} property. All other child elements are mapped as usual using
     * exact name matching.
     * </p>
     * <pre>{@code
     *      SetNestedPropertiesRule(
     *                new String[] {"alt-city", "alt-state"},
     *                new String[] {"city", "state"});
     * }</pre>
     * <strong>Example Two</strong>
     * <p>
     * The following constructs a rule that maps the {@code class} XML element to the {@code className}
     * property. The XML element {@code ignore-me} is not mapped, ie is ignored. All other elements are mapped as
     * usual using exact name matching.
     * </p>
     * <pre>{@code
     *      SetPropertiesRule(
     *                new String[] {"class", "ignore-me"},
     *                new String[] {"className"});
     * }</pre>
     *
     * @param elementNames names of elements to map
     * @param propertyNames names of properties mapped to
     */
    public SetNestedPropertiesRule( final String[] elementNames, final String[] propertyNames )
    {
        for ( int i = 0, size = elementNames.length; i < size; i++ )
        {
            String propName = null;
            if ( i < propertyNames.length )
            {
                propName = propertyNames[i];
            }

            this.elementNames.put( elementNames[i], propName );
        }
    }

    /**
     * Add an additional custom XML element to property mapping.
     * <p>
     * This is primarily intended to be used from the XML rules module (as it is not possible there to pass the
     * necessary parameters to the constructor for this class). However it is valid to use this method directly if
     * desired.
     * </p>
     *
     * @param elementName the XML element has to be mapped
     * @param propertyName the property name target
     */
    public void addAlias( final String elementName, final String propertyName )
    {
        elementNames.put( elementName, propertyName );
    }

    @Override
    public void begin( final String namespace, final String name, final Attributes attributes )
        throws Exception
    {
        final Rules oldRules = getDigester().getRules();
        final AnyChildRule anyChildRule = new AnyChildRule();
        anyChildRule.setDigester( getDigester() );
        final AnyChildRules newRules = new AnyChildRules( anyChildRule );
        newRules.init( getDigester().getMatch() + "/", oldRules );
        getDigester().setRules( newRules );
    }

    @Override
    public void body( final String namespace, final String name, final String text )
        throws Exception
    {
        final AnyChildRules newRules = (AnyChildRules) getDigester().getRules();
        getDigester().setRules( newRules.getOldRules() );
    }

    /**
     * Gets the flag to ignore any child element for which there is no corresponding object property
     *
     * @return flag to ignore any child element for which there is no corresponding object property
     * @see #setAllowUnknownChildElements(boolean)
     */
    public boolean getAllowUnknownChildElements()
    {
        return allowUnknownChildElements;
    }

    /**
     * Gets the flag to have leading and trailing whitespace removed.
     *
     * @see #setTrimData(boolean)
     * @return flag to have leading and trailing whitespace removed
     */
    public boolean getTrimData()
    {
        return trimData;
    }

    /**
     * Determines whether an error is reported when a nested element is encountered for which there is no corresponding
     * property-setter method.
     * <p>
     * When set to false, any child element for which there is no corresponding object property will cause an error to
     * be reported.
     * <p>
     * When set to true, any child element for which there is no corresponding object property will simply be ignored.
     * <p>
     * The default value of this attribute is false (unknown child elements are not allowed).
     *
     * @param allowUnknownChildElements flag to ignore any child element for which there is no corresponding
     *        object property
     */
    public void setAllowUnknownChildElements( final boolean allowUnknownChildElements )
    {
        this.allowUnknownChildElements = allowUnknownChildElements;
    }

    @Override
    public void setDigester( final Digester digester )
    {
        super.setDigester( digester );
        log = digester.getLogger();
    }

    /**
     * When set to true, any text within child elements will have leading and trailing whitespace removed before
     * assignment to the target object. The default value for this attribute is true.
     *
     * @param trimData flag to have leading and trailing whitespace removed
     */
    public void setTrimData( final boolean trimData )
    {
        this.trimData = trimData;
    }

    @Override
    public String toString()
    {
        return format( "SetNestedPropertiesRule[allowUnknownChildElements=%s, trimData=%s, elementNames=%s]",
                       allowUnknownChildElements,
                       trimData,
                       elementNames );
    }

}
