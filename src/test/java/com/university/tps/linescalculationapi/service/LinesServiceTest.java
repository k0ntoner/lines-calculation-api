package com.university.tps.linescalculationapi.service;

import com.university.tps.linescalculationapi.constant.Constants;
import com.university.tps.linescalculationapi.dto.LineDTO;
import com.university.tps.linescalculationapi.dto.LineInterceptFormDTO;
import com.university.tps.linescalculationapi.dto.LineSlopeInterceptFormDTO;
import com.university.tps.linescalculationapi.dto.LinesCalculationResultDTO;
import com.university.tps.linescalculationapi.dto.LinesDTO;
import com.university.tps.linescalculationapi.dto.PointDTO;
import com.university.tps.linescalculationapi.enums.IntersectionType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LinesServiceTest {

    private final LinesService linesService = new LinesService();

    @ParameterizedTest(name = "{index}: expected={1}")
    @MethodSource("validCases")
    void calculateIntersections_shouldReturnExpectedResult_forValidInput(
            LinesDTO input,
            IntersectionType expectedType,
            List<PointDTO> expectedPoints
    ) {
        LinesCalculationResultDTO result = linesService.calculateIntersections(input);

        assertNotNull(result);
        assertEquals(expectedType, result.getIntersectionType());

        List<PointDTO> actualPoints = result.getPoints();
        assertNotNull(actualPoints);

        assertEquals(expectedPoints.size(), actualPoints.size());

        for (PointDTO expectedPoint : expectedPoints) {
            assertTrue(containsPoint(actualPoints, expectedPoint),
                    "Expected point not found: x=" + expectedPoint.getX() + ", y=" + expectedPoint.getY());
        }
    }

    private static Stream<Arguments> validCases() {
        return Stream.of(
                // =========================
                // Case 1: THREE_POINTS
                // Line1 (Intercept): x/1 + y/1 = 1 -> x + y = 1
                // Line2 (Slope):     y = 0*x + 2 -> y = 2
                // Line3 (Slope):     y = 1*x + 1 -> y = x + 1
                // Intersections:
                // L1&L2: (-1,2), L1&L3: (0,1), L2&L3: (1,2)
                // =========================
                Arguments.of(
                        buildLinesDTO(
                                buildIntercept(1, 1),
                                buildSlope(0, 2),
                                buildSlope(1, 1)
                        ),
                        IntersectionType.THREE_POINTS,
                        List.of(
                                buildPoint(-1.0, 2.0),
                                buildPoint(0.0, 1.0),
                                buildPoint(1.0, 2.0)
                        )
                ),

                // =========================
                // Case 2: ONE_POINT
                // Line1 (Intercept): x/2 + y/2 = 1 -> x + y = 2 (passes through (1,1))
                // Line2 (Slope):     y = 0*x + 1 -> y = 1
                // Line3 (Slope):     y = 2*x - 1
                // All intersect at (1,1)
                // =========================
                Arguments.of(
                        buildLinesDTO(
                                buildIntercept(2, 2),
                                buildSlope(0, 1),
                                buildSlope(2, -1)
                        ),
                        IntersectionType.ONE_POINT,
                        List.of(
                                buildPoint(1.0, 1.0)
                        )
                ),

                // =========================
                // Case 3: NO_INTERSECTION
                // All three are parallel (slope = 1)
                // Line1 (Intercept): x/1 + y/(-1) = 1 -> x - y = 1 -> y = x - 1
                // Line2 (Slope):     y = x + 1
                // Line3 (Slope):     y = x + 2
                // No pair intersects -> 0 points
                // =========================
                Arguments.of(
                        buildLinesDTO(
                                buildIntercept(1, -1),
                                buildSlope(1, 1),
                                buildSlope(1, 2)
                        ),
                        IntersectionType.NO_INTERSECTION,
                        List.of()
                ),
                // =========================
                // Case 4: TWO_POINTS
                // =========================
                Arguments.of(
                        buildLinesDTO(
                                buildIntercept(1, -1),
                                buildSlope(1, 1),
                                buildSlope(-1, 1)
                        ),
                        IntersectionType.TWO_POINTS,
                        List.of(
                                buildPoint(1.0, 0.0),
                                buildPoint(0.0, 1.0)
                        )
                ),

                // =========================
                // Case 5: COINCIDENT
                // =========================
                Arguments.of(
                        buildLinesDTO(
                                buildIntercept(2, 2),
                                buildSlope(-1, 2),
                                buildSlope(-1, 2)
                        ),
                        IntersectionType.COINCIDENT,
                        List.of()
                )
        );
    }

    private static LinesDTO buildLinesDTO(LineDTO line1, LineDTO line2, LineDTO line3) {
        LinesDTO dto = new LinesDTO();
        dto.setLines(List.of(line1, line2, line3));
        return dto;
    }

    private static LineInterceptFormDTO buildIntercept(int a, int b) {
        LineInterceptFormDTO dto = new LineInterceptFormDTO();
        dto.setA(BigDecimal.valueOf(a));
        dto.setB(BigDecimal.valueOf(b));
        return dto;
    }

    private static LineSlopeInterceptFormDTO buildSlope(int k, int b) {
        LineSlopeInterceptFormDTO dto = new LineSlopeInterceptFormDTO();
        dto.setK(BigDecimal.valueOf(k));
        dto.setB(BigDecimal.valueOf(b));
        return dto;
    }

    private static PointDTO buildPoint(double x, double y) {
        return PointDTO.builder().x(x).y(y).build();
    }

    private static boolean containsPoint(List<PointDTO> points, PointDTO expected) {
        return points.stream().anyMatch(p ->
                Math.abs(p.getX() - expected.getX()) <= Constants.EPSILON
                        && Math.abs(p.getY() - expected.getY()) <= Constants.EPSILON
        );
    }
}