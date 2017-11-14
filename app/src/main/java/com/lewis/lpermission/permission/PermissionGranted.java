package com.lewis.lpermission.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: lewis
 * Date: 2017/11/3.
 * Description: a method invoked when permission request successfully
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PermissionGranted {
    int value() default 0;
}
