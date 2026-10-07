package net.dogadmin.base.module.support.repeatsubmit.annoation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RepeatSubmit {

    /**
     * 间隔时间/毫秒
     */
    int intervalMilliSecond() default 0;

}
