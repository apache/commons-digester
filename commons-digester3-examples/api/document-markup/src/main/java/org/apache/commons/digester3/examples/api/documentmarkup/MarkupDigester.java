package org.apache.commons.digester3.examples.api.documentmarkup;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.Rule;

import java.util.List;
import javax.xml.parsers.SAXParser;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

/**
 * This is a subclass of digester which supports rules which implement
 * the TextSegmentHandler interface, causing the "textSegment" method
 * on each matching rule (of the appropriate type) to be invoked when
 * an element contains a segment of text followed by a child element.
 * <p>
 * See the readme file included with this example for more information.
 */
public class MarkupDigester
    extends Digester
{

    /**
     * The text found in the current element since the last child element.
     */
    protected StringBuilder currTextSegment = new StringBuilder();

    /** See equivalent constructor in Digester class. */
    public MarkupDigester()
    {
    }

    /** See equivalent constructor in Digester class. */
    public MarkupDigester( final SAXParser parser )
    {
        super( parser );
    }

    //===================================================================

    /** See equivalent constructor in Digester class. */
    public MarkupDigester( final XMLReader reader )
    {
        super( reader );
    }

    /**
     * Process notification of character data received from the body of
     * an XML element.
     *
     * @param buffer The characters from the XML document
     * @param start Starting offset into the buffer
     * @param length Number of characters from the buffer
     * @throws SAXException if a parsing error is to be reported
     */
    @Override
    public void characters( final char[] buffer, final int start, final int length )
        throws SAXException
    {
        super.characters( buffer, start, length );
        currTextSegment.append( buffer, start, length );
    }

    /**
     * Process notification of the end of an XML element being reached.
     *
     * @param namespaceURI   The Namespace URI, or the empty string if the
     *   element has no Namespace URI or if Namespace processing is not
     *   being performed.
     * @param localName   The local name (without prefix), or the empty
     *   string if Namespace processing is not being performed.
     * @param qName   The qualified XML 1.0 name (with prefix), or the
     *   empty string if qualified names are not available.
     * @throws SAXException if a parsing error is to be reported
     */
    @Override
    public void endElement( final String namespaceURI, final String localName, final String qName )
        throws SAXException
    {
        handleTextSegments();
        currTextSegment.setLength( 0 );
        super.endElement( namespaceURI, localName, qName );
    }

    /**
     * Iterate over the list of rules most recently matched, and
     * if any of them implement the TextSegmentHandler interface then
     * invoke that rule's textSegment method passing the current
     * segment of text from the XML element body.
     */
    private void handleTextSegments()
        throws SAXException
    {
        if ( currTextSegment.length() > 0 )
        {
            final String segment = currTextSegment.toString();
            final List<Rule> parentMatches = getMatches().peek();
            for ( Rule r : parentMatches )
            {
                if ( r instanceof TextSegmentHandler )
                {
                    final TextSegmentHandler h = (TextSegmentHandler) r;
                    try
                    {
                        h.textSegment( segment );
                    }
                    catch ( final Exception e )
                    {
                        throw createSAXException( e );
                    }
                }
            }
        }
    }

    /**
     * Process notification of the start of an XML element being reached.
     *
     * @param namespaceURI The Namespace URI, or the empty string if the element
     *   has no Namespace URI or if Namespace processing is not being performed.
     * @param localName The local name (without prefix), or the empty
     *   string if Namespace processing is not being performed.
     * @param qName The qualified name (with prefix), or the empty
     *   string if qualified names are not available.
     * @param list The attributes attached to the element. If there are
     *   no attributes, it shall be an empty Attributes object.
     * @throws SAXException if a parsing error is to be reported
     */
    @Override
    public void startElement( final String namespaceURI, final String localName, final String qName, final Attributes list )
        throws SAXException
    {
        handleTextSegments();

        // Unlike bodyText, which accumulates despite intervening child
        // elements, currTextSegment gets cleared here. This means that
        // we don't need to save it on a stack either.
        currTextSegment.setLength( 0 );

        super.startElement( namespaceURI, localName, qName, list );
    }

}
