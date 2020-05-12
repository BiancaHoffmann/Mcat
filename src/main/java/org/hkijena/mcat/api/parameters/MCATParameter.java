package org.hkijena.mcat.api.parameters;

import org.scijava.Priority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a getter or setter function as parameter.
 * {@link MCATParameterAccess} will look for this annotation to find parameters.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MCATParameter {
    /**
     * The unique key of this parameter
     *
     * @return Parameter key
     */
    String value();

    /**
     * Sets if the parameter is visible to the user or only exported into JSON
     * Lower visibilities override higher visibilities.
     *
     * @return Parameter visibility
     */
    MCATParameterVisibility visibility() default MCATParameterVisibility.TransitiveVisible;

    /**
     * Sets the priority for (de)serializing this parameter.
     * Please use the priority constants provided by {@link Priority}
     *
     * @return the priority
     */
    double priority() default Priority.NORMAL;

    /**
     * A short key used for generating parameter strings.
     * Defaults to value() in {@link MCATParameterAccess} implementations if not provided
     * @return A short key used for generating parameter strings
     */
    String shortKey() default "";
}