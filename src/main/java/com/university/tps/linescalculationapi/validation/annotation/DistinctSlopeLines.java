package com.university.tps.linescalculationapi.validation.annotation;

import com.university.tps.linescalculationapi.validation.validator.DistinctSlopeLinesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DistinctSlopeLinesValidator.class)
@Documented
public @interface DistinctSlopeLines {

    String message() default "Slope lines must not be identical (k1,b1) must differ from (k2,b2)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
