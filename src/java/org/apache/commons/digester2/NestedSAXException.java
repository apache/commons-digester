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
 * In some places in SAXHandler classes, we are permitted only to throw
 * subclasses of SAXException. This class can be used to wrap the exception
 * we really want to throw; the receiver should unwrap it via the getCause
 * method.
 * <p>
 * We implement getCause directly here, in order to be able to support
 * java platforms earlier than 1.4.
 */

public class NestedSAXException extends org.xml.sax.SAXException {
    private Throwable cause;
    
    public NestedSAXException(String msg, Throwable t) {
        super(msg);
        cause = t;
    }
    
    public NestedSAXException(Throwable t) {
        super(t.getMessage());
        cause = t;
    }
    
    public Throwable getCause() {
        return cause;
    }
}

