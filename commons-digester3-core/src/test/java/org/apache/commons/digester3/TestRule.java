/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.digester3;

import java.util.List;

import org.apache.commons.digester3.binder.RuleProvider;
import org.xml.sax.Attributes;

/**
 * <p>
 * This rule implementation is intended to help test digester. The idea is that you can test which rule matches by
 * looking at the identifier.
 * </p>
 */

public class TestRule
    extends Rule
{

    public static class TestRuleProvider implements RuleProvider<TestRule>
    {

        private final String identifier;

        private final List<Rule> callOrder;

        public TestRuleProvider( final String identifier )
        {
            this( identifier, null );
        }

        public TestRuleProvider( final String identifier, final List<Rule> callOrder )
        {
            this.identifier = identifier;
            this.callOrder = callOrder;
        }

        @Override
        public TestRule get()
        {
            final TestRule testRule = new TestRule( identifier );
            testRule.setOrder( callOrder );
            return testRule;
        }

    }

    /** String identifying this particular {@code TestRule} */
    private final String identifier;

    /** Used when testing body text */
    private String bodyText;

    /** Used when testing call orders */
    private List<Rule> order;

    /**
     * Base constructor.
     *
     * @param identifier Used to tell which TestRule is which
     */
    public TestRule( final String identifier )
    {

        this.identifier = identifier;
    }

    /**
     * Constructor sets namespace URI.
     *
     * @param identifier Used to tell which TestRule is which
     * @param namespaceURI Set rule namespace
     */
    public TestRule( final String identifier, final String namespaceURI )
    {

        this.identifier = identifier;
        setNamespaceURI( namespaceURI );

    }

    /**
     * If a list has been set, append this to the list.
     */
    protected void appendCall()
    {
        if ( order != null ) {
            order.add( this );
        }
    }

    /**
     * 'Begin' call.
     */
    @Override
    public void begin( final String namespace, final String name, final Attributes attributes )
    {
        appendCall();
    }

    /**
     * 'Body' call.
     */
    @Override
    public void body( final String namespace, final String name, final String text )
    {
        this.bodyText = text;
        appendCall();
    }

    /**
     * 'End' call.
     */
    @Override
    public void end( final String namespace, final String name )
    {
        appendCall();
    }

    /**
     * Gets the body text that was set.
     */
    public String getBodyText()
    {
        return bodyText;
    }

    /**
     * Gets the identifier associated with this test.
     */
    public String getIdentifier()
    {
        return identifier;
    }

    /**
     * Gets call order list.
     */
    public List<Rule> getOrder()
    {
        return order;
    }

    /**
     * Sets call order list
     */
    public void setOrder( final List<Rule> order )
    {
        this.order = order;
    }

    /**
     * Return the identifier.
     */
    @Override
    public String toString()
    {
        return identifier;
    }

}
