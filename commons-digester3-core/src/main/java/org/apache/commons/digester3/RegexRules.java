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
import java.util.List;

import org.xml.sax.Attributes;

/**
 * <p>
 * Rules implementation that uses regular expression matching for paths.
 * </p>
 * <p>
 * The regex implementation is pluggable, allowing different strategies to be used. The basic way that this class work
 * does not vary. All patterns are tested to see if they match the path using the regex matcher. All those that do are
 * returned in the order in which the rules were added.
 * </p>
 *
 * @since 1.5
 */
public class RegexRules
    extends AbstractRulesImpl
{

    /** Used to associate rules with paths in the rules list */
    private static final class RegisteredRule
    {
        String pattern;

        Rule rule;

        RegisteredRule( final String pattern, final Rule rule )
        {
            this.pattern = pattern;
            this.rule = rule;
        }
    }

    /** All registered {@code Rule}'s */
    private final List<RegisteredRule> registeredRules = new ArrayList<>();

    /** The regex strategy used by this RegexRules */
    private RegexMatcher matcher;

    /**
     * Constructs sets the Regex matching strategy.
     *
     * @param matcher the regex strategy to be used, not null
     */
    public RegexRules( final RegexMatcher matcher )
    {
        setRegexMatcher( matcher );
    }

    @Override
    public void clear()
    {
        registeredRules.clear();
    }

    /**
     * Gets the current regex matching strategy.
     *
     * @return the current regex matching strategy.
     */
    public RegexMatcher getRegexMatcher()
    {
        return matcher;
    }

    @Override
    public List<Rule> match( final String namespaceURI, final String pattern, final String name, final Attributes attributes )
    {
        //
        // not a particularly quick implementation
        // regex is probably going to be slower than string equality
        // so probably should have a set of strings
        // and test each only once
        //
        // XXX FIX ME - Time And Optimize
        //
        final List<Rule> rules = new ArrayList<>( registeredRules.size() );
        for ( final RegisteredRule rr : registeredRules )
        {
            if ( matcher.match( pattern, rr.pattern ) )
            {
                rules.add( rr.rule );
            }
        }
        return rules;
    }

    @Override
    protected void registerRule( final String pattern, final Rule rule )
    {
        registeredRules.add( new RegisteredRule( pattern, rule ) );
    }

    @Override
    public List<Rule> rules()
    {
        final List<Rule> rules = new ArrayList<>( registeredRules.size() );
        for ( final RegisteredRule rr : registeredRules )
        {
            rules.add( rr.rule );
        }
        return rules;
    }

    /**
     * Sets the current regex matching strategy.
     *
     * @param matcher use this RegexMatcher, not null
     */
    public void setRegexMatcher( final RegexMatcher matcher )
    {
        if ( matcher == null )
        {
            throw new IllegalArgumentException( "RegexMatcher must not be null." );
        }
        this.matcher = matcher;
    }

}
