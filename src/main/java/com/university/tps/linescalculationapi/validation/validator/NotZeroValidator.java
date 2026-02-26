package com.university.tps.linescalculationapi.validation.validator;

import com.university.tps.linescalculationapi.validation.annotation.NotZero;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotZeroValidator implements ConstraintValidator<NotZero, Number> {

    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return value.doubleValue() != 0;
    }
}