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

import org.xml.sax.Attributes;

/**
 * <p>
 * Interface for use with {@link FactoryCreateRule}. The rule calls {@link #createObject} to create an object to be
 * pushed onto the {@code Digester} stack whenever it is matched.
 * </p>
 * <p>
 * {@link AbstractObjectCreationFactory} is an abstract implementation suitable for creating anonymous
 * {@code ObjectCreationFactory} implementations.
 *
 * @param <T> The object type created byt the factory.
 */
@FunctionalInterface
public interface ObjectCreationFactory<T>
{

    /**
     * Factory method called by {@link FactoryCreateRule} to supply an object based on the element's attributes.
     *
     * @param attributes the element's attributes
     * @return the object to be pushed onto the {@code Digester} stack
     * @throws Exception any exception thrown will be propagated upwards
     */
    T createObject( Attributes attributes )
        throws Exception;

    /**
     * Returns the {@link Digester} that was set by the {@link FactoryCreateRule} upon initialization.
     *
     * @return the {@link Digester} that was set by the {@link FactoryCreateRule} upon initialization
     */
    default Digester getDigester()
    {
        return null;
    }

    /**
     * Sets the {@link Digester} to allow the implementation to do logging, classloading based on the digester's
     * classloader, etc.
     *
     * @param digester parent Digester object
     */
    default void setDigester( Digester digester )
    {
    }

}
