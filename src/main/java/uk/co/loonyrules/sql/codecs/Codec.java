package uk.co.loonyrules.sql.codecs;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import uk.co.loonyrules.sql.codecs.types.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

/**
 * Codec's for encoding and decoding Objects
 * @param <T> type this Codec will be managing
 */
public abstract class Codec<T>
{

    private static final Map<Set<Class<?>>, Codec> codecs = Maps.newHashMap();

    /**
     * Get registered Codecs
     * @return All registered Codecs
     */
    public static Map<Set<Class<?>>, Codec> getCodecs()
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
        // Return null if not found or cast and return
        return (T) codecs.values().stream()
                .filter(codec -> codec.isManaging(clazz))
                .findFirst()
                .orElse(null);
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
        new IntegerCodec();
        new BooleanCodec();
        new DoubleCodec();
        new FloatCodec();
        new UUIDCodec();
        new LongCodec();
        new ListCodec();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Set<Class<?>> types;
    private String sqlType;
    private int maxLength;

    /**
     * Construct a new Codec with the type it'll be managing
     * @param types types to Encode and Decode
     * @param sqlType SQL type we're Encoding
     */
    public Codec(String sqlType, Class<?>... types)
    {
        this(sqlType, 0, types);
    }

    /**
     * Construct a new Codec with the type it'll be managing
     * @param types type to Encode and Decode
     * @param sqlType SQL type we're Encoding
     * @param maxLength the maximum length allowed for the data
     */
    public Codec(String sqlType, int maxLength, Class<?>... types)
    {
        // The SQL type this Codec will be storing
        this.sqlType = sqlType;

        // The maximum length allowed for this SQL type
        this.maxLength = maxLength;

        this.types = Sets.newHashSet(types);

        // Put into the codecs map
        codecs.put(this.types, this);
    }

    /**
     * Get the types this Codec is managing
     * @return types this Codec is managing
     */
    public Set<Class<?>> getTypes()
    {
        return types;
    }

    /**
     * Check if this Codec is managing an Object Type
     * @param type to check for
     * @return Whether it managed the type
     */
    public boolean isManaging(Class<?> type)
    {
        return getTypes().contains(type);
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
        return maxLength == -1 ? -1 : maxLength == 0 ? input : input > maxLength ? maxLength : input;
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

    @Override
    public String toString()
    {
        return "Codec{" +
                "types=" + types +
                ", sqlType='" + sqlType + '\'' +
                ", maxLength=" + maxLength +
                '}';
    }
}