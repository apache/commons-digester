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

package org.apache.commons.digester2;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.collections.ArrayStack;


/**
 * <p>Holds information that evolves as the parsing of an input xml document
 * progresses. The Action objects regularly interact with this object when their
 * various methods fire as a result of their associated patterns being matched
 * by input data. </p>
 *
 * <p>A new Context object is created for each document parsed, to ensure that
 * state left over from a previous parse does not affect later parses. It
 * is the SAXHandler class that creates the Context instances.</p>
 *
 * <p>This class does not hold information that is directly related to
 * handling sax parsing events.</p>
 */

public class Context {

    // --------------------------------------------------------- Constructors

    /**
     * Construct a new Context.
     */
    public Context(SAXHandler saxHandler, Log log) {
        this.saxHandler = saxHandler;
        this.log = log;
    }

    // --------------------------------------------------- Instance Variables

    /**
     * The owner of this object.
     */
    private SAXHandler saxHandler = null;

    /**
     * An object through which log messages can be issued. Note that this
     * is equivalent to saxHandler.getLogger(), and a direct reference is
     * kept here only for performance.
     */
    private Log log;

    /**
     * The owner of the set of rules (pattern, action pairs).
     */
    private RuleManager ruleManager = null;

    /**
     * Stack whose elements are List objects, each containing a list of
     * Action objects as returned from RuleManager.getMatchingActions().
     * As each xml element in the input is entered, the list of matching
     * actions is pushed onto this stack. After the end tag is reached, the
     * matches are popped again. The depth of is stack is therefore exactly
     * the same as the current "nesting" level of the input xml.
     */
    private ArrayStack matchedActionLists = new ArrayStack(10);

    /**
     * The path to the xml element for which Actions are currently being fired.
     */
    private Path currentElementPath = new Path();

    /**
     * The parameters stack being utilized by CallMethodAction and
     * CallParamAction.
     */
    private ArrayStack params = new ArrayStack();

    /**
     * The object that forms the root of the tree of objects being
     * created during a parse. Note that if setRoot has been called, then
     * this is the same as looking at the bottom element on the object stack.
     * However when an Action has created the root object, then the root should
     * be acessable even after the parse is complete (at which time the stack
     * will be empty).
     */
    private Object root;

    /**
     * The object stack being constructed.
     */
    private ArrayStack stack = new ArrayStack();

    /**
     * Stacks used by Action objects to store internal state.
     * They can also be used for inter-action communication.
     *
     * By convention, Action instances use their class name as the key
     * (or as a key prefix) to this map.
     */
    private HashMap stacksByName = new HashMap();

    // ------------------------------------------------------------- Properties

    /**
     * Return the current Logger associated with this instance of the Digester
     */
    public Log getLogger() {
        return log;
    }

    /**
     * Return the path to the xml element currently being processed.
     */
    public String getMatchPath() {
        return currentElementPath.getPath();
    }

    /**
     * Enter a new element.
     */
    public String pushMatchPath(String namespace, String elementName) {
        currentElementPath.push(namespace, elementName);
        return currentElementPath.getPath();
    }

    /**
     * Leave an element; restore the previous match path.
     */
    public String popMatchPath() {
        currentElementPath.pop();
        return currentElementPath.getPath();
    }

    public void pushMatchingActions(List actions) {
        matchedActionLists.push(actions);
    }

    public List popMatchingActions() {
        return (List) matchedActionLists.pop();
    }

    public List peekMatchingActions() {
        return (List) matchedActionLists.peek();
    }

    public ClassLoader getClassLoader() {
        return saxHandler.getClassLoader();
    }

    public SAXHandler getSAXHandler() {
        return saxHandler;
    }

    // --------------------------------------------------- Object Stack Methods

    /**
     * The root object of the Object stack.
     */
    public void setRoot(Object o) {
        stack.clear(); // just in case setRoot called multiple times
        stack.push(o);
        root = o;
    }

    /**
     * <p>Is the object stack empty?</p>
     *
     * @return true if the object stack if empty
     */
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    /**
     * Return the current depth of the object stack.
     */
    public int getStackSize() {
        return stack.size();
    }

    /**
     * Return the top object on the stack without removing it.
     *
     * @throws EmptyStackException (a RuntimeException subclass) if the stack
     * is empty. Note that all the Digester.parse methods will turn this into
     * a (checked) DigestionException.
     */
    public Object peek() throws EmptyStackException {
        return stack.peek();
    }

    /**
     * Return the n'th object down the stack, where 0 is the top element
     * and [getStackSize()-1] is the bottom element.
     *
     * @param n Index of the desired element, where 0 is the top of the stack,
     *  1 is the next element down, and so on.
     *
     * @throws EmptyStackException (a RuntimeException subclass) if the index
     * is out-of-range. Note that all the Digester.parse methods will turn this
     * into a (checked) DigestionException.
     *
     * @throws ArrayOutOfBoundsException (a RuntimeException subclass) if 
     * index < 0. Note that all the Digester.parse methods will turn this
     * into a (checked) DigestionException.
     */
    public Object peek(int n) 
    throws EmptyStackException, IndexOutOfBoundsException {
            return stack.peek(n);
    }

    /**
     * Pop the top object off of the stack, and return it.  
     *
     * @throws EmptyStackException (a RuntimeException subclass) if the stack
     * is empty. Note that all the Digester.parse methods will turn this into
     * a (checked) DigestionException.
     */
    public Object pop() throws EmptyStackException {
            return stack.pop();
    }

    /**
     * Push a new object onto the top of the object stack.
     *
     * @param object The new object
     */
    public void push(Object object) {
        if (stack.isEmpty()) {
            root = object;
        }
        stack.push(object);
    }

    /**
     * <p>Is the stack with the given name empty?</p>
     * <p><strong>Note:</strong> a stack is considered empty
     * if no objects have been pushed onto it yet.</p>
     *
     * @param stackName the name of the stack whose emptiness
     * should be evaluated
     *
     * @return true if the given stack if empty
     */
    public boolean isEmpty(String stackName) {
        boolean result = true;
        ArrayStack namedStack = (ArrayStack) stacksByName.get(stackName);
        if (namedStack != null ) {
            result = namedStack.isEmpty();
        }
        return result;
    }

    /**
     * Return the current depth of the specified stack.
     *
     * @param stackName the name of the stack to be peeked
     */
    public int getStackSize(String stackName) {
        boolean result = true;
        ArrayStack namedStack = (ArrayStack) stacksByName.get(stackName);
        if (namedStack != null ) {
            return namedStack.size();
        }
        return 0;
    }

    /**
     * <p>Gets the top object from the stack with the given name.
     * This method does not remove the object from the stack.
     * </p>
     * <p><strong>Note:</strong> a stack is considered empty
     * if no objects have been pushed onto it yet.</p>
     *
     * @param stackName the name of the stack to be peeked
     * @return the top <code>Object</code> on the stack.
     * @throws EmptyStackException if the named stack is empty
     */
    public Object peek(String stackName) throws EmptyStackException {
        ArrayStack namedStack = (ArrayStack) stacksByName.get(stackName);
        if (namedStack == null ) {
            if (log.isDebugEnabled()) {
                log.debug("Stack '" + stackName + "' is empty");
            }
            throw new EmptyStackException();
        } else {
            return namedStack.peek();
        }
    }

    /**
     * Returns an element from the stack with the given name. The element
     * returned is the n'th object down the stack, where 0 is the top element
     * and [getStackSize()-1] is the bottom element.
     *
     * <p><strong>Note:</strong> a stack is considered empty
     * if no objects have been pushed onto it yet.</p>
     *
     * @param stackName the name of the stack to be peeked
     *
     * @param n Index of the desired element, where 0 is the top of the stack,
     *  1 is the next element down, and so on.
     *
     * @throws EmptyStackException (a RuntimeException subclass) if the index
     * is out-of-range. Note that all the Digester.parse methods will turn this
     * into a (checked) DigestionException.
     *
     * @throws ArrayOutOfBoundsException (a RuntimeException subclass) if 
     * index < 0. Note that all the Digester.parse methods will turn this
     * into a (checked) DigestionException.
     */
    public Object peek(String stackName, int n) 
    throws EmptyStackException, IndexOutOfBoundsException {
        ArrayStack namedStack = (ArrayStack) stacksByName.get(stackName);
        if (namedStack == null ) {
            if (log.isDebugEnabled()) {
                log.debug("Stack '" + stackName + "' is empty");
            }
            throw new EmptyStackException();
        } else {
            return namedStack.peek(n);
        }
    }

    /**
     * <p>Pops (gets and removes) the top object from the stack with the 
     * given name.</p>
     *
     * <p><strong>Note:</strong> a stack is considered empty
     * if no objects have been pushed onto it yet.</p>
     *
     * @param stackName the name of the stack from which the top value is to 
     * be popped
     *
     * @return the top <code>Object</code> on the stack.
     *
     * @throws EmptyStackException if the named stack is empty, or has not 
     * yet been created.
     */
    public Object pop(String stackName) throws EmptyStackException {
        ArrayStack namedStack = (ArrayStack) stacksByName.get(stackName);
        if (namedStack == null) {
            if (log.isDebugEnabled()) {
                log.debug("Stack '" + stackName + "' is empty");
            }
            throw new EmptyStackException();
        } else {
            return namedStack.pop();
        }
    }

    /**
     * Pushes the given object onto the stack with the given name.
     * If no stack already exists with the given name then one will be created.
     *
     * @param stackName the name of the stack onto which the object should be pushed
     * @param value the Object to be pushed onto the named stack.
     */
    public void push(String stackName, Object value) {
        ArrayStack namedStack = (ArrayStack) stacksByName.get(stackName);
        if (namedStack == null) {
            namedStack = new ArrayStack();
            stacksByName.put(stackName, namedStack);
        }
        namedStack.push(value);
    }

    /**
     * Returns the root element of the tree of objects created as a result
     * of applying the action objects to the input XML.
     * <p>
     * If the digester stack was "primed" by explicitly calling setRoot before
     * parsing started, then that root object is returned here.
     * <p>
     * Alternatively, if an Action which creates an object (eg ObjectCreateAction)
     * matched the root element of the xml, then the object created will be
     * returned here.
     * <p>
     * In other cases, the object most recently pushed onto an empty digester
     * stack is returned. This would be a most unusual use of digester, however;
     * one of the previous configurations is much more likely.
     * <p>
     * Note that when using one of the Digester.parse methods, the return
     * value from the parse method is exactly the same as the return value
     * from this method. However when the Digester's SAXHandler has been used
     * explicitly as the content-handler for a user-provided sax parser, no
     * such return value is available; in this case, this method allows you
     * to access the root object that has been created after parsing has
     * completed.
     *
     * @return the root object that has been created after parsing
     *  or null if the digester has not parsed any XML yet.
     */
    public Object getRoot() {
        return root;
    }

    /**
     * <p>Return the top object on the parameters stack without removing it.</p>
     *
     * <p>The parameters stack is used to store <code>CallMethodAction</code>
     * parameters. See {@link #params}.</p>
     *
     * @throws EmptyStackException if the parameters stack is empty.
     */
    public Object peekParams() throws EmptyStackException {
        return params.peek();
    }

    /**
     * <p>Return the n'th object down the parameters stack, where 0 is the 
     * top element and [stacksize-1] is the bottom element.
     *
     * <p>The parameters stack is used to store <code>CallMethodAction</code> 
     * parameters. See {@link #params}.</p>
     *
     * @param n Index of the desired element, where 0 is the top of the stack,
     *  1 is the next element down, and so on.
     *
     * @throws EmptyStackException if the parameters stack is empty.
     */
    public Object peekParams(int n) throws EmptyStackException {
        return params.peek(n);
    }

    /**
     * <p>Pop the top object off of the parameters stack, and return it.</p>
     *
     * <p>The parameters stack is used to store <code>CallMethodAction</code>
     * parameters. See {@link #params}.</p>
     *
     * @throws EmptyStackException if the parameters stack is empty.
     */
    public Object popParams() throws EmptyStackException {
        return params.pop();
    }

    /**
     * <p>Push a new object onto the top of the parameters stack.</p>
     *
     * <p>The parameters stack is used to store <code>CallMethodAction</code> 
     * parameters. See {@link #params}.</p>
     *
     * @param object The new object
     */
    public void pushParams(Object object) {
        params.push(object);

    }
}
