package uk.co.loonyrules.sql.utils;

/**
 * A util classed used for parsing objects
 */
public class ParseUtil
{

    /**
     * Parse an Object to an Integer or use a defaultValue if an error is occurred
     * @param object to parse to an Integer
     * @param defaultValue to use if object cannot be parsed
     * @return the object data
     */
    public static int toInt(Object object, int defaultValue)
    {
        try {
            return object instanceof Number ? (Integer) object : Integer.parseInt((String) object);
        } catch(NumberFormatException e) {
            return defaultValue;
        }
    }

}