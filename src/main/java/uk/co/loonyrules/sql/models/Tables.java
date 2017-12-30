package uk.co.loonyrules.sql.models;

import uk.co.loonyrules.sql.annotations.Column;
import uk.co.loonyrules.sql.annotations.Table;

/**
 * InformationSchema Table model
 */
@Table(name = "information_schema.TABLES")
public class Tables
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

    public Tables()
    {

    }

    /**
     * Get the Table Catalog
     * @return table catalog
     */
    public String getTableCatalog()
    {
        return tableCatalog;
    }

    /**
     * Get the Table Schema (the database name)
     * @return table schema
     */
    public String getTableSchema()
    {
        return tableSchema;
    }

    /**
     * Get the Table name
     * @return table name
     */
    public String getTableName()
    {
        return tableName;
    }

    /**
     * Get the Table Type
     * @return table type
     */
    public String getTableType()
    {
        return tableType;
    }

    /**
     * Get the Engine
     * @return engine for the Table
     */
    public String getEngine()
    {
        return engine;
    }

    /**
     * Get the version
     * @return version
     */
    public int getVersion()
    {
        return version;
    }

    /**
     * Get the row format
     * @return row format
     */
    public String getRowFormat()
    {
        return rowFormat;
    }

    /**
     * Get the number of rows for the Table
     * @return number of rows
     */
    public long getTableRows()
    {
        return tableRows;
    }

    /**
     * Get the average row length
     * @return average row length
     */
    public long getAverageRowLength()
    {
        return averageRowLength;
    }

    /**
     * Get the total length of data
     * @return total length of data
     */
    public long getDataLength()
    {
        return dataLength;
    }

    /**
     * Get the maximum data length
     * @return maximum data length
     */
    public long getMaxDataLength()
    {
        return maxDataLength;
    }

    /**
     * Get the index length
     * @return index length
     */
    public int getIndexLength()
    {
        return indexLength;
    }

    /**
     * Get amount of data free
     * @return amount of data free
     */
    public long getDataFree()
    {
        return dataFree;
    }

    /**
     * Get auto increment next ID
     * @return auto increment next ID
     */
    public String getAutoIncrement()
    {
        return autoIncrement;
    }

    /**
     * Get the create time (TODO: Convert to SQL date)
     * @return creation time
     */
    public String getCreateTime()
    {
        return createTime;
    }

    /**
     * Get the last update time (TODO: Convert to SQL date)
     * @return last update time
     */
    public String getUpdateTime()
    {
        return updateTime;
    }

    /**
     * Get the last check time (TODO: Convert to SQL date)
     * @return last check time
     */
    public String getCheckTime()
    {
        return checkTime;
    }

    /**
     * Get the Table collation
     * @return table collation
     */
    public String getTableCollation()
    {
        return tableCollation;
    }

    /**
     * Get the checksum for this table
     * @return checksum
     */
    public String getChecksum()
    {
        return checksum;
    }

    /**
     * Get the Create Options (TODO: Ensure this is the correct var type)
     * @return create options
     */
    public String getCreateOptions()
    {
        return createOptions;
    }

    /**
     * Get the comment made for this table
     * @return comment for the table, if any
     */
    public String getTableComment()
    {
        return tableComment;
    }

    /**
     * Tables object as a String
     * @return object as a String
     */
    @Override
    public String toString() {
        return "Tables{" +
                "tableCatalog='" + tableCatalog + '\'' +
                ", tableSchema='" + tableSchema + '\'' +
                ", tableName='" + tableName + '\'' +
                ", tableType='" + tableType + '\'' +
                ", engine='" + engine + '\'' +
                ", version=" + version +
                ", rowFormat='" + rowFormat + '\'' +
                ", tableRows=" + tableRows +
                ", averageRowLength=" + averageRowLength +
                ", dataLength=" + dataLength +
                ", maxDataLength=" + maxDataLength +
                ", indexLength=" + indexLength +
                ", dataFree=" + dataFree +
                ", autoIncrement='" + autoIncrement + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", checkTime='" + checkTime + '\'' +
                ", tableCollation='" + tableCollation + '\'' +
                ", checksum='" + checksum + '\'' +
                ", createOptions='" + createOptions + '\'' +
                ", tableComment='" + tableComment + '\'' +
                '}';
    }

}