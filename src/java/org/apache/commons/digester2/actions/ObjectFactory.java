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
import org.apache.commons.digester2.AbstractAction;
import org.apache.commons.digester2.ParseException;

/**
 * Interface for use with {@link CreateObjectWithFactoryAction}.
 * <p>
 * When that action is fired, it calls method {@link #createObject} on some 
 * implementation of this interface to create an object to be pushed onto the 
 * <code>Digester</code> stack.
 * <p>
 * Class {@link AbstractObjectFactory} is an abstract implementation 
 * suitable for creating anonymous <code>ObjectFactory</code> implementations.
 * <p>
 * <strong>IMPORTANT NOTE</strong>: Anyone implementing a custom ObjectFactory
 * is strongly encouraged to subclass AbstractObjectFactory rather than 
 * implement this interface directly. Digester minor releases (2.x -> 2.y) 
 * guarantee that subclasses of AbstractObjectFactory will not be broken. 
 * However the ObjectFactory interface <i>may</i> change in minor releases, 
 * which will break any class which implements this interface directly.
 */
public interface ObjectFactory {

    /**
     * <p>Factory method called by {@link CreateObjectWithFactoryAction} to 
     * supply an object based.</p>
     *
     * <p>Note in particular that implementations of this method have the 
     * option of inspecting the element's attributes to determine what kind 
     * of object to create. Note also that when accessing attributes that are
     * not in any namespace, the empty string should be passed eg
     * <code>String value = attributes.getValue("", "attrName");</code>.</p>
     *
     * @param context is the current parsing context.
     * @param attributes is the current element's set of xml attributes.
     *
     * @throws ParseException if a problem of any sort occurs. Any such 
     *  exception will terminate parsing.
     */
    public Object createObject(Context context, Attributes attributes) 
        throws ParseException;
}
