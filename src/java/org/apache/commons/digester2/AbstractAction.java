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

import org.xml.sax.Attributes;

/**
 * Provides a base implementation for custom actions (ie things that
 * should be executed when certain input xml is seen).
 * <p>
 * Note that extending this abstract class rather than directly implementing
 * the Action interface provides much better "forward compatibility". Digester
 * minor releases (2.x -> 2.y) guarantee not to break any classes that subclass
 * this abstract class. However no such guarantee exists for classes that
 * directly implement the Action interface.
 * <p>
 * You <strong>must</strong> read the comments on the Action interface before
 * implementing any subclass of this class, as there are constraints on the
 * permitted behaviour of Action classes. 
 */

public abstract class AbstractAction implements Action {

    /**
     * This method is called once at the start of parsing of an input
     * document. The context object is the one that will be used during
     * the parsing of this document.
     */
    public void startParse(Context context) 
    throws ParseException {
    }
    
   /**
     * This method is called when the beginning of a matching XML element
     * is encountered.
     *
     * @param context is the current processing context object.
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the element has no namespace
     * @param name the local name of the element
     * @param attributes The attribute list of this element
     */
    public void begin(Context context, String namespace, String name, Attributes attributes)
    throws ParseException {
    }

    /**
     * This method is called when the body of a matching XML element is 
     * encountered.  If the element has no body, this method is not called at 
     * all.
     * <p>
     * Note that if the element has multiple pieces of body text separated by
     * child elements (ie is "mixed content") then this method is called once
     * for each separate block of text, at the point that the child element
     * is encountered. In each call, only the text since the last call to this
     * method (ie since the last nested child element) is passed.
     * <p>
     * In the case of an element with just text content (no child elements),
     * this method is exactly equivalent to the body method; either can be
     * overridden to perform the necessary work.
     *
     * @param context is the current processing context object.
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the element has no namespace
     * @param name the local name of the element.
     * @param text The text of the body of this element
     */
    public void bodySegment(Context context, String namespace, String name, String text)
    throws ParseException {
    }

    /**
     * This method is called when the body of a matching XML element is 
     * encountered.  If the element has no body, this method is not called at 
     * all.
     * <p>
     * Note that if the element has multiple pieces of body text separated by
     * child elements (ie is "mixed content") then all the text is merged
     * into a single string and this method is called only once with the
     * complete text as a parameter.
     *
     * @param context is the current processing context object.
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the element has no namespace
     * @param name the local name of the element.
     * @param text The text of the body of this element
     */
    public void body(Context context, String namespace, String name, String text)
    throws ParseException {
    }

    /**
     * This method is called when the end of a matching XML element
     * is encountered.
     *
     * @param context is the current processing context object.
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the element has no namespace
     * @param name the local name of the element.
     */
    public void end(Context context, String namespace, String name)
    throws ParseException {
    }

    /**
     * This method is called after all parsing methods have been
     * called, to allow Actions to remove temporary data.
     *
     * @param context is the current processing context object.
     */
    public void finishParse(Context context) 
    throws ParseException {
    }
}
