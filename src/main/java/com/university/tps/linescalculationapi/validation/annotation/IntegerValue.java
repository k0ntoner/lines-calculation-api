package com.university.tps.linescalculationapi.validation.annotation;

import com.university.tps.linescalculationapi.validation.validator.IntegerValueValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IntegerValueValidator.class)
public @interface IntegerValue {
    String message() default "Must be an integer value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}