package org.apache.commons.digester3.examples.api.dbinsert;

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

import java.sql.Connection;

import org.apache.commons.digester3.Rule;
import org.apache.commons.digester3.examples.api.dbinsert.Row.Column;

/**
 * See Main.java.
 */
public class RowInserterRule
    extends Rule
{

    private final Connection conn;

    public RowInserterRule( final Connection conn )
    {
        this.conn = conn;
    }

    /**
     * This method is invoked when the start tag for an XML element representing
     * a database row is encountered. It pushes a new Row instance onto the
     * digester stack (rather like an ObjectCreateRule) so that column data
     * can be stored on it.
     */
    @Override
    public void begin( final String namespace, final String name, final org.xml.sax.Attributes attrs )
    {
        getDigester().push( new Row() );
    }

    /**
     * This method is invoked when the end tag for an XML element representing
     * a database row is encountered. It pops a fully-configured Row instance
     * off the digester stack, accesses the object below it on the stack (a
     * Table object) to get the tablename, then does an SQL insert). Actually,
     * here we just print out text rather than do the sql insert, but the
     * real implementation should be fairly simple.
     * <p>
     * Note that after this rule completes, the row/column information is
     * <em>discarded</em>, ie this rule performs actions <i>as the input is
     * parsed</i>. This contrasts with the more usual way digester is used,
     * which is to build trees of objects for later use. But it's a perfectly
     * valid use of Digester.
     */
    @Override
    public void end( final String namespace, final String name )
    {
        final Row row = getDigester().pop();
        final Table table = getDigester().peek();

        // Obviously, all this would be replaced by code like:
        // stmt = conn.prepareStatement();
        // stmt.setString(n, value);
        //
        // Many improvements can then be implemented, such as using the
        // PreparedStatement.getParameterMetaData method to retrieve
        // retrieve parameter types, etc.

        final StringBuilder colnames = new StringBuilder();
        final StringBuilder colvalues = new StringBuilder();

        for (final Column column : row.getColumns()) {
            if ( colnames.length() > 0 )
            {
                colnames.append( ", " );
                colvalues.append( ", " );
            }

            colnames.append( "'" );
            colnames.append( column.getName() );
            colnames.append( "'" );

            colvalues.append( "'" );
            colvalues.append( column.getValue() );
            colvalues.append( "'" );
        }

        final StringBuilder buf = new StringBuilder();
        buf.append( "insert into " );
        buf.append( table.getName() );
        buf.append( " (" );
        buf.append( colnames );
        buf.append( ") values (" );
        buf.append( colvalues );
        buf.append( ")" );

        // here the prepared statement would be executed....
        System.out.println( buf );
    }

}
