package uk.co.loonyrules.sql.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link java.lang.reflect.Field} as a {@link Column}
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column
{

    /**
     * Name of the {@link Column} for the {@link Table}. Leave blank to use
     * the name of the field for the {@link Column}.
     *
     * @return modified name of the {@link Column}
     */
    String value() default "";

}