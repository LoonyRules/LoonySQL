package uk.co.loonyrules.sql;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Easily pre-generate Query data
 *
 * TODO: Make the experience nicer
 */
public class Query
{

    private final Map<String, Object> wheres = Maps.newHashMap();

    private int
            skip = 0,
            limit = 0;

    /**
     * Get all "WHERE" conditions
     * @return all "WHERE" conditions
     */
    public Map<String, Object> getWheres()
    {
        return wheres;
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