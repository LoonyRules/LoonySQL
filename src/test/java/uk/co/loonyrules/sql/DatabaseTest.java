package uk.co.loonyrules.sql;

import org.junit.Ignore;
import org.junit.Test;
import uk.co.loonyrules.sql.codecs.RankCodec;
import uk.co.loonyrules.sql.models.TableInfo;
import uk.co.loonyrules.sql.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class DatabaseTest
{

    private final Random random = new Random();
    private final UUID uuid = UUID.fromString("2179a7da-81e4-443b-a459-1b4157e07709");

    private Database database;
    private User user = new User(uuid, "LoonyRules");

    @Test
    public void connect()
    {
        // Initialising database connectivity and methods example
        {
            // Credentials for authentication
            Credentials credentials = new Credentials("localhost", 3306, "loonysql", "root", "");

            // Initialising our Database with our Credentials
            database = new Database(credentials);

            // Registering our placeholders
            database.addTablePlaceholder("environment", "test");

            // Attempt to connect
            database.connect();

            // Registering our Codecs
            new RankCodec();

            // Update the Table associated with User object
            database.updateTable(User.class);
        }
        
        // Inserting our User
        {
            database.save(user);

            System.out.println("Inserted our dummy user " + user);
        }

        // Select all and skip/limit example
        {
            // Selecting all User rows
            select("Select all User rows", User.class);

            // Selecting `information_schema.TABLES` rows but skipping/limiting random amounts
            int skip = random.nextInt(5);
            select("Select information_schema.TABLES with skipping and limiting.", TableInfo.class, new Query().skip(skip).limit(random.nextInt(skip + 5)));
        }

        // Example of how to get a User object with a "WHERE" condition
        {
            findUser();
        }

        // Reloading an Object due to data being updated elsewhere.
        {
            // Modifying the random column data in the Table without modifying our user object
            changeRandom(user.getUUID());

            // Get the old random int (proof that changeRandom(UUID uuid) doesn't modify the user object)
            int oldRandom = user.getRandom();

            // Reloading the user object (without a Query param initially to use our @Primary field automatically)
            database.reload(user);

            // Printing out data
            System.out.println("Random variable in the table was updated and user was reloaded.");
            System.out.println(" oldRandom: " + oldRandom);
            System.out.println(" " + user);
            System.out.println(" new -> old diff: " + (user.getRandom() - oldRandom));
        }

        // Print out all current User data
        {
            select("Users after modifying random column", User.class);
        }

        // Modifying user object and saving it example (INSERT [...] ON DUPLICATE KEY [...])
        {
            // Reversing banned state
            user.setBanned(!user.isBanned());

            // Updating our changed data
            database.save(user);

            // Print out the user
            System.out.println("After banned state change: " + user);
        }

        // Wait 5 seconds before deletion, use this time to view the Table as proof this is all working.
        {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Delete our user object so then next time this Test is ran, there's no data.
        {
            // Should be 1
            System.out.println("Find result prior to deletion: " + (database.find(user).size()));

            database.delete(user);

            // Should be 0
            System.out.println("Find result post deletion: " + (database.find(user).size()));
        }
    }

    private void findUser()
    {
        // Get a User for the UUID
        Optional<User> userOptional = database.findFirst(User.class, new Query().where("uuid", uuid));

        // Result returned a User else print no user found
        if(userOptional.isPresent())
        {
            user = userOptional.get();
            System.out.println("UUID \"" + uuid.toString() + "\" was found: " + user);
        } else System.out.println("No User with the UUID \"" + uuid.toString() + "\" found.");
    }

    private void changeRandom(UUID uuid)
    {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            // Get a new Connection
            connection = database.getConnection();

            // Prepare our statement
            preparedStatement = connection.prepareStatement("UPDATE `users` SET random = ? WHERE uuid = ?");

            // Setting our replacements
            preparedStatement.setInt(1, random.nextInt(50));
            preparedStatement.setString(2, uuid.toString());

            // Execute the update
            preparedStatement.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            database.closeResources(connection, preparedStatement);
        }
    }

    private void select(String prefix, Class<?> clazz)
    {
        select(prefix, clazz, new Query());
    }

    private void select(String prefix, Class<?> clazz, Query query)
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