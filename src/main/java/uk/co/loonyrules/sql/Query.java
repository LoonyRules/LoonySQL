package uk.co.loonyrules.sql;

import uk.co.loonyrules.sql.utils.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Easily pre-generate Query data
 *
 * TODO: Make the experience nicer
 */
public class Query
{

    /**
     * Generate a Query based upon an Object
     * @param object to generate a Query from
     * @return the generated Query
     */
    public static Query from(Object object)
    {
        // Query to return
        Query query = new Query();

        // Get all Field's for this Object's Class
        for(Field field : ReflectionUtil.getFields(object.getClass()).values())
        {
            // Get the Column name
            String columnName = ReflectionUtil.getColumnName(field);

            // TODO: Implement AutoIncrement support
            // Skipping if Primary not wanted, or @Primary is autoIncremented
            //Primary primary = field.getAnnotation(Primary.class);

            // We don't want a Primary field
            //if(primary != null && primary.autoIncrement())
            //    continue;

            // Add to the where conditions
            query.where(columnName, ReflectionUtil.getFieldValue(field, object));
        }

        // Returning our Query
        return query;
    }

    /**
     * Generate a Query based upon an @Primary annotation for an Object
     * @param object to generate the Query for
     * @return the generated Query
     */
    public static Query generatePrimary(Object object)
    {
        // Get the Primary Field
        Optional<Field> primaryOptional = ReflectionUtil.getPrimaryField(object.getClass());

        // No Primary field so throw unsupported operation
        if(!primaryOptional.isPresent())
            throw new UnsupportedOperationException("No @Primary Field found in " + object.getClass() + ".");

        // Get our Field
        Field field = primaryOptional.get();

        // Getting the value of the Field
        Object fieldValue = null;
        try {
            fieldValue = field.get(object);
        } catch (IllegalAccessException e) {
            fieldValue = null;
        }

        // We have a Primary key so generate a Query and return
        return new Query().where(ReflectionUtil.getColumnName(field), fieldValue);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final LinkedHashMap<String, Object> wheres = new LinkedHashMap<>();

    private int
            skip = 0,
            limit = 0;

    /**
     * Get all "WHERE" conditions
     * @return all "WHERE" conditions
     */
    public LinkedHashMap<String, Object> getWheres()
    {
        return wheres;
    }

    /**
     * Get all "WHERE" conditions as a (`column1`, column2`) string
     * @return all "WHERE" conditions as a column string
     */
    public String getWheresAsColumns()
    {
        return wheres.isEmpty() ? "" : wheres.entrySet().stream()
                .map(entry -> "`" + entry.getKey() + "`")
                .collect(Collectors.joining(", "));
    }

    /**
     * Get all "WHERE" conditions as a ? placeholder string
     * @return all "WHERE" conditions as a ? placeholder string
     */
    public String getWheresAsPlaceholders()
    {
        return wheres.isEmpty() ? "" : wheres.entrySet().stream()
                .map(entry -> "?")
                .collect(Collectors.joining(", "));
    }

    /**
     * Get the number of documents to skip
     * @return number of documents to skip
     */
    public int getSkip()
    {
        return skip;
    }

    /**
     * Get the maximum number of documents to retrieve
     * @return maximum number of documents to retrieve
     */
    public int getLimit()
    {
        return limit;
    }

    /**
     * Append onto the "WHERE" clause with your column name and value
     * @param column to check
     * @param value data associated with the column
     * @return current instance for chaining
     */
    public Query where(String column, Object value)
    {
        // Put into the conditions map for later
        wheres.put(column, value);

        // Returning instance for chaining
        return this;
    }

    /**
     * Set number of rows to skip
     * @param skip number of rows to skip
     * @return current instance for chaining
     */
    public Query skip(int skip)
    {
        // Updating variable
        this.skip = skip;

        // Returning instance for chaining
        return this;
    }

    /**
     * Set number of rows to limit when retrieving
     * @param limit maximum number of rows to retrieve
     * @return current instance for chaining
     */
    public Query limit(int limit)
    {
        // Updating variable
        this.limit = limit;

        // Returning instance for chaining
        return this;
    }

    /**
     * Build the current "WHERE" conditions as a string
     * @return "WHERE" conditions as a string
     */
    public String buildWhere()
    {
        return wheres.isEmpty() ? "" : "WHERE " + wheres.entrySet().stream()
                .map(entry -> entry.getKey() + "=?")
                .collect(Collectors.joining(" AND "));
    }

    /**
     *
     * @return
     */
    public String buildConditionPlaceholders()
    {
        return wheres.isEmpty() ? "" : wheres.entrySet().stream()
                .map(entry -> entry.getKey() + "=?")
                .collect(Collectors.joining(", "));
    }

    /**
     * Generate the query data and append the "WHERE" conditions along with the skip and limit data
     * @return condition statement appended with skip and limit
     */
    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder(buildWhere());

        // Managing skip/limit
        if(skip != 0 || limit != 0)
        {
            stringBuilder.append(" LIMIT ");

            if(skip != 0)
                stringBuilder.append(skip);

            if(limit != 0)
                stringBuilder.append(skip != 0 ? "," : "").append(limit);
        }

        return stringBuilder.toString();
    }

}