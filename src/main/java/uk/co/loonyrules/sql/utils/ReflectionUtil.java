package uk.co.loonyrules.sql.utils; // i love spoilerrules

import com.google.common.cache.CacheBuilder; // i hope spoilerrules marry with me
import com.google.common.cache.CacheLoader; // spoilerrules is very cool
import com.google.common.cache.LoadingCache; // i want kiss spoilerrules
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import uk.co.loonyrules.sql.annotations.Column;
import uk.co.loonyrules.sql.annotations.Primary;
import uk.co.loonyrules.sql.annotations.Table;
import uk.co.loonyrules.sql.storage.CaseInsensitiveMap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Useful methods for Reflection and Annotation stuff
 */
public class ReflectionUtil
{

    private static LoadingCache<Class<?>, Map<String, Field>> fieldCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5L, TimeUnit.MINUTES)
            .maximumSize(32L)
            .build(new CacheLoader<Class<?>, Map<String, Field>>()
    {
        @Override
        public Map<String, Field> load(Class<?> clazz)
        {
            final Map<String, Field> map = new CaseInsensitiveMap<>();

            // Starting at the Class type given
            Class<?> targetClass = clazz;

            // Looping through all Parent Classes and adding to map
            while (targetClass != null)
            {
                // Find fields and add to the map
                map.putAll(ReflectionUtil.findFields(targetClass));

                // Getting the next super class of the current class
                targetClass = targetClass.getSuperclass();
            }

            return map; // it's ok
        }
    });

    /**
     * Get all Fields for a Class's
     * @param clazz to get Field's for
     * @return all Fields that have a @Column annotation
     */
    public static Map<String, Field> getFields(Class<?> clazz)
    {
        try {
            return fieldCache.get(clazz);
        } catch(ExecutionException e) {
            return Maps.newHashMap();
        }
    }

    /**
     * Get Field for a Class by name
     * @param clazz to get Field's for
     * @param name of the Field to get
     * @return Field if known
     */
    public static Field getField(Class<?> clazz, String name)
    {
        try {
            return fieldCache.get(clazz).get(name);
        } catch (ExecutionException e) {
            // Print the stacktrace
            e.printStackTrace();

            // Return null
            return null;
        }
    }

    /**
     * Find all Field's that have an @Column annotation
     * @param clazz to get Field's for
     * @return all supported Field's
     */
    public static Map<String, Field> findFields(Class<?> clazz)
    {
        final Map<String, Field> fields = Maps.newHashMap();

        if(clazz == null)
            return fields;

        // Getting all declared fields
        for(Field field : clazz.getDeclaredFields())
        {
            // Ensuring it has the Column annotation
            if(!field.isAnnotationPresent(Column.class))
                continue;

            // Set accessible and then add to the fields map
            field.setAccessible(true);
            fields.put(field.getName(), field);
        }

        return fields;
    }

    /**
     * Get a Table annotation for a Class
     * @param clazz to get Table annotation for
     * @return Table annotation instance wrapped in Optional
     */
    public static Optional<Table> getTableAnnotation(Class<?> clazz)
    {
        return Optional.ofNullable(clazz.getAnnotation(Table.class));
    }

    /**
     * Get a Column annotation for a Class
     * @param field to get Column annotation for
     * @return Column annotation instance wrapped in Optional
     */
    public static Optional<Column> getColumnAnnotation(Field field)
    {
        return Optional.ofNullable(field.getAnnotation(Column.class));
    }

    /**
     * Get a Field by Column name
     * @param fields all known fields
     * @param column column name
     * @return the Field linked to the column name
     */
    public static Optional<Field> getColumnField(Map<String, Field> fields, String column)
    {
        return fields.values().stream().filter(field -> field.getName().equals(column) || field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class).name().equals(column)).findFirst();
    }

    /**
     * Get the Table name for a Class
     * @param clazz to get the Table name for
     * @return table name or null if not known
     */
    public static Object getTableName(Class<?> clazz)
    {
        return getTableAnnotation(clazz).<Object>map(Table::name).orElse(null);
    }

    /**
     * Get the Column name for a Field
     * @param field to get the true Column name for
     * @return the true name or null if not know
     */
    public static String getColumnName(Field field)
    {
        return getColumnAnnotation(field).map(column -> column.name().isEmpty() ? field.getName() : column.name()).orElse(null);
    }

    /**
     * Wrap the Field that has the @Primary annotation from a Class
     * @param clazz to get @Primary Field from
     * @return the primary field wrapped in an Optional
     */
    public static Optional<Field> getPrimaryField(Class<?> clazz)
    {
        return getFields(clazz).values().stream().filter(field -> field.isAnnotationPresent(Primary.class)).findFirst();
    }

    /**
     * Get a safe value for a Field
     * @param field to get the value from
     * @param object instance to get instance from
     * @return the Field's value
     */
    public static Object getFieldValue(Field field, Object object)
    {
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert an Object to an Enum via a class type
     * @param type to cast the object to
     * @param object the enum toString value
     */
    public static <T> T toEnum(Class<T> type, Object object)
    {
        try {
            return (T) type.getMethod("valueOf", String.class).invoke(null, object);
        } catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Get all @Column names from a Class
     * @param clazz to get column names from
     * @return all converted, true @Column names
     */
    public static List<String> getColumnNames(Class<?> clazz)
    {
        final List<String> columnNames = Lists.newArrayList();

        // Iterate through all Fields to get their @Column name
        for(Field field : getFields(clazz).values())
            columnNames.add(getColumnName(field));

        return columnNames;
    }

}
