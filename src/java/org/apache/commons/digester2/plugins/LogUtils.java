/* $Id$
 *
 * Copyright 2003-2004 The Apache Software Foundation.
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

import org.apache.commons.digester2.Context;
import org.apache.commons.logging.Log;

/**
 * Simple utility class to assist in logging.
 * <p>
 * This class is intended only for the use of the code in the
 * plugins packages. No "user" code should use this package.
 * <p>
 * The Digester library has an interesting approach to logging:
 * all logging should be done via the Log object stored on the
 * context instance that the object *doing* the logging is associated
 * with.
 * <p>
 * This is done because apparently some "container"-type applications
 * such as Avalon and Tomcat need to be able to configure different logging
 * for different <i>instances</i> of the Digester/SAXHandler class which have
 * been loaded from the same ClassLoader [info from Craig McClanahan]. 
 * All objects associated with that Digester instance should obey the 
 * reconfiguration of their owning saxhandler instance's logging. The current 
 * solution is to force all objects to output logging info via a single 
 * Log object stored on the saxhandler (and context).instance they are
 * associated with.
 * <p>
 * Of course this causes problems if logging is attempted before an
 * object <i>has</i> a valid reference to a context. The 
 * getLogging method provided here resolves this issue by returning a
 * Log object which silently discards all logging output in this
 * situation.
 * <p>
 * And it also implies that logging filtering can no longer be applied
 * to subcomponents of the Digester, because all logging is done via
 * a single Log object (a single Category). C'est la vie...
 */

class LogUtils {
    
  /**
   * Get the Log object associated with the specified context instance,
   * or a "no-op" logging object if the context reference is null.
   */
  static Log getLogger(Context context) {
    if (context == null) {
        return new org.apache.commons.logging.impl.NoOpLog();
    }
    
    return context.getLogger();
  }
}
