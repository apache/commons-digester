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

    // --------------------------------------------------- 
    // Local classes
    // ---------------------------------------------------
    
    /**
    * See method {@link #putInstanceData}.
     */
    private static class InstanceItem {
        public Object key;
        public Map map;
        
        public InstanceItem(Object key, Map map) {
            this.key = key;
            this.map = map;
        }
    }

    // --------------------------------------------------- 
    // Instance Variables
    // --------------------------------------------------- 

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
     * The Locator associated with our parser object. This object can be
     * consulted to find out which line of the input xml document we are
     * currently on - very useful when generating error messages.
     */
    private Locator documentLocator = null;

    /**
     * Registered namespaces we are currently processing.  The key is the
     * namespace prefix that was declared in the document.  The value is an
     * ArrayStack of the namespace URIs this prefix has been mapped to --
     * the top Stack element is the most current one.  (This architecture
     * is required because documents can declare nested uses of the same
     * prefix for different Namespace URIs).
     * <p>
     * TODO: move this data to the Context.
     */
    private HashMap namespaces = new HashMap();

    /**
     * If not null, then calls to the saxHandler's characters, startElement, 
     * endElement and processingInstruction methods are forwarded to the 
     * specified object. This is intended to allow rules to temporarily 
     * "take control" of the sax events. In particular, this is used by 
     * NodeCreateAction.
     */
    private ContentHandler contentHandler = null;

    /**
     * The body text of the current element since the most recent child
     * element (or start of the element if no child elements have yet been
     * seen). When a child element is found, this text is reported to 
     * matching actions via the Action.bodySegment method, then the buffer
     * can be cleared. There is no need for a stack of these.
     */
    private StringBuffer bodyTextSegment = new StringBuffer();

    /**
     * The body text of the current element. As the parser reports chunks
     * of text associated with the current element, they are appended here.
     * When the end of the element is reported, the full text content of the
     * current element should be here. Note that if the element has mixed
     * content, ie text intermingled with child elements, then this buffer
     * ends up with all the different text pieces appended to form one string.
     */
    private StringBuffer bodyText = new StringBuffer();

    /**
     * When processing an element with mixed content (ie text and child
     * elements), then when we start a child element we need to store the
     * current text seen so far, and restore it after we have finished
     * with the child element. This stack therefore contains StringBuffer
     * items containing the body text of "interrupted" xml elements.
     */
    private ArrayStack bodyTexts = new ArrayStack();

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

    /**
     * Place where other objects can store any data they like during a parse.
     * See method {@link #putInstanceData} for more information.
     */
    private List instanceData = new ArrayList();
    
    /**
     * The parameters stack being utilized by CallMethodAction and
     * CallParamAction.
     */
    private ArrayStack params = new ArrayStack();

    // --------------------------------------------------------- 
    // Constructors
    // --------------------------------------------------------- 

    /**
     * Construct a new Context.
     */
    public Context(SAXHandler saxHandler, Log log, Locator locator) {
        this.saxHandler = saxHandler;
        this.log = log;
        this.documentLocator = locator;
    }

    // ---------------------------------------------------
    // Properties
    // --------------------------------------------------- 

    /**
     * Return the current Logger associated with this instance of the Digester
     */
    public Log getLogger() {
        return log;
    }

    /**
     * Gets the document locator associated with our parser. This object 
     * can be consulted to find out which line of the input xml document 
     * we are currently on - very useful when generating error messages.
     *
     * @return the Locator supplied by the document parser
     */
    public Locator getDocumentLocator() {
        return documentLocator;
    }

    /**
     * Return the currently mapped namespace URI for the specified prefix,
     * if any; otherwise return <code>null</code>.  These mappings come and
     * go dynamically as the document is parsed.
     *
     * @param prefix Prefix to look up
     */
    public String findNamespaceURI(String prefix) {
        ArrayStack stack = (ArrayStack) namespaces.get(prefix);
        if (stack == null) {
            return null;
        }
        try {
            return (String) stack.peek();
        } catch (EmptyStackException e) {
            // This should never happen, as endPrefixMapping removes
            // the prefix from the namespaces map when the stack becomes
            // empty. Still, better safe than sorry..
            return null;
        }
    }

    /**
     * Register the specified prefix string as being an alias for the
     * specified namespace uri.
     */
    public void pushNamespace(String prefix, String namespaceURI) {
        ArrayStack stack = (ArrayStack) namespaces.get(prefix);
        if (stack == null) {
            stack = new ArrayStack();
            namespaces.put(prefix, stack);
        }
        stack.push(namespaceURI);
    }
     
    /**
     * Unregister the specified prefix string as being an alias for the
     * specified namespace uri.
     */
    public void popNamespace(String prefix) throws SAXException {
        ArrayStack stack = (ArrayStack) namespaces.get(prefix);
        if (stack == null) {
            throw createSAXException(
                "popNamespace called for unknown" +
                "namespace prefix '" + prefix + "'");
        }

        try {
            stack.pop();
            if (stack.empty())
                namespaces.remove(prefix);
        } catch (EmptyStackException e) {
            // This should never happen; it would indicate a serious
            // internal software flaw. If a prefix has no mapping, then
            // stack should have been null, not empty.
            throw createSAXException(
                "popNamespace called on prefix with empty stack.");
        }
    }

    /**
     * Returns the text seen in the current xml element since the last child 
     * element was seen (or since the start of the xml element if no child
     * elements have yet been encountered).
     */
    public StringBuffer getBodyTextSegment() {
        return bodyTextSegment;
    }
    
    /**
     * Clears the bodyTextSegment buffer. See {@link #getBodyTextSegment}.
     */
    public void clearBodyTextSegment() {
        bodyTextSegment.setLength(0);
    }
    
    /**
     * Save the buffer which is currently being used to accumulate text
     * content of the current xml element. This is expected to be called
     * just before starting processing of a child xml element.
     */
    public void pushBodyText() {
        bodyTexts.push(bodyText);
        bodyText = new StringBuffer();
    }
    
    /**
     * Restore a saved buffer. This is expected to be called just after
     * completing processing of a child xml element, to continue accumulating
     * text content for its parent.
     */
    public StringBuffer popBodyText() {
        StringBuffer tmp = bodyText;
        bodyText = (StringBuffer) bodyTexts.pop();
        return tmp;
    }
    
    /**
     * Append more text to the buffer representing the text content of the
     * current xml element. This is called in multiple stages because:
     * <ul>
     * <li>A sax parser is permitted to deliver a block of contiguous text
     *   in multiple callbacks if it wishes, and
     * <li>The text might be intermingled with child elements.
     * </ul>
     */
    public void appendBodyText(char[] buffer, int start, int length) {
        bodyTextSegment.append(buffer, start, length);
        bodyText.append(buffer, start, length);
    }
    
    /**
     * Return the Path object representing the path from the document root
     * to the current element.
     */
    public Path getCurrentPath() {
        return currentElementPath;
    }
    
    /**
     * Return the path to the xml element currently being processed.
     * This is exactly equivalent to <code>getCurrentPath().getPath()</code>.
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

    /**
     * Remember the list of Actions which matched the current element. This
     * method is expected to be called when the element's start tag has been
     * seen and the matching actions computed. The list is used later to
     * find the actions to invoke bodySegment/body/end methods on.
     *
     * @param actions should be a list of Action objects.
     */
    public void pushMatchingActions(List actions) {
        matchedActionLists.push(actions);
    }

    /**
     * Discard the list of Actions which matched the current element. This
     * method is expected to be called when the element's end tag has been
     * seen and the matching actions for the element are no longer needed.
     *
     * @return a list of Action objects.
     */
    public List popMatchingActions() {
        return (List) matchedActionLists.pop();
    }

    /**
     * Retrieve the list of Actions which matched the current element. 
     *
     * @return a list of Action objects.
     */
    public List peekMatchingActions() {
        return (List) matchedActionLists.peek();
    }

    /**
     * Obtain the classloader object that should be used when an Action class
     * needs to load a class to be instantiated to represent data in the input
     * xml being parsed.
     *
     * @return a classloader (never null)
     */
    public ClassLoader getClassLoader() {
        return saxHandler.getClassLoader();
    }

    /**
     * Return the SAXHandler which owns this object.
     */
    public SAXHandler getSAXHandler() {
        return saxHandler;
    }

    /**
     * Specify a contentHandler to forward calls to. If non-null, then
     * whenever this object receives calls from the XMLReader to any of
     * the following methods, the call will be forwarded on to the specified
     * objects instead of being processed in the normal manner.
     * <p>
     * This allows an Action to assume complete control of input handling
     * for a period of time. For example, this allows the NodeCreateAction
     * to build a DOM tree representing a portion of input.
     * <p>
     * Passing null restores normal operation, ie this object then resumes
     * processing of the callbacks itself.
     */
    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    /**
     * See {@link #setContentHandler}.
     */
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    // --------------------------------------------------- 
    // Object Stack Methods
    // --------------------------------------------------- 

    /**
     * The root object of the Object stack.
     */
    public void setRoot(Object o) {
        stack.clear(); // just in case setRoot called multiple times
        stack.push(o);
        root = o;
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
    
    // -----------------------------------------------
    // Other public methods
    // -----------------------------------------------
    
    /**
     * Place where an object (typically an Action) can store any data it likes
     * during a parse. Usually the "named stacks" facility is the most
     * appropriate location for action-related data, as using stacks allows
     * Actions to be re-entrant. However in some cases a stack is not
     * needed (eg when building a cache of Class objects used by an Action).
     * If the data is specific to an action instance, then the action instance
     * itself should be passed as the first parameter. If the data is used by
     * all action instances of a specific class, then the action's Class object
     * can be used as the first parameter.
     *
     * @param instance is a key to separate data related to this object from
     * other data stored on this context object.
     *
     * @param category identifies the particular item of data being stored,
     * so that an instance can store multiple data items.
     *
     * @param data is the object to be stored for later retrieval.
     */
    public void putInstanceData(Object instance, String category, Object data) {
        // Unfortunately, maps use equals to find a matching key rather than
        // identity, so we can't use a standard Map to implement this. As the
        // number of entries here is likely to be very small anyway, we will
        // just use a list-of-maps, and iterate over the list to find the
        // correct entry.
        //
        // Note that if we trusted System.identityHashCode to return a unique
        // string for an instance, then we could use a map. However that method
        // doesn't promise to return a unique string on all platforms.
        
        for(Iterator i = instanceData.iterator(); i.hasNext(); ) {
            InstanceItem item = (InstanceItem) i.next();
            if (item.key == instance) {
                item.map.put(category, data);
                return;
            }
        }
        
        // this instance has never stored data before
        Map map = new HashMap();
        map.put(category, data);
        InstanceItem instanceItem = new InstanceItem(instance, map);
        instanceData.add(instanceItem);
    }

    /**
     * Retrieve a piece of data stored earlier via putInstanceData.
     */
    public Object getInstanceData(Object instance, String category) {
        for(Iterator i = instanceData.iterator(); i.hasNext(); ) {
            InstanceItem item = (InstanceItem) i.next();
            if (item.key == instance) {
                return item.map.get(category);
            }
        }
        
        return null;
    }

    /**
     * Create a SAX exception which also understands about the location in
     * the digester file where the exception occurs. This method is expected
     * to be called by Action classes when they detect a problem.
     *
     * @return the new exception
     */
    public SAXException createSAXException(String message, Exception e) {
        if ((e != null) &&
            (e instanceof InvocationTargetException)) {
            Throwable t = ((InvocationTargetException) e).getTargetException();
            if ((t != null) && (t instanceof Exception)) {
                e = (Exception) t;
            }
        }

        if (documentLocator != null) {
            String error = 
                "Error at line " + documentLocator.getLineNumber() 
                + ", column " + documentLocator.getColumnNumber()
                + ": " + message;

            if (e != null) {
                return new SAXParseException(error, documentLocator, e);
            } else {
                return new SAXParseException(error, documentLocator);
            }
        }

        // The SAX parser doesn't have location info enabled, so we'll just
        // generate the best error message we can without it.
        log.error("No Locator!");
        if (e != null) {
            return new SAXException(message, e);
        } else {
            return new SAXException(message);
        }
    }

    /**
     * Create a SAX exception which also understands about the location in
     * the digester file where the exception occurs. This method is expected
     * to be called by Action classes when they detect a problem.
     *
     * @return the new exception
     */
    public SAXException createSAXException(Exception e) {
        if (e instanceof InvocationTargetException) {
            Throwable t = ((InvocationTargetException) e).getTargetException();
            if ((t != null) && (t instanceof Exception)) {
                e = (Exception) t;
            }
        }
        return createSAXException(e.getMessage(), e);
    }

    /**
     * Create a SAX exception which also understands about the location in
     * the digester file where the exception occurs. This method is expected
     * to be called by Action classes when they detect a problem.
     *
     * @return the new exception
     */
    public SAXException createSAXException(String message) {
        return createSAXException(message, null);
    }
}
