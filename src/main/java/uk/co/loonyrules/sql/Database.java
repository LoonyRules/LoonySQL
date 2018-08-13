package uk.co.loonyrules.sql;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import uk.co.loonyrules.sql.annotations.Column;
import uk.co.loonyrules.sql.annotations.Primary;
import uk.co.loonyrules.sql.annotations.Table;
import uk.co.loonyrules.sql.codecs.Codec;
import uk.co.loonyrules.sql.enums.ModifyType;
import uk.co.loonyrules.sql.models.TableColumn;
import uk.co.loonyrules.sql.models.TableInfo;
import uk.co.loonyrules.sql.models.TableSchema;
import uk.co.loonyrules.sql.utils.ParseUtil;
import uk.co.loonyrules.sql.utils.ReflectionUtil;
import uk.co.loonyrules.sql.utils.StorageUtil;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The main class that allows you to manage the entire Database
 * as the authenticated {@link Credentials} user.
 */
public class Database
{

    private final Credentials credentials;
    private final Map<String, String> tablePlaceholders = Maps.newHashMap();

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
        return this.hikariDataSource;
    }

    /**
     * Get the {@link ExecutorService} used for asynchronous query calls
     * @return {@link ExecutorService} for this Database
     */
    public ExecutorService getExecutorService()
    {
        return this.executorService;
    }

    /**
     * Get @Table name placeholders
     * @return all registered @Table name placeholders
     */
    public Map<String, String> getTablePlaceholders()
    {
        return this.tablePlaceholders;
    }

    /**
     * Get the placeholder value for an @Table name
     * @param placeholder to get the value for
     * @return the placeholder value wrapped in an Optional
     */
    public Optional<String> getTablePlaceholder(String placeholder)
    {
        return Optional.ofNullable(this.tablePlaceholders.get(placeholder));
    }

    /**
     * Get an @Table name placeholder
     * @param placeholder to get the value for
     * @param defaultValue data to return if not found
     * @return the name of the placeholder value or the defaultValue if not found
     */
    public String getTablePlaceholder(String placeholder, String defaultValue)
    {
        return getTablePlaceholder(placeholder).orElse(defaultValue);
    }

    /**
     * Replace a string with the placeholder value data if found
     * @param tableName to search for placeholders
     * @return the final string
     */
    public String replaceTableNamePlaceholders(String tableName)
    {
        // Iterating through all @Table name placeholders and replacing strings
        for(Map.Entry<String, String> entry : getTablePlaceholders().entrySet())
            tableName = tableName.replace(String.format("{{%s}}", entry.getKey()), entry.getValue());

        // Returning our table name
        return tableName;
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
        return this.hikariDataSource.getConnection();
    }

    /**
     * Check if the {@link HikariDataSource} is closed
     * @return if the {@link HikariDataSource} is closed
     */
    public boolean isConnected()
    {
        return this.hikariDataSource != null && !this.hikariDataSource.isClosed();
    }

    /**
     * Check if a @Table name placeholder is registered
     * @param placeholder to check for
     * @return whether or not it's registered
     */
    public boolean isTablePlaceholder(String placeholder)
    {
        return getTablePlaceholder(placeholder).isPresent();
    }

    /**
     * Insert (or overwrite existing) a @Table name placeholder
     * @param placeholder to replace with the value
     * @param value to replace the placeholder
     */
    public void addTablePlaceholder(String placeholder, String value)
    {
        this.tablePlaceholders.put(placeholder, value);
    }

    /**
     * Remove an existing @Table name placeholder
     * @param placeholder to remove
     */
    public void removeTablePlaceholder(String placeholder)
    {
        this.tablePlaceholders.remove(placeholder);
    }

    /**
     * Run something on the ExecutorService created for asynchronous executions
     * @param consumer to accept on async thread
     */
    public void runAsync(Consumer<Database> consumer)
    {
        this.executorService.execute(() -> consumer.accept(this));
    }

    /**
     * Attempt to connect using the {@link Credentials} given.
     */
    public void connect()
    {
        // Ensuring we're not already connected
        Preconditions.checkArgument(!isConnected(), "Already connected to the Database.");

        // Initialise our HikariConfig
        final HikariConfig hikariConfig = new HikariConfig();

        // Setting the driver class name
        if(this.credentials.getDriverClass() != null)
            hikariConfig.setDriverClassName(this.credentials.getDriverClass());

        // Setting the maximum pool size
        hikariConfig.setMaximumPoolSize(this.credentials.getMaximumPoolSize());

        // Setting the Jdbc url (TODO: Make TimeZone ID configurable?)
        hikariConfig.setJdbcUrl(String.format(
                "jdbc:mysql://%s:%s/%s?useLegacyDatetimeCode=false&serverTimezone=%s",
                this.credentials.getHost(),
                this.credentials.getPort(),
                this.credentials.getDatabase(),
                TimeZone.getDefault().getID()
        ));

        // Adding our databaseName as a property
        hikariConfig.addDataSourceProperty("databaseName", this.credentials.getDatabase());

        // Adding our encoding type as a property
        hikariConfig.addDataSourceProperty("characterEncoding", this.credentials.getEncoding());
        hikariConfig.addDataSourceProperty("collationConnection", this.credentials.getCollation());
        hikariConfig.addDataSourceProperty("useUnicode","true");

        hikariConfig.setConnectionInitSql("SET NAMES utf8mb4");

        // Setting the authentication credentials
        hikariConfig.setUsername(this.credentials.getUsername());
        hikariConfig.setPassword(this.credentials.getPassword());

        // Initialising the Data Source
        this.hikariDataSource = new HikariDataSource(hikariConfig);

        try {
            // Setting the Login Timeout
            this.hikariDataSource.setLoginTimeout(this.credentials.getTimeout());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Creating our executor for this Database
        this.executorService = Executors.newCachedThreadPool();

        // Add a shutdown hook
        Runtime.getRuntime().addShutdownHook(this.shutdownThread = new Thread(this::disconnect));
    }

    /**
     * Attempt to close the {@link Connection} and the {@link ExecutorService}
     */
    public void disconnect()
    {
        // Ensuring we're connected
        Preconditions.checkArgument(isConnected(), "Database connection has already been disconnected.");

        // Closing the hikariDataSource
        this.hikariDataSource.close();

        // Shutting down the pool
        this.executorService.shutdown();

        // If shutdownThread is active
        if(this.shutdownThread == null || !this.shutdownThread.isAlive())
            return;

        try {
            // Removing our ShutdownHook
            Runtime.getRuntime().removeShutdownHook(this.shutdownThread);
        } catch(IllegalStateException e) {
            // TODO: Do we want this to print?
        } finally {
            // Uninit our thread variable
            this.shutdownThread = null;
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
        final List<T> results = find(clazz, query.limit(query.getSkip() + 1));

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
        final List<T> results = Lists.newArrayList();

        // Get the Table annotation wrapped in an Optional
        final Optional<Table> tableOptional = ReflectionUtil.getTableAnnotation(clazz);

        // Not found so throw an error
        Preconditions.checkArgument(tableOptional.isPresent(), "@Table annotation not found for " + clazz + " when find results.");

        // Get the Table annotation
        final Table table = tableOptional.get();

        // Our SQL objects used
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        // Wrapping in a SQLException try and catch
        try {
            // Get a new connection
            connection = getConnection();

            // Preparing our statement
            preparedStatement = prepare(connection, String.format("SELECT * FROM %s %s", replaceTableNamePlaceholders(table.name()), query.toString()), query.getWheres().values().toArray());

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
     * Count the number of rows with the class {@Table} data
     *
     * @param clazz to count rows for
     * @return number of rows founc
     */
    public long count(Class<?> clazz)
    {
        // Get the Table annotation wrapped in an Optional
        final Optional<Table> tableOptional = ReflectionUtil.getTableAnnotation(clazz);

        // Not found so throw an error
        Preconditions.checkArgument(tableOptional.isPresent(), "@Table annotation not found for " + clazz + " when counting results.");

        // Get the Table annotation
        final Table table = tableOptional.get();

        // Our SQL objects used
        final Optional<TableInfo> tableInfoOptional = findFirst(TableInfo.class, new Query().where("TABLE_NAME", replaceTableNamePlaceholders(table.name())));

        // Return number of Table Rows found or -1 if we didn't find a Table with the name
        return tableInfoOptional.map(TableInfo::getTableRows).orElse(-1L);
    }

    /**
     * Count the number of rows for a {@link Class} that matches a {@link Query}
     * @param clazz to check for
     * @param query filter for this query
     * @return number of rows counted after filtering
     */
    public long count(Class<?> clazz, Query query)
    {
        // Number of rows counted
        long numberOfRows = 0;

        // Get the Table annotation wrapped in an Optional
        final Optional<Table> tableOptional = ReflectionUtil.getTableAnnotation(clazz);

        // Not found so throw an error
        Preconditions.checkArgument(tableOptional.isPresent(), "@Table annotation not found for " + clazz + " when counting results.");

        // Get the Table annotation
        final Table table = tableOptional.get();

        // Our SQL objects used
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        // Wrapping in a SQLException try and catch
        try {
            // Get a new connection
            connection = getConnection();

            // Preparing our statement
            preparedStatement = prepare(connection, String.format("SELECT COUNT(*) FROM %s %s", replaceTableNamePlaceholders(table.name()), query.toString()), query.getWheres().values().toArray());

            // Execute the query
            resultSet = preparedStatement.executeQuery();

            // Get number of rows
            if(resultSet.first() || resultSet.next())
            {
                // Get the number of rows it counted
                numberOfRows = resultSet.getInt("COUNT(*)");
            }
        } catch (SQLException e) {
            // Print the stacktrace
            e.printStackTrace();
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }

        // Return our results
        return numberOfRows;
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
        final Optional<Table> tableOptional = ReflectionUtil.getTableAnnotation(clazz);

        // Not found so throw an error
        Preconditions.checkArgument(tableOptional.isPresent(), "@Table annotation not found for " + clazz + " when deleting results.");

        // Get the Table annotation
        final Table table = tableOptional.get();

        // Our SQL objects used
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        // Wrapping in a SQLException try and catch
        try {
            // Get a new connection
            connection = getConnection();

            // Preparing our statement
            preparedStatement = prepare(connection, String.format("DELETE FROM %s %s", replaceTableNamePlaceholders(table.name()), query.toString()), query.getWheres().values().toArray());

            // Execute our PreparedStatement
            deletedCount = preparedStatement.executeLargeUpdate();
        } catch (SQLException e) {
            // Print the stacktrace
            e.printStackTrace();
        } finally {
            closeResources(connection, preparedStatement);
        }

        // Return our results
        return deletedCount;
    }

    /**
     * Describe (aka EXPLAIN) an @Table coming from a Class
     * @param clazz to get Describe/Explain data for
     * @return retrieved TableSchema
     */
    public TableSchema describe(Class<?> clazz)
    {
        // Get the Table annotation
        final Optional<Table> tableOptional = ReflectionUtil.getTableAnnotation(clazz);

        // Not found so throw an error
        Preconditions.checkArgument(tableOptional.isPresent(), "@Table annotation not found for " + clazz + " when describing Table.");

        // Get the Table annotation
        final Table table = tableOptional.get();

        // MySQL related data
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        // Data to return
        final List<TableColumn> columns = Lists.newArrayList();

        try {
            // Get a new Connection
            connection = getConnection();

            // Preparing our statement
            preparedStatement = connection.prepareStatement("DESCRIBE `" + replaceTableNamePlaceholders(table.name()) + "`");

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
        return new TableSchema(credentials.getDatabase(), replaceTableNamePlaceholders(table.name()), columns);
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
        final Optional<Table> tableOptional = ReflectionUtil.getTableAnnotation(clazz);

        // Not found so throw an error
        Preconditions.checkArgument(tableOptional.isPresent(), "@Table annotation not found for " + clazz + " when updating Table.");

        // Get the Table annotation
        final Table table = tableOptional.get();

        // We're not allowed to do any creation or modifications
        if(!table.create() && table.modifyType() == ModifyType.NONE)
            throw new IllegalArgumentException(String.valueOf("Attempted to update @Table for " + clazz + " even though the properties say we can't."));

        // Get our InformationSchema from our search
        final List<TableInfo> results = find(TableInfo.class, new Query()
                .where("TABLE_SCHEMA", credentials.getDatabase())
                .where("TABLE_NAME", replaceTableNamePlaceholders((String) ReflectionUtil.getTableName(clazz)))
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
        final TableSchema tableSchema = describe(clazz);

        // No data found, so do nothing
        if(tableSchema.isEmpty())
            return false;

        // Get Column names
        final List<String> schemaColumnNames = tableSchema.getColumnNames();

        // Get our Column names for our Class
        final List<String> classColumnNames = ReflectionUtil.getColumnNames(clazz);

        // Columns to add
        final List<String> toAdd = (table.modifyType() == ModifyType.ADD || table.modifyType() == ModifyType.ADD_REMOVE ? classColumnNames.stream().filter(name -> !schemaColumnNames.contains(name)).collect(Collectors.toList()) : Lists.newArrayList());

        // Columns to remove
        final List<String> toRemove = (table.modifyType() == ModifyType.REMOVE || table.modifyType() == ModifyType.ADD_REMOVE ? schemaColumnNames.stream().filter(name -> !classColumnNames.contains(name)).collect(Collectors.toList()) : Lists.newArrayList());

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
        final Optional<Table> tableOptional = ReflectionUtil.getTableAnnotation(object.getClass());

        // Not found so throw an error
        Preconditions.checkArgument(tableOptional.isPresent(), "@Table annotation not found for " + object.getClass() + " when reloading.");

        // Get the Table annotation
        final Table table = tableOptional.get();

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
            preparedStatement = prepare(connection, String.format("SELECT * FROM %s %s", replaceTableNamePlaceholders(table.name()), query.toString()), query.getWheres().values().toArray());

            // Execute our PreparedStatement
            resultSet = preparedStatement.executeQuery();

            // If there's results then populate else throw error
            if (resultSet.next())
                populate(object, resultSet);
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
        final Optional<Table> tableOptional = ReflectionUtil.getTableAnnotation(object.getClass());

        // Not found so throw an error
        Preconditions.checkArgument(tableOptional.isPresent(), "@Table annotation not found for " + object.getClass() + " when saving.");

        // Get the Table annotation
        final Table table = tableOptional.get();

        // Get the Primary Field and the true name of the Primary Column
        final Optional<Field> primaryOptional = ReflectionUtil.getPrimaryField(object.getClass());

        // Generating our Query objects
        final Query query = Query.from(object);

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
                    replaceTableNamePlaceholders(table.name()),
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
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            // Close the resources we've used.
            closeResources(connection, preparedStatement);

            // Check if the @Primary is autoIncrement type and value = 0
            if(primaryOptional.isPresent())
            {
                // Getting the Primary Field
                final Field field = primaryOptional.get();

                // autoIncrement type so get last data
                if(field.getAnnotation(Primary.class).autoIncrement())
                {
                    // Checking if the field is an int to get the last autoIncremented integer
                    if(ParseUtil.toInt(ReflectionUtil.getFieldValue(field, object), 0) == 0)
                    {
                        ResultSet resultSet = null;

                        try {
                            // Get a new Connection
                            connection = getConnection();

                            // Prepare our statement
                            preparedStatement = connection.prepareStatement("SELECT LAST_INSERT_ID()");

                            // Execution was a success
                            resultSet = preparedStatement.executeQuery();

                            // There was a result
                            if(resultSet.next())
                            {
                                try {
                                    // Setting Field's value
                                    field.set(object, resultSet.getInt(1));
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            // Closing connections
                            closeResources(connection, preparedStatement, resultSet);
                        }
                    }
                }
            }
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
        final PreparedStatement preparedStatement = connection.prepareStatement(statement);

        // Iterate through the data
        for(int i = 1; i <= data.length; i++)
        {
            // Get the current Field
            Object object = data[i - 1];

            // Get the Codec for this Type
            Codec codec = Codec.getCodec(object.getClass());

            // Not known so skip (TODO: Throw an exception?)
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
        final Optional<Table> tableOptional = ReflectionUtil.getTableAnnotation(clazz);

        // Not found so throw an error
        Preconditions.checkArgument(tableOptional.isPresent(), "@Table annotation not found for " + clazz + " when preparing Table creation.");

        // Get the Table annotation
        final Table table = tableOptional.get();

        // Get the Field's for this Class
        final Map<String, Field> fields = ReflectionUtil.getFields(clazz);

        // Where our query string data will be stored
        final StringBuilder query = new StringBuilder();

        // Our Primary Field
        Field primaryField = null;

        // Iterate through all Field'entry
        for (Map.Entry<String, Field> entry : fields.entrySet())
        {
            // The Field in question
            final Field field = entry.getValue();

            // The name of the Column in the Table
            final Column column = ReflectionUtil.getColumnAnnotation(field).get();
            final String columnName =  ReflectionUtil.getColumnName(entry.getValue());

            // Get the Codec for this Field
            final  Codec codec = Codec.getCodec(field.getType());

            // No Codec known, skip!
            if(codec == null)
                continue;

            // Getting the maxLength for our Column
            final int maxLength = codec.calculateMaxLength(column.maxLength());

            // Appending the column data first
            query
                    .append("`")
                    .append(columnName)
                    .append("` ")
                    .append(codec.getSQLType());

            // If the maxLength is allowed to be set...
            if(maxLength != -1)
            {
                query
                        .append("(")
                        .append(maxLength)
                        .append(")");
            }

            // Appending our comma
            query.append(", ");

            // If it's the Primary
            final Primary primary = field.getAnnotation(Primary.class);

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
        return connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS `%s` (%s) ENGINE=InnoDB DEFAULT CHARSET=utf8", replaceTableNamePlaceholders(table.name()), query.toString()));
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
        final Optional<Table> tableOptional = ReflectionUtil.getTableAnnotation(clazz);

        // Not found so throw an error
        Preconditions.checkArgument(tableOptional.isPresent(), "@Table annotation not found for " + clazz + " when preparing Table creation.");

        // Get the Table annotation
        final Table table = tableOptional.get();

        // Where our query string data will be stored
        final StringBuilder query = new StringBuilder();

        // Iterate through all Field'entry
        for (Map.Entry<String, Field> entry : ReflectionUtil.getFields(clazz).entrySet())
        {
            // The Field in question
            final Field field = entry.getValue();

            // The name of the Column in the Table
            final Column column = ReflectionUtil.getColumnAnnotation(field).get();
            final String columnName =  ReflectionUtil.getColumnName(entry.getValue());

            // Doesn't need to be added
            if(!toAdd.contains(columnName))
                continue;

            // Get the Codec for this Field
            final Codec codec = Codec.getCodec(field.getType());

            // No Codec known, skip!
            if(codec == null)
                continue;

            // Getting the maxLength for our Column
            final int maxLength = codec.calculateMaxLength(column.maxLength());

            // Appending the column data first
            query
                    .append("ADD COLUMN `")
                    .append(columnName)
                    .append("` ")
                    .append(codec.getSQLType());

            // If the maxLength is allowed to be set...
            if(maxLength != -1)
            {
                query
                        .append("(")
                        .append(maxLength)
                        .append(")");
            }

            // Appending our comma
            query.append(", ");
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
        return connection.prepareStatement(String.format("ALTER TABLE `%s` %s", replaceTableNamePlaceholders(table.name()), query.toString()));
    }

    /**
     * Populate an Object with data from a ResultSet
     * @param object to populate the data into
     * @param resultSet where our data is
     */
    private void populate(Object object, ResultSet resultSet)
    {
        // Getting the Fields for this object
        final Map<String, Field> fields = ReflectionUtil.getFields(object.getClass());

        // No known fields so just return
        if(fields == null || fields.isEmpty())
            return;

        // Iterate through ResultSet data as a Map
        for(Map.Entry<String, Object> entry : StorageUtil.toMap(resultSet).entrySet())
        {
            // Name of the Column
            final String column = entry.getKey();

            // Get the Field associated with the Column name
            final Optional<Field> fieldOptional = ReflectionUtil.getColumnField(fields, column);

            // No Field found for this Column name
            if(!fieldOptional.isPresent())
                continue;

            // The Field for this data
            final Field field = fieldOptional.get();

            // Get the Codec for this type
            final Codec codec = Codec.getCodec(field.getType());

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