package com.lguipeng.notes.injector;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
//限定符注解 @Qualifier ： 个人觉得有点像 “Web中自定义标签”的感觉，也有点像 "C语言里宏定义" 的样子。
@Documented
@Retention(RUNTIME)
public @interface ContextLifeCycle {
    String value() default "App";
}
