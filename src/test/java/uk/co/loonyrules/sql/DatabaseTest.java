package uk.co.loonyrules.sql;

import org.junit.Test;
import uk.co.loonyrules.sql.utils.MapUtil;

import java.sql.*;

public class DatabaseTest
{

    private Database database;

    @Test
    public void connect()
    {
        // Credentials for authentication
        Credentials credentials = new Credentials("localhost", 3306, "loonysql", "root", "");

        // Initialising our Database with our Credentials
        database = new Database(credentials);

        // Attempt to connect
        database.connect();

        // Selecting all rows
        selectAll();
    }

    public void selectAll()
    {
        // Variables used
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // Getting a new Connection
            connection = database.getConnection();

            // Preparing our PreparedStatement
            preparedStatement = connection.prepareStatement("SELECT * FROM `users`");

            // Execute the Query
            resultSet = preparedStatement.executeQuery();

            // Just print out the data in the ResultSet
            print(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
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
    }

    private void print(ResultSet resultSet)
    {
        try {
            // Our row count
            int count = 0;

            // Iterate through all results and print out the data
            while (resultSet.next())
                System.out.println("Row " + ++count + ": " + MapUtil.toMap(resultSet));
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

}