/*
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

import org.apache.commons.digester2.Digester;
import org.apache.commons.digester2.InvalidRuleException;
import org.apache.commons.digester2.DigestionException;
import org.apache.commons.digester2.factory.ActionFactory;

/**
 * A simple program to demonstrate the basic functionality of the
 * Commons Digester module.
 * <p>
 * This code will parse the provided "example.xml" file to build a tree
 * of java objects, then cause those objects to print out their values
 * to demonstrate that the input file has been processed correctly.
 * <p>
 * As with all code, there are many ways of achieving the same goal;
 * the solution here is only one possible solution to the problem.
* <p> 
 * Very verbose comments are included here, as this class is intended
 * as a tutorial; if you look closely at method "addRules", you will
 * see that the amount of code required to use the Digester is actually
 * quite low.
 * <p>
 * Usage: java Main example.xml
 */
public class Main {
    
    /**
     * Main method : entry point for running this example program.
     * <p>
     * Usage: java Example example.xml
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            usage();
            System.exit(-1);
        }
        
        String filename = args[0];
        
        // Create a Digester instance
        Digester d = new Digester();
        
        // Prime the digester stack with an object for rules to
        // operate on. Note that it is quite common for "this"
        // to be the object pushed.
        AddressBook book = new AddressBook();
        d.setInitialObject(book);
        
        // Add rules to the digester that will be triggered while
        // parsing occurs.
        try {
            addRules(d);
        } catch(InvalidRuleException ex) {
            System.out.println("Error defining digester rules:" + ex.getMessage());
            System.exit(-1);
        }
        
        // Process the input file.
        try {
            java.io.File srcfile = new java.io.File(filename);
            d.parse(srcfile);
        }
        catch(java.io.IOException ex) {
            System.out.println("Error reading input file:" + ex.getMessage());
            System.exit(-1);
        }
        catch(org.xml.sax.SAXException ex) {
            System.out.println("Error parsing input file:" + ex.getMessage());
            System.exit(-1);
        }
        catch(DigestionException ex) {
            System.out.println("Error parsing input file:" + ex.getMessage());
            System.exit(-1);
        }
        
        
        // Print out all the contents of the address book, as loaded from
        // the input file.
        book.print();
    }
    
    private static void addRules(Digester d) throws InvalidRuleException {

        ActionFactory f = new ActionFactory(d);

        //--------------------------------------------------        
        // when we encounter a "person" tag, do the following:

        // create a new instance of class Person, and push that
        // object onto the digester stack of objects
        f.addCreateObject("/address-book/person", Person.class);
        
        // map *any* attributes on the tag to appropriate
        // setter-methods on the top object on the stack (the Person
        // instance created by the preceeding rule). 
        //
        // For example:
        // if attribute "id" exists on the xml tag, and method setId 
        // with one parameter exists on the object that is on top of
        // the digester object stack, then a call will be made to that
        // method. The value will be type-converted from string to
        // whatever type the target method declares (where possible), 
        // using the commons ConvertUtils functionality.
        //
        // Attributes on the xml tag for which no setter methods exist
        // on the top object on the stack are just ignored.
        f.addSetProperties("/address-book/person");

        // call the addPerson method on the second-to-top object on
        // the stack (the AddressBook object), passing the top object
        // on the stack (the recently created Person object).
        f.addSetNext("/address-book/person", "addPerson");        
        
        //--------------------------------------------------        
        // when we encounter a "name" tag, call setName on the top
        // object on the stack, passing the text contained within the
        // body of that name element [specifying a zero parameter count
        // implies one actual parameter, being the body text]. 
        // The top object on the stack will be a person object, because 
        // the pattern address-book/person always triggers the 
        // CreateObjectAction we added previously.
        f.addCallMethod("/address-book/person/name", "setName", 0);
        
        //--------------------------------------------------        
        // when we encounter an "email" tag, call addEmail on the top
        // object on the stack, passing two parameters: the "type"
        // attribute, and the text within the tag body.
        f.addCallMethod("/address-book/person/email", "addEmail", 2);
        f.addCallParam("/address-book/person/email", 0, "type");
        f.addCallParam("/address-book/person/email", 1);
    }

    private static void usage() {
        System.out.println("Usage: java Main example.xml");
    }
}