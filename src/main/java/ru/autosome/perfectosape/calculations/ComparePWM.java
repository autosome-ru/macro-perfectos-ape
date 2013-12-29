package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.*;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.calculations.findPvalue.CanFindPvalue;
import ru.autosome.perfectosape.motifModels.PWM;

import java.util.ArrayList;
import java.util.List;

public class ComparePWM {
  static public class SimilarityInfo extends AlignedPWMIntersection.SimilarityInfo {
    public final PWMAligned alignment;
    public SimilarityInfo(PWMAligned alignment, double recognizedByBoth, double recognizedByFirst, double recognizedBySecond) {
      super(recognizedByBoth, recognizedByFirst, recognizedBySecond);
      this.alignment = alignment;
    }
    public SimilarityInfo(PWMAligned alignment, AlignedPWMIntersection.SimilarityInfo similarityInfo) {
      super(similarityInfo.recognizedByBoth,
            similarityInfo.recognizedByFirst,
            similarityInfo.recognizedBySecond);
      this.alignment = alignment;
    }
    public Double realPvalueFirst(BackgroundModel background) {
      return realPvalueFirst(background, alignment.length());
    }
    public Double realPvalueSecond(BackgroundModel background) {
      return realPvalueSecond(background, alignment.length());
    }

    public int shift() {
      return alignment.shift();
    }

    public String orientation() {
      return alignment.orientation();
    }

    public int overlap() {
      return alignment.overlapSize();
    }
  }

  public final PWM firstPWM;
  public final PWM secondPWM;
  public final BackgroundModel firstBackground;
  public final BackgroundModel secondBackground;
  public final CanFindPvalue firstPvalueCalculator;
  public final CanFindPvalue secondPvalueCalculator;
  public final Double discretization;
  public Integer maxPairHashSize;

  public ComparePWM(PWM firstPWM, PWM secondPWM,
                    BackgroundModel firstBackground,
                    BackgroundModel secondBackground,
                    CanFindPvalue firstPvalueCalculator,
                    CanFindPvalue secondPvalueCalculator,
                    Double discretization, Integer maxPairHashSize) {
    this.firstPWM = firstPWM;
    this.secondPWM = secondPWM;
    this.firstBackground = firstBackground;
    this.secondBackground = secondBackground;
    this.firstPvalueCalculator = firstPvalueCalculator;
    this.secondPvalueCalculator = secondPvalueCalculator;
    this.discretization = discretization;
    this.maxPairHashSize = maxPairHashSize;
  }

  private ComparePWMCountsGiven calculatorWithCountsGiven() {
    return new ComparePWMCountsGiven(firstPWM, secondPWM,
                                     firstBackground, secondBackground,
                                     discretization, maxPairHashSize);
  }

  public SimilarityInfo jaccard(double threshold_first, double threshold_second) throws HashOverflowException {
    double firstCount = firstPvalueCalculator
                         .pvalueByThreshold(threshold_first)
                         .numberOfRecognizedWords(firstBackground, firstPWM.length());
    double secondCount = secondPvalueCalculator
                          .pvalueByThreshold(threshold_second)
                          .numberOfRecognizedWords(secondBackground, secondPWM.length());

    return calculatorWithCountsGiven().jaccard(threshold_first, threshold_second,
                                               firstCount, secondCount);
  }

  public SimilarityInfo jaccardAtPosition(double threshold_first, double threshold_second,
                                          Position position) throws HashOverflowException {
    double firstCount = firstPvalueCalculator
                         .pvalueByThreshold(threshold_first)
                         .numberOfRecognizedWords(firstBackground, firstPWM.length());
    double secondCount = secondPvalueCalculator
                          .pvalueByThreshold(threshold_second)
                          .numberOfRecognizedWords(secondBackground, secondPWM.length());
    return calculatorWithCountsGiven().jaccardAtPosition(threshold_first, threshold_second,
                                                         firstCount, secondCount, position);
  }

  /*
  public SimilarityInfo jaccard_by_pvalue(double pvalue) throws HashOverflowException {
    double threshold_first = firstPWMCounting.threshold(pvalue).threshold;
    double threshold_second = secondPWMCounting.threshold(pvalue).threshold;
    return jaccard(threshold_first, threshold_second);
  }

  public SimilarityInfo jaccard_by_weak_pvalue(double pvalue) throws HashOverflowException {
    double threshold_first = firstPWMCounting.weak_threshold(pvalue).threshold;
    double threshold_second = secondPWMCounting.weak_threshold(pvalue).threshold;
    return jaccard(threshold_first, threshold_second);
  }
  */

  public static class ComparePWMCountsGiven {
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

    public SimilarityInfo jaccard(double thresholdFirst, double thresholdSecond,
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

      return new SimilarityInfo(bestAlignment, bestIntersection, firstCountRenormedBest, secondCountRenormedBest);

    }

    public SimilarityInfo jaccardAtPosition(double thresholdFirst, double thresholdSecond,
                                                       double firstCount, double secondCount,
                                                       Position position) throws HashOverflowException {

      PWMAligned alignment = new PWMAligned(firstPWM, secondPWM, position);
      AlignedPWMIntersection calculator = new AlignedPWMIntersection(alignment, firstBackground, secondBackground);
      double intersection = calculator.count_in_intersection(upscaleThreshold(thresholdFirst), upscaleThreshold(thresholdSecond));

      double firstCountRenormed = firstCount * firstCountRenormMultiplier(alignment);
      double secondCountRenormed = secondCount * secondCountRenormMultiplier(alignment);

      return new SimilarityInfo(alignment, intersection, firstCountRenormed, secondCountRenormed);
    }

  }
}
