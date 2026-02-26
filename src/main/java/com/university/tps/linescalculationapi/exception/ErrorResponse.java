package com.university.tps.linescalculationapi.exception;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private String message;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ViolationDTO> violations;
}