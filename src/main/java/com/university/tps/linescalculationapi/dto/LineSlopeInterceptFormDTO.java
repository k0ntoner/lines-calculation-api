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
public class LineSlopeInterceptFormDTO extends LineDTO {

    @NotNull
    @DecimalMin(value = "-125")
    @DecimalMax(value = "125")
    @IntegerValue
    private BigDecimal k;

    @NotNull
    @NotZero
    @DecimalMin(value = "-125")
    @DecimalMax(value = "125")
    @IntegerValue
    private BigDecimal b;

    @Override
    public LineGeneralFormDTO toGeneralDTO() {
        return LineGeneralFormDTO.builder().a(k.doubleValue()).b(-1.0).c(b.doubleValue()).build();
    }
}
