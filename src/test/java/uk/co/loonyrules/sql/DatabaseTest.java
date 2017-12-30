package uk.co.loonyrules.sql;

import org.junit.Test;
import uk.co.loonyrules.sql.models.Tables;
import uk.co.loonyrules.sql.models.User;

import java.util.List;
import java.util.Random;

public class DatabaseTest
{

    private final Random random = new Random();
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
        selectAll("Select all User rows", User.class);

        // Selecting InformationSchema.TABLES rows but skipping and limiting a random amount
        int skip = random.nextInt(50);
        selectAll("Select information_schema.TABLES with skipping and limiting.", Tables.class, new Query().skip(skip).limit(random.nextInt(skip + 50)));

        // Delete User rows
        database.delete(User.class);
    }

    private void selectAll(String prefix, Class<?> clazz)
    {
        selectAll(prefix, clazz, new Query());
    }

    private void selectAll(String prefix, Class<?> clazz, Query query)
    {
        // Get all User results
        List<?> results = database.find(clazz, query);

        // Print result count
        System.out.println(prefix + " results: " + results.size());

        int i = 0;
        for (Object result : results)
            System.out.println(" " + (++i) + " -> " + result.toString());
    }

}