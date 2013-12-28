package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.PWMAligned;
import ru.autosome.perfectosape.Position;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.motifModels.PWM;

import java.util.ArrayList;
import java.util.List;

public class ComparePWMCountsGiven {
  public final PWM firstPWM; // here we store discreted PWMs
  public final PWM secondPWM;
  public final BackgroundModel firstBackground;
  public final BackgroundModel secondBackground;
  public final Double discretization; // PWMs are already stored discreted, disretization needed in order to upscale thresholds
  public Integer maxPairHashSize;

  public ComparePWMCountsGiven(PWM firstPWM, PWM secondPWM,
                              BackgroundModel firstBackground,
                              BackgroundModel secondBackground,
                              Double discretization, Integer maxPairHashSize) {
    this.firstPWM = firstPWM.discrete(discretization);
    this.secondPWM = secondPWM.discrete(discretization);
    this.firstBackground = firstBackground;
    this.secondBackground = secondBackground;
    this.discretization = discretization;
    this.maxPairHashSize = maxPairHashSize;
  }

  private double upscaleThreshold(double threshold) {
    if (discretization == null) {
      return threshold;
    } else {
      return threshold * discretization;
    }
  }

  private List<Position> relative_alignments() {
    List<Position> result = new ArrayList<Position>();
    for(int shift = -secondPWM.length(); shift <= firstPWM.length(); ++shift) {
      result.add(new Position(shift, true));
      result.add(new Position(shift, false));
    }
    return result;
  }

  double firstCountRenormMultiplier(PWMAligned alignment) {
    return Math.pow(firstBackground.volume(), alignment.length() - firstPWM.length());
  }
  double secondCountRenormMultiplier(PWMAligned alignment) {
    return Math.pow(secondBackground.volume(), alignment.length() - secondPWM.length());
  }

  public ComparePWM.SimilarityInfo jaccard(double thresholdFirst, double thresholdSecond,
                                           double firstCount, double secondCount) throws HashOverflowException {
    PWMAligned bestAlignment = null;
    double bestSimilarity = -1;
    double bestIntersection = 0;
    for (Position position: relative_alignments()) {
      PWMAligned alignment = new PWMAligned(firstPWM, secondPWM, position);
      AlignedPWMIntersection calculator = new AlignedPWMIntersection(alignment, firstBackground, secondBackground);
      double intersection = calculator.count_in_intersection(upscaleThreshold(thresholdFirst), upscaleThreshold(thresholdSecond));

      double firstCountRenormed = firstCount * firstCountRenormMultiplier(alignment);
      double secondCountRenormed = secondCount * secondCountRenormMultiplier(alignment);
      double similarity = AlignedPWMIntersection.SimilarityInfo.jaccardByCounts(firstCountRenormed,
                                                                                secondCountRenormed,
                                                                                intersection);
      if (similarity > bestSimilarity) {
        bestAlignment = alignment;
        bestSimilarity = similarity;
        bestIntersection = intersection;
      }
    }

    double firstCountRenormedBest = firstCount * firstCountRenormMultiplier(bestAlignment);
    double secondCountRenormedBest = secondCount * secondCountRenormMultiplier(bestAlignment);

    return new ComparePWM.SimilarityInfo(bestAlignment, bestIntersection, firstCountRenormedBest, secondCountRenormedBest);

  }

  public ComparePWM.SimilarityInfo jaccardAtPosition(double thresholdFirst, double thresholdSecond,
                                                     double firstCount, double secondCount,
                                                     Position position) throws HashOverflowException {

    PWMAligned alignment = new PWMAligned(firstPWM, secondPWM, position);
    AlignedPWMIntersection calculator = new AlignedPWMIntersection(alignment, firstBackground, secondBackground);
    double intersection = calculator.count_in_intersection(upscaleThreshold(thresholdFirst), upscaleThreshold(thresholdSecond));

    double firstCountRenormed = firstCount * firstCountRenormMultiplier(alignment);
    double secondCountRenormed = secondCount * secondCountRenormMultiplier(alignment);

    return new ComparePWM.SimilarityInfo(alignment, intersection, firstCountRenormed, secondCountRenormed);
  }

}
