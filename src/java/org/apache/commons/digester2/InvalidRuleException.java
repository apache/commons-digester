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
 * Thrown when a problem is detected as a rule is added to a RuleManager.
 * One possible cause is that the pattern is invalid, but there may be
 * others depending upon the RuleManager and Action involved.
 */

public class InvalidRuleException extends DigestionException {
    public InvalidRuleException(String msg) {
        super(msg);
    }

    public InvalidRuleException(Throwable t) {
        super(t);
    }
    
    public InvalidRuleException(String msg, Throwable t) {
        super(msg, t);
    }
    
}

