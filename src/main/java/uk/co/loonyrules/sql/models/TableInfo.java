package uk.co.loonyrules.sql.models;

import uk.co.loonyrules.sql.annotations.Column;
import uk.co.loonyrules.sql.annotations.Table;

/**
 * InformationSchema TableInfo model
 */
@Table(name = "information_schema.TABLES")
public class TableInfo
{

    @Column(name = "TABLE_CATALOG")
    private String tableCatalog;

    @Column(name = "TABLE_SCHEMA")
    private String tableSchema;

    @Column(name = "TABLE_NAME")
    private String tableName;

    @Column(name = "TABLE_TYPE")
    private String tableType;

    @Column(name = "ENGINE")
    private String engine;

    @Column(name = "VERSION")
    private int version;

    @Column(name = "ROW_FORMAT")
    private String rowFormat;

    @Column(name = "TABLE_ROWS")
    private long tableRows;

    @Column(name = "AVG_ROW_LENGTH")
    private long averageRowLength;

    @Column(name = "DATA_LENGTH")
    private long dataLength;

    @Column(name = "MAX_DATA_LENGTH")
    private long maxDataLength;

    @Column(name = "INDEX_LENGTH")
    private int indexLength;

    @Column(name = "DATA_FREE")
    private long dataFree;

    @Column(name = "AUTO_INCREMENT")
    private String autoIncrement;

    @Column(name = "CREATE_TIME")
    private String createTime;

    @Column(name = "UPDATE_TIME")
    private String updateTime;

    @Column(name = "CHECK_TIME")
    private String checkTime;

    @Column(name = "TABLE_COLLATION")
    private String tableCollation;

    @Column(name = "CHECKSUM")
    private String checksum;

    @Column(name = "CREATE_OPTIONS")
    private String createOptions;

    @Column(name = "TABLE_COMMENT")
    private String tableComment;

    public TableInfo()
    {

    }

    /**
     * Get the Table Catalog
     * @return table catalog
     */
    public String getTableCatalog()
    {
        return this.tableCatalog;
    }

    /**
     * Get the Table Schema (the database name)
     * @return table schema
     */
    public String getTableSchema()
    {
        return this.tableSchema;
    }

    /**
     * Get the Table name
     * @return table name
     */
    public String getTableName()
    {
        return this.tableName;
    }

    /**
     * Get the Table Type
     * @return table type
     */
    public String getTableType()
    {
        return this.tableType;
    }

    /**
     * Get the Engine
     * @return engine for the Table
     */
    public String getEngine()
    {
        return this.engine;
    }

    /**
     * Get the version
     * @return version
     */
    public int getVersion()
    {
        return this.version;
    }

    /**
     * Get the row format
     * @return row format
     */
    public String getRowFormat()
    {
        return this.rowFormat;
    }

    /**
     * Get the number of rows for the Table
     * @return number of rows
     */
    public long getTableRows()
    {
        return this.tableRows;
    }

    /**
     * Get the average row length
     * @return average row length
     */
    public long getAverageRowLength()
    {
        return this.averageRowLength;
    }

    /**
     * Get the total length of data
     * @return total length of data
     */
    public long getDataLength()
    {
        return this.dataLength;
    }

    /**
     * Get the maximum data length
     * @return maximum data length
     */
    public long getMaxDataLength()
    {
        return this.maxDataLength;
    }

    /**
     * Get the index length
     * @return index length
     */
    public int getIndexLength()
    {
        return this.indexLength;
    }

    /**
     * Get amount of data free
     * @return amount of data free
     */
    public long getDataFree()
    {
        return this.dataFree;
    }

    /**
     * Get auto increment next ID
     * @return auto increment next ID
     */
    public String getAutoIncrement()
    {
        return this.autoIncrement;
    }

    /**
     * Get the create time (TODO: Convert to SQL date)
     * @return creation time
     */
    public String getCreateTime()
    {
        return this.createTime;
    }

    /**
     * Get the last update time (TODO: Convert to SQL date)
     * @return last update time
     */
    public String getUpdateTime()
    {
        return this.updateTime;
    }

    /**
     * Get the last check time (TODO: Convert to SQL date)
     * @return last check time
     */
    public String getCheckTime()
    {
        return this.checkTime;
    }

    /**
     * Get the Table collation
     * @return table collation
     */
    public String getTableCollation()
    {
        return this.tableCollation;
    }

    /**
     * Get the checksum for this table
     * @return checksum
     */
    public String getChecksum()
    {
        return this.checksum;
    }

    /**
     * Get the Create Options (TODO: Ensure this is the correct var type)
     * @return create options
     */
    public String getCreateOptions()
    {
        return this.createOptions;
    }

    /**
     * Get the comment made for this table
     * @return comment for the table, if any
     */
    public String getTableComment()
    {
        return this.tableComment;
    }

    /**
     * TableInfo object as a String
     * @return object as a String
     */
    @Override
    public String toString()
    {
        return "TableInfo{" +
                "tableCatalog='" + this.tableCatalog + '\'' +
                ", tableSchema='" + this.tableSchema + '\'' +
                ", tableName='" + this.tableName + '\'' +
                ", tableType='" + this.tableType + '\'' +
                ", engine='" + this.engine + '\'' +
                ", version=" + this.version +
                ", rowFormat='" + this.rowFormat + '\'' +
                ", tableRows=" + this.tableRows +
                ", averageRowLength=" + this.averageRowLength +
                ", dataLength=" + this.dataLength +
                ", maxDataLength=" + this.maxDataLength +
                ", indexLength=" + this.indexLength +
                ", dataFree=" + this.dataFree +
                ", autoIncrement='" + this.autoIncrement + '\'' +
                ", createTime='" + this.createTime + '\'' +
                ", updateTime='" + this.updateTime + '\'' +
                ", checkTime='" + this.checkTime + '\'' +
                ", tableCollation='" + this.tableCollation + '\'' +
                ", checksum='" + this.checksum + '\'' +
                ", createOptions='" + this.createOptions + '\'' +
                ", tableComment='" + this.tableComment + '\'' +
                '}';
    }

}