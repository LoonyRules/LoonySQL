package uk.co.loonyrules.sql.codecs.types;

import com.google.common.collect.Lists;
import uk.co.loonyrules.sql.codecs.Codec;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Codec for Encoding/Decoding a List<String> object
 *
 * TODO: Make a better system to support a Generic Type that's not a String
 */
public class ListCodec extends Codec<List<String>>
{

    /**
     * Construct and register this ListCodec
     */
    public ListCodec()
    {
        super("longtext", -1, List.class, ArrayList.class);
    }

    /**
     * Decode the data given into a List object
     *
     * @param resultSet The ResultSet to get our data from
     * @param type      The type of Field this is
     * @param fieldName The name of this Field (the Column name)
     * @return The Boolean decoded
     * @throws SQLException If a MySQL error is encountered
     */
    @Override
    public List<String> decode(ResultSet resultSet, Class<?> type, String fieldName) throws SQLException
    {
        // Creating the list we're returning
        List<String> list = Lists.newArrayList();

        // Getting the List encoded data
        String parsedList = resultSet.getString(fieldName);

        // Iterating through and add (TODO: Improve this because any comma inside the List's entry will be parsed as a separate entry)
        list.addAll(Arrays.asList(parsedList.replaceAll("\\[", "").replace("]", "").split(", ")));

        // Return our final List
        return list;
    }

    /**
     * Encode the List into the PreparedStatement
     *
     * @param statement The PreparedStatement to modify
     * @param index     The index of this Column
     * @param data      The data to input
     * @throws SQLException If a MySQL error is encountered
     */
    @Override
    public void encode(PreparedStatement statement, int index, List<String> data) throws SQLException
    {
        // Get the Iterator for our List<String>
        Iterator<String> iterator = data.iterator();

        // No entry so return empty
        if (!iterator.hasNext())
        {
            // Inserting an empty list as a string
            statement.setString(index, "[]");

            // Return so nothing else is executed
            return;
        }

        // Generating a StringBuilder
        StringBuilder stringBuilder = new StringBuilder("[");

        // Iterating through
        while (iterator.hasNext())
        {
            // Appending to the StringBuilder
            stringBuilder.append(iterator.next()).append(", ");
        }

        // Removing trailing comma
        stringBuilder.setLength(stringBuilder.length() - 2);

        // Ending the builder
        stringBuilder.append("]");

        // Inserting our built List as a string
        statement.setString(index, stringBuilder.toString());
    }

    @Override
    public String toString()
    {
        return "ListCodec{}";
    }

}