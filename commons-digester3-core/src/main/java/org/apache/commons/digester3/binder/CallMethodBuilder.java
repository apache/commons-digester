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
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static java.lang.String.format;

import java.util.Arrays;

import org.apache.commons.digester3.CallMethodRule;

/**
 * Builder chained when invoking {@link LinkedRuleBuilder#callMethod(String)}.
 *
 * @since 3.0
 */
public final class CallMethodBuilder
    extends AbstractBackToLinkedRuleBuilder<CallMethodRule>
{

    private final String methodName;

    private final ClassLoader classLoader;

    private int targetOffset;

    private int paramCount;

    private Class<?>[] paramTypes = {};

    private boolean useExactMatch;

    CallMethodBuilder( final String keyPattern, final String namespaceURI, final RulesBinder mainBinder, final LinkedRuleBuilder mainBuilder,
                       final String methodName, final ClassLoader classLoader )
    {
        super( keyPattern, namespaceURI, mainBinder, mainBuilder );
        this.methodName = methodName;
        this.classLoader = classLoader;
    }

    @Override
    protected CallMethodRule createRule()
    {
        final CallMethodRule callMethodRule = new CallMethodRule( targetOffset, methodName, paramCount, paramTypes );
        callMethodRule.setUseExactMatch( useExactMatch );
        return callMethodRule;
    }

    /**
     * Should {@code MethodUtils.invokeExactMethod} be used for the reflection.
     *
     * @param useExactMatch Flag to mark exact matching or not
     * @return this builder instance
     */
    public CallMethodBuilder useExactMatch( final boolean useExactMatch )
    {
        this.useExactMatch = useExactMatch;
        return this;
    }

    /**
     * Prepare the {@link CallMethodRule} to be invoked using the matching element body as argument.
     *
     * @return this builder instance
     */
    public CallMethodBuilder usingElementBodyAsArgument()
    {
        return withParamCount( 0 );
    }

    /**
     * The number of parameters to collect, or zero for a single argument from the body of this element.
     *
     * @param paramCount The number of parameters to collect, or zero for a single argument
     *        from the body of this element.
     * @return this builder instance
     */
    public CallMethodBuilder withParamCount( final int paramCount )
    {
        if ( paramCount < 0 )
        {
            reportError( format( "callMethod(\"%s\").withParamCount(int)", this.methodName ),
                              "negative parameters counter not allowed" );
        }

        this.paramCount = paramCount;

        if ( this.paramCount == 0 )
        {
            if ( this.paramTypes == null || this.paramTypes.length != 1 )
            {
                this.paramTypes = new Class<?>[] { String.class };
            }
        }
        else
        {
            this.paramTypes = new Class<?>[this.paramCount];
            for ( int i = 0; i < paramTypes.length; i++ )
            {
                this.paramTypes[i] = String.class;
            }
        }
        return this;
    }

    /**
     * Sets the Java classes that represent the parameter types of the method arguments.
     *
     * If you wish to use a primitive type, specify the corresponding Java wrapper class instead,
     * such as {@link Boolean#TYPE} for a {@code boolean} parameter.
     *
     * @param paramTypes The Java classes that represent the parameter types of the method arguments
     * @return this builder instance
     */
    public CallMethodBuilder withParamTypes( final Class<?>... paramTypes )
    {
        this.paramTypes = paramTypes;

        if ( paramTypes != null )
        {
            this.paramCount = paramTypes.length;
        }
        else
        {
            paramCount = 0;
        }

        return this;
    }

    /**
     * Sets the Java class names that represent the parameter types of the method arguments.
     *
     * If you wish to use a primitive type, specify the corresponding Java wrapper class instead,
     * such as {@link Boolean#TYPE} for a {@code boolean} parameter.
     *
     * @param paramTypeNames The Java classes names that represent the parameter types of the method arguments
     * @return this builder instance
     */
    public CallMethodBuilder withParamTypes( final String... paramTypeNames )
    {
        Class<?>[] paramTypes = null;
        if ( paramTypeNames != null )
        {
            paramTypes = new Class<?>[paramTypeNames.length];
            for ( int i = 0; i < paramTypeNames.length; i++ )
            {
                try
                {
                    paramTypes[i] = classLoader.loadClass( paramTypeNames[i] );
                }
                catch ( final ClassNotFoundException e )
                {
                    reportError( format( "callMethod( \"%s\" ).withParamTypes( %s )", this.methodName,
                                                     Arrays.toString( paramTypeNames ) ),
                                      format( "class '%s' cannot be load", paramTypeNames[i] ) );
                }
            }
        }

        return withParamTypes( paramTypes );
    }

    /**
     * Sets the location of the target object.
     *
     * Positive numbers are relative to the top of the digester object stack.
     * Negative numbers are relative to the bottom of the stack. Zero implies the top object on the stack.
     *
     * @param targetOffset location of the target object.
     * @return this builder instance
     */
    public CallMethodBuilder withTargetOffset( final int targetOffset )
    {
        this.targetOffset = targetOffset;
        return this;
    }

}
