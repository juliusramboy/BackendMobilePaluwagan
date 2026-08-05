package com.example.MobilePaluwagan.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PaymentRateLimited {
    String action();
    int maxAttempts() default 5;
    long windowSeconds() default 900;
}
