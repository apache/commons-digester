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
 * Defines the interface required of classes which implement actions to be 
 * taken when a corresponding nested pattern of XML elements has been matched.
 * <p>
 * Classes implementing the Action interface <i>must</i> not alter any instance
 * variables during parsing (ie from the init/begin/body/end/finish methods).
 * All necessary state-related data <i>must</i> be stored instead on the Context
 * object passed as a parameter to each parsing-related method.
 * <p>
 * An Action instance may be added to the same RuleManager more than once, with 
 * different associated patterns, or the pattern may include wildcards; either 
 * of these situations may cause the rule to be invoked in a "re-entrant" 
 * manner, where the pattern of calls is like:
 * <pre>
 *    begin[1]/begin[2]/body[2]/end[2]/body[1]/end[1]
 * </pre>
 * This obviously will cause errors if the Action object stores state on itself. 
 * <p>
 * In addition, an Action object may be added to different Digester instances,
 * which potentially are parsing different documents concurrently in
 * different threads. Again, storing state on the Action instance itself will
 * cause problems in these situations.
 * <p>
 * <strong>IMPORTANT NOTE</strong>: Anyone implementing a custom Action is
 * strongly encouraged to subclass AbstractAction rather than implement this
 * interface directly. Digester minor releases (2.x -> 2.y) guarantee that
 * subclasses of AbstractAction will not be broken. However the Action
 * interface <i>may</i> change in minor releases, which will break any class
 * which implements this interface directly.
 */

public interface Action {

    /**
     * This method is called once at the start of parsing of an input
     * document. The context object is the one that will be used during
     * the parsing of this document.
     */
    public void startParse(Context context) throws ParseException;
    
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
    public void begin(
    Context context, String namespace, 
    String name, Attributes attributes)
    throws ParseException;

    /**
     * This method is called when the body of a matching XML element is 
     * encountered.  If the element has no body, this method is not called at 
     * all.
     * <p>
     * Note that if the element has multiple pieces of body text separated by
     * child elements (ie is "mixed content") then this method is called once
     * for each separate block of text, at the point that the child element
     * is encountered. In each call, only the text since the last call to this
     * method is passed.
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
    throws ParseException;

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
    throws ParseException;

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
    throws ParseException;

    /**
     * This method is called after all parsing methods have been
     * called, to allow Actions to remove temporary data.
     *
     * @param context is the current processing context object.
     */
    public void finishParse(Context context) throws ParseException;
}
