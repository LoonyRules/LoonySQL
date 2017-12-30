package uk.co.loonyrules.sql.codecs.types;

import uk.co.loonyrules.sql.codecs.Codec;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Codec for Encoding/Decoding a Boolean
 */
public class BooleanCodec extends Codec<Boolean>
{

    /**
     * Construct and register this BooleanCodec
     */
    public BooleanCodec()
    {
        super(boolean.class);
    }

    /**
     * Decode the data given into a Boolean object
     * @param resultSet The ResultSet to get our data from
     * @param type The type of Field this is
     * @param fieldName The name of this Field (the Column name)
     * @return The Boolean decoded
     * @throws SQLException If a MySQL error is encountered
     */
    @Override
    public Boolean decode(ResultSet resultSet, Class<?> type, String fieldName) throws SQLException
    {
        return resultSet.getBoolean(fieldName);
    }

    /**
     * Encode the Boolean into the PreparedStatement
     * @param statement The PreparedStatement to modify
     * @param index The index of this Column
     * @param data The data to input
     * @throws SQLException If a MySQL error is encountered
     */
    @Override
    public void encode(PreparedStatement statement, int index, Boolean data) throws SQLException
    {
        statement.setBoolean(index, data);
    }

}