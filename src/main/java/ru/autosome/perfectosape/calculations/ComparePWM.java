package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.Discretizer;
import ru.autosome.perfectosape.MotifsAligned;
import ru.autosome.perfectosape.Position;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.calculations.ScoringModelDistributions.CountingPWM;
import ru.autosome.perfectosape.calculations.findPvalue.CanFindPvalue;

public class ComparePWM {
  static public class SimilarityInfo extends AlignedPWMIntersection.SimilarityInfo {
    public final MotifsAligned alignment;
    public SimilarityInfo(MotifsAligned alignment, double recognizedByBoth, double recognizedByFirst, double recognizedBySecond) {
      super(recognizedByBoth, recognizedByFirst, recognizedBySecond);
      this.alignment = alignment;
    }
    public SimilarityInfo(MotifsAligned<CountingPWM> alignment, AlignedPWMIntersection.SimilarityInfo similarityInfo) {
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

  private final CountingPWM firstPWMCounting;
  private final CountingPWM secondPWMCounting;
  private final CanFindPvalue firstPvalueCalculator;
  private final CanFindPvalue secondPvalueCalculator;
  private final Integer maxPairHashSize;
  private final Discretizer discretizer;

  public ComparePWM(CountingPWM firstPWMCounting, CountingPWM secondPWMCounting,
                    CanFindPvalue firstPvalueCalculator,
                    CanFindPvalue secondPvalueCalculator,
                    Discretizer discretizer,
                    Integer maxPairHashSize) {
    this.firstPWMCounting = firstPWMCounting;
    this.secondPWMCounting = secondPWMCounting;
    this.firstPvalueCalculator = firstPvalueCalculator;
    this.secondPvalueCalculator = secondPvalueCalculator;
    this.maxPairHashSize = maxPairHashSize;
    this.discretizer = discretizer;
  }

  private ComparePWMCountsGiven calculatorWithCountsGiven() {
    return new ComparePWMCountsGiven(firstPWMCounting.discrete(discretizer),
                                     secondPWMCounting.discrete(discretizer),
                                     maxPairHashSize);
  }

  double firstCount(double thresholdFirst) throws HashOverflowException {
    return firstPvalueCalculator
            .pvalueByThreshold(thresholdFirst)
            .numberOfRecognizedWords(firstPWMCounting.background, firstPWMCounting.length());
  }

  double secondCount(double thresholdSecond) throws HashOverflowException {
    return secondPvalueCalculator
            .pvalueByThreshold(thresholdSecond)
            .numberOfRecognizedWords(secondPWMCounting.background, secondPWMCounting.length());
  }

  public SimilarityInfo jaccard(double threshold_first, double threshold_second) throws HashOverflowException {
    return calculatorWithCountsGiven().jaccard(discretizer.upscale(threshold_first),
                                               discretizer.upscale(threshold_second),
                                               firstCount(threshold_first), secondCount(threshold_second));
  }

  public SimilarityInfo jaccardAtPosition(double threshold_first, double threshold_second,
                                          Position position) throws HashOverflowException {
    return calculatorWithCountsGiven().jaccardAtPosition(discretizer.upscale(threshold_first),
                                                         discretizer.upscale(threshold_second),
                                                         firstCount(threshold_first), secondCount(threshold_second),
                                                         position);
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
