package uk.co.loonyrules.sql;

import org.junit.Test;
import uk.co.loonyrules.sql.models.Tables;
import uk.co.loonyrules.sql.models.User;

import java.util.List;

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

        // Update the Table for our User
        database.updateTable(User.class);

        // Selecting all User rows
        selectAll(User.class);

        // Selecting all InformationSchema rows
        selectAll(Tables.class);
    }

    public void selectAll(Class<?> clazz)
    {
        // Get all User results
        List<?> results = database.find(clazz);

        // Print result count
        System.out.println("Results: " + results.size());

        int i = 0;
        for (Object result : results)
            System.out.println(" " + (++i) + " -> " + result.toString());
    }

}