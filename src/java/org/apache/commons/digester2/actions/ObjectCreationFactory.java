/* $Id: $
 *
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.apache.commons.digester2.actions;


import org.xml.sax.Attributes;


import org.apache.commons.digester2.Context;
import org.apache.commons.digester2.AbstractAction;
import org.apache.commons.digester2.ParseException;

/**
 * <p> Interface for use with {@link FactoryCreateAction}.
 * The rule calls {@link #createObject} to create an object
 * to be pushed onto the <code>Digester</code> stack
 * whenever it is matched.</p>
 *
 * <p> {@link AbstractObjectCreationFactory} is an abstract
 * implementation suitable for creating anonymous
 * <code>ObjectCreationFactory</code> implementations.
 */
public interface ObjectCreationFactory {

    /**
     * <p>Factory method called by {@link FactoryCreateAction} to supply an
     * object based on the element's attributes.
     *
     * @param attributes the element's attributes
     *
     * @throws Exception any exception thrown will be propagated upwards
     */
    public Object createObject(Context context, Attributes attributes) 
        throws ParseException;
}
