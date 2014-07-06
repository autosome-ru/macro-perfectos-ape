package ru.autosome.macroape.calculation.generalized;

import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.motifModel.Alignable;
import ru.autosome.macroape.model.PairAligned;

abstract public class AlignedModelIntersection <ModelType extends Alignable<ModelType>,
                                       BackgroundType extends GeneralizedBackgroundModel> {
  public final BackgroundType firstBackground;
  public final BackgroundType secondBackground;
  public final PairAligned<ModelType> alignment;
  public Double maxPairHashSize;

  public AlignedModelIntersection(PairAligned<ModelType> alignment,
                                  BackgroundType firstBackground, BackgroundType secondBackground) {
    this.firstBackground = firstBackground;
    this.secondBackground = secondBackground;
    this.alignment = alignment;
  }

  public AlignedModelIntersection(ModelType firstPWM, ModelType secondPWM,
                                  BackgroundType firstBackground, BackgroundType secondBackground,
                                  Position relativePosition) {
    this.firstBackground = firstBackground;
    this.secondBackground = secondBackground;
    this.alignment = new PairAligned<ModelType>(firstPWM, secondPWM, relativePosition);
  }

  public double count_in_intersection(double first_threshold, double second_threshold) throws HashOverflowException {
    double[] intersections = counts_for_two_matrices(first_threshold, second_threshold);

    return combine_intersection_values(intersections[0], intersections[1]);
  }

  public double combine_intersection_values(double intersection_count_1, double intersection_count_2) {
    return Math.sqrt(intersection_count_1 * intersection_count_2);
  }

  private double[] counts_for_two_matrices(double threshold_first, double threshold_second) throws HashOverflowException {
    if (firstBackground.equals(secondBackground)) {
      final BackgroundType background = firstBackground;
      double result = get_counts(threshold_first, threshold_second, background);

      return new double[] {result, result};
    } else {
      // unoptimized code (two-pass instead of one) but it's rare case
      double first_result = get_counts(threshold_first, threshold_second, firstBackground);
      double second_result = get_counts(threshold_first, threshold_second, secondBackground);

      return new double[] {first_result, second_result};
    }
  }

  abstract protected double get_counts(double threshold_first, double threshold_second, BackgroundType background) throws HashOverflowException;
}