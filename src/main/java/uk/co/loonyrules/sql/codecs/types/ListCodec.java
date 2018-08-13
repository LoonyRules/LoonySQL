package uk.co.loonyrules.sql.codecs.types;

import com.google.common.collect.Lists;
import uk.co.loonyrules.sql.codecs.Codec;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
        final List<String> list = Lists.newArrayList();

        // Getting the List encoded data
        final String parsedList = resultSet.getString(fieldName);

        // Parsed list isn't null and isn't empty
        if(parsedList != null && !parsedList.isEmpty())
        {
            // Iterating through all entries to add (TODO: Escape support via quotes and commas. Passing a string that has a comma will BREAK this!)
            for(String parseEntry : parsedList.replaceAll("\\[", "").replace("]", "").split(", "))
            {
                // Add to the list of final String entries
                list.add(parseEntry);
            }
        }

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
        final Iterator<String> iterator = data.iterator();

        // No entry so return empty
        if (!iterator.hasNext())
        {
            // Inserting an empty list as a string
            statement.setString(index, "[]");

            // Return so nothing else is executed
            return;
        }

        // Generating a StringBuilder
        final StringBuilder stringBuilder = new StringBuilder("[");

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