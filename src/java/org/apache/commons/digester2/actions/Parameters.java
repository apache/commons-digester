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


package org.apache.commons.digester2.actions;

import org.apache.commons.logging.Log;

/**
 * Represents a set of parameters to a method. Instances of this class
 * are created when a CallMethodAction fires, and populated when
 * CallParam....Action instances fire. The values are then extracted in
 * order to be passed to the target method.
 */

public class Parameters {
    private Object[] values;
    
    public Parameters(int size) {
        values = new Object[size];
    }
    
    public void put(int index, Object obj) {
        values[index] = obj;
    }
    
    public Object[] getValues() {
        return values;
    }
}
