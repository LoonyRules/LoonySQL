package uk.co.loonyrules.sql.codecs.types;

import uk.co.loonyrules.sql.codecs.Codec;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Codec for Encoding/Decoding an long
 */
public class LongCodec extends Codec<Long>
{

    /**
     * Construct and register this LongCodec
     */
    public LongCodec()
    {
        super("bigint", long.class, Long.class);
    }

    /**
     * Decode the data given into a Long object
     * @param resultSet The ResultSet to get our data from
     * @param type The type of Field this is
     * @param fieldName The name of this Field (the Column name)
     * @return The Long decoded
     * @throws SQLException If a MySQL error is encountered
     */
    @Override
    public Long decode(ResultSet resultSet, Class<?> type, String fieldName) throws SQLException
    {
        return resultSet.getLong(fieldName);
    }

    /**
     * Encode the Long into the PreparedStatement
     * @param statement The PreparedStatement to modify
     * @param index The index of this Column
     * @param data The data to input
     * @throws SQLException If a MySQL error is encountered
     */
    @Override
    public void encode(PreparedStatement statement, int index, Long data) throws SQLException
    {
        statement.setLong(index, data);
    }

    @Override
    public String toString()
    {
        return "LongCodec{}";
    }

}