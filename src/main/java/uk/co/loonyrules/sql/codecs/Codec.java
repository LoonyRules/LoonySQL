package uk.co.loonyrules.sql.codecs;

import com.google.common.collect.Maps;
import uk.co.loonyrules.sql.codecs.types.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Codec's for encoding and decoding Objects
 * @param <T> type this Codec will be managing
 */
public abstract class Codec<T>
{

    private static final Map<Class<?>, Codec> codecs = Maps.newHashMap();

    /**
     * Get registered Codecs
     * @return All registered Codecs
     */
    public static Map<Class<?>, Codec> getCodecs()
    {
        return codecs;
    }

    /**
     * Get a Codec via its mapping type
     * @param clazz to get the Codec for
     * @param <T> type that the Codec is managing
     * @return type of Codec (Eg: String.class -> StringCodec.class)
     */
    public static <T> T getCodec(Class<?> clazz)
    {
        // Retrieved Codec
        Codec codec = codecs.get(clazz);

        // Return null if not found or cast and return
        return codec == null ? null : (T) codec;
    }

    /**
     * Check if a Codec class is registered
     * @param codec to check for
     * @return Whether or not it's registered
     */
    public static boolean isRegistered(Class<? extends Codec> codec)
    {
        return getCodec(codec) != null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static {
        new StringCodec();
        new IntCodec();
        new IntegerCodec();
        new BooleanCodec();
        new DoubleCodec();
        new FloatCodec();
        new UUIDCodec();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Class<?> type;
    private String sqlType;
    private int maxLength;

    /**
     * Construct a new Codec with the type it'll be managing
     * @param type type to Encode and Decode
     * @param sqlType SQL type we're Encoding
     */
    public Codec(Class<?> type, String sqlType)
    {
        this(type, sqlType, 0);
    }

    /**
     * Construct a new Codec with the type it'll be managing
     * @param type type to Encode and Decode
     * @param sqlType SQL type we're Encoding
     * @param maxLength the maximum length allowed for the data
     */
    public Codec(Class<?> type, String sqlType, int maxLength)
    {
        // The type this Codec will be managing
        this.type = type;

        // The SQL type this Codec will be storing
        this.sqlType = sqlType;

        // The maximum length allowed for this SQL type
        this.maxLength = maxLength;

        // Put into the codecs map
        codecs.put(type, this);
    }

    /**
     * Get the type this Codec is managing
     * @return type this Codec is managing
     */
    public Class<?> getType()
    {
        return type;
    }

    /**
     * Get the SQL column type for this Codec
     * @return the SQL column type
     */
    public String getSQLType()
    {
        return sqlType;
    }

    /**
     * Get the maximum data length allowed for this SQL type
     * @return maximum data length allowed
     */
    public int getMaxLength()
    {
        return maxLength;
    }

    /**
     * Automatically calculate the maximum length allowed
     * @param input the input length
     * @return the max length
     */
    public int calculateMaxLength(int input)
    {
        return maxLength > input ? maxLength : input;
    }

    /**
     * Decode the data from a {@link ResultSet} into the Object specified for the Field
     *
     * @param resultSet our data
     * @param type of field
     * @param fieldName name of the {@link java.lang.reflect.Field}
     * @return The Object the Codec is decoding
     * @throws SQLException If a MySQL error occurred
     */
    public abstract T decode(ResultSet resultSet, Class<?> type, String fieldName) throws SQLException;

    /**
     * Encode the data into the {@link PreparedStatement}
     * @param statement the encoded data is inserted into
     * @param index for this {@link java.lang.reflect.Field}
     * @param data to encode
     * @throws SQLException If a MySQL error occurred
     */
    public abstract void encode(PreparedStatement statement, int index, T data) throws SQLException;

}