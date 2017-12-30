package uk.co.loonyrules.sql.annotations;

import uk.co.loonyrules.sql.enums.ModifyType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Class as a {@link uk.co.loonyrules.sql.Database} Object
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table
{

    /**
     * Name of the {@link Table}
     * @return table name
     */
    String name();

    /**
     * Should we create the {@link Table} if it doesn't exist?
     * @return whether or not we're allowed to create the {@link Table}
     */
    boolean create() default true;

    /**
     * {@link ModifyType} for this {@link Table}
     * @return the ModifyType allowed for this {@link Table}
     */
    ModifyType modifyType() default ModifyType.ADD;

}