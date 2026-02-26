package com.university.tps.linescalculationapi.dto;

import com.university.tps.linescalculationapi.validation.annotation.DistinctSlopeLines;
import com.university.tps.linescalculationapi.validation.annotation.ValidLines;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@ValidLines
@DistinctSlopeLines
public class LinesDTO {
    @NotEmpty
    @NotNull
    @Valid
    private List<LineDTO> lines;
}
