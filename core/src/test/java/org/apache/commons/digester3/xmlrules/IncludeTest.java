/* $Id$
 *
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

package org.apache.commons.digester3.xmlrules;

import static org.apache.commons.digester3.binder.DigesterLoader.newLoader;
import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.Rule;
import org.apache.commons.digester3.binder.AbstractRulesModule;
import org.junit.Test;

/**
 * Test for the include class functionality
 */
public class IncludeTest
{

    public static class TestDigesterRulesModule
        extends AbstractRulesModule
    {

        @Override
        protected void configure()
        {
            forPattern( "bar" ).addRule( new Rule()
            {

                @Override
                public void body( String namespace, String name, String text )
                    throws Exception
                {
                    List<String> stringList = getDigester().peek();
                    stringList.add( text );
                }

            } );
        }

    }

    @Test
    public void testBasicInclude()
        throws Exception
    {
        final String rulesXml = "<?xml version='1.0'?>"
                + "<!DOCTYPE digester-rules PUBLIC \"-//Apache Commons //DTD digester-rules XML V1.0//EN\" "
                + "\"http://commons.apache.org/digester/dtds/digester-rules-3.0.dtd\">"
                + "<digester-rules>"
                + " <pattern value='root/foo'>"
                + "   <include class='org.apache.commons.digester3.xmlrules.IncludeTest$TestDigesterRulesModule' />"
                + " </pattern>"
                + "</digester-rules>";

        String xml = "<?xml version='1.0' ?><root><foo><bar>short</bar></foo></root>";

        List<String> list = new ArrayList<String>();
        Digester digester = newLoader( new FromXmlRulesModule()
        {

            @Override
            protected void loadRules()
            {
                loadXMLRulesFromText( rulesXml );
            }

        } ).newDigester();
        digester.push( list );
        digester.parse( new StringReader( xml ) );

        assertEquals( "Number of entries", 1, list.size() );
        assertEquals( "Entry value", "short", list.get( 0 ) );
    }

    @Test
    public void testUrlInclude()
        throws Exception
    {
        final String rulesXml = "<?xml version='1.0'?>"
                + "<!DOCTYPE digester-rules PUBLIC \"-//Apache Commons //DTD digester-rules XML V1.0//EN\" "
                + "\"http://commons.apache.org/digester/dtds/digester-rules-3.0.dtd\">"
                + "<digester-rules>"
                + " <pattern value='root/foo1'>"
                + "   <include url='classpath:org/apache/commons/digester3/xmlrules/testrulesinclude.xml' />"
                + " </pattern>"
                + " <pattern value='root/foo2'>"
                + "   <include url='classpath:org/apache/commons/digester3/xmlrules/testrulesinclude.xml' />"
                + " </pattern>"
                + "</digester-rules>";

        String xml = "<?xml version='1.0' ?><root><foo1><bar><foo value='foo1'/></bar></foo1><foo2><bar><foo value='foo2'/></bar></foo2></root>";

        List<String> list = new ArrayList<String>();
        Digester digester = newLoader( new FromXmlRulesModule()
        {

            @Override
            protected void loadRules()
            {
                loadXMLRulesFromText( rulesXml );
            }

        }).newDigester();
        digester.push( list );
        digester.parse( new StringReader( xml ) );
        assertEquals( "[foo1, foo2]", list.toString() );
    }

    /**
     * Validates that circular includes are detected and result in an exception
     */
    @Test( expected = org.apache.commons.digester3.binder.DigesterLoadingException.class )
    public void testCircularInclude()
        throws Exception
    {
        final URL url = ClassLoader.getSystemResource( "org/apache/commons/digester3/xmlrules/testCircularRules.xml" );
        newLoader( new FromXmlRulesModule()
        {

            @Override
            protected void loadRules()
            {
                loadXMLRules( url );
            }

        }).newDigester();
    }

}
