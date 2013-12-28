package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.*;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.calculations.findPvalue.CanFindPvalue;
import ru.autosome.perfectosape.motifModels.PWM;

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
}
