package uk.co.loonyrules.sql.utils;

import com.google.common.collect.Maps;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * Utility class that allows you to convert a ResultSet iterator into a Map.
 */
public class MapUtil
{

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

}