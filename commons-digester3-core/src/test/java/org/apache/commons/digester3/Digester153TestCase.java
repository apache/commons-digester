package org.apache.commons.digester3;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.apache.commons.digester3.binder.DigesterLoader.newLoader;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.digester3.annotations.FromAnnotationsRuleModule;
import org.apache.commons.digester3.binder.AbstractRulesModule;
import org.apache.commons.digester3.binder.RulesModule;
import org.apache.commons.digester3.xmlrules.FromXmlRulesModule;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

/**
 * DIGESTER-153
 */
public final class Digester153TestCase
{

    @Test
    public void basicConstructorWithWrongParameters()
    {
        final ObjectCreateRule createRule = new ObjectCreateRule( TestBean.class );
        createRule.setConstructorArgumentTypes( boolean.class );

        final Digester digester = new Digester();
        digester.addRule( "toplevel/bean", createRule );

        assertThrows( SAXParseException.class, () -> digester.parse( getClass().getResourceAsStream( "BasicConstructor.xml" ) ) );
    }

    private void successfulConstructor( final RulesModule rulesModule )
        throws Exception
    {
        final TestBean bean = newLoader( rulesModule )
                            .newDigester()
                            .parse( getClass().getResourceAsStream( "BasicConstructor.xml" ) );

        assertTrue( bean.getBooleanProperty() );
        assertEquals( 9.99D, bean.getDoubleProperty(), 0 );
    }

    @Test
    public void testBasicConstructor()
        throws Exception
    {
        final ObjectCreateRule createRule = new ObjectCreateRule( TestBean.class );
        createRule.setConstructorArgumentTypes( boolean.class, double.class );

        final Digester digester = new Digester();
        digester.addRule( "toplevel/bean", createRule );
        digester.addCallParam( "toplevel/bean", 0, "boolean" );
        digester.addCallParam( "toplevel/bean", 1, "double" );

        TestBean bean = digester.parse( getClass().getResourceAsStream( "BasicConstructor.xml" ) );

        assertTrue( bean.getBooleanProperty() );
        assertEquals( 9.99D, bean.getDoubleProperty(), 0 );

        // do it again to exercise the cglib Factory:
        bean = digester.parse( getClass().getResourceAsStream( "BasicConstructor.xml" ) );

        assertTrue( bean.getBooleanProperty() );
        assertEquals( 9.99D, bean.getDoubleProperty(), 0 );
    }

    @Test
    public void testBasicConstructorViaAnnotations()
        throws Exception
    {
        successfulConstructor( new FromAnnotationsRuleModule()
        {

            @Override
            protected void configureRules()
            {
                bindRulesFrom( TestBean.class );
            }

        } );
    }

    @Test
    public void testBasicConstructorViaBinder()
        throws Exception
    {
        successfulConstructor( new AbstractRulesModule()
        {

            @Override
            protected void configure()
            {
                forPattern( "toplevel/bean" )
                    .createObject().ofType( TestBean.class ).usingConstructor( boolean.class, double.class )
                    .then()
                    .callParam().fromAttribute( "boolean" ).ofIndex( 0 )
                    .then()
                    .callParam().fromAttribute( "double" ).ofIndex( 1 );
            }

        } );
    }

    @Test
    public void testBasicConstructorViaXML()
        throws Exception
    {
        successfulConstructor( new FromXmlRulesModule()
        {

            @Override
            protected void loadRules()
            {
                loadXMLRules( getClass().getResourceAsStream( "xmlrules/constructor-testrules.xml" ) );
            }

        } );
    }

    @Test
    public void testBasicConstructorWithValuesNotFound()
        throws Exception
    {
        final ObjectCreateRule createRule = new ObjectCreateRule( TestBean.class );
        createRule.setConstructorArgumentTypes( boolean.class, double.class );

        final Digester digester = new Digester();
        digester.addRule( "toplevel/bean", createRule );
        digester.addCallParam( "toplevel/bean", 0, "notFound1" );
        digester.addCallParam( "toplevel/bean", 1, "notFound2" );

        final TestBean bean = digester.parse( getClass().getResourceAsStream( "BasicConstructor.xml" ) );

        assertFalse( bean.getBooleanProperty() );
        assertEquals( 0D, bean.getDoubleProperty(), 0 );
    }

    @Test
    public void testConstructorWithAttributeAndElement()
        throws Exception
    {
        final ObjectCreateRule createRule = new ObjectCreateRule( TestBean.class );
        createRule.setConstructorArgumentTypes( boolean.class, double.class );

        final Digester digester = new Digester();
        digester.addRule( "toplevel/bean", createRule );
        digester.addCallParam( "toplevel/bean", 0, "boolean" );
        digester.addCallParam( "toplevel/bean/double", 1 );
        digester.addBeanPropertySetter("toplevel/bean/float", "floatProperty");

        TestBean bean = digester.parse( getClass().getResourceAsStream( "ConstructorWithAttributeAndElement.xml" ) );

        assertTrue( bean.getBooleanProperty() );
        assertEquals( 9.99D, bean.getDoubleProperty(), 0 );
        assertEquals( Float.valueOf( 5.5f ), Float.valueOf( bean.getFloatProperty() ) );

        // do it again to exercise the cglib Factory:
        bean = digester.parse( getClass().getResourceAsStream( "ConstructorWithAttributeAndElement.xml" ) );

        assertTrue( bean.getBooleanProperty() );
        assertEquals( 9.99D, bean.getDoubleProperty(), 0 );
        assertEquals( Float.valueOf( 5.5f ), Float.valueOf( bean.getFloatProperty() ) );
    }

    @Test
    public void testConstructorWithClassDefinedInAttribute()
        throws Exception
    {
        final ObjectCreateRule createRule = new ObjectCreateRule( null, "type" );
        createRule.setConstructorArgumentTypes( boolean.class, double.class );

        final Digester digester = new Digester();
        digester.addRule( "toplevel/bean", createRule );
        digester.addCallParam( "toplevel/bean", 0, "boolean" );
        digester.addCallParam( "toplevel/bean", 1, "double" );

        final TestBean bean = digester.parse( getClass().getResourceAsStream( "AttributeDefinedConstructor.xml" ) );

        assertTrue( bean.getBooleanProperty() );
        assertEquals( 9.99D, bean.getDoubleProperty(), 0 );
    }

}
