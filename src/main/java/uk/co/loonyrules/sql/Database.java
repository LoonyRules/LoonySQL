package uk.co.loonyrules.sql;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The main class that allows you to manage the entire Database
 * as the authenticated {@link Credentials} user.
 */
public class Database
{

    private final Credentials credentials;

    private HikariDataSource hikariDataSource;
    private ExecutorService executorService;

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
        Preconditions.checkArgument(!isConnected(), "Connection hasn't been initialised.");

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
        Preconditions.checkArgument(isConnected(), "Already connected to the Database.");

        // Initialise our HikariConfig
        HikariConfig hikariConfig = new HikariConfig();

        // Setting the Jbdc url
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
        Runtime.getRuntime().addShutdownHook(new Thread(this::disconnect));
    }

    /**
     * Attempt to close the {@link Connection} and the {@link ExecutorService}
     */
    public void disconnect()
    {
        // Ensuring we're connected
        Preconditions.checkArgument(!isConnected(), "Database connection has already been disconnected.");

        // Closing the hikariDataSource
        hikariDataSource.close();

        // Shutting down the pool
        executorService.shutdown();
    }

}