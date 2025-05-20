/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.digester3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Test Case for the Digester class. These tests perform parsing of XML documents to exercise the built-in rules.
 * </p>
 */
public class RuleTestCase
{

    /**
     * The digester instance we will be processing.
     */
    protected Digester digester;

    /**
     * Gets an appropriate InputStream for the specified test file (which must be inside our current package.
     *
     * @param name Name of the test file we want
     * @throws IOException if an input/output error occurs
     */
    protected InputStream getInputStream( final String name )
    {

        return this.getClass().getResourceAsStream( "/org/apache/commons/digester3/" + name );

    }

    /**
     * Sets up instance variables required by this test case.
     */
    @BeforeEach
    public void setUp()
    {

        digester = new Digester();

    }

    /**
     * Tear down instance variables required by this test case.
     */
    @AfterEach
    public void tearDown()
    {

        digester = null;

    }

    /**
     * Test rule addition - this boils down to making sure that digester is set properly on rule addition.
     */
    @Test
    public void testAddRule()
    {
        final Digester digester = new Digester();
        final TestRule rule = new TestRule( "Test" );
        digester.addRule( "/root", rule );

        assertEquals( digester, rule.getDigester(), "Digester is not properly on rule addition." );

    }

    /**
     * Test object creation (and associated property setting) with nothing on the stack, which should cause an
     * appropriate Employee object to be returned.
     */
    @Test
    public void testObjectCreate1() throws Exception
    {

        // Configure the digester as required
        digester.addObjectCreate( "employee", "org.apache.commons.digester3.Employee" );
        digester.addSetProperties( "employee" );

        // Parse our test input.
        final Employee employee = digester.parse( getInputStream( "Test1.xml" ) );

        assertNotNull( employee, "Digester returned an object" );
        assertEquals( "First Name", employee.getFirstName(), "First name is correct" );
        assertEquals( "Last Name", employee.getLastName(), "Last name is correct" );

    }

    /**
     * Test object creation (and associated property setting) with nothing on the stack, which should cause an
     * appropriate Employee object to be returned. The processing rules will process the nested Address elements as
     * well, but will not attempt to add them to the Employee.
     */
    @Test
    public void testObjectCreate2() throws Exception
    {

        // Configure the digester as required
        digester.addObjectCreate( "employee", Employee.class );
        digester.addSetProperties( "employee" );
        digester.addObjectCreate( "employee/address", "org.apache.commons.digester3.Address" );
        digester.addSetProperties( "employee/address" );

        // Parse our test input.
        final Employee employee = digester.parse( getInputStream( "Test1.xml" ) );

        assertNotNull( employee, "Digester returned an object" );
        assertEquals( "First Name", employee.getFirstName(), "First name is correct" );
        assertEquals( "Last Name", employee.getLastName(), "Last name is correct" );

    }

    /**
     * Test object creation (and associated property setting) with nothing on the stack, which should cause an
     * appropriate Employee object to be returned. The processing rules will process the nested Address elements as
     * well, and will add them to the owning Employee.
     */
    @Test
    public void testObjectCreate3() throws Exception
    {

        // Configure the digester as required
        digester.addObjectCreate( "employee", Employee.class );
        digester.addSetProperties( "employee" );
        digester.addObjectCreate( "employee/address", "org.apache.commons.digester3.Address" );
        digester.addSetProperties( "employee/address" );
        digester.addSetNext( "employee/address", "addAddress" );

        // Parse our test input once
        Object root = digester.parse( getInputStream( "Test1.xml" ) );

        validateObjectCreate3( root );

        // Parse the same input again
        root = digester.parse( getInputStream( "Test1.xml" ) );

        validateObjectCreate3( root );

    }

    /**
     * Same as testObjectCreate1(), except use individual call method rules to set the properties of the Employee.
     */
    @Test
    public void testObjectCreate4() throws Exception
    {

        // Configure the digester as required
        digester.addObjectCreate( "employee", Employee.class );
        digester.addCallMethod( "employee", "setFirstName", 1 );
        digester.addCallParam( "employee", 0, "firstName" );
        digester.addCallMethod( "employee", "setLastName", 1 );
        digester.addCallParam( "employee", 0, "lastName" );

        // Parse our test input.
        final Employee employee = digester.parse( getInputStream( "Test1.xml" ) );

        assertNotNull( employee, "Digester returned an object" );
        assertEquals( "First Name", employee.getFirstName(), "First name is correct" );
        assertEquals( "Last Name", employee.getLastName(), "Last name is correct" );

    }

    /**
     * Same as testObjectCreate1(), except use individual call method rules to set the properties of the Employee. Bean
     * data are defined using elements instead of attributes. The purpose is to test CallMethod with a paramCount=0 (ie
     * the body of the element is the argument of the method).
     */
    @Test
    public void testObjectCreate5() throws Exception
    {

        // Configure the digester as required
        digester.addObjectCreate( "employee", Employee.class );
        digester.addCallMethod( "employee/firstName", "setFirstName", 0 );
        digester.addCallMethod( "employee/lastName", "setLastName", 0 );

        // Parse our test input.
        final Employee employee = digester.parse( getInputStream( "Test5.xml" ) );

        assertNotNull( employee, "Digester returned an object" );
        assertEquals( "First Name", employee.getFirstName(), "First name is correct" );
        assertEquals( "Last Name", employee.getLastName(), "Last name is correct" );

    }

    /**
     * It should be possible to parse the same input twice, and get trees of objects that are isomorphic but not be
     * identical object instances.
     */
    @Test
    public void testRepeatedParse() throws Exception
    {

        // Configure the digester as required
        digester.addObjectCreate( "employee", Employee.class );
        digester.addSetProperties( "employee" );
        digester.addObjectCreate( "employee/address", "org.apache.commons.digester3.Address" );
        digester.addSetProperties( "employee/address" );
        digester.addSetNext( "employee/address", "addAddress" );

        // Parse our test input the first time
        Object root1;
        root1 = digester.parse( getInputStream( "Test1.xml" ) );

        validateObjectCreate3( root1 );

        // Parse our test input the second time
        Object root2;
        root2 = digester.parse( getInputStream( "Test1.xml" ) );

        validateObjectCreate3( root2 );

        // Make sure that it was a different root
        assertNotSame( root1, root2, "Different tree instances were returned" );

    }

    /**
     * Test object creation (and associated property setting) with nothing on the stack, which should cause an
     * appropriate Employee object to be returned. The processing rules will process the nested Address elements as
     * well, but will not attempt to add them to the Employee.
     */
    @Test
    public void testRuleSet1() throws Exception
    {

        // Configure the digester as required
        final RuleSet rs = new TestRuleSet();
        digester.addRuleSet( rs );

        // Parse our test input.
        final Employee employee = digester.parse( getInputStream( "Test1.xml" ) );

        assertNotNull( employee, "Digester returned an object" );
        assertEquals( "First Name", employee.getFirstName(), "First name is correct" );
        assertEquals( "Last Name", employee.getLastName(), "Last name is correct" );
        assertNotNull( employee.getAddress( "home" ), "Can retrieve home address" );
        assertNotNull( employee.getAddress( "office" ), "Can retrieve office address" );

    }

    /**
     * Same as {@code testRuleSet1} except using a single namespace.
     */
    @Test
    public void testRuleSet2() throws Exception
    {

        // Configure the digester as required
        digester.setNamespaceAware( true );
        final RuleSet rs = new TestRuleSet( null, "http://commons.apache.org/digester/Foo" );
        digester.addRuleSet( rs );

        // Parse our test input.
        final Employee employee = digester.parse( getInputStream( "Test2.xml" ) );

        assertNotNull( employee, "Digester returned an object" );
        assertEquals( "First Name", employee.getFirstName(), "First name is correct" );
        assertEquals( "Last Name", employee.getLastName(), "Last name is correct" );
        assertNotNull( employee.getAddress( "home" ), "Can retrieve home address" );
        assertNotNull( employee.getAddress( "office" ), "Can retrieve office address" );

    }

    /**
     * Same as {@code testRuleSet2} except using a namespace for employee that we should recognize, and a namespace
     * for address that we should skip.
     */
    @Test
    public void testRuleSet3() throws Exception
    {

        // Configure the digester as required
        digester.setNamespaceAware( true );
        final RuleSet rs = new TestRuleSet( null, "http://commons.apache.org/digester/Foo" );
        digester.addRuleSet( rs );

        // Parse our test input.
        final Employee employee = digester.parse( getInputStream( "Test3.xml" ) );

        assertNotNull( employee, "Digester returned an object" );
        assertEquals( "First Name", employee.getFirstName(), "First name is correct" );
        assertEquals( "Last Name", employee.getLastName(), "Last name is correct" );
        assertNull( employee.getAddress( "home" ), "Can not retrieve home address" );
        assertNull( employee.getAddress( "office" ), "Can not retrieve office address" );

    }

    /**
     */
    @Test
    public void testSetCustomProperties() throws Exception
    {

        final Digester digester = new Digester();

        digester.setValidating( false );

        digester.addObjectCreate( "toplevel", ArrayList.class );
        digester.addObjectCreate( "toplevel/one", Address.class );
        digester.addSetNext( "toplevel/one", "add" );
        digester.addObjectCreate( "toplevel/two", Address.class );
        digester.addSetNext( "toplevel/two", "add" );
        digester.addObjectCreate( "toplevel/three", Address.class );
        digester.addSetNext( "toplevel/three", "add" );
        digester.addObjectCreate( "toplevel/four", Address.class );
        digester.addSetNext( "toplevel/four", "add" );
        digester.addSetProperties( "toplevel/one" );
        digester.addSetProperties( "toplevel/two", new String[] { "alt-street", "alt-city", "alt-state" },
                                   new String[] { "street", "city", "state" } );
        digester.addSetProperties( "toplevel/three", new String[] { "aCity", "state" }, new String[] { "city" } );
        digester.addSetProperties( "toplevel/four", "alt-city", "city" );

        final ArrayList<?> root = digester.parse( getInputStream( "Test7.xml" ) );

        assertEquals( 4, root.size(), "Wrong array size" );

        // note that the array is in popped order (rather than pushed)

        Object obj = root.get( 0 );
        assertInstanceOf( Address.class, obj, "(1) Should be an Address " );
        final Address addressOne = (Address) obj;
        assertEquals( "New Street", addressOne.getStreet(), "(1) Street attribute" );
        assertEquals( "Las Vegas", addressOne.getCity(), "(1) City attribute" );
        assertEquals( "Nevada", addressOne.getState(), "(1) State attribute" );

        obj = root.get( 1 );
        assertInstanceOf( Address.class, obj, "(2) Should be an Address " );
        final Address addressTwo = (Address) obj;
        assertEquals( "Old Street", addressTwo.getStreet(), "(2) Street attribute" );
        assertEquals( "Portland", addressTwo.getCity(), "(2) City attribute" );
        assertEquals( "Oregon", addressTwo.getState(), "(2) State attribute" );

        obj = root.get( 2 );
        assertInstanceOf( Address.class, obj, "(3) Should be an Address " );
        final Address addressThree = (Address) obj;
        assertEquals( "4th Street", addressThree.getStreet(), "(3) Street attribute" );
        assertEquals( "Dayton", addressThree.getCity(), "(3) City attribute" );
        assertEquals( "US", addressThree.getState(), "(3) State attribute" );

        obj = root.get( 3 );
        assertInstanceOf( Address.class, obj, "(4) Should be an Address " );
        final Address addressFour = (Address) obj;
        assertEquals( "6th Street", addressFour.getStreet(), "(4) Street attribute" );
        assertEquals( "Cleveland", addressFour.getCity(), "(4) City attribute" );
        assertEquals( "Ohio", addressFour.getState(), "(4) State attribute" );

    }

    @Test
    public void testSetNext() throws Exception
    {
        final Digester digester = new Digester();
        digester.setRules( new ExtendedBaseRules() );
        digester.setValidating( false );

        digester.addObjectCreate( "!*/b", BetaBean.class );
        digester.addObjectCreate( "!*/a", AlphaBean.class );
        digester.addObjectCreate( "root", ArrayList.class );
        digester.addSetProperties( "!*" );
        digester.addSetNext( "!*/b/?", "setChild" );
        digester.addSetNext( "!*/a/?", "setChild" );
        digester.addSetNext( "!root/?", "add" );
        final ArrayList<?> root = digester.parse( getInputStream( "Test4.xml" ) );

        assertEquals( 2, root.size(), "Wrong array size" );
        final AlphaBean one = (AlphaBean) root.get( 0 );
        assertInstanceOf( BetaBean.class, one.getChild() );
        final BetaBean two = (BetaBean) one.getChild();
        assertEquals( two.getName(), "TWO", "Wrong name (1)" );
        assertInstanceOf( AlphaBean.class, two.getChild() );
        final AlphaBean three = (AlphaBean) two.getChild();
        assertEquals( three.getName(), "THREE", "Wrong name (2)" );
        final BetaBean four = (BetaBean) root.get( 1 );
        assertEquals( four.getName(), "FOUR", "Wrong name (3)" );
        assertInstanceOf( BetaBean.class, four.getChild() );
        final BetaBean five = (BetaBean) four.getChild();
        assertEquals( five.getName(), "FIVE", "Wrong name (4)" );

    }

    @Test
    public void testSetTop() throws Exception
    {
        final Digester digester = new Digester();
        digester.setRules( new ExtendedBaseRules() );
        digester.setValidating( false );

        digester.addObjectCreate( "!*/b", BetaBean.class );
        digester.addObjectCreate( "!*/a", AlphaBean.class );
        digester.addObjectCreate( "root", ArrayList.class );
        digester.addSetProperties( "!*" );
        digester.addSetTop( "!*/b/?", "setParent" );
        digester.addSetTop( "!*/a/?", "setParent" );
        digester.addSetRoot( "!*/a", "add" );
        digester.addSetRoot( "!*/b", "add" );
        final ArrayList<?> root = digester.parse( getInputStream( "Test4.xml" ) );

        assertEquals( 5, root.size(), "Wrong array size" );

        // note that the array is in popped order (rather than pushed)

        Object obj = root.get( 1 );
        assertInstanceOf( BetaBean.class, obj, "TWO should be a BetaBean" );
        final BetaBean two = (BetaBean) obj;
        assertNotNull( two.getParent(), "Two's parent should not be null" );
        assertEquals( "TWO", two.getName(), "Wrong name (1)" );
        assertEquals( "ONE", two.getParent().getName(), "Wrong name (2)" );

        obj = root.get( 0 );
        assertInstanceOf( AlphaBean.class, obj, "THREE should be an AlphaBean" );
        final AlphaBean three = (AlphaBean) obj;
        assertNotNull( three.getParent(), "Three's parent should not be null" );
        assertEquals( "THREE", three.getName(), "Wrong name (3)" );
        assertEquals( "TWO", three.getParent().getName(), "Wrong name (4)" );

        obj = root.get( 3 );
        assertInstanceOf( BetaBean.class, obj, "FIVE should be a BetaBean" );
        final BetaBean five = (BetaBean) obj;
        assertNotNull( five.getParent(), "Five's parent should not be null" );
        assertEquals( "FIVE", five.getName(), "Wrong name (5)" );
        assertEquals( "FOUR", five.getParent().getName(), "Wrong name (6)" );

    }

    /**
     * Test the two argument version of the SetTopRule rule. This test is based on testObjectCreate3 and should result
     * in the same tree of objects. Instead of using the SetNextRule rule which results in a method invocation on the
     * (top-1) (parent) object with the top object (child) as an argument, this test uses the SetTopRule rule which
     * results in a method invocation on the top object (child) with the top-1 (parent) object as an argument. The three
     * argument form is tested in {@code testSetTopRule2}.
     */
    @Test
    public void testSetTopRule1() throws Exception
    {

        // Configure the digester as required
        digester.addObjectCreate( "employee", "org.apache.commons.digester3.Employee" );
        digester.addSetProperties( "employee" );
        digester.addObjectCreate( "employee/address", "org.apache.commons.digester3.Address" );
        digester.addSetProperties( "employee/address" );
        digester.addSetTop( "employee/address", "setEmployee" );

        // Parse our test input.
        Object root;
        root = digester.parse( getInputStream( "Test1.xml" ) );
        validateObjectCreate3( root );

    }

    /**
     * Same as {@code testSetTopRule1} except using the three argument form of the SetTopRule rule.
     */
    @Test
    public void testSetTopRule2() throws Exception
    {

        // Configure the digester as required
        digester.addObjectCreate( "employee", "org.apache.commons.digester3.Employee" );
        digester.addSetProperties( "employee" );
        digester.addObjectCreate( "employee/address", "org.apache.commons.digester3.Address" );
        digester.addSetProperties( "employee/address" );
        digester.addSetTop( "employee/address", "setEmployee", "org.apache.commons.digester3.Employee" );

        // Parse our test input.
        Object root;
        root = digester.parse( getInputStream( "Test1.xml" ) );

        validateObjectCreate3( root );

    }

    /**
     * Validate the assertions for ObjectCreateRule3.
     *
     * @param root Root object returned by {@code digester.parse()}
     */
    protected void validateObjectCreate3( final Object root )
    {

        // Validate the retrieved Employee
        assertNotNull( root, "Digester returned an object" );
        assertInstanceOf( Employee.class, root, "Digester returned an Employee" );
        final Employee employee = (Employee) root;
        assertEquals( "First Name", employee.getFirstName(), "First name is correct" );
        assertEquals( "Last Name", employee.getLastName(), "Last name is correct" );

        // Validate the corresponding "home" Address
        final Address home = employee.getAddress( "home" );
        assertNotNull( home, "Retrieved home address" );
        assertEquals( "Home Street", home.getStreet(), "Home street" );
        assertEquals( "Home City", home.getCity(), "Home city" );
        assertEquals( "HS", home.getState(), "Home state" );
        assertEquals( "HmZip", home.getZipCode(), "Home zip" );

        // Validate the corresponding "office" Address
        final Address office = employee.getAddress( "office" );
        assertNotNull( office, "Retrieved office address" );
        assertEquals( "Office Street", office.getStreet(), "Office street" );
        assertEquals( "Office City", office.getCity(), "Office city" );
        assertEquals( "OS", office.getState(), "Office state" );
        assertEquals( "OfZip", office.getZipCode(), "Office zip" );

    }

}
