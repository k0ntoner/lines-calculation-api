package com.university.tps.linescalculationapi.validation.validator;

import com.university.tps.linescalculationapi.dto.LineInterceptFormDTO;
import com.university.tps.linescalculationapi.dto.LineSlopeInterceptFormDTO;
import com.university.tps.linescalculationapi.dto.LinesDTO;
import com.university.tps.linescalculationapi.validation.annotation.ValidLines;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidLinesValidator implements ConstraintValidator<ValidLines, LinesDTO> {

    @Override
    public boolean isValid(LinesDTO dto,
                           ConstraintValidatorContext context) {

        if (dto == null || dto.getLines() == null)
            return true;

        long interceptCount = dto.getLines().stream()
                .filter(LineInterceptFormDTO.class::isInstance)
                .count();

        long slopeCount = dto.getLines().stream()
                .filter(LineSlopeInterceptFormDTO.class::isInstance)
                .count();

        return interceptCount == 1 && slopeCount == 2;
    }
}