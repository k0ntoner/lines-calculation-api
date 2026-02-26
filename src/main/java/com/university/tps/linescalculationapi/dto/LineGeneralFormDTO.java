package com.university.tps.linescalculationapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineGeneralFormDTO {

    private Double a;

    private Double b;

    private Double c;
}
