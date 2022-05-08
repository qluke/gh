package com.luke.spring.boot.autoconfigure.annotation;

import java.lang.annotation.*;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GlobalHistory {

    /**
     * 描述
     * @return {String}
     */
    String tableName() default "tb_update_history";


    /**
     * ONLY_CARE_UPDATE_COLUMNS
     */
    boolean onlyCareUpdateColumns() default true;


}
