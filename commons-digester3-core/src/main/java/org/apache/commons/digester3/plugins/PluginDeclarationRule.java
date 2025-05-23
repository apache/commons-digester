package org.apache.commons.digester3.plugins;

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

import java.util.Properties;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.Rule;
import org.xml.sax.Attributes;

/**
 * A Digester rule which allows the user to pre-declare a class which is to be referenced later at a plugin point by a
 * PluginCreateRule.
 * <p>
 * Normally, a PluginDeclarationRule is added to a Digester instance with the pattern "{root}/plugin" or "* /plugin"
 * where {root} is the name of the root tag in the input document.
 *
 * @since 1.6
 */
public class PluginDeclarationRule
    extends Rule
{

    /**
     * Helper method to declare a plugin inside the given Digester.
     *
     * @param digester The Digester instance to declare plugin
     * @param props the properties where extracting plugin attributes
     * @throws PluginException if any error occurs while declaring the plugin
     */
    public static void declarePlugin( final Digester digester, final Properties props )
        throws PluginException
    {
        final String id = props.getProperty( "id" );
        final String pluginClassName = props.getProperty( "class" );

        if ( id == null )
        {
            throw new PluginInvalidInputException( "mandatory attribute id not present on plugin declaration" );
        }

        if ( pluginClassName == null )
        {
            throw new PluginInvalidInputException( "mandatory attribute class not present on plugin declaration" );
        }

        final Declaration newDecl = new Declaration( pluginClassName );
        newDecl.setId( id );
        newDecl.setProperties( props );

        final PluginRules rc = (PluginRules) digester.getRules();
        final PluginManager pm = rc.getPluginManager();

        newDecl.init( digester, pm );
        pm.addDeclaration( newDecl );

        // Note that it is perfectly safe to redeclare a plugin, because
        // the declaration doesn't add any rules to digester; all it does
        // is create a RuleLoader instance which is *capable* of adding the
        // rules to the digester.
    }

    /** Constructor */
    public PluginDeclarationRule()
    {
    }

    /**
     * Invoked upon reading a tag defining a plugin declaration. The tag must have the following mandatory attributes:
     * <ul>
     * <li>id</li>
     * <li>class</li>
     * </ul>
     *
     * @param namespace The XML namespace in which the XML element which triggered this rule resides.
     * @param name The name of the XML element which triggered this rule.
     * @param attributes The set of attributes on the XML element which triggered this rule.
     * @throws Exception if any error occurs
     */
    @Override
    public void begin( final String namespace, final String name, final Attributes attributes )
        throws Exception
    {
        final int nAttrs = attributes.getLength();
        final Properties props = new Properties();
        for ( int i = 0; i < nAttrs; ++i )
        {
            String key = attributes.getLocalName( i );
            if ( key == null || key.isEmpty() )
            {
                key = attributes.getQName( i );
            }
            final String value = attributes.getValue( i );
            props.setProperty( key, value );
        }

        try
        {
            declarePlugin( getDigester(), props );
        }
        catch ( final PluginInvalidInputException ex )
        {
            throw new PluginInvalidInputException( "Error on element [" + getDigester().getMatch() + "]: "
                + ex.getMessage() );
        }
    }

}
