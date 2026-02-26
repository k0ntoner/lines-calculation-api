package com.university.tps.linescalculationapi.dto;


import com.university.tps.linescalculationapi.enums.IntersectionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinesCalculationResultDTO {
    private IntersectionType intersectionType;
    private List<PointDTO> points;
}
