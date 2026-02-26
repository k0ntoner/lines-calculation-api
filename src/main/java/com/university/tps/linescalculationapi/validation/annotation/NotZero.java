package com.university.tps.linescalculationapi.validation.annotation;

import com.university.tps.linescalculationapi.validation.validator.NotZeroValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotZeroValidator.class)
public @interface NotZero {

    String message() default "Value Must not be 0";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}