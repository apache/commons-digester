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

package org.apache.commons.digester2.plugins;

import org.apache.commons.digester2.ParseException;

/**
 * Thrown when some plugin-related error has occurred, and none of the
 * other exception types are appropriate.
 */

public class PluginException extends ParseException {
    /**
     * @param cause underlying exception that caused this to be thrown
     */
    public PluginException(Throwable cause) {
        super(cause);
    }

    /**
     * @param msg describes the reason this exception is being thrown.
     */
    public PluginException(String msg) {
        super(msg);
    }

    /**
     * @param msg describes the reason this exception is being thrown.
     * @param cause underlying exception that caused this to be thrown
     */
    public PluginException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
