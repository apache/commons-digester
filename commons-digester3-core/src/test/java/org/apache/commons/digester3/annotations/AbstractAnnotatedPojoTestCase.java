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
package org.apache.commons.digester3.annotations;

import static org.apache.commons.digester3.binder.DigesterLoader.newLoader;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.RulesModule;

/**
 * Abstract implementation of Class-&gt;Digester Rules-&gt;parse & confronting.
 */
public abstract class AbstractAnnotatedPojoTestCase
{

    protected Collection<RulesModule> getAuxModules() {
        return new ArrayList<>();
    }

    /**
     * Loads the digester rules parsing the expected object class, parses the
     * XML and verify the digester produces the same result.
     *
     * @param expected the expected object
     * @throws Exception if any error occurs
     */
    public final void verifyExpectedEqualsToParsed(final Object expected) throws Exception {
        final Class<?> clazz = expected.getClass();

        final String resource = clazz.getSimpleName() + ".xml";
        final InputStream input = clazz.getResourceAsStream(resource);

        final Collection<RulesModule> modules = getAuxModules();
        modules.add(new FromAnnotationsRuleModule()
        {

            @Override
            protected void configureRules()
            {
                bindRulesFrom( clazz );
            }

        });

        final Digester digester = newLoader(modules).newDigester();
        final Object actual = digester.parse(input);

        if (input != null) {
            input.close();
        }

        assertEquals(expected, actual);
    }

}
