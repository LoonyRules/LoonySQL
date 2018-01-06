package uk.co.loonyrules.sql.models;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TableSchema that's used for storing "EXPLAIN" and "DESCRIBE" query data
 */
public class TableSchema implements Iterable<TableColumn>
{

    private final String
            database,
            table;

    private final List<TableColumn> columns;

    /**
     * Initialise a new TableSchema instance
     * @param database this data is coming from
     * @param table this data is coming from
     * @param tableColumns that this table has
     */
    public TableSchema(String database, String table, TableColumn... tableColumns)
    {
        this(database, table, Arrays.asList(tableColumns));
    }

    /**
     * Initialise a new TableSchema instance
     * @param database this data is coming from
     * @param table this data is coming from
     * @param tableColumns that this table has
     */
    public TableSchema(String database, String table, List<TableColumn> tableColumns)
    {
        this.database = database;
        this.table = table;
        this.columns = tableColumns;
    }

    /**
     * Get the database name
     * @return database name
     */
    public String getDatabase()
    {
        return database;
    }

    /**
     * Get the table name
     * @return table name
     */
    public String getTable()
    {
        return table;
    }

    /**
     * Get all {@link TableColumn}'s for this Table
     * @return all known TableColumns
     */
    public List<TableColumn> getColumns()
    {
        return columns;
    }

    /**
     * Get a list of the column names
     * @return column names
     */
    public List<String> getColumnNames()
    {
        return getColumns().stream().map(TableColumn::getField).collect(Collectors.toList());
    }

    /**
     * Check if there's any columns retrieved
     * @return if any columns were found
     */
    public boolean isEmpty()
    {
        return columns.isEmpty();
    }

    /**
     * Iterate through all the known column data
     * @return all known column data
     */
    @Override
    public Iterator<TableColumn> iterator()

    {
        return Iterators.filter(columns.iterator(), Predicates.notNull());
    }

    /**
     * TableSchema represented as a String
     * @return current instance as a String
     */
    @Override
    public String toString()
    {
        return "TableSchema{" +
                "database='" + database + '\'' +
                ", table='" + table + '\'' +
                ", columns=" + columns +
                '}';
    }

}