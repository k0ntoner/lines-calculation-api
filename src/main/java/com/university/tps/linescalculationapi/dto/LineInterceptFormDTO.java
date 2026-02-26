package com.university.tps.linescalculationapi.dto;

import com.university.tps.linescalculationapi.validation.annotation.IntegerValue;
import com.university.tps.linescalculationapi.validation.annotation.NotZero;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
public class LineInterceptFormDTO extends LineDTO {
    @NotNull
    @NotZero
    @DecimalMin(value = "-125")
    @DecimalMax(value = "125")
    @IntegerValue
    private BigDecimal a;

    @NotNull
    @NotZero
    @DecimalMin(value = "-125")
    @DecimalMax(value = "125")
    @IntegerValue
    private BigDecimal b;

    @Override
    public LineGeneralFormDTO toGeneralDTO() {

        double aInt = a.doubleValue();
        double bInt = b.doubleValue();
        return LineGeneralFormDTO.builder()
                .a(bInt)
                .b(aInt)
                .c(-1.0 * aInt * bInt)
                .build();
    }
}
