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
 * The base class from which all digester-specific exception classes inherit.
 * <p>
 * This class supports the concept of a "cause" exception, even on versions
 * of java prior to 1.4 (where this became standard behaviour for exception
 * classes). 
 */

public class DigestionException extends Exception {
    Throwable cause = null;
    
    public DigestionException(String msg) {
        super(msg);
    }

    public DigestionException(String msg, Throwable t) {
        super(msg);
        cause = t;
    }
    
    public DigestionException(Throwable t) {
        cause = t;
    }
    
    public Throwable getCause() {
        return cause;
    }
}

