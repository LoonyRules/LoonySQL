package uk.co.loonyrules.sql.models;

import uk.co.loonyrules.sql.annotations.Column;

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

    public TableColumn()
    {

    }

    public String getField()
    {
        return field;
    }

    public String getType()
    {
        return type;
    }

    public Object getNullObject()
    {
        return nullObject;
    }

    public Object getExtra()
    {
        return extra;
    }

    public Object getDefaultObject()
    {
        return defaultObject;
    }

    public Object getKey()
    {
        return key;
    }

    @Override
    public String toString()
    {
        return "TableColumn{" +
                "field='" + field + '\'' +
                ", type='" + type + '\'' +
                ", nullObject=" + nullObject +
                ", extra=" + extra +
                ", defaultObject=" + defaultObject +
                ", key=" + key +
                '}';
    }

}