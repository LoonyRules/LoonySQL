package uk.co.loonyrules.sql.utils;

import com.google.common.collect.Maps;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * Utility class that allows you to convert a ResultSet iterator into a Map.
 */
public class StorageUtil
{

    /**
     * Turn a ResultSet into a Map result
     * @param resultSet to parse into a Map
     * @return the final result as a Map
     */
    public static Map<String, Object> toMap(ResultSet resultSet)
    {
        // Map to put our data int
        Map<String, Object> columnData = Maps.newHashMap();

        try {
            // Get the ResultSetMetaData for this index
            ResultSetMetaData metaData = resultSet.getMetaData();

            // Iterate through columns (column index starts at 1) and put into the map
            for(int i = 1; i <= metaData.getColumnCount(); ++i)
                columnData.put(metaData.getColumnLabel(i), resultSet.getObject(i));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return our final data
        return columnData;
    }

    /**
     * Combine two Object[]'s together
     * @param object1 to merge first
     * @param object2 to merge last
     * @return the final Object[]
     */
    public static Object[] combine(Object[] object1, Object[] object2)
    {
        Object[] finalObjects = new Object[object1.length + object2.length];

        int index = 0;

        // Adding object1 data
        for(int i = 0; i < object1.length; i++)
        {
            finalObjects[index] = object1[i];
            index++;
        }

        // Adding object2 data
        for(int i = 0; i < object2.length; i++)
        {
            finalObjects[index] = object2[i];
            index++;
        }

        // Returning our finalObjects
        return finalObjects;
    }

}