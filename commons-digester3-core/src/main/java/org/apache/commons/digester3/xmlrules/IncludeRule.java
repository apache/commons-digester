package org.apache.commons.digester3.xmlrules;

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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.apache.commons.digester3.Rule;
import org.apache.commons.digester3.binder.RulesBinder;
import org.apache.commons.digester3.binder.RulesModule;
import org.xml.sax.Attributes;

/**
 * A rule for including one rules XML file within another. Included files behave as if they are 'macro-expanded' within
 * the includer. This means that the values of the pattern stack are prefixed to every pattern in the included rules.
 * <p>
 * This rule will detect 'circular' includes, which would result in infinite recursion. It throws a
 * CircularIncludeException when a cycle is detected, which will terminate the parse.
 */
final class IncludeRule
    extends Rule
{

    private static final String CLASSPATH_URL_PREFIX = "classpath:";

    private final WithMemoryRulesBinder memoryRulesBinder;

    private final RulesBinder targetRulesBinder;

    public IncludeRule( final WithMemoryRulesBinder memoryRulesBinder, final RulesBinder targetRulesBinder )
    {
        this.memoryRulesBinder = memoryRulesBinder;
        this.targetRulesBinder = targetRulesBinder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void begin(final String namespace, final String name, final Attributes attributes) throws Exception {
        handleInclude(attributes);
        handleClass(attributes);
    }

    private void handleInclude(Attributes attributes) {
        final String fileName = attributes.getValue("url");
        if (fileName != null && !fileName.isEmpty()) {
            final URL xmlRulesResource = resolveXmlRulesResource(fileName);

            if (xmlRulesResource != null) {
                processXmlRulesResource(xmlRulesResource);
            }
        }
    }

    private URL resolveXmlRulesResource(String fileName) {
        if (fileName.startsWith(CLASSPATH_URL_PREFIX)) {
            return resolveClasspathResource(fileName);
        } else {
            return resolveExternalResource(fileName);
        }
    }

    private URL resolveClasspathResource(String fileName) {
        String path = fileName.substring(CLASSPATH_URL_PREFIX.length());
        if ('/' == path.charAt(0)) {
            path = path.substring(1);
        }
        URL xmlRulesResource = this.targetRulesBinder.getContextClassLoader().getResource(path);
        if (xmlRulesResource == null) {
            targetRulesBinder.addError("Resource '%s' not found, please make sure it is in the classpath", path);
            return null;
        }
        return xmlRulesResource;
    }

    private URL resolveExternalResource(String fileName) {
        try {
            return new URL(fileName);
        } catch (MalformedURLException e) {
            targetRulesBinder.addError("An error occurred while including file from '%s': %s", fileName, e.getMessage());
            return null;
        }
    }

    private void processXmlRulesResource(URL xmlRulesResource) {
        final Set<String> includedFiles = memoryRulesBinder.getIncludedFiles();
        final String xmlRulesResourceString = xmlRulesResource.toString();
        if (includedFiles.add(xmlRulesResourceString)) {
            try {
                installFromXmlRulesModule(xmlRulesResource);
            } finally {
                includedFiles.remove(xmlRulesResourceString);
            }
        } else {
            targetRulesBinder.addError("Circular file inclusion detected for XML rules: %s", xmlRulesResource);
        }
    }

    private void installFromXmlRulesModule(URL xmlRulesResource) {
        install(new FromXmlRulesModule() {
            @Override
            protected void loadRules() {
                loadXMLRules(xmlRulesResource);
            }
        });
    }

    private void handleClass(Attributes attributes) {
        final String className = attributes.getValue("class");
        if (className != null && !className.isEmpty()) {
            processClassAttribute(className);
        }
    }

    private void processClassAttribute(String className) {
        try {
            final Class<?> cls = Class.forName(className);
            if (!RulesModule.class.isAssignableFrom(cls)) {
                targetRulesBinder.addError("Class '%s' is not a '%s' implementation", className, RulesModule.class.getName());
                return;
            }

            final RulesModule rulesSource = (RulesModule) cls.newInstance();

            install(rulesSource);
        } catch (Exception e) {
            targetRulesBinder.addError("Impossible to include programmatic rules from class '%s': %s", className, e.getMessage());
        }
    }

    private void install( final RulesModule rulesModule )
    {
        // that's an hack, shall not be taken in consideration!!! :)
        rulesModule.configure( new PrefixedRulesBinder( targetRulesBinder,
                                                        memoryRulesBinder.getPatternStack().toString() ) );
    }

}
