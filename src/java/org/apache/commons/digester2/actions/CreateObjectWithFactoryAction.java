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

import org.apache.commons.logging.Log;

import org.apache.commons.digester2.Context;
import org.apache.commons.digester2.AbstractAction;
import org.apache.commons.digester2.ParseException;
import org.apache.commons.digester2.ArrayStack;

/**
 * <p>An Action that uses an {@link ObjectFactory} to create a new object
 * which it pushes onto the object stack.  When the element is complete, the
 * object is popped from that stack.</p>
 *
 * <p>This action is intended in situations where instances of different classes
 * may be created and the CreateObjectAction is not sufficiently flexible. It
 * does require a custom ObjectFactory implementation to be written in order to
 * implement the logic which decides what class the new instance is created
 * from.</p>
 *
 * <p>A common scenario is for the ObjectFactory implementation to use the
 * xml attributes of the matched xml element as parameters in a call to either
 * a factory method or to a non-empty constructor.</p>
 */

public class CreateObjectWithFactoryAction extends AbstractAction {

    // -----------------------------------------------------
    // Instance Variables
    // -----------------------------------------------------

    /**
     * The object creation factory (if any) explicitly provided to a
     * constructor. Note that (as per Action requirements) this value
     * never changes during parsing; if this is initially null, and
     * an ObjectFactory is later created, then that object is stored
     * on the Context, not here.
     */
    protected ObjectFactory objectFactory = null;

    /**
     * The Java class name of the ObjectCreationFactory to be created.
     * This class must have a no-arguments constructor. Not relevant
     * if creationFactory is non-null.
     */
    protected String factoryClassName = null;

    /**
     * The attribute containing an override class name if it is present.
     */
    protected String attributeName = null;

    // -----------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------

    /**
     * Construct a factory create rule using the given {@link ObjectFactory}.
     * <p>
     * Exceptions thrown during the object creation process will be propagated.
     *
     * @param factory called on to create the object.
     */
    public CreateObjectWithFactoryAction(ObjectFactory factory) {
        this.objectFactory = factory;
    }

    /**
     * <p>Create an object that will use the specified class name to create
     * an {@link ObjectFactory} which will then be used to create an object
     * and push it on the stack.</p>
     *
     * <p>The SAXHandler.getClassLoader method will be used to obtain a
     * classloader to load the specified class through.</p>
     *
     * <p>Exceptions thrown during the object creation process will be propagated.</p>
     *
     * @param className Java class name of the object creation factory class
     */
    public CreateObjectWithFactoryAction(String className) {
        this.factoryClassName = className;
    }

    /**
     * <p>Create an object that will use the specified class name to create
     * an {@link ObjectFactory} which will then be used to create an object
     * and push it on the stack.</p>
     *
     * <p>Exceptions thrown during the object creation process will be propagated.</p>
     *
     * <p>Note that this is equivalent to calling
     * <code>CreateObjectWithFactoryAction(clazz.getName())</code>.
     * In particular, the classloader associated with the clazz parameter
     * is ignored.</p>
     *
     * @param clazz Java class name of the object creation factory class
     */
    public CreateObjectWithFactoryAction(Class clazz) {
        this.factoryClassName = clazz.getName();
    }

    /**
     * <p>Construct a factory create rule that will use the specified
     * class name (possibly overridden by the specified attribute if present)
     * to create an {@link ObjectCreationFactory}, which will then be used
     * to instantiate an object instance and push it onto the stack.</p>
     *
     * <p>Exceptions thrown during the object creation process will be propagated.</p>
     *
     * @param className Default Java class name of the factory class
     * @param attributeName Attribute name which, if present, contains an
     *  override of the class name of the object creation factory to create.
     */
    public CreateObjectWithFactoryAction(String className, String attributeName) {
        this.factoryClassName = className;
        this.attributeName = attributeName;
    }

    /**
     * <p>Construct a factory create rule that will use the specified
     * class (possibly overridden by the specified attribute if present)
     * to create an {@link ObjectFactory}, which will then be used
     * to instantiate an object instance and push it onto the stack.</p>
     *
     * <p>Note that this is equivalent to calling
     * <code>CreateObjectWithFactoryAction(clazz.getName(), attributeName)</code>.
     * In particular, the classloader associated with the clazz parameter
     * is ignored.</p>
     *
     * <p>Exceptions thrown during the object creation process will be propagated.</p>
     *
     * @param clazz Default Java class name of the factory class
     * @param attributeName Attribute name which, if present, contains an
     *  override of the class name of the object creation factory to create.
     */
    public CreateObjectWithFactoryAction(Class clazz, String attributeName) {
        this.factoryClassName = clazz.getName();
        this.attributeName = attributeName;
    }

    // ---------------------------------------------------------
    // Public Methods
    // ---------------------------------------------------------

    /**
     * Process the beginning of this element.
     *
     * @param attributes The attribute list of this element
     */
    public void begin(
    Context context,
    String namespace, String name, Attributes attributes)
    throws ParseException {
        Log log = context.getLogger();

        ObjectFactory factory = getFactory(context, attributes);
        Object instance = factory.createObject(context, attributes);

        if (log.isDebugEnabled()) {
            log.debug("[CreateObjectWithFactoryAction]{" + context.getMatchPath() +
                    "} New " + instance.getClass().getName());
        }
        context.push(instance);
    }

    /**
     * Process the end of this element.
     */
    public void end(
    Context context, String namespace, String name)
    throws ParseException {
        Log log = context.getLogger();

        Object top = context.pop();
        if (log.isDebugEnabled()) {
            log.debug("[CreateObjectWithFactoryAction]{" + context.getMatchPath() +
                    "} Pop " + top.getClass().getName());
        }
    }

    /**
     * Render a printable version of this Rule.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("CreateObjectWithFactoryAction[");
        if (objectFactory != null) {
            sb.append(", objectFactory=");
            sb.append(objectFactory);
        } else {
            sb.append("className=");
            sb.append(factoryClassName);
            sb.append(", attributeName=");
            sb.append(attributeName);
        }
        sb.append("]");
        return (sb.toString());
    }

    // ------------------------------------------------------ 
    // Protected Methods
    // ------------------------------------------------------ 

    /**
     * Return an instance of our associated object factory,
     * creating one if necessary. Factory instances created here are
     * cached for future reuse by this Action instance (but other instances
     * of this same class will create their own ObjectFactory instances).
     *
     * @param attributes Attributes passed to our factory creation element
     *
     * @exception Exception if any error occurs
     */
    protected ObjectFactory getFactory(
    Context context, Attributes attributes)
    throws ParseException {
        
        if (objectFactory != null) {
            // if objectFactory is set, then className and attributeName
            // will always be null...
            return objectFactory;
        }

        Log log = context.getLogger();

        String realClassName = factoryClassName;
        if (attributeName != null) {
            String overrideClassName = attributes.getValue(attributeName);
            if (overrideClassName != null) {
                realClassName = overrideClassName;
            }
        }
        
        // now retrieve the cached ObjectFactory for this classname
        ObjectFactory factory = 
            (ObjectFactory) context.getInstanceData(this, "objectFactory");
        
        if (factory == null) {
            // this rule instance has never created a factory of this class
            if (log.isDebugEnabled()) {
                log.debug("[CreateObjectWithFactoryAction]{" + context.getMatchPath() +
                        "} New factory " + realClassName);
            }

            try {
                // create the factory object
                Class clazz = context.getClassLoader().loadClass(realClassName);
                factory = (ObjectFactory) clazz.newInstance();
                
                // and cache the object for later retrieval by this instance
                context.putInstanceData(this, "objectFactory", factory);
            } catch(ClassNotFoundException ex) {
                throw new ParseException(
                    "Unable to load class '" + realClassName + "'", ex);
            } catch(InstantiationException ex) {
                throw new ParseException(
                    "Unable to create instance of class '" + realClassName + "'", ex);
            } catch(IllegalAccessException ex) {
                throw new ParseException(
                    "Unable to access constructor of class '" + realClassName + "'", ex);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("[CreateObjectWithFactoryAction]{" + context.getMatchPath() +
                        "} Reusing cached factory " + realClassName);
            }
        }

        return factory;
    }
}
