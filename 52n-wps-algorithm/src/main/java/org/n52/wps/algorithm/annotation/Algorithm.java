package org.n52.wps.algorithm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author tkunicki
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Algorithm {
    String identifier() default "";
    String title() default "";
    String abstrakt() default "";  // 'abstract' is java reserved keyword
    String version();
    boolean storeSupported() default true;
    boolean statusSupported() default true;
}
