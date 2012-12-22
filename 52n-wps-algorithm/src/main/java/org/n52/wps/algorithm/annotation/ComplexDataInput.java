package org.n52.wps.algorithm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.n52.wps.io.data.IComplexData;

/**
 *
 * @author tkunicki
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface ComplexDataInput {
    String identifier();  // identifier
    String title() default "";
    String abstrakt() default "";  // 'abstract' is java reserved keyword
    int minOccurs() default 1;
    int maxOccurs() default 1;
    int maximumMegaBytes() default 0;
    Class <? extends IComplexData> binding();
}
