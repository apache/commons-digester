package org.apache.commons.digester3.binder;

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

import static java.lang.String.format;

import org.apache.commons.digester3.SetPropertyRule;

/**
 * Builder chained when invoking {@link LinkedRuleBuilder#setProperty(String)}.
 *
 * @since 3.0
 */
public final class SetPropertyBuilder
    extends AbstractBackToLinkedRuleBuilder<SetPropertyRule>
{

    private final String attributePropertyName;

    private String valueAttributeName;

    SetPropertyBuilder( final String keyPattern, final String namespaceURI, final RulesBinder mainBinder, final LinkedRuleBuilder mainBuilder,
                        final String attributePropertyName )
    {
        super( keyPattern, namespaceURI, mainBinder, mainBuilder );
        this.attributePropertyName = attributePropertyName;
    }

    @Override
    protected SetPropertyRule createRule()
    {
        return new SetPropertyRule( attributePropertyName, valueAttributeName );
    }

    /**
     * Sets the name of the attribute that will contain the value to which the property should be set.
     *
     * @param valueAttributeName Name of the attribute that will contain the value to which the property should be set.
     * @return this builder instance
     */
    public SetPropertyBuilder extractingValueFromAttribute( final String valueAttributeName )
    {
        if ( attributePropertyName == null || attributePropertyName.isEmpty() )
        {
            reportError( format( "setProperty(\"%s\").extractingValueFromAttribute(String)}", attributePropertyName ),
                         "empty 'valueAttributeName' not allowed" );
        }

        this.valueAttributeName = valueAttributeName;
        return this;
    }

}
