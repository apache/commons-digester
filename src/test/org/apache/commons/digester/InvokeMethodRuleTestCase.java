/*
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


package org.apache.commons.digester;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xml.sax.SAXException;

//import org.apache.commons.logging.impl.SimpleLog;

/**
 * <p>Tests for the <code>InvokeMethodRule</code> and associated 
 * <code>InvokeParamFrom...Rule</code> rules.
 */
public class InvokeMethodRuleTestCase extends TestCase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The digester instance we will be processing.
     */
    protected Digester digester = null;


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public InvokeMethodRuleTestCase(String name) {

        super(name);

    }


    // --------------------------------------------------- Overall Test Methods


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {

        digester = new Digester();

    }


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {

        return (new TestSuite(InvokeMethodRuleTestCase.class));

    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {

        digester = null;

    }



    // ------------------------------------------------ Individual Test Methods


    /**
     * Test method calls with the InvokeMethodRule rule. It should be possible
     * to call a method with no arguments using several rule syntaxes.
     */
    public void testBasic() throws SAXException, IOException {
        
        // Configure the digester as required
        digester.addObjectCreate("employee", Employee.class);
        // try all syntax permutations
        digester.addInvokeMethod("employee", "toString", 0, (Class[])null);
        digester.addInvokeMethod("employee", "toString", 0, (String[])null);
        digester.addInvokeMethod("employee", "toString", 0, new Class[] {});
        digester.addInvokeMethod("employee", "toString", 0, new String[] {});
        digester.addInvokeMethod("employee", "toString", 0);

        // Parse our test input
        Object root1 = null;
        // an exception will be thrown if the method can't be found
        root1 = digester.parse(getInputStream("Test5.xml"));

    }


    /**
     * Test method calls with the InvokeMethodRule reading from the element
     * body, with no InvokeParamMethod rules added.
     */
    public void testInvokeMethodOnly() throws Exception {

        // Configure the digester as required
        digester.addObjectCreate("employee", Employee.class);
        digester.addInvokeMethod("employee/firstName", "setFirstName", -1);
        digester.addInvokeMethod("employee/lastName", "setLastName", -1);

        // Parse our test input
        Employee employee = (Employee)
            digester.parse(getInputStream("Test9.xml"));
        assertNotNull("parsed an employee", employee);

        // Validate that the property setters were called
        assertEquals("Set first name", "First Name", employee.getFirstName());
        assertEquals("Set last name", "Last Name", employee.getLastName());
    }


    /**
     * Test InvokeMethodRule variants which specify the classes of the
     * parameters to target methods. String, int, boolean, float should all 
     * be acceptable as parameter types.
     */
    public void testSettingProperties() throws SAXException, IOException {
            
        // Configure the digester as required
        digester.addObjectCreate("employee", Employee.class);
        // try all syntax permutations
        digester.addInvokeMethod("employee", "setLastName", 1, 
                                new String[] {"java.lang.String"});
        digester.addInvokeParamFromBody("employee/lastName", 0);
                
        // Parse our test input
        Object root1 = null;
        
        // an exception will be thrown if the method can't be found
        root1 = digester.parse(getInputStream("Test5.xml"));
        Employee employee = (Employee) root1;
        assertEquals("Failed to call Employee.setLastName", 
                    "Last Name", employee.getLastName()); 
        

        digester = new Digester();
        // Configure the digester as required
        digester.addObjectCreate("employee", Employee.class);
        // try out primitive convertion
        digester.addInvokeMethod("employee", "setAge", 1, 
                                new Class[] {int.class});
        digester.addInvokeParamFromBody("employee/age", 0);         
                
        // Parse our test input
        root1 = null;
        
        // an exception will be thrown if the method can't be found
        root1 = digester.parse(getInputStream("Test5.xml"));
        employee = (Employee) root1;
        assertEquals("Failed to call Employee.setAge", 21, employee.getAge()); 
        
        digester = new Digester();
        // Configure the digester as required
        digester.addObjectCreate("employee", Employee.class);      
        digester.addInvokeMethod("employee", "setActive", 1, 
                                new Class[] {boolean.class});
        digester.addInvokeParamFromBody("employee/active", 0);    
                
        // Parse our test input
        root1 = null;

        // an exception will be thrown if the method can't be found
        root1 = digester.parse(getInputStream("Test5.xml"));
        employee = (Employee) root1;
        assertEquals("Failed to call Employee.setActive", 
                        true, employee.isActive()); 
        
        digester = new Digester();            
        // Configure the digester as required
        digester.addObjectCreate("employee", Employee.class); 
        digester.addInvokeMethod("employee", "setSalary", 1, 
                                new Class[] {float.class});
        digester.addInvokeParamFromBody("employee/salary", 0);    
                
        // Parse our test input
        root1 = null;
        // an exception will be thrown if the method can't be found
        root1 = digester.parse(getInputStream("Test5.xml"));
        employee = (Employee) root1;
        assertEquals("Failed to call Employee.setSalary", 
                        1000000.0f, employee.getSalary(), 0.1f); 
    }


    /**
     * This tests the call methods params enhancement that provides 
     * for more complex stack-based calls.
     */
    public void testParamsFromStack() throws SAXException, IOException {

        StringBuffer xml = new StringBuffer().
            append("<?xml version='1.0'?>").
            append("<map>").
            append("  <key name='The key'/>").
            append("  <value name='The value'/>").
            append("</map>");

        digester.addObjectCreate("map", HashMap.class);
        digester.addInvokeMethod("map", "put", 2);
        digester.addObjectCreate("map/key", AlphaBean.class);
        digester.addSetProperties("map/key");
        digester.addInvokeParamFromStack("map/key", 0);
        digester.addObjectCreate("map/value", BetaBean.class);
        digester.addSetProperties("map/value");
        digester.addInvokeParamFromStack("map/value", 1);

        Map map = (Map) digester.parse(new StringReader(xml.toString()));

        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("The key",
                     ((AlphaBean)map.keySet().iterator().next()).getName());
        assertEquals("The value",
                     ((BetaBean)map.values().iterator().next()).getName());
    }


    /**
     * Test that the target object for a InvokeMethodRule is the object that was
     * on top of the object stack when the InvokeMethodRule fired, even when other
     * rules fire between the InvokeMethodRule and its associated Param rules.
     */
    public void testOrderNestedPartA() throws Exception {

        // Configure the digester as required

        // Here, we use the "grandchild element name" as a parameter to
        // the created element, to ensure that all the params aren't
        // avaiable to the InvokeMethodRule until some other rules have fired,
        // in particular an ObjectCreateRule. The InvokeMethodRule should still
        // function correctly in this scenario.
        digester.addObjectCreate("toplevel/element", NamedBean.class);
        digester.addInvokeMethod("toplevel/element", "setName", 1);
        digester.addInvokeParamFromAttr("toplevel/element/element/element", 0, "name");
        
        digester.addObjectCreate("toplevel/element/element", NamedBean.class);

        // Parse our test input
        NamedBean root1 = null;
        try {
            // an exception will be thrown if the method can't be found
            root1 = (NamedBean) digester.parse(getInputStream("Test8.xml"));
            
        } catch (Throwable t) {
            // this means that the method can't be found and so the test fails
            fail("Digester threw Exception:  " + t);
        }
        
        // if the InvokeMethodRule were to incorrectly invoke the method call
        // on the second-created NamedBean instance, then the root one would
        // have a null name. If it works correctly, the target element will
        // be the first-created (root) one, despite the fact that a second
        // object instance was created between the firing of the 
        // InvokeMethodRule and its associated InvokeParamRule.
        assertEquals("Wrong method call order", "C", root1.getName());
    }

    /**
     * Test nested InvokeMethod rules.
     * <p>
     * The current implementation of InvokeMethodRule, in which the method is
     * invoked in its end() method, causes behaviour which some users find
     * non-intuitive. In this test it can be seen to "reverse" the order of
     * data processed. However this is the way InvokeMethodRule has always 
     * behaved, and it is expected that apps out there rely on this call order 
     * so this test is present to ensure that no-one changes this behaviour.
     */
    public void testOrderNestedPartB() throws Exception {
        
        // Configure the digester as required
        StringBuffer word = new StringBuffer();
        digester.push(word);
        digester.addInvokeMethod("*/element", "append", 1);
        digester.addInvokeParamFromAttr("*/element", 0, "name");
        
        // Parse our test input
        Object root1 = null;
        try {
            // an exception will be thrown if the method can't be found
            root1 = digester.parse(getInputStream("Test8.xml"));
            
        } catch (Throwable t) {
            // this means that the method can't be found and so the test fails
            fail("Digester threw Exception:  " + t);
        }

        // IMPORTANT! The CallMethodRule rule returns "CBA" in this case.
        // InvokeMethodRule returns "ABC".        
        //assertEquals("Wrong method call order", "CBA", word.toString());
        assertEquals("Wrong method call order", "ABC", word.toString());
    }

    public void testPrimitiveReading() throws Exception {
        StringReader reader = new StringReader(
            "<?xml version='1.0' ?><root><bean good='true'/><bean good='false'/><bean/>"
            + "<beanie bad='Fee Fie Foe Fum' good='true'/><beanie bad='Fee Fie Foe Fum' good='false'/>"
            + "<beanie bad='Fee Fie Foe Fum'/></root>");
            
        Digester digester = new Digester();
        
        //SimpleLog log = new SimpleLog("[testPrimitiveReading:Digester]");
        //log.setLevel(SimpleLog.LOG_LEVEL_TRACE);
        //digester.setLogger(log);
        
        digester.addObjectCreate("root/bean", PrimitiveBean.class);
        digester.addSetNext("root/bean", "add");
        Class [] params = { Boolean.TYPE };
        digester.addInvokeMethod("root/bean", "setBoolean", 1, params);
        digester.addInvokeParamFromAttr("root/bean", 0, "good");
        
        digester.addObjectCreate("root/beanie", PrimitiveBean.class);
        digester.addSetNext("root/beanie", "add");
        Class [] beanieParams = { String.class, Boolean.TYPE };
        digester.addInvokeMethod("root/beanie", "testSetBoolean", 2, beanieParams);
        digester.addInvokeParamFromAttr("root/beanie", 0, "bad");
        digester.addInvokeParamFromAttr("root/beanie", 1, "good");
        Rule r = new InvokeParamFromDefaultsRule(new Object[]{null, null});
        digester.addRule("root/beanie", r);
        
        ArrayList list = new ArrayList();
        digester.push(list);
        digester.parse(reader);
        
        assertEquals("Wrong number of beans in list", 6, list.size());
        PrimitiveBean bean = (PrimitiveBean) list.get(0);
        assertTrue("Bean 0 property not called", bean.getSetBooleanCalled());
        assertEquals("Bean 0 property incorrect", true, bean.getBoolean());
        bean = (PrimitiveBean) list.get(1);
        assertTrue("Bean 1 property not called", bean.getSetBooleanCalled());
        assertEquals("Bean 1 property incorrect", false, bean.getBoolean());
        bean = (PrimitiveBean) list.get(2);
        // no attibute, no call is what's expected
        assertTrue("Bean 2 property called", !bean.getSetBooleanCalled());
        bean = (PrimitiveBean) list.get(3);
        assertTrue("Bean 3 property not called", bean.getSetBooleanCalled());
        assertEquals("Bean 3 property incorrect", true, bean.getBoolean());
        bean = (PrimitiveBean) list.get(4);
        assertTrue("Bean 4 property not called", bean.getSetBooleanCalled());
        assertEquals("Bean 4 property incorrect", false, bean.getBoolean());
        bean = (PrimitiveBean) list.get(5);
        assertTrue("Bean 5 property not called", bean.getSetBooleanCalled());
        assertEquals("Bean 5 property incorrect", false, bean.getBoolean());       
    }
    
    public void testFromStack() throws Exception {
    
        StringReader reader = new StringReader(
            "<?xml version='1.0' ?><root><one/><two/><three/><four/><five/></root>");
            
        Digester digester = new Digester();
        
        Class [] params = { String.class };
        
        digester.addObjectCreate("root/one", NamedBean.class);
        digester.addSetNext("root/one", "add");
        digester.addInvokeMethod("root/one", "setName", 1, params);
        digester.addInvokeParamFromStack("root/one", 0, 2);
        
        digester.addObjectCreate("root/two", NamedBean.class);
        digester.addSetNext("root/two", "add");
        digester.addInvokeMethod("root/two", "setName", 1, params);
        digester.addInvokeParamFromStack("root/two", 0, 3);
        
        digester.addObjectCreate("root/three", NamedBean.class);
        digester.addSetNext("root/three", "add");
        digester.addInvokeMethod("root/three", "setName", 1, params);
        digester.addInvokeParamFromStack("root/three", 0, 4);
        
        digester.addObjectCreate("root/four", NamedBean.class);
        digester.addSetNext("root/four", "add");
        digester.addInvokeMethod("root/four", "setName", 1, params);
        digester.addInvokeParamFromStack("root/four", 0, 5);
        
        digester.addObjectCreate("root/five", NamedBean.class);
        digester.addSetNext("root/five", "add");
        Class [] newParams = { String.class, String.class };
        digester.addInvokeMethod("root/five", "test", 2, newParams);
        digester.addInvokeParamFromStack("root/five", 0, 10);
        digester.addInvokeParamFromStack("root/five", 1, 3);
        
        // prepare stack
        digester.push("That lamb was sure to go.");
        digester.push("And everywhere that Mary went,");
        digester.push("It's fleece was white as snow.");
        digester.push("Mary had a little lamb,");
        
        ArrayList list = new ArrayList();
        digester.push(list);
        digester.parse(reader);
        
        assertEquals("Wrong number of beans in list", 5, list.size());
        NamedBean bean = (NamedBean) list.get(0);
        assertEquals("Parameter not set from stack (1)", "Mary had a little lamb,", bean.getName());
        bean = (NamedBean) list.get(1);
        assertEquals("Parameter not set from stack (2)", "It's fleece was white as snow.", bean.getName());
        bean = (NamedBean) list.get(2);
        assertEquals("Parameter not set from stack (3)", "And everywhere that Mary went,", bean.getName());
        bean = (NamedBean) list.get(3);
        assertEquals("Parameter not set from stack (4)", "That lamb was sure to go.", bean.getName());
        bean = (NamedBean) list.get(4);
        assertEquals("Out of stack not set to null", null , bean.getName());
    }
    
    public void testTwoCalls() throws Exception {
        
    
        StringReader reader = new StringReader(
            "<?xml version='1.0' ?><root>"
            + "<param class='int' coolness='true'>25</param>"
            + "<param class='long'>50</param>"
            + "<param class='float' coolness='false'>90</param></root>");
            
        Digester digester = new Digester();
        //SimpleLog log = new SimpleLog("{testTwoCalls:Digester]");
        //log.setLevel(SimpleLog.LOG_LEVEL_TRACE);
        //digester.setLogger(log);
        
        digester.addObjectCreate( "root/param", ParamBean.class );
        digester.addSetNext( "root/param", "add" );
        digester.addInvokeMethod( "root/param", "setThisAndThat", 2 );
        digester.addInvokeParamFromAttr( "root/param", 0, "class" );
        digester.addInvokeParamFromBody( "root/param", 1 );
        digester.addInvokeMethod( "root/param", "setCool", 1, new Class[] {boolean.class } );
        digester.addInvokeParamFromAttr( "root/param", 0, "coolness" );
        
        ArrayList list = new ArrayList();
        digester.push(list);
        digester.parse(reader);
    
        assertEquals("Wrong number of objects created", 3, list.size());
        ParamBean bean = (ParamBean) list.get(0);
        assertEquals("Coolness wrong (1)", true, bean.isCool());
        assertEquals("This wrong (1)", "int", bean.getThis());
        assertEquals("That wrong (1)", "25", bean.getThat());
        bean = (ParamBean) list.get(1);
        assertEquals("Coolness wrong (2)", false, bean.isCool());
        assertEquals("This wrong (2)", "long", bean.getThis());
        assertEquals("That wrong (2)", "50", bean.getThat());
        bean = (ParamBean) list.get(2);
        assertEquals("Coolness wrong (3)", false, bean.isCool());
        assertEquals("This wrong (3)", "float", bean.getThis());
        assertEquals("That wrong (3)", "90", bean.getThat());
    }

    public void testNestedBody() throws Exception {
        
        StringReader reader = new StringReader(
            "<?xml version='1.0' ?><root>"
            + "<spam>Simple</spam>"
            + "<spam>Complex<spam>Deep<spam>Deeper<spam>Deepest</spam></spam></spam></spam>"
            + "</root>");
            
        Digester digester = new Digester();        

        //SimpleLog log = new SimpleLog("[testPrimitiveReading:Digester]");
        //log.setLevel(SimpleLog.LOG_LEVEL_TRACE);
        //digester.setLogger(log);
        
        
        digester.addObjectCreate("root/spam", NamedBean.class);
        digester.addSetRoot("root/spam", "add");
        digester.addInvokeMethod( "root/spam", "setName", 1 );
        digester.addInvokeParamFromBody( "root/spam", 0);
        
        digester.addObjectCreate("root/spam/spam", NamedBean.class);
        digester.addSetRoot("root/spam/spam", "add");
        digester.addInvokeMethod( "root/spam/spam", "setName", 1 );
        digester.addInvokeParamFromBody( "root/spam/spam", 0);        
        
        digester.addObjectCreate("root/spam/spam/spam", NamedBean.class);
        digester.addSetRoot("root/spam/spam/spam", "add");
        digester.addInvokeMethod( "root/spam/spam/spam", "setName", 1 );
        digester.addInvokeParamFromBody( "root/spam/spam/spam", 0);      

        
        digester.addObjectCreate("root/spam/spam/spam/spam", NamedBean.class);
        digester.addSetRoot("root/spam/spam/spam/spam", "add");
        digester.addInvokeMethod( "root/spam/spam/spam/spam", "setName", 1 );
        digester.addInvokeParamFromBody( "root/spam/spam/spam/spam", 0);   
        
        ArrayList list = new ArrayList();
        digester.push(list);
        digester.parse(reader);
        
        NamedBean bean = (NamedBean) list.get(0);
        assertEquals("Wrong name (1)", "Simple", bean.getName());
        // these are added in deepest first order by the addRootRule
        bean = (NamedBean) list.get(4);
        assertEquals("Wrong name (2)", "Complex", bean.getName());
        bean = (NamedBean) list.get(3);
        assertEquals("Wrong name (3)", "Deep", bean.getName());
        bean = (NamedBean) list.get(2);
        assertEquals("Wrong name (4)", "Deeper", bean.getName());
        bean = (NamedBean) list.get(1);
        assertEquals("Wrong name (5)", "Deepest", bean.getName());
    }
    
    public void testProcessingHook() throws Exception {
        
        class TestInvokeMethodRule extends InvokeMethodRule {
            Object result;
            TestInvokeMethodRule(String methodName, int paramCount)
            {
                super(methodName, paramCount);
            }
            protected void processMethodCallResult(Object result) {
                this.result = result;
            }
        }
    
        StringReader reader = new StringReader(
            "<?xml version='1.0' ?><root>"
            + "<param class='float' coolness='false'>90</param></root>");
        
            
        Digester digester = new Digester();
        //SimpleLog log = new SimpleLog("{testTwoCalls:Digester]");
        //log.setLevel(SimpleLog.LOG_LEVEL_TRACE);
        //digester.setLogger(log);
        
        digester.addObjectCreate( "root/param", ParamBean.class );
        digester.addSetNext( "root/param", "add" );
        TestInvokeMethodRule rule = new TestInvokeMethodRule( "setThisAndThat" , 2 );
        digester.addRule( "root/param", rule );
        digester.addInvokeParamFromAttr( "root/param", 0, "class" );
        digester.addInvokeParamFromAttr( "root/param", 1, "coolness" );
        
        ArrayList list = new ArrayList();
        digester.push(list);
        digester.parse(reader);
    
        assertEquals("Wrong number of objects created", 1, list.size());
        assertEquals("Result not passed into hook", "The Other", rule.result);
    }

    /** Test for the InvokeParamFromPathRule */
    public void testInvokeParamFromPath() throws Exception {
        String xml = "<?xml version='1.0'?><main>"
            + "<alpha><beta>Ignore this</beta></alpha>"
            + "<beta><epsilon><gamma>Ignore that</gamma></epsilon></beta>"
            + "</main>";
    
        SimpleTestBean bean = new SimpleTestBean();
        bean.setAlphaBeta("[UNSET]", "[UNSET]");
        
        StringReader in = new StringReader(xml);
        Digester digester = new Digester();
        digester.setRules(new ExtendedBaseRules());
        digester.addInvokeMethod("main", "setAlphaBeta", 2);
        digester.addInvokeParamFromPath("*/alpha/?", 0);
        digester.addInvokeParamFromPath("*/epsilon/?", 1);
        
        digester.push(bean);
        
        digester.parse(in);
        
        assertEquals("Test alpha property setting", "main/alpha/beta" , bean.getAlpha());
        assertEquals("Test beta property setting", "main/beta/epsilon/gamma" , bean.getBeta());
    }


    /** 
     * Test invoking an object which does not exist on the stack.
     */
    public void testCallInvalidTarget() throws Exception {
    
        Digester digester = new Digester();
        digester.addObjectCreate("employee", HashMap.class);

        // there should be only one object on the stack (index zero),
        // so selecting a target object with index 1 on the object stack
        // should result in an exception.
        InvokeMethodRule r = new InvokeMethodRule(1, "put", -1);
        digester.addRule("employee", r);
        
        try {
            digester.parse(getInputStream("Test5.xml"));
            fail("Exception should be thrown for invalid target offset");
        }
        catch(SAXException e) {
            // ok, exception expected
        }
    }

    /** 
     * Test invoking an object which is at top-1 on the stack, like
     * SetNextRule does...
     */
    public void testCallNext() throws Exception {
    
        Digester digester = new Digester();
        digester.addObjectCreate("employee", HashMap.class);

        digester.addObjectCreate("employee/address", Address.class);
        digester.addSetNestedProperties("employee/address");
        InvokeMethodRule r = new InvokeMethodRule(1, "put", 2);
        digester.addRule("employee/address", r);
        digester.addInvokeParamFromBody("employee/address/type", 0);
        digester.addInvokeParamFromStack("employee/address", 1, 0);
        
        HashMap map = (HashMap) digester.parse(getInputStream("Test5.xml"));
        
        assertNotNull(map);
        java.util.Set keys = map.keySet();
        assertEquals(2, keys.size());
        Address home = (Address) map.get("home");
        assertNotNull(home);
        assertEquals("HmZip", home.getZipCode());
        Address office = (Address) map.get("office");
        assertNotNull(office);
        assertEquals("OfZip", office.getZipCode());
    }

    /** 
     * Test invoking an object which is at the root of the stack, like
     * SetRoot does...
     */
    public void testCallRoot() throws Exception {
    
        Digester digester = new Digester();
        digester.addObjectCreate("employee", HashMap.class);

        digester.addObjectCreate("employee/address", Address.class);
        digester.addSetNestedProperties("employee/address");
        InvokeMethodRule r = new InvokeMethodRule(-1, "put", 2);
        digester.addRule("employee/address", r);
        digester.addInvokeParamFromBody("employee/address/type", 0);
        digester.addInvokeParamFromStack("employee/address", 1, 0);
        
        HashMap map = (HashMap) digester.parse(getInputStream("Test5.xml"));
        
        assertNotNull(map);
        java.util.Set keys = map.keySet();
        assertEquals(2, keys.size());
        Address home = (Address) map.get("home");
        assertNotNull(home);
        assertEquals("HmZip", home.getZipCode());
        Address office = (Address) map.get("office");
        assertNotNull(office);
        assertEquals("OfZip", office.getZipCode());
    }

    // ------------------------------------------------ Utility Support Methods


    /**
     * Return an appropriate InputStream for the specified test file (which
     * must be inside our current package.
     *
     * @param name Name of the test file we want
     *
     * @exception IOException if an input/output error occurs
     */
    protected InputStream getInputStream(String name) throws IOException {

        return (this.getClass().getResourceAsStream
                ("/org/apache/commons/digester/" + name));

    }


}

