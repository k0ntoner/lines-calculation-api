package com.university.tps.linescalculationapi.validation.annotation;


import com.university.tps.linescalculationapi.validation.validator.ValidLinesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidLinesValidator.class)
@Documented
public @interface ValidLines {

    String message() default
            "Must be exactly 1 line in intercept form and exactly 2 lines in slope intercept form";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}