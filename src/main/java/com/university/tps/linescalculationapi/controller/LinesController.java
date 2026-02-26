package com.university.tps.linescalculationapi.controller;

import com.university.tps.linescalculationapi.dto.LinesDTO;
import com.university.tps.linescalculationapi.service.LinesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lines")
@RequiredArgsConstructor
public class LinesController {

    private final LinesService linesService;

    @PostMapping("/calculate")
    public ResponseEntity<?> calculate(@RequestBody @Valid LinesDTO lines) {
        return ResponseEntity.status(HttpStatus.OK).body(linesService.calculateIntersections(lines));
    }
}
