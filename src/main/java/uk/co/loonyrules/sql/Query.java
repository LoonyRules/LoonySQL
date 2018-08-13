package uk.co.loonyrules.sql;

import uk.co.loonyrules.sql.annotations.Primary;
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
        final Query query = new Query();

        // Get all Field's for this Object's Class
        for(Field field : ReflectionUtil.getFields(object.getClass()).values())
        {
            // Get the Column name
            final String columnName = ReflectionUtil.getColumnName(field);

            // Get our @Primary annotation
            final Primary primary = field.getAnnotation(Primary.class);

            try {
                // We don't have a Primary key or it's an incrementation that's not yet assigned.
                if(primary != null && primary.autoIncrement() && ((int) field.get(object)) == 0)
                    continue;
            } catch (IllegalAccessException e) {
                // Throw the error (int's only supported)
                e.printStackTrace();
            }

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
        final Optional<Field> primaryOptional = ReflectionUtil.getPrimaryField(object.getClass());

        // No Primary field so throw unsupported operation
        if(!primaryOptional.isPresent())
            throw new UnsupportedOperationException("No @Primary Field found in " + object.getClass() + ".");

        // Get our Field
        final Field field = primaryOptional.get();

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
        return this.wheres;
    }

    /**
     * Get all "WHERE" conditions as a (`column1`, column2`) string
     * @return all "WHERE" conditions as a column string
     */
    public String getWheresAsColumns()
    {
        return this.wheres.isEmpty() ? "" : this.wheres.entrySet().stream()
                .map(entry -> "`" + entry.getKey() + "`")
                .collect(Collectors.joining(", "));
    }

    /**
     * Get all "WHERE" conditions as a ? placeholder string
     * @return all "WHERE" conditions as a ? placeholder string
     */
    public String getWheresAsPlaceholders()
    {
        return this.wheres.isEmpty() ? "" : this.wheres.entrySet().stream()
                .map(entry -> "?")
                .collect(Collectors.joining(", "));
    }

    /**
     * Get the number of documents to skip
     * @return number of documents to skip
     */
    public int getSkip()
    {
        return this.skip;
    }

    /**
     * Get the maximum number of documents to retrieve
     * @return maximum number of documents to retrieve
     */
    public int getLimit()
    {
        return this.limit;
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
        this.wheres.put(column, value);

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
     * Check if a where condition exists
     * @param where to check for
     * @return if it was found or not
     */
    public boolean hasWhere(String where)
    {
        return this.wheres.containsKey(where);
    }

    /**
     * Build the current "WHERE" conditions as a string
     * @return "WHERE" conditions as a string
     */
    public String buildWhere()
    {
        return this.wheres.isEmpty() ? "" : "WHERE " + this.wheres.entrySet().stream()
                .map(entry -> entry.getKey() + "=?")
                .collect(Collectors.joining(" AND "));
    }

    /**
     * Build the current "WHERE" conditions with their
     * placeholder characters instead of their values.
     *
     * @return "WHERE" condition with placeholders for
     *         {@link java.sql.PreparedStatement}'s.
     */
    public String buildConditionPlaceholders()
    {
        return this.wheres.isEmpty() ? "" : this.wheres.entrySet().stream()
                .map(entry -> entry.getKey() + "=?")
                .collect(Collectors.joining(", "));
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final Query query = (Query) o;

        if (this.skip != query.skip)
            return false;

        if (this.limit != query.limit)
            return false;

        if (!this.wheres.equals(query.wheres))
            return false;

        return true;
    }
    @Override
    public int hashCode()
    {
        int result = this.wheres.hashCode();
        result = 31 * result + this.skip;
        result = 31 * result + this.limit;
        return result;
    }

    /**
     * Generate the query data and append the "WHERE" conditions along with the skip and limit data
     * @return condition statement appended with skip and limit
     */
    @Override
    public String toString()
    {
        final StringBuilder stringBuilder = new StringBuilder(buildWhere());

        // Managing skip/limit
        if(this.skip != 0 || this.limit != 0)
        {
            stringBuilder.append(" LIMIT ");

            if(this.skip != 0)
                stringBuilder.append(this.skip);

            if(this.limit != 0)
                stringBuilder.append(this.skip != 0 ? "," : "").append(this.limit);
        }

        return stringBuilder.toString();
    }

}