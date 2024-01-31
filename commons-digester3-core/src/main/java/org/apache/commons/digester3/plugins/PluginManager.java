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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.digester3.Digester;
import org.apache.commons.logging.Log;

/**
 * Coordinates between PluginDeclarationRule and PluginCreateRule objects, providing a place to share data between
 * instances of these rules.
 * <p>
 * One instance of this class exists per PluginRules instance.
 *
 * @since 1.6
 */
public class PluginManager
{

    /** Map of class name->Declaration */
    private final HashMap<String, Declaration> declarationsByClass = new HashMap<String, Declaration>();

    /** Map of id->Declaration */
    private final HashMap<String, Declaration> declarationsById = new HashMap<String, Declaration>();

    /** The parent manager to which this one may delegate lookups. */
    private PluginManager parent;

    /**
     * The object containing data that should only exist once for each Digester instance.
     */
    private final PluginContext pluginContext;

    /**
     * Constructs a "root" PluginManager, ie one with no parent.
     *
     * @param r The object containing data that should only exist once for each Digester instance.
     */
    public PluginManager( final PluginContext r )
    {
        pluginContext = r;
    }

    /**
     * Constructs a "child" PluginManager. When declarations are added to a "child", they are stored within the child and
     * do not modify the parent, so when the child goes out of scope, those declarations disappear. When asking a
     * "child" to retrieve a declaration, it delegates the search to its parent if it does not hold a matching entry
     * itself.
     * <p>
     *
     * @param parent must be non-null.
     */
    public PluginManager( final PluginManager parent )
    {
        this.parent = parent;
        this.pluginContext = parent.pluginContext;
    }

    /**
     * Add the declaration to the set of known declarations.
     * <p>
     * TODO: somehow get a reference to a Digester object so that we can really log here. Currently, all logging is
     * disabled from this method.
     *
     * @param decl an object representing a plugin class.
     */
    public void addDeclaration( final Declaration decl )
    {
        final Log log = LogUtils.getLogger( null );
        final boolean debug = log.isDebugEnabled();

        final Class<?> pluginClass = decl.getPluginClass();
        final String id = decl.getId();

        declarationsByClass.put( pluginClass.getName(), decl );

        if ( id != null )
        {
            declarationsById.put( id, decl );
            if ( debug )
            {
                log.debug( "Indexing plugin-id [" + id + "]" + " -> class [" + pluginClass.getName() + "]" );
            }
        }
    }

    /**
     * Given a plugin class and some associated properties, scan the list of known RuleFinder instances until one
     * detects a source of custom rules for this plugin (aka a RuleLoader).
     * <p>
     * If no source of custom rules can be found, null is returned.
     *
     * @param digester The digester instance where locating plugin classes
     * @param id The id that the user associated with a particular plugin declaration in the input xml
     * @param pluginClass The plugin Java class
     * @param props The properties object that holds any xml attributes the user may have specified on the plugin
     *        declaration in order to indicate how to locate the plugin rules.
     * @return The discovered Rule loader instance
     * @throws PluginException if any error occurs while finding the loader
     */
    public RuleLoader findLoader( final Digester digester, final String id, final Class<?> pluginClass, final Properties props )
        throws PluginException
    {

        // iterate over the list of RuleFinders, trying each one
        // until one of them locates a source of dynamic rules given
        // this specific plugin class and the associated declaration
        // properties.
        final Log log = LogUtils.getLogger( digester );
        final boolean debug = log.isDebugEnabled();
        log.debug( "scanning ruleFinders to locate loader.." );

        final List<RuleFinder> ruleFinders = pluginContext.getRuleFinders();
        RuleLoader ruleLoader = null;
        for ( final Iterator<RuleFinder> i = ruleFinders.iterator(); i.hasNext() && ruleLoader == null; )
        {

            final RuleFinder finder = i.next();
            if ( debug )
            {
                log.debug( "checking finder of type " + finder.getClass().getName() );
            }
            try
            {
                ruleLoader = finder.findLoader( digester, pluginClass, props );
            }
            catch ( final PluginException e )
            {
                throw new PluginException( "Unable to locate plugin rules for plugin" + " with id [" + id + "]"
                    + ", and class [" + pluginClass.getName() + "]" + ":" + e.getMessage(), e.getCause() );
            }
        }
        log.debug( "scanned ruleFinders." );

        return ruleLoader;
    }

    /**
     * Gets the declaration object with the specified class. If no such plugin is known, null is returned.
     *
     * @param className The {@link Declaration} class name
     * @return The Declaration instance obtained by the input class name
     */
    public Declaration getDeclarationByClass( final String className )
    {
        Declaration decl = declarationsByClass.get( className );

        if ( ( decl == null ) && ( parent != null ) )
        {
            decl = parent.getDeclarationByClass( className );
        }

        return decl;
    }

    /**
     * Gets the declaration object with the specified id. If no such plugin is known, null is returned.
     *
     * @param id Description of the Parameter
     * @return The declaration value
     */
    public Declaration getDeclarationById( final String id )
    {
        Declaration decl = declarationsById.get( id );

        if ( ( decl == null ) && ( parent != null ) )
        {
            decl = parent.getDeclarationById( id );
        }

        return decl;
    }

}
