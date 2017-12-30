package uk.co.loonyrules.sql.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@link Primary} Field for this {@link Table}
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Primary
{

    /**
     * Whether this Primary Key is auto incrementable
     * @return Whether or not this Primary Key is auto incrementable
     */
    boolean autoIncrement() default true;

}