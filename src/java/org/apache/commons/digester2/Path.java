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

/**
 * A simple class that represents the path to a particular XML element.
 */

public class Path {
    StringBuffer buf = new StringBuffer();
    ArrayStack lengths = new ArrayStack();
    
    public Path() {
    }
    
    public void push(String namespace, String elementName) {
        lengths.push(new Integer(buf.length()));
        buf.append("/");
        if ((namespace != null) && (namespace.length()>0)) {
            buf.append('{');
            buf.append(namespace);
            buf.append('}');
        }
        buf.append(elementName);
    }
    
    public void pop() {
        int length = ((Integer)lengths.pop()).intValue();
        buf.setLength(length);
    }
    
    public String getPath() {
        return buf.toString();
    }
    
    public void clear() {
        buf.setLength(0);
        lengths.clear();
    }
}

