package uk.co.loonyrules.sql.codecs.types;

import uk.co.loonyrules.sql.codecs.Codec;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Codec for Encoding/Decoding a String
 */
public class StringCodec extends Codec<String>
{

    /**
     * Construct and register this StringCodec
     */
    public StringCodec()
    {
        super(String.class);
    }

    /**
     * Decode the data given into a String object
     * @param resultSet The ResultSet to get our data from
     * @param type The type of Field this is
     * @param fieldName The name of this Field (the Column name)
     * @return The String decoded
     * @throws SQLException If a MySQL error is encountered
     */
    @Override
    public String decode(ResultSet resultSet, Class<?> type, String fieldName) throws SQLException
    {
        return resultSet.getString(fieldName);
    }

    /**
     * Encode the String into the PreparedStatement
     * @param statement The PreparedStatement to modify
     * @param index The index of this Column
     * @param data The data to input
     * @throws SQLException If a MySQL error is encountered
     */
    @Override
    public void encode(PreparedStatement statement, int index, String data) throws SQLException
    {
        statement.setString(index, data);
    }

}