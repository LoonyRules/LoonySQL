package uk.co.loonyrules.sql.codecs.types;

import uk.co.loonyrules.sql.codecs.Codec;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Codec for Encoding/Decoding a UUID
 *
 * TODO: Support null UUIDs
 */
public class UUIDCodec extends Codec<UUID>
{

    /**
     * Construct and register this UUIDCodec
     */
    public UUIDCodec()
    {
        super("varchar", 36, UUID.class);
    }

    /**
     * Decode the data given into a UUID object
     * @param resultSet The ResultSet to get our data from
     * @param type The type of Field this is
     * @param fieldName The name of this Field (the Column name)
     * @return The UUID decoded
     * @throws SQLException If a MySQL error is encountered
     */
    @Override
    public UUID decode(ResultSet resultSet, Class<?> type, String fieldName) throws SQLException
    {
        String string = resultSet.getString(fieldName);

        // Allows for returning null UUID's
        return string == null || string.isEmpty() ? null : UUID.fromString(string);
    }

    /**
     * Encode the UUID into the PreparedStatement
     * @param statement The PreparedStatement to modify
     * @param index The index of this Column
     * @param data The data to input
     * @throws SQLException If a MySQL error is encountered
     */
    @Override
    public void encode(PreparedStatement statement, int index, UUID data) throws SQLException
    {
        statement.setString(index, data == null ? "" : data.toString());
    }

    @Override
    public String toString()
    {
        return "UUIDCodec{}";
    }

}