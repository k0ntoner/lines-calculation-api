package com.university.tps.linescalculationapi.validation.validator;

import com.university.tps.linescalculationapi.validation.annotation.IntegerValue;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class IntegerValueValidator implements ConstraintValidator<IntegerValue, BigDecimal> {

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.stripTrailingZeros().scale() <= 0;
    }
}