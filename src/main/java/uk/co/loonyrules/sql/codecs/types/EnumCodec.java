package uk.co.loonyrules.sql.codecs.types;

import uk.co.loonyrules.sql.codecs.Codec;
import uk.co.loonyrules.sql.utils.ReflectionUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A base Codec for Encoding/Decoding an Enum
 */
public class EnumCodec<T> extends Codec<T>
{

    /**
     * Construct and register this EnumCodec
     */
    public EnumCodec(Class<?> clazz)
    {
        super("varchar", 255, clazz);
    }

    /**
     * Decode the data given into an Enum object
     * @param resultSet The ResultSet to get our data from
     * @param type The type of Field this is
     * @param fieldName The name of this Field (the Column name)
     * @return The Enum decoded
     * @throws SQLException If a MySQL error is encountered
     */
    @Override
    public T decode(ResultSet resultSet, Class<?> type, String fieldName) throws SQLException
    {
        return ReflectionUtil.toEnum((Class<T>) type, resultSet.getString(fieldName));
    }

    /**
     * Encode the Enum into the PreparedStatement
     * @param statement The PreparedStatement to modify
     * @param index The index of this Column
     * @param data The data to input
     * @throws SQLException If a MySQL error is encountered
     */
    @Override
    public void encode(PreparedStatement statement, int index, T data) throws SQLException
    {
        statement.setString(index, data.toString());
    }

    @Override
    public String toString()
    {
        return "EnumCodec{}";
    }

}