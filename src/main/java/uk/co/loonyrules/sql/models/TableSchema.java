package uk.co.loonyrules.sql.models;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class TableSchema implements Iterable<TableColumn> {

    private final String
            database,
            table;

    private final List<TableColumn> columns;

    public TableSchema(String database, String table, TableColumn... tableColumns)
    {
        this(database, table, Arrays.asList(tableColumns));
    }

    public TableSchema(String database, String table, List<TableColumn> tableColumns)
    {
        this.database = database;
        this.table = table;
        this.columns = tableColumns;
    }

    public String getDatabase()
    {
        return database;
    }

    public String getTable()
    {
        return table;
    }

    public List<TableColumn> getColumns()
    {
        return columns;
    }

    public List<String> getColumnNames()
    {
        return getColumns().stream().map(TableColumn::getField).collect(Collectors.toList());
    }

    public boolean isEmpty()
    {
        return columns.isEmpty();
    }

    @Override
    public Iterator<TableColumn> iterator()
    {
        return Iterators.filter(columns.iterator(), Predicates.notNull());
    }

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