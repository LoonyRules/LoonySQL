package uk.co.loonyrules.sql.models;

import uk.co.loonyrules.sql.annotations.Column;

/**
 * Model Column for {@link TableSchema}
 */
public class TableColumn
{

    @Column(name = "Field")
    private String field;

    @Column(name = "Type")
    private String type;

    @Column(name = "Null")
    private Object nullObject;

    @Column(name = "Extra")
    private Object extra;

    @Column(name = "Default")
    private Object defaultObject;

    @Column(name = "Key")
    private Object key;

    /**
     * Construct a new instance of TableColumn use for Reflection
     */
    public TableColumn()
    {

    }

    /**
     * Get the name of the Column
     * @return name of the Column
     */
    public String getField()
    {
        return this.field;
    }

    /**
     * Get the type of the Column
     * @return data type
     */
    public String getType()
    {
        return this.type;
    }

    /**
     * Get the Null object state
     * @return Null state
     */
    public Object getNullObject()
    {
        return this.nullObject;
    }

    /**
     * Get an extra data saved for this Column
     * @return any extra data
     */
    public Object getExtra()
    {
        return this.extra;
    }

    /**
     * Get the default object used for this Column
     * @return default value
     */
    public Object getDefaultObject()
    {
        return this.defaultObject;
    }

    /**
     * Get the key for this Column
     * @return key for the Column
     */
    public Object getKey()
    {
        return this.key;
    }

    /**
     * TableColumn represented as a String
     * @return current instance to String
     */
    @Override
    public String toString()
    {
        return "TableColumn{" +
                "field='" + this.field + '\'' +
                ", type='" + this.type + '\'' +
                ", nullObject=" + this.nullObject +
                ", extra=" + this.extra +
                ", defaultObject=" + this.defaultObject +
                ", key=" + this.key +
                '}';
    }

}