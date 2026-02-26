package com.university.tps.linescalculationapi.service;

import com.university.tps.linescalculationapi.constant.Constants;
import com.university.tps.linescalculationapi.dto.*;
import com.university.tps.linescalculationapi.enums.IntersectionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for computing intersection results for exactly three lines.
 *
 * <p><b>Input:</b> {@link LinesDTO} containing exactly three {@link LineDTO} items.</p>
 *
 * <p><b>Internal representation:</b> each input line is converted to the general form:
 * <pre>
 *     a*x + b*y + c = 0
 * </pre>
 * where {@code a}, {@code b}, {@code c} are {@code double} values in {@link LineGeneralFormDTO}.</p>
 *
 * <p><b>Intersection of two lines</b> is computed using Cramer's rule for the linear system:
 * <pre>
 *     a1*x + b1*y + c1 = 0
 *     a2*x + b2*y + c2 = 0
 * </pre>
 * <p>
 * Determinant:
 * <pre>
 *     D = a1*b2 - a2*b1
 * </pre>
 * <p>
 * If {@code D != 0} (within {@link Constants#EPSILON}), there is a single intersection point:
 * <pre>
 *     x = (b1*c2 - b2*c1) / D
 *     y = (a2*c1 - a1*c2) / D
 * </pre>
 * <p>
 * If {@code D == 0}:
 * <ul>
 *   <li>Lines are <b>coincident</b> when all three pairwise determinants are ~0:
 *   <pre>
 *       a1*b2 - a2*b1 = 0
 *       a1*c2 - a2*c1 = 0
 *       b1*c2 - b2*c1 = 0
 *   </pre></li>
 *   <li>Otherwise, lines are <b>parallel</b> (no intersection point).</li>
 * </ul>
 *
 * <p><b>For three lines</b>, the service computes pairwise intersections (1-2, 1-3, 2-3),
 * keeps only unique points (with EPSILON comparison), and returns:</p>
 * <ul>
 *   <li>{@link IntersectionType#COINCIDENT} if all three lines are coincident</li>
 *   <li>{@link IntersectionType#NO_INTERSECTION} if there are 0 unique points</li>
 *   <li>{@link IntersectionType#ONE_POINT} if there is 1 unique point</li>
 *   <li>{@link IntersectionType#TWO_POINTS} if there are 2 unique points</li>
 *   <li>{@link IntersectionType#THREE_POINTS} if there are 3 unique points</li>
 * </ul>
 */
@Service
@Slf4j
public class LinesService {

    /**
     * Calculates the intersection result for exactly three lines.
     *
     * @param linesDTO DTO containing exactly three input lines; must not be {@code null}
     * @return result DTO containing intersection type and unique intersection points
     * @throws IllegalArgumentException if {@code linesDTO} is null, list is null, or list size is not exactly 3
     * @throws IllegalStateException    if the number of unique intersection points is outside expected range [0..3]
     */
    public LinesCalculationResultDTO calculateIntersections(LinesDTO linesDTO) {

        log.info("Starting intersection calculation");

        if (linesDTO == null || linesDTO.getLines() == null) {
            log.error("LinesDTO or lines list is null");
            throw new IllegalArgumentException("LinesDTO and lines list must not be null");
        }

        List<LineDTO> inputLines = linesDTO.getLines();

        log.debug("Received {} lines", inputLines.size());

        if (inputLines.size() != 3) {
            log.error("Invalid number of lines: {}", inputLines.size());
            throw new IllegalArgumentException("Exactly 3 lines are required");
        }

        LineGeneralFormDTO line1 = inputLines.get(0).toGeneralDTO();
        LineGeneralFormDTO line2 = inputLines.get(1).toGeneralDTO();
        LineGeneralFormDTO line3 = inputLines.get(2).toGeneralDTO();

        log.debug("Converted lines to general form:");
        log.debug("Line1: a={}, b={}, c={}", line1.getA(), line1.getB(), line1.getC());
        log.debug("Line2: a={}, b={}, c={}", line2.getA(), line2.getB(), line2.getC());
        log.debug("Line3: a={}, b={}, c={}", line3.getA(), line3.getB(), line3.getC());

        // All three coincident
        if (areCoincident(line1, line2) && areCoincident(line1, line3)) {
            log.info("All three lines are coincident");
            return LinesCalculationResultDTO.builder()
                    .intersectionType(IntersectionType.COINCIDENT)
                    .points(List.of())
                    .build();
        }

        List<PointDTO> uniquePoints = new ArrayList<>();

        addIntersectionPointIfExists(line1, line2, uniquePoints);
        addIntersectionPointIfExists(line1, line3, uniquePoints);
        addIntersectionPointIfExists(line2, line3, uniquePoints);

        log.debug("Found {} unique intersection points", uniquePoints.size());

        IntersectionType type = switch (uniquePoints.size()) {
            case 0 -> IntersectionType.NO_INTERSECTION;
            case 1 -> IntersectionType.ONE_POINT;
            case 2 -> IntersectionType.TWO_POINTS;
            case 3 -> IntersectionType.THREE_POINTS;
            default -> {
                log.error("Unexpected number of intersection points: {}", uniquePoints.size());
                throw new IllegalStateException("Unexpected number of intersection points");
            }
        };

        log.info("Intersection calculation finished. Result type: {}", type);

        return LinesCalculationResultDTO.builder()
                .intersectionType(type)
                .points(List.copyOf(uniquePoints))
                .build();
    }

    /**
     * Computes intersection between two lines in general form and adds it to {@code uniquePoints}
     * only if a single intersection point exists and it is not already in the list (EPSILON-based).
     *
     * @param first        first line in general form: a*x + b*y + c = 0
     * @param second       second line in general form: a*x + b*y + c = 0
     * @param uniquePoints mutable list of unique points
     */
    private void addIntersectionPointIfExists(LineGeneralFormDTO first,
                                              LineGeneralFormDTO second,
                                              List<PointDTO> uniquePoints) {

        log.debug("Calculating intersection between two lines");

        IntersectionPairResult result = intersectTwoLines(first, second);

        switch (result.type) {
            case SINGLE_POINT -> {
                log.debug("Intersection point found: x={}, y={}", result.point.getX(), result.point.getY());
                addUniquePoint(uniquePoints, result.point);
            }
            case PARALLEL -> log.debug("Lines are parallel, no intersection");
            case COINCIDENT -> log.debug("Lines are coincident");
        }
    }

    /**
     * Adds {@code candidate} to {@code uniquePoints} only if there is no existing point considered equal
     * by EPSILON-based comparison.
     *
     * @param uniquePoints list of points to update
     * @param candidate    candidate point
     */
    private void addUniquePoint(List<PointDTO> uniquePoints, PointDTO candidate) {

        boolean exists = uniquePoints.stream().anyMatch(p -> pointsEqual(p, candidate));

        if (!exists) {
            log.debug("Adding new unique point: x={}, y={}", candidate.getX(), candidate.getY());
            uniquePoints.add(candidate);
        } else {
            log.debug("Point already exists, skipping duplicate");
        }
    }

    /**
     * Compares points using EPSILON-based comparison on both coordinates:
     * <pre>
     *     |x1 - x2| <= EPSILON AND |y1 - y2| <= EPSILON
     * </pre>
     *
     * @param p1 first point
     * @param p2 second point
     * @return true if both coordinates are equal within EPSILON
     */
    private boolean pointsEqual(PointDTO p1, PointDTO p2) {
        boolean equal = isZero(p1.getX() - p2.getX()) && isZero(p1.getY() - p2.getY());
        log.trace("Comparing points: equal={}", equal);
        return equal;
    }

    /**
     * Computes intersection result for two lines in general form:
     * <pre>
     *     a1*x + b1*y + c1 = 0
     *     a2*x + b2*y + c2 = 0
     * </pre>
     * <p>
     * Determinant:
     * <pre>
     *     D = a1*b2 - a2*b1
     * </pre>
     * <p>
     * If {@code D != 0} -> single intersection point (Cramer's rule):
     * <pre>
     *     x = (b1*c2 - b2*c1) / D
     *     y = (a2*c1 - a1*c2) / D
     * </pre>
     * <p>
     * If {@code D == 0} -> either coincident or parallel.
     *
     * @param l1 first line in general form
     * @param l2 second line in general form
     * @return pair intersection result (single point / parallel / coincident)
     */
    private IntersectionPairResult intersectTwoLines(LineGeneralFormDTO l1, LineGeneralFormDTO l2) {

        double a1 = l1.getA();
        double b1 = l1.getB();
        double c1 = l1.getC();

        double a2 = l2.getA();
        double b2 = l2.getB();
        double c2 = l2.getC();

        double determinant = a1 * b2 - a2 * b1;
        log.debug("Determinant D={}", determinant);

        if (!isZero(determinant)) {

            double x = (b1 * c2 - b2 * c1) / determinant;
            double y = (a2 * c1 - a1 * c2) / determinant;

            log.debug("Intersection computed: x={}, y={}", x, y);

            return IntersectionPairResult.singlePoint(
                    PointDTO.builder().x(x).y(y).build()
            );
        }

        if (areCoincident(l1, l2)) {
            log.debug("Lines are coincident");
            return IntersectionPairResult.coincident();
        }

        log.debug("Lines are parallel");
        return IntersectionPairResult.parallel();
    }

    /**
     * Checks if two lines in general form are coincident (the same geometric line).
     *
     * <p>The check is performed via three determinants being zero (within EPSILON):
     * <pre>
     *     a1*b2 - a2*b1 = 0
     *     a1*c2 - a2*c1 = 0
     *     b1*c2 - b2*c1 = 0
     * </pre>
     * If all are ~0, then (a1, b1, c1) is proportional to (a2, b2, c2).</p>
     *
     * @param l1 first line
     * @param l2 second line
     * @return true if lines are coincident
     */
    private boolean areCoincident(LineGeneralFormDTO l1, LineGeneralFormDTO l2) {

        double a1 = l1.getA();
        double b1 = l1.getB();
        double c1 = l1.getC();

        double a2 = l2.getA();
        double b2 = l2.getB();
        double c2 = l2.getC();

        boolean coincident =
                isZero(a1 * b2 - a2 * b1) &&
                        isZero(a1 * c2 - a2 * c1) &&
                        isZero(b1 * c2 - b2 * c1);

        log.trace("Coincident check result={}", coincident);

        return coincident;
    }

    /**
     * Returns true if {@code value} is considered zero within EPSILON:
     * <pre>
     *     |value| <= EPSILON
     * </pre>
     *
     * @param value numeric value
     * @return true if value is near zero
     */
    private boolean isZero(double value) {
        boolean result = Math.abs(value) <= Constants.EPSILON;
        log.trace("isZero check: value={}, result={}", value, result);
        return result;
    }

    private enum PairIntersectionType {
        SINGLE_POINT,
        PARALLEL,
        COINCIDENT
    }

    private static final class IntersectionPairResult {

        private final PairIntersectionType type;
        private final PointDTO point;

        private IntersectionPairResult(PairIntersectionType type, PointDTO point) {
            this.type = type;
            this.point = point;
        }

        static IntersectionPairResult singlePoint(PointDTO point) {
            return new IntersectionPairResult(PairIntersectionType.SINGLE_POINT, point);
        }

        static IntersectionPairResult parallel() {
            return new IntersectionPairResult(PairIntersectionType.PARALLEL, null);
        }

        static IntersectionPairResult coincident() {
            return new IntersectionPairResult(PairIntersectionType.COINCIDENT, null);
        }
    }
}