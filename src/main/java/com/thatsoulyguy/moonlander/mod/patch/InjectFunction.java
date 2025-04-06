package com.thatsoulyguy.moonlander.mod.patch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface InjectFunction
{
    String value();

    InjectionPoint injectionPoint() default InjectionPoint.START;
}