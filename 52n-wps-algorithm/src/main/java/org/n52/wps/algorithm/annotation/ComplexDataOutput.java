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
public @interface ComplexDataOutput {
    String identifier();  // identifier
    String title() default "";
    String abstrakt() default "";  // 'abstract' is java reserved keyword
    Class <? extends IComplexData> binding();
}
