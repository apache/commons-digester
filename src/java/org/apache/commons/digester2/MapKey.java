/* $Id$
 *
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.commons.digester2;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.EmptyStackException;
import java.lang.reflect.InvocationTargetException;

import org.xml.sax.Locator;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.apache.commons.logging.Log;

/**
 * Simple class whose instances can be used as keys into a map.
 * <p>
 * Classes such as Context provide generic data storage facilities which can
 * be used by other instances to store data for later retrieval. This storage
 * is in the form of a map, which requires that the objects that write and
 * read objects within the map have some convention to avoid stomping on each
 * other's data. This class provides a mechanism for creating unique keys 
 * for such a purpose.
 * <p>
 * The idea is that some object declares an instance of this class as a
 * member. It then uses that member as a key into a map held elsewhere.
 * There can never be any conflicts with other users of that map, because
 * the object is unique. An alternative would be to use strings as
 * keys into the shared map, but that would involve inventing a convention
 * for creating unique string values, which this approach doesn't need.
 * <p>
 * The important technical point that makes this object usable as a key
 * into a map is that it does <i>not</i> override the default Object.equals
 * method, ie two instances are equal only if they are the same object.
 * <p>
 * Example usage:
 * <br>
 * If an instance of class Foo wants to be able to store data in a shared map
 * without conflicting with any other class, or even with other instances of
 * the Foo class, then it should declare a private non-static id instance:
 * <pre>
 *    private final MapKey WIDGET_ID
 *      = new MapKey(Foo.class, "Widget", this);
 *    ....
 *    someCommonMap.put(WIDGET_ID, theWidget);
 *    ....
 *    Widget savedWidget = (Widget) someCommonMap.get(WIDGET_ID);
 * </pre>
 * <p>
 * If an class Bar wishes to share a particular map entry across all instances
 * of itself, then it should declare a static id:
 * <pre>
 *    private static final MapKey GADGET_ID
 *      = new MapKey(Bar.class, "Gadget");
 * </pre>
 * <p>
 * If a class wishes to share an item in a shared map with objects that are
 * not of the same class, then it can follow the above example but declare
 * access to be protected or public. Other classes can then access it via:
 * <pre>
 *   someCommonMap.get(Bar.WIDGET_ID);
 * </pre>
 */

public class MapKey {

    /**
     * A string used only for generating debug output.
     */
    private String desc;

    /**
     * Create an instance which has no specific owner object.
     */
    public MapKey(Class sourceClass, String desc) {
        this.desc = sourceClass.getName() + ":" + desc;
    }

    /**
     * Create an instance which has an owner object. This is equivalent
     * to the constructor without an owner object, except that the 
     * diagnostic output (toString) doesn't show the identity of the
     * object associated with this key.
     */
    public MapKey(Class sourceClass, String desc, Object owner) {
        this.desc = sourceClass.getName() + ":" + desc
           + ":" + System.identityHashCode(owner);
    }

    /**
     * Provides a nice string which shows what class declares this StackId,
     * what it is intended to be used for ("desc") and what specific
     * owner (if any) the stack is associated with.
     */
    public String toString() {
        return desc;
    }
}
