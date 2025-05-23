package org.apache.commons.digester3.examples.plugins.pipeline;

/*
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

import java.util.LinkedList;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.plugins.PluginCreateRule;

/**
 * An implementation of the Transform interface which is configured with
 * a sequence of "subtransforms", and applies them one by one to the input
 * data.
 * <p>
 * This demonstrates that a plugged-in class can itself define plugin-points
 * for user-defined classes if it wishes.
 */
public class CompoundTransform
    implements Transform
{

    public static void addRules( final Digester d, final String patternPrefix )
    {
        final PluginCreateRule pcr = new PluginCreateRule( Transform.class );
        d.addRule( patternPrefix + "/subtransform", pcr );
        d.addSetNext( patternPrefix + "/subtransform", "addTransform" );
    }

    private final LinkedList<Transform> transforms = new LinkedList<>();

    public void addTransform( final Transform transform )
    {
        transforms.add( transform );
    }

    @Override
    public String transform( String s )
    {
        for (final Transform t : transforms) {
            s = t.transform( s );
        }
        return s;
    }

}
