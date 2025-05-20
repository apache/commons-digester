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

import java.util.List;

import org.xml.sax.Attributes;

/**
 * Public interface defining a collection of Rule instances (and corresponding matching patterns) plus an implementation
 * of a matching policy that selects the rules that match a particular pattern of nested elements discovered during
 * parsing.
 */
public interface Rules
{

    /**
     * Register a new Rule instance matching the specified pattern.
     *
     * @param pattern Nesting pattern to be matched for this Rule
     * @param rule Rule instance to be registered
     */
    void add( String pattern, Rule rule );

    /**
     * Clear all existing Rule instance registrations.
     */
    void clear();

    /**
     * Gets the Digester instance with which this Rules instance is associated.
     *
     * @return the Digester instance with which this Rules instance is associated
     */
    Digester getDigester();

    /**
     * Gets the namespace URI that will be applied to all subsequently added {@code Rule} objects.
     *
     * @return the namespace URI that will be applied to all subsequently added {@code Rule} objects.
     */
    String getNamespaceURI();

    /**
     * Gets a List of all registered Rule instances that match the specified nesting pattern, or a zero-length List if
     * there are no matches. If more than one Rule instance matches, they <strong>must</strong> be returned in the order
     * originally registered through the {@code add()} method.
     *
     * @param namespaceURI Namespace URI for which to select matching rules, or {@code null} to match regardless of
     *            namespace URI
     * @param pattern Nesting pattern to be matched
     * @param name the local name if the parser is namespace aware, or just the element name otherwise
     * @param attributes The attribute list of the current matching element
     * @return a List of all registered Rule instances that match the specified nesting pattern
     */
    List<Rule> match( String namespaceURI, String pattern, String name, Attributes attributes );

    /**
     * Gets a List of all registered Rule instances, or a zero-length List if there are no registered Rule instances.
     * If more than one Rule instance has been registered, they <strong>must</strong> be returned in the order
     * originally registered through the {@code add()} method.
     *
     * @return a List of all registered Rule instances
     */
    List<Rule> rules();

    /**
     * Sets the Digester instance with which this Rules instance is associated.
     *
     * @param digester The newly associated Digester instance
     */
    void setDigester( Digester digester );

    /**
     * Sets the namespace URI that will be applied to all subsequently added {@code Rule} objects.
     *
     * @param namespaceURI Namespace URI that must match on all subsequently added rules, or {@code null} for
     *            matching regardless of the current namespace URI
     */
    void setNamespaceURI( String namespaceURI );

}
