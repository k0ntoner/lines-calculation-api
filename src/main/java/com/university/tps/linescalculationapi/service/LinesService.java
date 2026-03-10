package com.university.tps.linescalculationapi.service;

import com.university.tps.linescalculationapi.constant.Constants;
import com.university.tps.linescalculationapi.dto.LineDTO;
import com.university.tps.linescalculationapi.dto.LineGeneralFormDTO;
import com.university.tps.linescalculationapi.dto.LinesCalculationResultDTO;
import com.university.tps.linescalculationapi.dto.LinesDTO;
import com.university.tps.linescalculationapi.dto.PointDTO;
import com.university.tps.linescalculationapi.enums.IntersectionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for calculating the intersection type for exactly three lines.
 *
 * <p>This implementation is organized according to the theoretical equivalence classes
 * derived for the specific input combination:</p>
 *
 * <pre>
 * line 1 -> form (4): x / a1 + y / b1 = 1
 * line 2 -> form (5): y = k2 * x + b2
 * line 3 -> form (5): y = k3 * x + b3
 * </pre>
 *
 * <p>The service uses the already available conversion to the general form:</p>
 *
 * <pre>
 * A*x + B*y + C = 0
 * </pre>
 *
 * <p>For the fixed case (4,5,5), the coefficients used in the derived formulas are:</p>
 *
 * <pre>
 * A1 = b1,  B1 = a1,  C1 = -a1*b1
 * A2 = k2,  B2 = -1,  C2 = b2
 * A3 = k3,  B3 = -1,  C3 = b3
 * </pre>
 *
 * <p>The classification is then performed according to the derived conditions
 * for classes 2, 3, 4 and 5.</p>
 *
 * <p>Although class 1 is considered impossible in the chosen analytical setup,
 * a technical coincident check is still kept as a safeguard.</p>
 */
@Service
@Slf4j
public class LinesService {

    /**
     * Calculates the intersection result for exactly three lines.
     *
     * <p>The method assumes the ordered input:</p>
     * <pre>
     * index 0 -> line of type (4)
     * index 1 -> line of type (5)
     * index 2 -> line of type (5)
     * </pre>
     *
     * @param linesDTO DTO containing exactly three lines
     * @return result DTO with intersection type and corresponding unique intersection points
     * @throws IllegalArgumentException if input is null or does not contain exactly three lines
     * @throws IllegalStateException if none of the expected classes can be determined
     */
    public LinesCalculationResultDTO calculateIntersections(LinesDTO linesDTO) {
        log.info("Starting intersection calculation for case (4,5,5)");

        validateInput(linesDTO);

        List<LineDTO> inputLines = linesDTO.getLines();

        LineGeneralFormDTO line1 = inputLines.get(0).toGeneralDTO();
        LineGeneralFormDTO line2 = inputLines.get(1).toGeneralDTO();
        LineGeneralFormDTO line3 = inputLines.get(2).toGeneralDTO();

        Coefficients455 coefficients = extractCoefficientsForCase455(line1, line2, line3);

        log.debug(
                "Extracted coefficients: a1={}, b1={}, k2={}, b2={}, k3={}, b3={}",
                coefficients.a1,
                coefficients.b1,
                coefficients.k2,
                coefficients.b2,
                coefficients.k3,
                coefficients.b3
        );

        /**
         * Technical safeguard.
         * Even though class 1 is not part of the target analytical implementation,
         * this check protects the system from unexpected coincident input.
         */
        if (areCoincident(line1, line2) && areCoincident(line1, line3)) {
            log.info("All three lines are coincident");
            return LinesCalculationResultDTO.builder()
                    .intersectionType(IntersectionType.COINCIDENT)
                    .points(List.of())
                    .build();
        }

        if (isClass2NoIntersection(coefficients)) {
            log.info("Detected class 2: no intersection points");
            return LinesCalculationResultDTO.builder()
                    .intersectionType(IntersectionType.NO_INTERSECTION)
                    .points(List.of())
                    .build();
        }

        if (isClass3OnePoint(coefficients)) {
            log.info("Detected class 3: one common intersection point");
            List<PointDTO> uniquePoints = collectUniqueIntersectionPoints(line1, line2, line3);
            return LinesCalculationResultDTO.builder()
                    .intersectionType(IntersectionType.ONE_POINT)
                    .points(List.copyOf(uniquePoints))
                    .build();
        }

        if (isClass4TwoPoints(coefficients)) {
            log.info("Detected class 4: two intersection points");
            List<PointDTO> uniquePoints = collectUniqueIntersectionPoints(line1, line2, line3);
            return LinesCalculationResultDTO.builder()
                    .intersectionType(IntersectionType.TWO_POINTS)
                    .points(List.copyOf(uniquePoints))
                    .build();
        }

        if (isClass5ThreePoints(coefficients)) {
            log.info("Detected class 5: three intersection points");
            List<PointDTO> uniquePoints = collectUniqueIntersectionPoints(line1, line2, line3);
            return LinesCalculationResultDTO.builder()
                    .intersectionType(IntersectionType.THREE_POINTS)
                    .points(List.copyOf(uniquePoints))
                    .build();
        }

        log.error("Failed to classify lines for case (4,5,5)");
        throw new IllegalStateException("Unable to determine intersection class for case (4,5,5)");
    }

    /**
     * Validates input DTO structure.
     *
     * @param linesDTO input DTO
     * @throws IllegalArgumentException if DTO, list, or list size is invalid
     */
    private void validateInput(LinesDTO linesDTO) {
        if (linesDTO == null || linesDTO.getLines() == null) {
            log.error("LinesDTO or lines list is null");
            throw new IllegalArgumentException("LinesDTO and lines list must not be null");
        }

        if (linesDTO.getLines().size() != 3) {
            log.error("Invalid number of lines: {}", linesDTO.getLines().size());
            throw new IllegalArgumentException("Exactly 3 lines are required");
        }
    }

    /**
     * Extracts coefficients used in the derived formulas for the fixed case (4,5,5).
     *
     * <p>From general form:</p>
     *
     * <pre>
     * line 1: b1*x + a1*y - a1*b1 = 0  -> a1 = B1, b1 = A1
     * line 2: k2*x - y + b2 = 0        -> k2 = A2, b2 = C2
     * line 3: k3*x - y + b3 = 0        -> k3 = A3, b3 = C3
     * </pre>
     *
     * @param line1 first line in general form
     * @param line2 second line in general form
     * @param line3 third line in general form
     * @return extracted coefficients container
     */
    private Coefficients455 extractCoefficientsForCase455(LineGeneralFormDTO line1,
                                                          LineGeneralFormDTO line2,
                                                          LineGeneralFormDTO line3) {

        double a1 = line1.getB();
        double b1 = line1.getA();

        double k2 = line2.getA();
        double b2 = line2.getC();

        double k3 = line3.getA();
        double b3 = line3.getC();

        return new Coefficients455(a1, b1, k2, b2, k3, b3);
    }

    /**
     * Checks class 2 for case (4,5,5).
     *
     * <p>Class 2 means that the three lines have no common intersection points.
     * According to the derived formulas, this happens when:</p>
     *
     * <pre>
     * -b1 - k2*a1 = 0
     * -b1 - k3*a1 = 0
     * (b1*b2 + k2*a1*b1)^2 + (b1*b3 + k3*a1*b1)^2 != 0
     * </pre>
     *
     * @param c extracted coefficients
     * @return true if class 2 condition holds
     */
    private boolean isClass2NoIntersection(Coefficients455 c) {
        double parallel12 = -c.b1 - c.k2 * c.a1;
        double parallel13 = -c.b1 - c.k3 * c.a1;

        double coincidencePart12 = c.b1 * c.b2 + c.k2 * c.a1 * c.b1;
        double coincidencePart13 = c.b1 * c.b3 + c.k3 * c.a1 * c.b1;

        return isZero(parallel12)
                && isZero(parallel13)
                && !isZero(square(coincidencePart12) + square(coincidencePart13));
    }

    /**
     * Checks class 3 for case (4,5,5).
     *
     * <p>Class 3 means that all three lines intersect in exactly one common point.</p>
     *
     * <p>The derived condition is:</p>
     *
     * <pre>
     * (-b1 - k2*a1 != 0) OR (k3 - k2 != 0) OR (-b1 - k3*a1 != 0)
     * </pre>
     *
     * <p>and simultaneously:</p>
     *
     * <pre>
     * b1*(b2 - b3) - k2*a1*(b3 - b1) + k3*a1*(b2 - b1) = 0
     * </pre>
     *
     * @param c extracted coefficients
     * @return true if class 3 condition holds
     */
    private boolean isClass3OnePoint(Coefficients455 c) {
        boolean notAllParallel =
                !isZero(-c.b1 - c.k2 * c.a1)
                        || !isZero(c.k3 - c.k2)
                        || !isZero(-c.b1 - c.k3 * c.a1);

        double concurrencyDeterminant =
                c.b1 * (c.b2 - c.b3)
                        - c.k2 * c.a1 * (c.b3 - c.b1)
                        + c.k3 * c.a1 * (c.b2 - c.b1);

        return notAllParallel && isZero(concurrencyDeterminant);
    }

    /**
     * Checks class 4 for case (4,5,5).
     *
     * <p>Class 4 means that there are exactly two intersection points.
     * This happens when exactly one pair of lines is parallel but not coincident,
     * and the third line intersects both of them.</p>
     *
     * <p>The derived condition is the logical OR of three cases:</p>
     *
     * <pre>
     * 1) (-b1-k2*a1 = 0) AND (b1*b2 + k2*a1*b1 != 0) AND (-b1-k3*a1 != 0)
     * 2) (-b1-k3*a1 = 0) AND (b1*b3 + k3*a1*b1 != 0) AND (-b1-k2*a1 != 0)
     * 3) (k3-k2 = 0) AND (k2*b3 - k3*b2 != 0) AND (-b1-k3*a1 != 0)
     * </pre>
     *
     * @param c extracted coefficients
     * @return true if class 4 condition holds
     */
    private boolean isClass4TwoPoints(Coefficients455 c) {
        boolean case12Parallel =
                isZero(-c.b1 - c.k2 * c.a1)
                        && !isZero(c.b1 * c.b2 + c.k2 * c.a1 * c.b1)
                        && !isZero(-c.b1 - c.k3 * c.a1);

        boolean case13Parallel =
                isZero(-c.b1 - c.k3 * c.a1)
                        && !isZero(c.b1 * c.b3 + c.k3 * c.a1 * c.b1)
                        && !isZero(-c.b1 - c.k2 * c.a1);

        boolean case23Parallel =
                isZero(c.k3 - c.k2)
                        && !isZero(c.k2 * c.b3 - c.k3 * c.b2)
                        && !isZero(-c.b1 - c.k3 * c.a1);

        return case12Parallel || case13Parallel || case23Parallel;
    }

    /**
     * Checks class 5 for case (4,5,5).
     *
     * <p>Class 5 means that each pair of lines intersects in its own distinct point,
     * so the total number of intersection points is three.</p>
     *
     * <p>The derived condition is:</p>
     *
     * <pre>
     * (-b1 - k2*a1 != 0)
     * AND (k3 - k2 != 0)
     * AND (-b1 - k3*a1 != 0)
     * AND
     * (b1*(b2-b3) - k2*a1*(b3-b1) + k3*a1*(b2-b1) != 0)
     * </pre>
     *
     * @param c extracted coefficients
     * @return true if class 5 condition holds
     */
    private boolean isClass5ThreePoints(Coefficients455 c) {
        boolean noParallelPairs =
                !isZero(-c.b1 - c.k2 * c.a1)
                        && !isZero(c.k3 - c.k2)
                        && !isZero(-c.b1 - c.k3 * c.a1);

        double concurrencyDeterminant =
                c.b1 * (c.b2 - c.b3)
                        - c.k2 * c.a1 * (c.b3 - c.b1)
                        + c.k3 * c.a1 * (c.b2 - c.b1);

        return noParallelPairs && !isZero(concurrencyDeterminant);
    }

    /**
     * Collects all unique intersection points between the three input lines.
     *
     * <p>Pairwise intersections are checked for pairs (1,2), (1,3), (2,3).
     * Only single-point intersections are added. Parallel and coincident pairs
     * do not contribute points.</p>
     *
     * @param line1 first line
     * @param line2 second line
     * @param line3 third line
     * @return list of unique intersection points
     */
    private List<PointDTO> collectUniqueIntersectionPoints(LineGeneralFormDTO line1,
                                                           LineGeneralFormDTO line2,
                                                           LineGeneralFormDTO line3) {

        List<PointDTO> uniquePoints = new ArrayList<>();

        addIntersectionPointIfExists(line1, line2, uniquePoints);
        addIntersectionPointIfExists(line1, line3, uniquePoints);
        addIntersectionPointIfExists(line2, line3, uniquePoints);

        return uniquePoints;
    }

    /**
     * Computes intersection between two lines in general form and adds it to {@code uniquePoints}
     * if the intersection is a single point and such point is not already present.
     *
     * @param first first line in general form
     * @param second second line in general form
     * @param uniquePoints mutable list of unique points
     */
    private void addIntersectionPointIfExists(LineGeneralFormDTO first,
                                              LineGeneralFormDTO second,
                                              List<PointDTO> uniquePoints) {

        IntersectionPairResult result = intersectTwoLines(first, second);

        if (result.type == PairIntersectionType.SINGLE_POINT) {
            addUniquePoint(uniquePoints, result.point);
        }
    }

    /**
     * Adds a point to the list only if there is no equal point already present.
     *
     * @param uniquePoints list of unique points
     * @param candidate point to add
     */
    private void addUniquePoint(List<PointDTO> uniquePoints, PointDTO candidate) {
        boolean exists = uniquePoints.stream().anyMatch(point -> pointsEqual(point, candidate));

        if (!exists) {
            uniquePoints.add(candidate);
        }
    }

    /**
     * Compares two points using EPSILON-based comparison.
     *
     * @param first first point
     * @param second second point
     * @return true if coordinates are equal within EPSILON
     */
    private boolean pointsEqual(PointDTO first, PointDTO second) {
        return isZero(first.getX() - second.getX()) && isZero(first.getY() - second.getY());
    }

    /**
     * Computes the intersection result for two lines in general form.
     *
     * <p>For lines:</p>
     * <pre>
     * a1*x + b1*y + c1 = 0
     * a2*x + b2*y + c2 = 0
     * </pre>
     *
     * <p>The determinant is:</p>
     * <pre>
     * D = a1*b2 - a2*b1
     * </pre>
     *
     * <p>If D != 0, the lines intersect at one point:
     * <pre>
     * x = (b1*c2 - b2*c1) / D
     * y = (a2*c1 - a1*c2) / D
     * </pre>
     *
     * <p>If D == 0, the lines are either coincident or parallel.</p>
     *
     * @param first first line
     * @param second second line
     * @return pair intersection result
     */
    private IntersectionPairResult intersectTwoLines(LineGeneralFormDTO first, LineGeneralFormDTO second) {
        double a1 = first.getA();
        double b1 = first.getB();
        double c1 = first.getC();

        double a2 = second.getA();
        double b2 = second.getB();
        double c2 = second.getC();

        double determinant = a1 * b2 - a2 * b1;

        if (!isZero(determinant)) {
            double x = (b1 * c2 - b2 * c1) / determinant;
            double y = (a2 * c1 - a1 * c2) / determinant;

            return IntersectionPairResult.singlePoint(
                    PointDTO.builder()
                            .x(x)
                            .y(y)
                            .build()
            );
        }

        if (areCoincident(first, second)) {
            return IntersectionPairResult.coincident();
        }

        return IntersectionPairResult.parallel();
    }

    /**
     * Checks whether two lines in general form are coincident.
     *
     * <p>Two lines are coincident if all three determinants are zero:</p>
     *
     * <pre>
     * a1*b2 - a2*b1 = 0
     * a1*c2 - a2*c1 = 0
     * b1*c2 - b2*c1 = 0
     * </pre>
     *
     * @param first first line
     * @param second second line
     * @return true if the lines are coincident
     */
    private boolean areCoincident(LineGeneralFormDTO first, LineGeneralFormDTO second) {
        double a1 = first.getA();
        double b1 = first.getB();
        double c1 = first.getC();

        double a2 = second.getA();
        double b2 = second.getB();
        double c2 = second.getC();

        return isZero(a1 * b2 - a2 * b1)
                && isZero(a1 * c2 - a2 * c1)
                && isZero(b1 * c2 - b2 * c1);
    }

    /**
     * Returns the square of a value.
     *
     * @param value input value
     * @return value squared
     */
    private double square(double value) {
        return value * value;
    }

    /**
     * Checks whether a value is zero within EPSILON tolerance.
     *
     * @param value input value
     * @return true if absolute value is less than or equal to EPSILON
     */
    private boolean isZero(double value) {
        return Math.abs(value) <= Constants.EPSILON;
    }

    /**
     * Pair classification for two-line intersection.
     */
    private enum PairIntersectionType {
        SINGLE_POINT,
        PARALLEL,
        COINCIDENT
    }

    /**
     * Internal result holder for the intersection of two lines.
     */
    private static final class IntersectionPairResult {

        private final PairIntersectionType type;
        private final PointDTO point;

        private IntersectionPairResult(PairIntersectionType type, PointDTO point) {
            this.type = type;
            this.point = point;
        }

        private static IntersectionPairResult singlePoint(PointDTO point) {
            return new IntersectionPairResult(PairIntersectionType.SINGLE_POINT, point);
        }

        private static IntersectionPairResult parallel() {
            return new IntersectionPairResult(PairIntersectionType.PARALLEL, null);
        }

        private static IntersectionPairResult coincident() {
            return new IntersectionPairResult(PairIntersectionType.COINCIDENT, null);
        }
    }

    /**
     * Container with coefficients used in the derived formulas for case (4,5,5).
     *
     * @param a1 coefficient a1 from the intercept form line
     * @param b1 coefficient b1 from the intercept form line
     * @param k2 slope coefficient of line 2
     * @param b2 intercept coefficient of line 2
     * @param k3 slope coefficient of line 3
     * @param b3 intercept coefficient of line 3
     */
    private record Coefficients455(
            double a1,
            double b1,
            double k2,
            double b2,
            double k3,
            double b3
    ) {
    }
}