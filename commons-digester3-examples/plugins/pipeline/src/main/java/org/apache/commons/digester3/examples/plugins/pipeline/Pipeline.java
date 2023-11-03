package org.apache.commons.digester3.examples.plugins.pipeline;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.plugins.PluginRules;
import org.apache.commons.digester3.plugins.PluginCreateRule;
import java.io.*;

/**
 * This is the "main" class for this example.
 * <p>
 * It can be run via {@code java Pipeline config-file-name}.
 * <p>
 * The specified config file is parsed using the Apache Commons Digester.
 * This config file specifies an input file to be read, a number of
 * user-defined transform classes to be instantiated and configured from
 * the config file, and an output file.
 * <p>
 * The contents of the input file is then passed to the transform objects,
 * and the output written to the output file.
 * <p>
 * Why not try writing your own transform classes, and plugging them in.
 * Note that they can configure themselves from the main config file in
 * any manner the Digester supports, without changing a line of this
 * application.
 */
public class Pipeline
{

    public static void main( final String[] args )
    {
        if ( args.length != 1 )
        {
            System.err.println( "usage: pipeline config-file" );
            System.exit( -1 );
        }
        final String configFile = args[0];

        final Digester digester = new Digester();
        final PluginRules rc = new PluginRules();
        digester.setRules( rc );

        digester.addObjectCreate( "pipeline", Pipeline.class );

        digester.addCallMethod( "pipeline/source", "setSource", 1 );
        digester.addCallParam( "pipeline/source", 0, "file" );

        final PluginCreateRule pcr = new PluginCreateRule( Transform.class );
        digester.addRule( "pipeline/transform", pcr );
        digester.addSetNext( "pipeline/transform", "setTransform" );

        digester.addCallMethod( "pipeline/destination", "setDest", 1 );
        digester.addCallParam( "pipeline/destination", 0, "file" );

        Pipeline pipeline = null;
        try
        {
            pipeline = digester.parse( configFile );
        }
        catch ( final Exception e )
        {
            System.err.println( "oops exception occurred during parse." );
            e.printStackTrace();
            System.exit( -1 );
        }

        try
        {
            pipeline.execute();
        }
        catch ( final Exception e )
        {
            System.err.println( "oops exception occurred during pipeline execution." );
            e.printStackTrace();
            System.exit( -1 );
        }
    }

    private String source;

    private String dest;

    private Transform transformer;

    private void execute()
        throws IOException
    {
        final FileReader inRaw = new FileReader( source );
        final FileWriter out = new FileWriter( dest );

        final BufferedReader in = new BufferedReader( inRaw );

        while ( true )
        {
            final String inStr = in.readLine();
            if ( inStr == null ) {
                break;
            }

            final String outStr = transformer.transform( inStr );
            out.write( outStr );
            out.write( '\n' );
        }

        inRaw.close();
        out.close();

        System.out.println( "Contents of file " + source + " have been transformed, and" + " written to file " + dest
            + "." );
    }

    public void setDest( final String dest )
    {
        this.dest = dest;
    }

    public void setSource( final String source )
    {
        this.source = source;
    }

    public void setTransform( final Transform transformer )
    {
        this.transformer = transformer;
    }

}
