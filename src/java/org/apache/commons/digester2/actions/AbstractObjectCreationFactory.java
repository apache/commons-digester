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
import org.apache.commons.digester2.ParseException;

/**
 * <p>Abstract base class for <code>ObjectCreationFactory</code>
 * implementations.</p>
 */
abstract public class AbstractObjectCreationFactory implements ObjectCreationFactory {

    // --------------------------------------------------------- Public Methods

    /**
     * <p>Factory method called by {@link CreateObjectWithFactoryAction} to 
     * supply an object based on the element's attributes.
     *
     * @param attributes the element's attributes
     *
     * @throws Exception any exception thrown will be propagated upwards
     */
    public abstract Object createObject(Context context, Attributes attributes) 
        throws ParseException;

}
