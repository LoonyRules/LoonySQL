package uk.co.loonyrules.sql.enums;

/**
 * Modification Type for the {@link uk.co.loonyrules.sql.annotations.Table} annotation.
 */
public enum ModifyType
{

    /**
     * Do not modify anything
     */
    NONE,

    /**
     * Only allow addition modifications
     */
    ADD,

    /**
     * Only allow removal modifications
     */
    REMOVE,

    /**
     * Allow addition and removal modifications
     */
    ADD_REMOVE

}