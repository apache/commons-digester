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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;

/**
 * <p>
 * Extension of {@link RulesBase} for complex schema.
 * </p>
 * <p>
 * This is an extension of the basic pattern matching scheme intended to improve support for mapping complex xml-schema.
 * It is intended to be a minimal extension of the standard rules big enough to support complex schema but without the
 * full generality offered by more exotic matching pattern rules.
 * </p>
 * <h2>When should you use this rather than the original?</h2>
 * <p>
 * This pattern-matching engine is complex and slower than the basic default RulesBase class, but offers more
 * functionality:
 * </p>
 * <ul>
 * <li>Universal patterns allow patterns to be specified which will match regardless of whether there are
 * "better matching" patterns available.</li>
 * <li>Parent-match patterns (eg "a/b/?") allow matching for all direct children of a specified element.</li>
 * <li>Ancestor-match patterns (eg "a/b/*") allow matching all elements nested within a specified element to any nesting
 * depth.</li>
 * <li>Completely-wildcard patterns ("*" or "!*") allow matching all elements.</li>
 * </ul>
 * <h2>Universal Match Patterns</h2>
 * <p>
 * The default RulesBase pattern-matching engine always attempts to find the "best matching pattern", and will ignore
 * rules associated with other patterns that match but are not "as good". As an example, if the pattern "a/b/c" is
 * associated with rules 1 and 2, and "*&#47;c" is associated with rules 3 and 4 then element "a/b/c" will cause only
 * rules 1 and 2 to execute. Rules 3 and 4 do have matching patterns, but because the patterns are shorter and include
 * wildcard characters they are regarded as being "not as good" as a direct match. In general, exact patterns are better
 * than wildcard patterns, and among multiple patterns with wildcards, the longest is preferred. See the RulesBase class
 * for more information.
 * </p>
 * <p>
 * This feature of preferring "better" patterns can be a powerful tool. However it also means that patterns can interact
 * in unexpected ways.
 * </p>
 * <p>
 * When using the ExtendedBaseRules, any pattern prefixed with '!' bypasses the "best match" feature. Even if there is
 * an exact match or a longer wildcard match, patterns prefixed by '!' will still be tested to see if they match, and if
 * so their associated Rule objects will be included in the set of rules to be executed in the normal manner.
 * </p>
 * <ul>
 * <li>Pattern {@code "!*&#47;a/b"} matches whenever an 'b' element is inside an 'a'.</li>
 * <li>Pattern {@code "!a/b/?"} matches any child of a parent matching {@code "a/b"} (see
 * "Parent Match Patterns").</li>
 * <li>Pattern {@code "!*&#47;a/b/?"} matches any child of a parent matching {@code "!*&#47;a/b"} (see
 * "Parent Match Patterns").</li>
 * <li>Pattern {@code "!a/b/*"} matches any element whose path starts with "a" then "b" (see
 * "Ancestor Match Patterns").</li>
 * <li>Pattern {@code "!*&#47;a/b/*"} matches any elements whose path contains 'a/b' (see
 * "Ancestor Match Patterns").</li>
 * </ul>
 * <h2>Parent Match Patterns</h2>
 * <p>
 * These will match direct child elements of a particular parent element.
 * </p>
 * <ul>
 * <li>
 *  {@code "a/b/c/?"} matches any child whose parent matches {@code "a/b/c"}. Exact parent rules take
 * precedence over Ancestor Match patterns.</li>
 * <li>
 *  {@code "*&#47;a/b/c/?"} matches any child whose parent matches {@code "*&#47;a/b/c"}. The longest
 * matching still applies to parent matches but the length excludes the '?', which effectively means that standard
 * wildcard matches with the same level of depth are chosen in preference.</li>
 * </ul>
 * <h2>Ancestor Match Patterns</h2>
 * <p>
 * These will match elements whose parentage includes a particular sequence of elements.
 * </p>
 * <ul>
 * <li>
 *  {@code "a/b/*"} matches any element whose path starts with 'a' then 'b'. Exact parent and parent match rules
 * take precedence. The longest ancestor match will take precedence.</li>
 * <li>
 *  {@code "*&#47;a/b/*"} matches any elements whose path contains an element 'a' followed by an element 'b'.
 * The longest matching still applies but the length excludes the '*' at the end.</li>
 * </ul>
 * <h2>Completely Wildcard Patterns</h2>
 * <p>
 * Pattern {@code "*"} matches every pattern that isn't matched by any other basic rule.
 * </p>
 * <p>
 * Pattern {@code "!*"} matches every pattern.
 * </p>
 * <h2>Using The Extended Rules</h2>
 * <p>
 * By default, a Digester instance uses a {@link RulesBase} instance as its pattern matching engine. To use an
 * ExtendedBaseRules instance, call the Digester.setRules method before adding any Rule objects to the digester
 * instance:
 * </p>
 *
 * <pre>
 * Digester digester = new Digester();
 * digester.setRules( new ExtendedBaseRules() );
 * </pre>
 *
 * <p>
 * The most important thing to remember when using the extended rules is that universal and non-universal patterns are
 * completely independent. Universal patterns are never affected by the addition of new patterns or the removal of
 * existing ones. Non-universal patterns are never affected by the addition of new <em>universal</em> patterns or the
 * removal of existing <em>universal</em> patterns. As in the basic matching rules, non-universal (basic) patterns
 * <strong>can</strong> be affected by the addition of new <em>non-universal</em> patterns or the removal of existing
 * <em>non-universal</em> patterns, because only rules associated with the "best matching" pattern for each xml element
 * are executed.
 * </p>
 * <p>
 * This means that you can use universal patterns to build up the simple parts of your structure - for example defining
 * universal creation and property setting rules. More sophisticated and complex mapping will require non-universal
 * patterns and this might mean that some of the universal rules will need to be replaced by a series of special cases
 * using non-universal rules. But by using universal rules as your backbone, these additions should not break your
 * existing rules.
 * </p>
 */
public class ExtendedBaseRules
    extends RulesBase
{

    /**
     * Counts the entry number for the rules.
     */
    private int counter;

    /**
     * The decision algorithm used (unfortunately) doesn't preserve the entry order. This map is used by a comparator
     * which orders the list of matches before it's returned. This map stores the entry number keyed by the rule.
     */
    private final Map<Rule, Integer> order = new HashMap<>();

    /**
     * Standard match. Matches the end of the pattern to the key.
     *
     * @param key The key to be found
     * @param pattern The pattern where looking for the key
     * @return true, if {@code key} is found inside {@code pattern}, false otherwise
     */
    private boolean basicMatch( final String key, final String pattern )
    {
        return pattern.equals( key.substring( 2 ) ) || pattern.endsWith( key.substring( 1 ) );
    }

    /**
     * Finds an exact ancestor match for given pattern
     *
     * @param parentPattern The input pattern
     * @return A list of {@code Rule} related to the input pattern
     */
    private List<Rule> findExactAncesterMatch( final String parentPattern )
    {
        int lastIndex = parentPattern.length();
        while ( lastIndex-- > 0 )
        {
            lastIndex = parentPattern.lastIndexOf( '/', lastIndex );
            if ( lastIndex > 0 )
            {
                List<Rule> matchingRules = this.cache.get( parentPattern.substring( 0, lastIndex ) + "/*" );
                if ( matchingRules != null )
                {
                    return matchingRules;
                }
            }
        }
        return null;
    }

    @Override
    public List<Rule> match( final String namespaceURI, final String pattern, final String name, final Attributes attributes )
    {
        // calculate the pattern of the parent (if the element has one)
        String parentPattern = "";
        final int lastIndex = pattern.lastIndexOf( '/' );

        boolean hasParent = true;
        if ( lastIndex == -1 )
        {
            // element has no parent
            hasParent = false;
        }
        else
        {
            // calculate the pattern of the parent
            parentPattern = pattern.substring( 0, lastIndex );
        }

        // we keep the list of universal matches separate
        final List<Rule> universalList = new ArrayList<>( counter );

        // Universal wildcards ('*') in the middle of the pattern-string
        List<Rule> recList = null;
        // temporary parentPattern
        // we don't want to change anything....
        String tempParentPattern = parentPattern;
        int parentLastIndex = tempParentPattern.lastIndexOf( '/' );
        // look for pattern. Here, we search the whole parent. Not ideal, but does the thing....
        while ( parentLastIndex > -1 && recList == null )
        {
            recList = this.cache.get( tempParentPattern + "/*/" + pattern.substring( lastIndex + 1 ) );
            if ( recList != null )
            {
                // when /*/-pattern-string is found, add method list to universalList.
                // Digester will do the rest
                universalList.addAll( recList );
            }
            else
            {
                // if not, shorten tempParent to move /*/ one position to the left.
                // as last part of pattern is always added we make sure pattern is allowed anywhere.
                tempParentPattern = parentPattern.substring( 0, parentLastIndex );
            }

            parentLastIndex = tempParentPattern.lastIndexOf( '/' );
        }

        // Universal all wildcards ('!*')
        // These are always matched so always add them
        List<Rule> tempList = this.cache.get( "!*" );
        if ( tempList != null )
        {
            universalList.addAll( tempList );
        }

        // Universal exact parent match
        // need to get this now since only wildcards are considered later
        tempList = this.cache.get( "!" + parentPattern + "/?" );
        if ( tempList != null )
        {
            universalList.addAll( tempList );
        }

        // base behavior means that if we certain matches, we don't continue but we just have
        // a single combined loop and so we have to set a variable
        boolean ignoreBasicMatches = false;

        // see if we have an exact basic pattern match
        List<Rule> rulesList = this.cache.get( pattern );
        if ( rulesList != null )
        {
            // we have a match! so ignore all basic matches from now on
            ignoreBasicMatches = true;
        }
        else if ( hasParent ) // see if we have an exact child match
        {
            // matching children takes preference
            rulesList = this.cache.get( parentPattern + "/?" );
            if ( rulesList != null )
            {
                // we have a match! so ignore all basic matches from now on
                ignoreBasicMatches = true;
            }
            else
            {
                // we don't have a match yet - so try exact ancestor
                rulesList = findExactAncesterMatch( pattern );
                if ( rulesList != null )
                {
                    // we have a match! so ignore all basic matches from now on
                    ignoreBasicMatches = true;
                }
            }
        }

        // OK - we're ready for the big loop!
        // Unlike the basic rules case, we have to go through for all those universal rules in all cases.

        // Find the longest key, ie more discriminant
        int longKeyLength = 0;

        for ( String key : this.cache.keySet() )
        {
            // find out if it's a universal pattern
            // set a flag
            final boolean isUniversal = key.startsWith( "!" );
            if ( isUniversal )
            {
                // and find the underlying key
                key = key.substring( 1 );
            }

            // don't need to check exact matches
            final boolean wildcardMatchStart = key.startsWith( "*/" );
            final boolean wildcardMatchEnd = key.endsWith( "/*" );
            if ( wildcardMatchStart || isUniversal && wildcardMatchEnd )
            {
                boolean parentMatched = false;
                boolean basicMatched = false;
                boolean ancestorMatched = false;

                final boolean parentMatchEnd = key.endsWith( "/?" );
                if ( parentMatchEnd )
                {
                    // try for a parent match
                    parentMatched = parentMatch( key, parentPattern );
                }
                else if ( wildcardMatchEnd )
                {
                    // check for ancestor match
                    if ( wildcardMatchStart )
                    {
                        final String patternBody = key.substring( 2, key.length() - 2 );
                        if ( pattern.endsWith( patternBody ) )
                        {
                            ancestorMatched = true;
                        }
                        else
                        {
                            ancestorMatched = pattern.contains( patternBody + "/" );
                        }
                    }
                    else
                    {
                        final String bodyPattern = key.substring( 0, key.length() - 2 );
                        if ( pattern.startsWith( bodyPattern ) )
                        {
                            if ( pattern.length() == bodyPattern.length() )
                            {
                                // exact match
                                ancestorMatched = true;
                            }
                            else
                            {
                                ancestorMatched = pattern.charAt( bodyPattern.length() ) == '/';
                            }
                        }
                    }
                }
                else
                {
                    // try for a base match
                    basicMatched = basicMatch( key, pattern );
                }

                if ( parentMatched || basicMatched || ancestorMatched )
                {
                    if ( isUniversal )
                    {
                        // universal rules go straight in (no longest matching rule)
                        tempList = this.cache.get( "!" + key );
                        if ( tempList != null )
                        {
                            universalList.addAll( tempList );
                        }
                    }
                    else if ( !ignoreBasicMatches )
                    {
                        // ensure that all parent matches are SHORTER than rules with same level of matching.
                        //
                        // the calculations below don't work for universal matching,
                        // but we don't care because in that case this if-stmt is not entered.
                        int keyLength = key.length();
                        if ( wildcardMatchStart )
                        {
                            --keyLength;
                        }
                        if ( wildcardMatchEnd || parentMatchEnd )
                        {
                            --keyLength;
                        }

                        if ( keyLength > longKeyLength )
                        {
                            rulesList = this.cache.get( key );
                            longKeyLength = keyLength;
                        }
                    }
                }
            }
        }

        // '*' works in practice as a default matching (this is because anything is a deeper match!)
        if ( rulesList == null )
        {
            rulesList = this.cache.get( "*" );
        }

        // if we've matched a basic pattern, then add to the universal list
        if ( rulesList != null )
        {
            universalList.addAll( rulesList );
        }

        // don't filter if namespace is null
        if ( namespaceURI != null )
        {
            // remove invalid namespaces
            universalList.removeIf( rule -> rule.getNamespaceURI() != null && !rule.getNamespaceURI().equals( namespaceURI ) );
        }

        // need to make sure that the collection is sorted in the order of addition. We use a custom comparator for this
        universalList.sort( Comparator.nullsFirst( Comparator.comparing( order::get ) ) );

        return universalList;
    }

    /**
     * Checks the input parentPattern contains the input key at the end.
     *
     * @param key The key to be found
     * @param parentPattern The pattern where looking for the key
     * @return true, if {@code key} is found inside {@code parentPattern}, false otherwise
     */
    private boolean parentMatch( final String key, final String parentPattern )
    {
        return parentPattern.endsWith( key.substring( 1, key.length() - 2 ) );
    }

    @Override
    protected void registerRule( final String pattern, final Rule rule )
    {
        super.registerRule( pattern, rule );
        counter++;
        order.put( rule, counter );
    }

}
