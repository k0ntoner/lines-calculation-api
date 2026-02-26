package com.university.tps.linescalculationapi.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.university.tps.linescalculationapi.enums.LineEquationForm;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LineInterceptFormDTO.class, name = "INTERCEPT"),
        @JsonSubTypes.Type(value = LineSlopeInterceptFormDTO.class, name = "SLOPE_INTERCEPT")
})
@Data
public abstract class LineDTO {

    @NotNull(message = "Line type must be specified")
    private LineEquationForm type;

    public abstract LineGeneralFormDTO toGeneralDTO();
}
