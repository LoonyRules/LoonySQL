package uk.co.loonyrules.sql;

/**
 * Credential file used for a {@link Database} connection initialisation.
 */
public class Credentials
{

    private final String
            host;
    private final String database;
    private final String username;
    private final String password;
    private final int port;
    private final long timeout;

    /**
     * Initialise new Credentials with a default timeout of 30,000ms
     * @param host of the MySQL server
     * @param port of the MySQL server
     * @param database to connect to
     * @param username to authenticate as
     * @param password to authenticate as
     */
    public Credentials(String host, int port, String database, String username, String password)
    {
        this(host, port, database, username, password, 30000L);
    }

    /**
     * Initialise new Credentials with a default timeout of 30,000ms
     * @param host of the MySQL server
     * @param port of the MySQL server
     * @param database to connect to
     * @param username to authenticate as
     * @param password to authenticate as
     * @param timeout time in ms before connection timeout occurs
     */
    public Credentials(String host, int port, String database, String username, String password, long timeout)
    {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.timeout = timeout;
    }

    /**
     * Get the host address
     * @return host address of the MySQL server
     */
    public String getHost()
    {
        return host;
    }

    /**
     * Get the database name
     * @return database name to connect to
     */
    public String getDatabase()
    {
        return database;
    }

    /**
     * Get the username uses for authentication
     * @return the username to authenticate as
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Get the password used for authentication
     * @return the password to authenticate with
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Get the port of the MySQL server
     * @return the port of the MySQL server
     */
    public int getPort()
    {
        return port;
    }

    /**
     * Get the timeout time for a connection request
     * @return the time in milliseconds before a connection request will time out
     */
    public long getTimeout()
    {
        return timeout;
    }

    /**
     * Create a copy of the current Credentials but with modifications
     * @param database modified database name
     * @return new instance with modified details
     */
    public Credentials copyOf(String database)
    {
        return copyOf(null, 0, database);
    }

    /**
     * Create a copy of the current Credentials but with modifications
     * @param host modified host string (null if no changes were made)
     * @param port modified port (or 0 if we want to use the old one)
     * @return new instance with modified details
     */
    public Credentials copyOf(String host, int port)
    {
        return copyOf(host, port, null);
    }

    /**
     * Create a copy of the current Credentials but with modifications
     * @param host modified host string (null if no changes were made)
     * @param port modified port (or 0 if we want to use the old one)
     * @param database modified database (null if no changes were made)
     * @return new instance with modified details
     */
    public Credentials copyOf(String host, int port, String database)
    {
        return copyOf(host, port, database, null);
    }

    /**
     * Create a copy of the current Credentials but with modifications
     * @param host modified host string (null if no changes were made)
     * @param port modified port (or 0 if we want to use the old one)
     * @param database modified database (null if no changes were made)
     * @param username modified username (null if no changes were made)
     * @return new instance with modified details
     */
    public Credentials copyOf(String host, int port, String database, String username)
    {
        return copyOf(host, port, database, username, null);
    }

    /**
     * Create a copy of the current Credentials but with modifications
     * @param host modified host string (null if no changes were made)
     * @param port modified port (or 0 if we want to use the old one)
     * @param database modified database (null if no changes were made)
     * @param username modified username (null if no changes were made)
     * @param password modified password (null if no changes were made)
     * @return new instance with modified details
     */
    public Credentials copyOf(String host, int port, String database, String username, String password)
    {
        return new Credentials(
                /* Get the correct host if modified */
                host == null ? this.host : host,

                /* Get the correct port if modified */
                port == 0 ? this.port : port,

                /* Get the correct database if modified */
                database == null ? this.database : database,

                /* Get the correct username if modified */
                username == null ? this.username : username,

                /* Get the correct password if modified */
                password == null ? this.password : password
        );
    }

}