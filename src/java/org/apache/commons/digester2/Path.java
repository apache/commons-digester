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

/**
 * A simple class that represents the path to a particular XML element.
 */

public class Path {
    /**
     * Contains a string of form "/foo/{ns1}bar/baz" which describes the
     * complete path from the root of the document to the current element.
     */
    private StringBuffer buf = new StringBuffer();
    
    /**
     * Contains offsets into the "buf" buffer. As new elements are added to
     * the path, the old buffer length is stored so that it is easy to
     * restore the old path when an element is "popped" from the path.
     * Note that namespaces may have forward-slashes in them, so simply
     * performing string scans to "remove" an element from the path is
     * not effective.
     * <p>
     * Note that this could be recomputed from the information present
     * in the namespaces and localNames stacks, but that would be very
     * inefficient.
     */
    private ArrayStack lengths = new ArrayStack();
    
    /**
     * The namespaces of the elements found so far. This stack is correlated
     * with the localNames stack; they always have the same number of 
     * elements and entries with the same stack offset are a (namespace, name)
     * pair.
     */
    private ArrayStack namespaces = new ArrayStack();

    /**
     * The localnames of the elements found so far. This stack is correlated
     * with the namespaces stack; they always have the same number of 
     * elements and entries with the same stack offset are a (namespace, name)
     * pair.
     */
    private ArrayStack localNames = new ArrayStack();
    
    /**
     * Create a new initial path. Note that this does not represent
     * "the root of a document", but rather "no path". It should not
     * be used until the push method has been called for the first
     * time, to define the root element of the document.
     */
    public Path() {
    }
    
    /**
     * Expand the path to include the specified child element.
     */
    
    public void push(String namespace, String elementName) {
        lengths.push(new Integer(buf.length()));
        buf.append("/");
        if ((namespace != null) && (namespace.length()>0)) {
            buf.append('{');
            buf.append(namespace);
            buf.append('}');
        }
        buf.append(elementName);
        
        namespaces.push(namespace);
        localNames.push(elementName);
    }
    
    /**
     * Remove the last-pushed element, restoring the path to its
     * previous state.
     */
    public void pop() {
        int length = ((Integer)lengths.pop()).intValue();
        buf.setLength(length);
        
        namespaces.pop();
        localNames.pop();
    }
    
    /**
     * Return the path to the current element.
     */
    public String getPath() {
        return buf.toString();
    }

    /**
     * Returns the number of xml elements currently in the path.
     */
    public int getDepth() {
        return namespaces.size();
    }
    
    /**
     * Returns the namespace of the element at the specified offset from
     * the top of the stack. An offset of zero returns the most recently
     * pushed element, while an offset of getDepth()-1 returns the first
     * pushed element.
     */
    public String peekNamespace(int offset) {
        return (String) namespaces.peek(offset);
    }
    
    /**
     * Returns the localname of the element at the specified offset from
     * the top of the stack. An offset of zero returns the most recently
     * pushed element, while an offset of getDepth()-1 returns the first
     * pushed element.
     */
    public String peekLocalname(int offset) {
        return (String) localNames.peek(offset);
    }
    
    /**
     * Reset this object to its initially-constructed state.
     */
    public void clear() {
        buf.setLength(0);
        lengths.clear();
        namespaces.clear();
        localNames.clear();
    }
    
    public boolean matches(String pathToMatch) {
        if (pathToMatch.charAt(0) == '/') {
            // absolute
            return getPath().equals(pathToMatch);
        } else {
            // relative
            // XXX looks wrong but protects a match of 
            // "a/b" against a path of "/gotcha/b", but
            // still allows
            // "a/b" to match against "/a/b"
            return getPath().endsWith("/" + pathToMatch);
        }
    }
    
    /** 
     * Checks if this path matches any of the paths given. This means we iterate through 
     * <code>pathsToMatch</code> and match every entry to this path.
     */
    public boolean matchsAny(String[] pathsToMatch) {
        for (int i = 0; i < pathsToMatch.length; i++) {
            if (matches(pathsToMatch[i]))
                return true;
        }
        return false;
    }

    public String toString() {
        return getPath();
    }

}

