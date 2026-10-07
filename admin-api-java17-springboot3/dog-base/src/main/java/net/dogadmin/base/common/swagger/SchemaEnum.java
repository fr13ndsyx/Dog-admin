package net.dogadmin.base.common.swagger;

import net.dogadmin.base.common.enumeration.BaseEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SchemaEnum {

    /**
     * 枚举类对象
     *
     */
    Class<? extends BaseEnum> value();

    String example() default "";

    boolean hidden() default false;

    boolean required() default true;

    String dataType() default "";

    String desc() default "";

}
