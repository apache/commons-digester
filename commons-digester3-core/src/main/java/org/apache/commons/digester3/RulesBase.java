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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;

/**
 * <p>
 * Default implementation of the {@code Rules} interface that supports the standard rule matching behavior. This
 * class can also be used as a base class for specialized {@code Rules} implementations.
 * </p>
 * <p>
 * The matching policies implemented by this class support two different types of pattern matching rules:
 * </p>
 * <ul>
 * <li><em>Exact Match</em> - A pattern "a/b/c" exactly matches a {@code &lt;c&gt;} element, nested inside a
 * {@code &lt;b&gt;} element, which is nested inside an {@code &lt;a&gt;} element.</li>
 * <li><em>Tail Match</em> - A pattern "&#42;/a/b" matches a {@code &lt;b&gt;} element, nested inside an
 * {@code &lt;a&gt;} element, no matter how deeply the pair is nested.</li>
 * </ul>
 * <p>
 * Note that wildcard patterns are ignored if an explicit match can be found (and when multiple wildcard patterns match,
 * only the longest, ie most explicit, pattern is considered a match).
 * </p>
 * <p>
 * See the package documentation for package org.apache.commons.digester3 for more information.
 * </p>
 */

public class RulesBase
    extends AbstractRulesImpl
{

    /**
     * The set of registered Rule instances, keyed by the matching pattern. Each value is a List containing the Rules
     * for that pattern, in the order that they were originally registered.
     */
    protected HashMap<String, List<Rule>> cache = new HashMap<>();

    /**
     * The subset of registered Rule instances with wildcard pattern.
     */
    protected List<String> wildcardCache = new LinkedList<>();

    /**
     * The set of registered Rule instances, in the order that they were originally registered.
     */
    protected ArrayList<Rule> rules = new ArrayList<>();

    @Override
    public void clear()
    {
        wildcardCache.clear();
        cache.clear();
        rules.clear();
    }

    /**
     * Return a List of Rule instances for the specified pattern that also match the specified namespace URI (if any).
     * If there are no such rules, return {@code null}.
     *
     * @param namespaceURI Namespace URI to match, or {@code null} to select matching rules regardless of namespace
     *            URI
     * @param pattern Pattern to be matched
     * @return a List of Rule instances for the specified pattern that also match the specified namespace URI (if any)
     */
    protected List<Rule> lookup( final String namespaceURI, final String pattern )
    {
        // Optimize when no namespace URI is specified
        final List<Rule> list = this.cache.get( pattern );
        if ( list == null )
        {
            return null;
        }
        if ( namespaceURI == null || namespaceURI.isEmpty() )
        {
            return list;
        }

        // Select only Rules that match on the specified namespace URI
        final List<Rule> results = new ArrayList<>();
        for ( final Rule item : list )
        {
            if ( namespaceURI.equals( item.getNamespaceURI() ) || item.getNamespaceURI() == null )
            {
                results.add( item );
            }
        }
        return results;
    }

    @Override
    public List<Rule> match( final String namespaceURI, final String pattern, final String name, final Attributes attributes )
    {
        // List rulesList = (List) this.cache.get(pattern);
        List<Rule> rulesList = lookup( namespaceURI, pattern );
        if ( rulesList == null || rulesList.isEmpty() )
        {
            // Find the longest key, ie more discriminant
            String longKey = "";
            for ( final String key : wildcardCache )
            {
                if ( ( pattern.equals( key.substring( 1 ) ) || pattern.endsWith( key ) )
                    && key.length() > longKey.length() )
                {
                    longKey = key;
                }
            }
            if ( !longKey.isEmpty() )
            {
                rulesList = lookup( namespaceURI, "*" + longKey );
            }
        }
        if ( rulesList == null )
        {
            rulesList = new ArrayList<>();
        }
        return rulesList;
    }

    @Override
    protected void registerRule( String pattern, final Rule rule )
    {
        // to help users who accidentally add '/' to the end of their patterns
        final int patternLength = pattern.length();
        if ( patternLength > 1 && pattern.endsWith( "/" ) )
        {
            pattern = pattern.substring( 0, patternLength - 1 );
        }

        List<Rule> list = cache.get( pattern );
        if ( list == null )
        {
            list = new ArrayList<>();
            if ( pattern.startsWith( "*/" ) )
            {
                wildcardCache.add( pattern.substring( 1 ) );
            }
            cache.put( pattern, list );
        }
        list.add( rule );
        rules.add( rule );
    }

    @Override
    public List<Rule> rules()
    {
        return this.rules;
    }

    @Override
    public void setDigester( final Digester digester )
    {
        super.setDigester( digester );
        for ( final Rule rule : rules )
        {
            rule.setDigester( digester );
        }
    }

}
