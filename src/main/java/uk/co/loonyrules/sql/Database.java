package uk.co.loonyrules.sql;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javafx.util.Pair;
import uk.co.loonyrules.sql.annotations.Column;
import uk.co.loonyrules.sql.annotations.Primary;
import uk.co.loonyrules.sql.annotations.Table;
import uk.co.loonyrules.sql.codecs.Codec;
import uk.co.loonyrules.sql.enums.ModifyType;
import uk.co.loonyrules.sql.models.TableColumn;
import uk.co.loonyrules.sql.models.TableInfo;
import uk.co.loonyrules.sql.models.TableSchema;
import uk.co.loonyrules.sql.utils.StorageUtil;
import uk.co.loonyrules.sql.utils.ReflectionUtil;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * The main class that allows you to manage the entire Database
 * as the authenticated {@link Credentials} user.
 */
public class Database
{

    private final Credentials credentials;

    private HikariDataSource hikariDataSource;
    private ExecutorService executorService;
    private Thread shutdownThread;

    /**
     * Initialise a new Database connection using a set of {@link Credentials}
     * @param credentials to connect and authenticate with
     */
    public Database(Credentials credentials)
    {
        // Ensuring the Credentials object isn't instance
        Preconditions.checkNotNull(credentials, "Credentials cannot be null.");

        // Assigning the Credentials variable
        this.credentials = credentials;
    }

    /**
     * Get the {@link HikariDataSource} for this Database
     * @return {@link HikariDataSource} this Database uses
     */
    public HikariDataSource getHikariDataSource()
    {
        return hikariDataSource;
    }

    /**
     * Get the {@link ExecutorService} used for asynchronous query calls
     * @return {@link ExecutorService} for this Database
     */
    public ExecutorService getExecutorService()
    {
        return executorService;
    }

    /**
     * Get a new {@link Connection} from the {@link HikariDataSource}
     * @return A new {@link Connection} from the {@link HikariDataSource}
     * @throws SQLException if an error is encountered
     */
    public Connection getConnection() throws SQLException
    {
        // Ensuring we're connected before retrieving a Connection
        Preconditions.checkArgument(isConnected(), "Connection hasn't been initialised.");

        // Return a new Connection
        return hikariDataSource.getConnection();
    }

    /**
     * Check if the {@link HikariDataSource} is closed
     * @return if the {@link HikariDataSource} is closed
     */
    public boolean isConnected()
    {
        return hikariDataSource != null && !hikariDataSource.isClosed();
    }

    /**
     * Attempt to connect using the {@link Credentials} given.
     */
    public void connect()
    {
        // Ensuring we're not already connected
        Preconditions.checkArgument(!isConnected(), "Already connected to the Database.");

        // Initialise our HikariConfig
        HikariConfig hikariConfig = new HikariConfig();

        // Setting the dataSource's class name
        hikariConfig.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");

        // Setting the Jdbc url
        hikariConfig.setJdbcUrl(String.format("jdbc:mysql//%s:%s/%s", credentials.getHost(), credentials.getPort(), credentials.getDatabase()));

        // Adding our databaseName as a property
        hikariConfig.addDataSourceProperty("databaseName", credentials.getDatabase());

        // Setting the authentication credentials
        hikariConfig.setUsername(credentials.getUsername());
        hikariConfig.setPassword(credentials.getPassword());

        // Setting our timeout
        hikariConfig.setConnectionTimeout(credentials.getTimeout());

        // Initialising the Data Source
        hikariDataSource = new HikariDataSource(hikariConfig);

        // Creating our executor for this Database
        executorService = Executors.newCachedThreadPool();

        // Add a shutdown hook
        Runtime.getRuntime().addShutdownHook(shutdownThread = new Thread(this::disconnect));
    }

    /**
     * Attempt to close the {@link Connection} and the {@link ExecutorService}
     */
    public void disconnect()
    {
        // Ensuring we're connected
        Preconditions.checkArgument(isConnected(), "Database connection has already been disconnected.");

        // Closing the hikariDataSource
        hikariDataSource.close();

        // Shutting down the pool
        executorService.shutdown();

        // If shutdownThread is active
        if(shutdownThread == null || !shutdownThread.isAlive())
            return;

        try {
            // Removing our ShutdownHook
            Runtime.getRuntime().removeShutdownHook(shutdownThread);
            shutdownThread = null;
        } catch(IllegalStateException e) {

        }
    }

    /**
     * Find the first row and parse to the object provided
     * @param clazz to get data for
     * @param <T> the type to parse to
     * @return first found result wrapped in an Optional
     */
    public <T> Optional<T> findFirst(Class<T> clazz)
    {
        return findFirst(clazz, new Query());
    }

    /**
     * Find the first row and parse to the object provided
     * @param clazz to get data for
     * @param <T> the type to parse to
     * @param query filter for the query
     * @return first found result wrapped in an Optional
     */
    public <T> Optional<T> findFirst(Class<T> clazz, Query query)
    {
        // Find results associated with the current Query but limit the results
        List<T> results = find(clazz, query.limit(query.getSkip() + 1));

        // Return the found data
        return Optional.ofNullable(results.iterator().hasNext() ? results.iterator().next() : null);
    }

    /**
     * Find all rows and get back a list of the object provided
     * @param clazz to get data for
     * @param <T> the type to parse to
     * @return all found results
     */
    public <T> List<T> find(Class<T> clazz)
    {
        return find(clazz, new Query());
    }

    /**
     * Delete a row specified with an Object that has a @Primary @Column Field
     * @param object to generate Query off of for deletion
     * @return number of rows deleted
     */
    public <T> List<T> find(Object object)
    {
        return find((Class<T>) object.getClass(), Query.generatePrimary(object));
    }


    /**
     * Find all rows and get back a list of the object provided
     * @param clazz to get data for
     * @param query filter for the query
     * @param <T> the type to parse to
     * @return all found results
     */
    public <T> List<T> find(Class<T> clazz, Query query)
    {
        // Where we'll store our Results
        List<T> results = Lists.newArrayList();

        // Get the Table annotation wrapped in an Optional
        Optional<Table> tableOptional = ReflectionUtil.getTableAnnotation(clazz);

        // Not found so throw an error
        Preconditions.checkArgument(tableOptional.isPresent(), "@Table annotation not found for " + clazz + " when find results.");

        // Get the Table annotation
        Table table = tableOptional.get();

        // Our SQL objects used
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        // Wrapping in a SQLException try and catch
        try {
            // Get a new connection
            connection = getConnection();

            // Preparing our statement
            preparedStatement = prepare(connection, String.format("SELECT * FROM %s %s", table.name(), query.toString()), query.getWheres().values().toArray());

            // Execute our PreparedStatement
            resultSet = preparedStatement.executeQuery();

            // Whilst there's results, parse and add to the results
            while (resultSet.next())
            {
                try {
                    // Create a new instance for this class
                    T instance = clazz.newInstance();

                    // Attempt to parse the class
                    populate(instance, resultSet);

                    // Add to the results
                    results.add(instance);
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            // Print the stacktrace
            e.printStackTrace();
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }

        // Return our results
        return results;
    }

    /**
     * Finds all rows associated with the clazz @Table data and deletes them
     * @param clazz to get data for
     * @return number of rows deleted
     */
    public long delete(Class<?> clazz)
    {
        return delete(clazz, new Query());
    }

    /**
     * Delete a row specified with an Object that has a @Primary @Column Field
     * @param object to generate Query off of for deletion
     * @return number of rows deleted
     */
    public long delete(Object object)
    {
        // Delete via an auto-generated Query using a @Primary Field
        return delete(object.getClass(), Query.generatePrimary(object));
    }

    /**
     * Finds all rows associated with the clazz @Table data and deletes them
     * @param clazz to get data for
     * @param query filter for the query
     * @return number of rows deleted
     */
    public long delete(Class<?> clazz, Query query)
    {
        // Number of rows deleted
        long deletedCount = 0;

        // Get the Table annotation wrapped in an Optional
        Optional<Table> tableOptional = ReflectionUtil.getTableAnnotation(clazz);

        // Not found so throw an error
        Preconditions.checkArgument(tableOptional.isPresent(), "@Table annotation not found for " + clazz + " when deleting results.");

        // Get the Table annotation
        Table table = tableOptional.get();

        // Our SQL objects used
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        // Wrapping in a SQLException try and catch
        try {
            // Get a new connection
            connection = getConnection();

            // Preparing our statement
            preparedStatement = prepare(connection, String.format("DELETE FROM %s %s", table.name(), query.toString()), query.getWheres().values().toArray());

            // Execute our PreparedStatement
            deletedCount = preparedStatement.executeLargeUpdate();
        } catch (SQLException e) {
            // Print the stacktrace
            e.printStackTrace();
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }

        // Return our results
        return deletedCount;
    }

    public TableSchema describe(Class<?> clazz)
    {
        // Get the Table annotation
        Optional<Table> tableOptional = ReflectionUtil.getTableAnnotation(clazz);

        // Not found so throw an error
        Preconditions.checkArgument(tableOptional.isPresent(), "@Table annotation not found for " + clazz + " when describing Table.");

        // Get the Table annotation
        Table table = tableOptional.get();

        // MySQL related data
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        // Data to return
        List<TableColumn> columns = Lists.newArrayList();

        try {
            // Get a new Connection
            connection = getConnection();

            // Preparing our statement
            preparedStatement = connection.prepareStatement("DESCRIBE `" + table.name() + "`");

            // Execute our query
            resultSet = preparedStatement.executeQuery();

            // While we have results...
            while (resultSet.next())
            {
                // Our new TableColumn instance to populate
                TableColumn tableColumn = new TableColumn();

                // Populate our Object
                populate(tableColumn, resultSet);

                // Adding to the List
                columns.add(tableColumn);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            // Close our resources
            closeResources(connection, preparedStatement, resultSet);
        }

        // Returning our TableSchema
        return new TableSchema(credentials.getDatabase(), table.name(), columns);
    }


    /**
     * Verify a {@link uk.co.loonyrules.sql.annotations.Table}'s data on the MySQL server.
     * This will create the table if it doesn't exist, or modify the table depending on the {@link uk.co.loonyrules.sql.enums.ModifyType}
     * @param clazz to verify table integrity for
     * @return whether any data was modified or not.
     */
    public boolean updateTable(Class<?> clazz)
    {
        // Whether anything was modified or not
        boolean modified = false;

        // Get the Table annotation
        Optional<Table> tableOptional = ReflectionUtil.getTableAnnotation(clazz);

        // Not found so throw an error
        Preconditions.checkArgument(tableOptional.isPresent(), "@Table annotation not found for " + clazz + " when updating Table.");

        // Get the Table annotation
        Table table = tableOptional.get();

        // We're not allowed to do any creation or modifications
        if(!table.create() && table.modifyType() == ModifyType.NONE)
            throw new IllegalArgumentException(String.valueOf("Attempted to update @Table for " + clazz + " even though the properties say we can't."));

        // Get our InformationSchema from our search
        List<TableInfo> results = find(TableInfo.class, new Query()
                .where("TABLE_SCHEMA", credentials.getDatabase())
                .where("TABLE_NAME", ReflectionUtil.getTableName(clazz))
                .limit(1));

        // MySQL data
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        // No results so lets Create the Table
        if(results.isEmpty())
        {
            // Get a new Connection
            try {
                // Get a new Connection
                connection = getConnection();

                // Prepare a new statement
                preparedStatement = prepareCreate(connection, clazz);

                // Execute the prepared statement
                modified = preparedStatement.execute();
            } catch(SQLException e) {
                // Print the stacktrace
                e.printStackTrace();

                // Nothing was modified because an error
                modified = false;
            } finally {
                // Closing our resources
                closeResources(connection, preparedStatement);
            }

            // Returning the modified state
            return modified;
        }

        // ModifyType is NONE so we'll just ignore
        if(table.modifyType() == ModifyType.NONE)
            return false;

        // Get our TableSchema result
        TableSchema tableSchema = describe(clazz);

        // No data found, so do nothing
        if(tableSchema.isEmpty())
            return false;

        // Get Column names
        List<String> schemaColumnNames = tableSchema.getColumnNames();

        // Get our Column names for our Class
        List<String> classColumnNames = ReflectionUtil.getColumnNames(clazz);

        // Columns to add
        List<String> toAdd = (table.modifyType() == ModifyType.ADD || table.modifyType() == ModifyType.ADD_REMOVE ? classColumnNames.stream().filter(name -> !schemaColumnNames.contains(name)).collect(Collectors.toList()) : Lists.newArrayList());

        // Columns to remove
        List<String> toRemove = (table.modifyType() == ModifyType.REMOVE || table.modifyType() == ModifyType.ADD_REMOVE ? schemaColumnNames.stream().filter(name -> !classColumnNames.contains(name)).collect(Collectors.toList()) : Lists.newArrayList());

        // Query stuff
        try {
            // Get a new Connection
            connection = getConnection();

            // Prepare our alteration query
            preparedStatement = prepareAlter(connection, clazz, toAdd, toRemove);

            // Execute update
            modified = preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(connection, preparedStatement);
        }

        // Return the modified variable
        return modified;
    }

    /**
     * Reload an @Table object to get new data
     * @param object the object to reload data for
     */
    public void reload(Object object)
    {
        reload(object, Query.generatePrimary(object));
    }

    /**
     * Reload an @Table object to get new data
     * @param object to reload data for
     * @param query query to execute to get the row data
     */
    public void reload(Object object, Query query)
    {
        // Get the Table annotation wrapped in an Optional
        Optional<Table> tableOptional = ReflectionUtil.getTableAnnotation(object.getClass());

        // Not found so throw an error
        Preconditions.checkArgument(tableOptional.isPresent(), "@Table annotation not found for " + object.getClass() + " when reloading.");

        // Get the Table annotation
        Table table = tableOptional.get();

        // Limit our response to 1
        query.limit(1);

        // Our SQL objects used
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        // Wrapping in a SQLException try and catch
        try {
            // Get a new connection
            connection = getConnection();

            // Preparing our statement
            preparedStatement = prepare(connection, String.format("SELECT * FROM %s %s", table.name(), query.toString()), query.getWheres().values().toArray());

            // Execute our PreparedStatement
            resultSet = preparedStatement.executeQuery();

            // If there's results then populate else throw error
            if (resultSet.next())
                populate(object, resultSet);
            else throw new NullPointerException("Couldn't find row matching Query for " + object);
        } catch (SQLException e) {
            // Print the stacktrace
            e.printStackTrace();
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }
    }

    /**
     * Save an Object with a @Table annotation
     * @param object to save
     */
    public void save(Object object)
    {
        // Get the Table annotation wrapped in an Optional
        Optional<Table> tableOptional = ReflectionUtil.getTableAnnotation(object.getClass());

        // Not found so throw an error
        Preconditions.checkArgument(tableOptional.isPresent(), "@Table annotation not found for " + object.getClass() + " when saving.");

        // Get the Table annotation
        Table table = tableOptional.get();

        // Get the Primary Field and the true name of the Primary Column
        Optional<Field> primaryOptional = ReflectionUtil.getPrimaryField(object.getClass());
        String primaryName = primaryOptional.isPresent() ? ReflectionUtil.getColumnName(primaryOptional.get()) : null;

        // Generating our Query objects
        Query query = Query.from(object);

        // Our SQL objects used
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            // Get a new Connection
            connection = getConnection();

            // Prepare our query string
            String queryString = String.format(
                    /* Our query string with formatting */
                    "INSERT INTO `%s` (%s) VALUES (%s) ON DUPLICATE KEY UPDATE %s",
                    /* Table name */
                    table.name(),
                    /* Get the column names */
                    query.getWheresAsColumns(),
                    /* Combining stuff */
                    query.getWheresAsPlaceholders(),
                    /* Get our placeholders for our UPDATE formatting */
                    query.buildConditionPlaceholders()
            );

            // Prepare our query
            preparedStatement = prepare(
                    connection,
                    queryString,
                    StorageUtil.combine(
                            query.getWheres().values().toArray(),
                            query.getWheres().values().toArray()
                    )
            );

            // Execute the statement
            preparedStatement.execute();

            // If Primary is known,
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            // Close the resources we've used.
            closeResources(connection, preparedStatement);
        }
    }

    /**
     * Close MySQL resources used
     * @param connection used in a query
     */
    public void closeResources(Connection connection)
    {
        closeResources(connection, null);
    }

    /**
     * Close MySQL resources used
     * @param connection used in a query
     * @param preparedStatement used in a query
     */
    public void closeResources(Connection connection, PreparedStatement preparedStatement)
    {
        closeResources(connection, preparedStatement, null);
    }

    /**
     * Close MySQL resources used
     * @param connection used in a query
     * @param preparedStatement used in a query
     * @param resultSet used in a query
     */
    public void closeResources(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet)
    {
        try {
            // Closing our Connection
            if(connection != null && !connection.isClosed())
                connection.close();

            // Closing our PreparedStatement
            if(preparedStatement != null && !preparedStatement.isClosed())
                preparedStatement.close();

            // Closing our ResultSet
            if(resultSet != null && !resultSet.isClosed())
                resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Generate a PreparedStatement with specified data
     * @return the generated PreparedStatement
     */
    private PreparedStatement prepare(Connection connection, String statement, Object[] data) throws SQLException
    {
        // Prepare our PreparedStatement
        PreparedStatement preparedStatement = connection.prepareStatement(statement);

        // Iterate through the data
        for(int i = 1; i <= data.length; i++)
        {
            // Get the current Field
            Object object = data[i - 1];

            // Get the Codec for this Type
            Codec codec = Codec.getCodec(object.getClass());

            // Not known so skip
            if(codec == null)
                continue;

            // Encode the data
            codec.encode(preparedStatement, i, object);
        }

        // Return our statement
        return preparedStatement;
    }

    /**
     * Generate a PreparedStatement for creating a Table
     * @param connection to create the PreparedStatement from
     * @param clazz to create the Table from
     * @return the generated PreparedStatement
     * @throws SQLException if an error occurs
     */
    private PreparedStatement prepareCreate(Connection connection, Class<?> clazz) throws SQLException
    {
        // Get the Table annotation
        Optional<Table> tableOptional = ReflectionUtil.getTableAnnotation(clazz);

        // Not found so throw an error
        Preconditions.checkArgument(tableOptional.isPresent(), "@Table annotation not found for " + clazz + " when preparing Table creation.");

        // Get the Table annotation
        Table table = tableOptional.get();

        // Get the Field's for this Class
        Map<String, Field> fields = ReflectionUtil.getFields(clazz);

        // Where our query string data will be stored
        StringBuilder query = new StringBuilder();

        // Our Primary Field
        Field primaryField = null;

        // Iterate through all Field'entry
        for (Map.Entry<String, Field> entry : fields.entrySet())
        {
            // The Field in question
            Field field = entry.getValue();

            // The name of the Column in the Table
            Column column = ReflectionUtil.getColumnAnnotation(field).get();
            String columnName =  ReflectionUtil.getColumnName(entry.getValue());

            // Get the Codec for this Field
            Codec codec = Codec.getCodec(field.getType());

            // No Codec known, skip!
            if(codec == null)
                continue;

            // Append the data for this Column
            query
                    .append("`")
                    .append(columnName)
                    .append("` ")
                    .append(codec.getSQLType())
                    .append("(")
                    .append(codec.calculateMaxLength(column.maxLength()))
                    .append("), ");

            // If it's the Primary
            Primary primary = field.getAnnotation(Primary.class);

            // Not Primary annotation so ignore
            if(primary == null)
                continue;

            // Ensuring there's not a double Primary annotation
            if(primaryField != null)
                throw new IllegalArgumentException("Found 2 @Primary annotations in " + clazz);

            // Assign primary field
            primaryField = field;

            // Not an auto increment Primary so don't append Query data
            if(!primary.autoIncrement())
                continue;

            // Appending our query data
            query.setLength(query.length() - 2);
            query.append(" AUTO_INCREMENT, ");
        }

        // There was a Primary Field so append some more data
        if(primaryField == null)
            query.setLength(query.length() - 2);
        else query.append("PRIMARY KEY (`").append(ReflectionUtil.getColumnName(primaryField)).append("`)");

        // Return our PreparedStatement
        return connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS `%s` (%s) ENGINE=InnoDB DEFAULT CHARSET=utf8", table.name(), query.toString()));
    }

    /**
     * Generate a PreparedStatement for altering an @Table
     * @param connection to create the PreparedStatement from
     * @param clazz to get the table data for
     * @param toAdd columns to add
     * @param toRemove columns to remove
     * @return generated PreparedStatement
     */
    private PreparedStatement prepareAlter(Connection connection, Class<?> clazz, List<String> toAdd, List<String> toRemove) throws SQLException
    {
        // Get the Table annotation
        Optional<Table> tableOptional = ReflectionUtil.getTableAnnotation(clazz);

        // Not found so throw an error
        Preconditions.checkArgument(tableOptional.isPresent(), "@Table annotation not found for " + clazz + " when preparing Table creation.");

        // Get the Table annotation
        Table table = tableOptional.get();

        // Where our query string data will be stored
        StringBuilder query = new StringBuilder();

        // Iterate through all Field'entry
        for (Map.Entry<String, Field> entry : ReflectionUtil.getFields(clazz).entrySet())
        {
            // The Field in question
            Field field = entry.getValue();

            // The name of the Column in the Table
            Column column = ReflectionUtil.getColumnAnnotation(field).get();
            String columnName =  ReflectionUtil.getColumnName(entry.getValue());

            // Doesn't need to be added
            if(!toAdd.contains(columnName))
                continue;

            // Get the Codec for this Field
            Codec codec = Codec.getCodec(field.getType());

            // No Codec known, skip!
            if(codec == null)
                continue;

            // Appending onto our Column
            query
                    .append("ADD COLUMN `")
                    .append(columnName)
                    .append("` ")
                    .append(codec.getSQLType())
                    .append("(")
                    .append(codec.calculateMaxLength(column.maxLength()))
                    .append("), ");
        }

        // Appending DROP data to Query
        for(String removeColumnName : toRemove)
        {
            query
                    .append("DROP ")
                    .append(removeColumnName)
                    .append(", ");
        }

        // Removing trailing comma
        if(query.length() > 2)
            query.setLength(query.length() - 2);

        // Return our prepared statement
        return connection.prepareStatement(String.format("ALTER TABLE `%s` %s", table.name(), query.toString()));
    }

    /**
     * Populate an Object with data from a ResultSet
     * @param object to populate the data into
     * @param resultSet where our data is
     */
    private void populate(Object object, ResultSet resultSet)
    {
        // Getting the Fields for this object
        Map<String, Field> fields = ReflectionUtil.getFields(object.getClass());

        // No known fields so just return
        if(fields == null || fields.isEmpty())
            return;

        // Iterate through ResultSet data as a Map
        for(Map.Entry<String, Object> entry : StorageUtil.toMap(resultSet).entrySet())
        {
            // Name of the Column
            String column = entry.getKey();

            // Get the Field associated with the Column name
            Optional<Field> fieldOptional = ReflectionUtil.getColumnField(fields, column);

            // No Field found for this Column name
            if(!fieldOptional.isPresent())
                continue;

            // The Field for this data
            Field field = fieldOptional.get();

            // Get the Codec for this type
            Codec codec = Codec.getCodec(field.getType());

            // No Codec
            if(codec == null)
                continue;

            // Assigning the field's value with the decoded data
            try {
                field.set(object, codec.decode(resultSet, field.getType(), column));
            } catch (IllegalAccessException | SQLException e) {
                // TODO: LoggerFactory
                System.out.println("Error occurred when decoding " + field);
                e.printStackTrace();
            }
        }
    }

}