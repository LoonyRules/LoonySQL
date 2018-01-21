package uk.co.loonyrules.sql.codecs.types;

import uk.co.loonyrules.sql.codecs.Codec;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Codec for Encoding/Decoding a Double
 */
public class DoubleCodec extends Codec<Double>
{

    /**
     * Construct and register this DoubleCodec
     */
    public DoubleCodec()
    {
        super("double", -1, double.class, Double.class);
    }

    /**
     * Decode the data given into a Double object
     * @param resultSet The ResultSet to get our data from
     * @param type The type of Field this is
     * @param fieldName The name of this Field (the Column name)
     * @return The Double decoded
     * @throws SQLException If a MySQL error is encountered
     */
    @Override
    public Double decode(ResultSet resultSet, Class<?> type, String fieldName) throws SQLException
    {
        return resultSet.getDouble(fieldName);
    }

    /**
     * Encode the Double into the PreparedStatement
     * @param statement The PreparedStatement to modify
     * @param index The index of this Column
     * @param data The data to input
     * @throws SQLException If a MySQL error is encountered
     */
    @Override
    public void encode(PreparedStatement statement, int index, Double data) throws SQLException
    {
        statement.setDouble(index, data);
    }

    @Override
    public String toString()
    {
        return "DoubleCodec{}";
    }

}