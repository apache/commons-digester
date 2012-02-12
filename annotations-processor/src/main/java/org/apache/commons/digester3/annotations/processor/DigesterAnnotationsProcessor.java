package org.apache.commons.digester3.annotations.processor;

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

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

import org.apache.commons.digester3.annotations.rules.BeanPropertySetter;
import org.apache.commons.digester3.annotations.rules.CallMethod;
import org.apache.commons.digester3.annotations.rules.CallParam;
import org.apache.commons.digester3.annotations.rules.CreationRule;
import org.apache.commons.digester3.annotations.rules.FactoryCreate;
import org.apache.commons.digester3.annotations.rules.ObjectCreate;
import org.apache.commons.digester3.annotations.rules.PathCallParam;
import org.apache.commons.digester3.annotations.rules.SetNext;
import org.apache.commons.digester3.annotations.rules.SetProperty;
import org.apache.commons.digester3.annotations.rules.SetRoot;
import org.apache.commons.digester3.annotations.rules.SetTop;

/**
 * @since 3.3
 */
public class DigesterAnnotationsProcessor
    extends AbstractProcessor
{

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getSupportedAnnotationTypes()
    {
        return new HashSet<String>( asList( BeanPropertySetter.class.getName(),
                                            CallMethod.class.getName(),
                                            CallParam.class.getName(),
                                            CreationRule.class.getName(),
                                            FactoryCreate.class.getName(),
                                            ObjectCreate.class.getName(),
                                            PathCallParam.class.getName(),
                                            SetNext.class.getName(),
                                            SetProperty.class.getName(),
                                            SetRoot.class.getName(),
                                            SetTop.class.getName() ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process( Set<? extends TypeElement> annotations, RoundEnvironment environment )
    {
        return false;
    }

}
