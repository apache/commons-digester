/*
 *
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

package org.apache.commons.digester3;

import org.apache.commons.digester3.annotations.rules.CallParam;
import org.apache.commons.digester3.annotations.rules.ObjectCreate;

/**
 * General purpose test bean for Digester tests.
 */
public class TestBean
{

    /**
     * A boolean property whose initial value is true.
     */
    private boolean booleanProperty = true;

    /**
     * A double property.
     */
    private double doubleProperty = 321.0;

    /**
     * A boolean property whose initial value is false
     */
    private boolean falseProperty;

    /**
     * A float property.
     */
    private float floatProperty = (float) 123.0;

    /**
     * Integer arrays that are accessed as an array as well as indexed.
     */
    private int[] intArray = { 0, 10, 20, 30, 40 };

    private final int[] intIndexed = { 0, 10, 20, 30, 40 };

    private int[] intMultibox = {};

    /**
     * An integer property.
     */
    private int intProperty = 123;

    /**
     * A long property.
     */
    private long longProperty = 321;

    /**
     * A multiple-String SELECT element.
     */
    private String[] multipleSelect = { "Multiple 3", "Multiple 5", "Multiple 7" };

    /**
     * A nested reference to another test bean (populated as needed).
     */
    private TestBean nested;

    /**
     * A String property with an initial value of null.
     */
    private String nullProperty;

    /**
     * A short property.
     */
    private short shortProperty = (short) 987;

    /**
     * A single-String value for a SELECT element.
     */
    private String singleSelect = "Single 5";

    /**
     * String arrays that are accessed as an array as well as indexed.
     */
    private String[] stringArray = { "String 0", "String 1", "String 2", "String 3", "String 4" };

    private final String[] stringIndexed = { "String 0", "String 1", "String 2", "String 3", "String 4" };

    /**
     * A String property.
     */
    private String stringProperty = "This is a string";

    /**
     * An empty String property.
     */
    private String emptyStringProperty = "";

    public TestBean()
    {
        // do nothing
    }

    @ObjectCreate( pattern = "toplevel/bean" )
    public TestBean( @CallParam( pattern = "toplevel/bean", attributeName = "boolean" ) final boolean booleanProperty,
                     @CallParam( pattern = "toplevel/bean", attributeName = "double" ) final double doubleProperty )
    {
        setBooleanProperty( booleanProperty );
        setDoubleProperty( doubleProperty );
    }

    /**
     * see DIGESTER-154
     *
     * @param booleanProperty
     * @param doubleProperty
     */
    public TestBean( final Boolean booleanProperty, final Double doubleProperty )
    {
        this( booleanProperty.booleanValue(), doubleProperty.doubleValue() );
    }

    public boolean getBooleanProperty()
    {
        return booleanProperty;
    }

    public double getDoubleProperty()
    {
        return this.doubleProperty;
    }

    public String getEmptyStringProperty()
    {
        return this.emptyStringProperty;
    }

    public boolean getFalseProperty()
    {
        return falseProperty;
    }

    public float getFloatProperty()
    {
        return this.floatProperty;
    }

    public int[] getIntArray()
    {
        return this.intArray;
    }

    public int getIntIndexed( final int index )
    {
        return intIndexed[index];
    }

    public int[] getIntMultibox()
    {
        return this.intMultibox;
    }

    public int getIntProperty()
    {
        return this.intProperty;
    }

    public long getLongProperty()
    {
        return this.longProperty;
    }

    public String[] getMultipleSelect()
    {
        return this.multipleSelect;
    }

    public TestBean getNested()
    {
        if ( nested == null ) {
            nested = new TestBean();
        }
        return nested;
    }

    public String getNullProperty()
    {
        return this.nullProperty;
    }

    public short getShortProperty()
    {
        return this.shortProperty;
    }

    public String getSingleSelect()
    {
        return this.singleSelect;
    }

    public String[] getStringArray()
    {
        return this.stringArray;
    }

    public String getStringIndexed( final int index )
    {
        return stringIndexed[index];
    }

    public String getStringProperty()
    {
        return this.stringProperty;
    }

    public void setBooleanProperty( final boolean booleanProperty )
    {
        this.booleanProperty = booleanProperty;
    }

    public void setDoubleProperty( final double doubleProperty )
    {
        this.doubleProperty = doubleProperty;
    }

    public void setEmptyStringProperty( final String emptyStringProperty )
    {
        this.emptyStringProperty = emptyStringProperty;
    }

    public void setFalseProperty( final boolean falseProperty )
    {
        this.falseProperty = falseProperty;
    }

    public void setFloatProperty( final float floatProperty )
    {
        this.floatProperty = floatProperty;
    }

    public void setIntArray( final int[] intArray )
    {
        this.intArray = intArray;
    }

    public void setIntIndexed( final int index, final int value )
    {
        intIndexed[index] = value;
    }

    public void setIntMultibox( final int[] intMultibox )
    {
        this.intMultibox = intMultibox;
    }

    public void setIntProperty( final int intProperty )
    {
        this.intProperty = intProperty;
    }

    public void setLongProperty( final long longProperty )
    {
        this.longProperty = longProperty;
    }

    public void setMultipleSelect( final String[] multipleSelect )
    {
        this.multipleSelect = multipleSelect;
    }

    public void setNullProperty( final String nullProperty )
    {
        this.nullProperty = nullProperty;
    }

    public void setShortProperty( final short shortProperty )
    {
        this.shortProperty = shortProperty;
    }

    public void setSingleSelect( final String singleSelect )
    {
        this.singleSelect = singleSelect;
    }

    public void setStringArray( final String[] stringArray )
    {
        this.stringArray = stringArray;
    }

    public void setStringIndexed( final int index, final String value )
    {
        stringIndexed[index] = value;
    }

    public void setStringProperty( final String stringProperty )
    {
        this.stringProperty = stringProperty;
    }

}
