/* $Id$
 *
 * Copyright 2001-2005 The Apache Software Foundation.
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
 * Abstract base class for <code>ObjectFactory</code> implementations.
 * <p>
 * Note that extending this abstract class rather than directly implementing
 * the ObjectFactory interface provides much better "forward compatibility". 
 * Digester minor releases (2.x -> 2.y) guarantee not to break any classes that
 * subclass this abstract class. However no such guarantee exists for classes 
 * that directly implement the ObjectFactory interface.
 */

abstract public class AbstractObjectFactory implements ObjectFactory {

    /**
     * <p>Factory method called by {@link CreateObjectWithFactoryAction} to 
     * supply an object based.</p>
     *
     * <p>Note in particular that implementations of this method have the 
     * option of inspecting the element's attributes to determine what kind 
     * of object to create.</p>
     *
     * @param context is the current parsing context.
     * @param attributes is the current element's set of xml attributes.
     *
     * @throws ParseException if a problem of any sort occurs. Any such 
     *  exception will terminate parsing.
     */
    public abstract Object createObject(Context context, Attributes attributes) 
        throws ParseException;
}
