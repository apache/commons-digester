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


package org.apache.commons.digester2;

/**
 * A collection of static utility methods for matching patterns against 
 * xml element paths using simple matching functionality.
 */

public class SimpleMatchUtils {
    
    /**
     * Returns true if the pathToMatch is an absolute path that matches
     * the input path, or if it is a relative path that matches the last
     * part of the specified path.
     */
    public static boolean matches(String path, String pathToMatch) {
        if (pathToMatch.charAt(0) == '/') {
            // absolute
            return path.equals(pathToMatch);
        } else {
            // relative
            // XXX looks wrong but protects a match of
            // "a/b" against a path of "/gotcha/b", but
            // still allows
            // "a/b" to match against "/a/b"
            return path.endsWith("/" + pathToMatch);
        }
    }

    /**
     * Checks if this path matches any of the paths given. This means we
     * iterate through <code>pathsToMatch</code> and match every entry to 
     * this path.
     */
    public static boolean matchsAny(String path, String[] pathsToMatch) {
        for (int i = 0; i < pathsToMatch.length; i++) {
            if (matches(path, pathsToMatch[i]))
                return true;
        }
        return false;
    }
}

