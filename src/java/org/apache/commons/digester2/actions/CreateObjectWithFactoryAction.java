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

import org.apache.commons.logging.Log;

import org.apache.commons.digester2.Context;
import org.apache.commons.digester2.AbstractAction;
import org.apache.commons.digester2.ParseException;
import org.apache.commons.digester2.ArrayStack;

/**
 * <p>Rule implementation that uses an {@link ObjectCreationFactory} to create
 * a new object which it pushes onto the object stack.  When the element is
 * complete, the object will be popped.</p>
 *
 * <p>This rule is intended in situations where the element's attributes are
 * needed before the object can be created.  A common scenario is for the
 * ObjectCreationFactory implementation to use the attributes  as parameters
 * in a call to either a factory method or to a non-empty constructor.
 */

public class CreateObjectWithFactoryAction extends AbstractAction {

    // ----------------------------------------------------------- Fields
    
    /** Should exceptions thrown by the factory be ignored? */
    private boolean ignoreCreateExceptions;
    /** Stock to manage */
    private ArrayStack exceptionIgnoredStack;

    // ----------------------------------------------------------- Constructors

    /**
     * <p>Construct a factory create rule that will use the specified
     * class name to create an {@link ObjectCreationFactory} which will
     * then be used to create an object and push it on the stack.</p>
     *
     * <p>Exceptions thrown during the object creation process will be propagated.</p>
     *
     * @param className Java class name of the object creation factory class
     */
    public CreateObjectWithFactoryAction(String className) {
        this(className, false);
    }

    /**
     * <p>Construct a factory create rule that will use the specified
     * class to create an {@link ObjectCreationFactory} which will
     * then be used to create an object and push it on the stack.</p>
     *
     * <p>Exceptions thrown during the object creation process will be propagated.</p>
     *
     * @param clazz Java class name of the object creation factory class
     */
    public CreateObjectWithFactoryAction(Class clazz) {
        this(clazz, false);
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
        this(className, attributeName, false);
    }

    /**
     * <p>Construct a factory create rule that will use the specified
     * class (possibly overridden by the specified attribute if present)
     * to create an {@link ObjectCreationFactory}, which will then be used
     * to instantiate an object instance and push it onto the stack.</p>
     *
     * <p>Exceptions thrown during the object creation process will be propagated.</p>
     *
     * @param clazz Default Java class name of the factory class
     * @param attributeName Attribute name which, if present, contains an
     *  override of the class name of the object creation factory to create.
     */
    public CreateObjectWithFactoryAction(Class clazz, String attributeName) {
        this(clazz, attributeName, false);
    }

    /**
     * <p>Construct a factory create rule using the given, already instantiated,
     * {@link ObjectCreationFactory}.</p>
     *
     * <p>Exceptions thrown during the object creation process will be propagated.</p>
     *
     * @param creationFactory called on to create the object.
     */
    public CreateObjectWithFactoryAction(ObjectCreationFactory creationFactory) {
        this(creationFactory, false);
    }
    
    /**
     * Construct a factory create rule that will use the specified
     * class name to create an {@link ObjectCreationFactory} which will
     * then be used to create an object and push it on the stack.
     *
     * @param className Java class name of the object creation factory class
     * @param ignoreCreateExceptions if true, exceptions thrown by the object
     *  creation factory
     * will be ignored.
     */
    public CreateObjectWithFactoryAction(String className, boolean ignoreCreateExceptions) {
        this(className, null, ignoreCreateExceptions);
    }

    /**
     * Construct a factory create rule that will use the specified
     * class to create an {@link ObjectCreationFactory} which will
     * then be used to create an object and push it on the stack.
     *
     * @param clazz Java class name of the object creation factory class
     * @param ignoreCreateExceptions if true, exceptions thrown by the
     *  object creation factory
     * will be ignored.
     */
    public CreateObjectWithFactoryAction(Class clazz, boolean ignoreCreateExceptions) {
        this(clazz, null, ignoreCreateExceptions);
    }

    /**
     * Construct a factory create rule that will use the specified
     * class name (possibly overridden by the specified attribute if present)
     * to create an {@link ObjectCreationFactory}, which will then be used
     * to instantiate an object instance and push it onto the stack.
     *
     * @param className Default Java class name of the factory class
     * @param attributeName Attribute name which, if present, contains an
     *  override of the class name of the object creation factory to create.
     * @param ignoreCreateExceptions if true, exceptions thrown by the object
     *  creation factory will be ignored.
     */
    public CreateObjectWithFactoryAction(
                                String className, 
                                String attributeName,
                                boolean ignoreCreateExceptions) {

        this.className = className;
        this.attributeName = attributeName;
        this.ignoreCreateExceptions = ignoreCreateExceptions;

    }


    /**
     * Construct a factory create rule that will use the specified
     * class (possibly overridden by the specified attribute if present)
     * to create an {@link ObjectCreationFactory}, which will then be used
     * to instantiate an object instance and push it onto the stack.
     *
     * @param clazz Default Java class name of the factory class
     * @param attributeName Attribute name which, if present, contains an
     *  override of the class name of the object creation factory to create.
     * @param ignoreCreateExceptions if true, exceptions thrown by the object
     *  creation factory will be ignored.
     */
    public CreateObjectWithFactoryAction(
                                Class clazz, 
                                String attributeName,
                                boolean ignoreCreateExceptions) {

        this(clazz.getName(), attributeName, ignoreCreateExceptions);

    }


    /**
     * Construct a factory create rule using the given, already instantiated,
     * {@link ObjectCreationFactory}.
     *
     * @param creationFactory called on to create the object.
     * @param ignoreCreateExceptions if true, exceptions thrown by the object
     *  creation factory will be ignored.
     */
    public CreateObjectWithFactoryAction(
                            ObjectCreationFactory creationFactory, 
                            boolean ignoreCreateExceptions) {

        this.creationFactory = creationFactory;
        this.ignoreCreateExceptions = ignoreCreateExceptions;
    }

    // ----------------------------------------------------- Instance Variables


    /**
     * The attribute containing an override class name if it is present.
     */
    protected String attributeName = null;


    /**
     * The Java class name of the ObjectCreationFactory to be created.
     * This class must have a no-arguments constructor.
     */
    protected String className = null;


    /**
     * The object creation factory we will use to instantiate objects
     * as required based on the attributes specified in the matched XML
     * element.
     */
    protected ObjectCreationFactory creationFactory = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Process the beginning of this element.
     *
     * @param attributes The attribute list of this element
     */
    public void begin(
    Context context, String namespace, String name, Attributes attributes) 
    throws ParseException {
        
        Log log = context.getLogger();
        
        if (ignoreCreateExceptions) {
        
            if (exceptionIgnoredStack == null) {
                exceptionIgnoredStack = new ArrayStack();
            }
            
            try {
                Object instance = 
                    getFactory(context, attributes).createObject(context, attributes);
                
                if (log.isDebugEnabled()) {
                    log.debug("[CreateObjectWithFactoryAction]{" + context.getMatchPath() +
                            "} New " + instance.getClass().getName());
                }
                context.push(instance);
                exceptionIgnoredStack.push(Boolean.FALSE);
                
            } catch (Exception e) {
                // log message and error
                if (log.isInfoEnabled()) {
                    log.info("[CreateObjectWithFactoryAction] Create exception ignored: " +
                        ((e.getMessage() == null) ? e.getClass().getName() : e.getMessage()));
                    if (log.isDebugEnabled()) {
                        log.debug("[CreateObjectWithFactoryAction] Ignored exception:", e);
                    }
                }
                exceptionIgnoredStack.push(Boolean.TRUE);
            }
            
        } else {
            Object instance = getFactory(context, attributes).createObject(context, attributes);
            
            if (log.isDebugEnabled()) {
                log.debug("[CreateObjectWithFactoryAction]{" + context.getMatchPath() +
                        "} New " + instance.getClass().getName());
            }
            context.push(instance);
        }
    }


    /**
     * Process the end of this element.
     */
    public void end(
    Context context, String namespace, String name) 
    throws ParseException {
        
        Log log = context.getLogger();

        // check if object was created 
        // this only happens if an exception was thrown and we're ignoring them
        if (	
                ignoreCreateExceptions &&
                exceptionIgnoredStack != null &&
                !(exceptionIgnoredStack.empty())) {
                
            if (((Boolean) exceptionIgnoredStack.pop()).booleanValue()) {
                // creation exception was ignored
                // nothing was put onto the stack
                if (log.isTraceEnabled()) {
                    log.trace("[CreateObjectWithFactoryAction] No creation so no push so no pop");
                }
                return;
            }
        } 

        Object top = context.pop();
        if (log.isDebugEnabled()) {
            log.debug("[CreateObjectWithFactoryAction]{" + context.getMatchPath() +
                    "} Pop " + top.getClass().getName());
        }

    }


    /**
     * Clean up after parsing is complete.
     */
    public void finishParse() throws ParseException {
        if (attributeName != null) {
            creationFactory = null;
        }
    }

    /**
     * Render a printable version of this Rule.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("CreateObjectWithFactoryAction[");
        sb.append("className=");
        sb.append(className);
        sb.append(", attributeName=");
        sb.append(attributeName);
        if (creationFactory != null) {
            sb.append(", creationFactory=");
            sb.append(creationFactory);
        }
        sb.append("]");
        return (sb.toString());

    }


    // ------------------------------------------------------ Protected Methods

    /**
     * Return an instance of our associated object creation factory,
     * creating one if necessary.
     *
     * @param attributes Attributes passed to our factory creation element
     *
     * @exception Exception if any error occurs
     */
    protected ObjectCreationFactory getFactory(
    Context context, Attributes attributes)
    throws ParseException {

        Log log = context.getLogger();

        if (creationFactory == null) {
            String realClassName = className;
            if (attributeName != null) {
                String value = attributes.getValue(attributeName);
                if (value != null) {
                    realClassName = value;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("[CreateObjectWithFactoryAction]{" + context.getMatchPath() +
                        "} New factory " + realClassName);
            }
            try {
            Class clazz = context.getClassLoader().loadClass(realClassName);
            creationFactory = (ObjectCreationFactory)
                    clazz.newInstance();
            } catch(ClassNotFoundException ex) {
                throw new ParseException("Unable to load class '" + realClassName + "'", ex);
            } catch(InstantiationException ex) {
                throw new ParseException("Unable to create instance of class '" + realClassName + "'", ex);
            } catch(IllegalAccessException ex) {
                throw new ParseException("Unable to access constructor of class '" + realClassName + "'", ex);
            }
        }
        return creationFactory;
    }    
}
