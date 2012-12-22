package org.n52.wps.algorithm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.n52.wps.io.data.ILiteralData;

/**
 *
 * @author tkunicki
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface LiteralDataOutput {
    String identifier();  // identifier
    String title() default "";
    String abstrakt() default "";  // 'abstract' is java reserved keyword
    Class <? extends ILiteralData> binding() default ILiteralData.class;
}
