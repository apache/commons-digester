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

import org.xml.sax.Attributes;

/**
 * Abstract base class for <code>ObjectCreationFactory</code> implementations.
 *
 * @param <T> The object type will be instantiate by this factory.
 */
public abstract class AbstractObjectCreationFactory<T>
    implements ObjectCreationFactory<T>
{

    // ----------------------------------------------------- Instance Variables

    /**
     * The associated <code>Digester</code> instance that was set up by {@link FactoryCreateRule} upon initialization.
     */
    private Digester digester;

    // --------------------------------------------------------- Public Methods

    /**
     * Factory method called by {@link FactoryCreateRule} to supply an object based on the element's attributes.
     *
     * @param attributes the element's attributes
     * @return creates a new T instance
     * @throws Exception any exception thrown will be propagated upwards
     */
    @Override
    public abstract T createObject( Attributes attributes )
        throws Exception;

    /**
     * {@inheritDoc}
     */
    @Override
    public Digester getDigester()
    {
        return ( this.digester );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDigester( final Digester digester )
    {
        this.digester = digester;
    }

}
