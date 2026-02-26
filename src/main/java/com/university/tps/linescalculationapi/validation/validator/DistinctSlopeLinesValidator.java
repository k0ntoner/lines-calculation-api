package com.university.tps.linescalculationapi.validation.validator;

import com.university.tps.linescalculationapi.dto.LineSlopeInterceptFormDTO;
import com.university.tps.linescalculationapi.dto.LinesDTO;
import com.university.tps.linescalculationapi.validation.annotation.DistinctSlopeLines;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class DistinctSlopeLinesValidator
        implements ConstraintValidator<DistinctSlopeLines, LinesDTO> {

    @Override
    public boolean isValid(LinesDTO dto, ConstraintValidatorContext context) {

        if (dto == null || dto.getLines() == null)
            return true;

        List<LineSlopeInterceptFormDTO> slopeLines =
                dto.getLines().stream()
                        .filter(LineSlopeInterceptFormDTO.class::isInstance)
                        .map(LineSlopeInterceptFormDTO.class::cast)
                        .toList();

        if (slopeLines.size() != 2)
            return true;

        LineSlopeInterceptFormDTO l1 = slopeLines.get(0);
        LineSlopeInterceptFormDTO l2 = slopeLines.get(1);

        boolean identical =
                l1.getK().equals(l2.getK()) &&
                        l1.getB().equals(l2.getB());

        return !identical;
    }
}