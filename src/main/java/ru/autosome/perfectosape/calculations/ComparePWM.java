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
  };

  public final CountingPWM firstPWMCounting;
  public final CountingPWM secondPWMCounting;
  public final CanFindPvalue firstPvalueCalculator;
  public final CanFindPvalue secondPvalueCalculator;
  public final Discretizer discretizer;
  public Integer maxPairHashSize;

  public ComparePWM(CountingPWM firstPWMCounting, CountingPWM secondPWMCounting,
                    CanFindPvalue firstPvalueCalculator,
                    CanFindPvalue secondPvalueCalculator,
                    Double discretization, Integer maxPairHashSize) {
    this.firstPWMCounting = firstPWMCounting.discrete(discretization);
    this.secondPWMCounting = secondPWMCounting.discrete(discretization);
    this.firstPvalueCalculator = firstPvalueCalculator;
    this.secondPvalueCalculator = secondPvalueCalculator;
    this.discretizer = new Discretizer(discretization);
    this.maxPairHashSize = maxPairHashSize;
  }

  private ComparePWMCountsGiven calculatorWithCountsGiven() {
    return new ComparePWMCountsGiven(firstPWMCounting, secondPWMCounting, maxPairHashSize);
  }

  public SimilarityInfo jaccard(double threshold_first, double threshold_second) throws HashOverflowException {
    double firstCount = firstPvalueCalculator
                         .pvalueByThreshold(threshold_first)
                         .numberOfRecognizedWords(firstPWMCounting.background, firstPWMCounting.length());
    double secondCount = secondPvalueCalculator
                          .pvalueByThreshold(threshold_second)
                          .numberOfRecognizedWords(secondPWMCounting.background, secondPWMCounting.length());

    return calculatorWithCountsGiven().jaccard(discretizer.upscale(threshold_first),
                                               discretizer.upscale(threshold_second),
                                               firstCount, secondCount);
  }

  public SimilarityInfo jaccardAtPosition(double threshold_first, double threshold_second,
                                          Position position) throws HashOverflowException {
    double firstCount = firstPvalueCalculator
                         .pvalueByThreshold(threshold_first)
                         .numberOfRecognizedWords(firstPWMCounting.background, firstPWMCounting.length());
    double secondCount = secondPvalueCalculator
                          .pvalueByThreshold(threshold_second)
                          .numberOfRecognizedWords(secondPWMCounting.background, secondPWMCounting.length());
    return calculatorWithCountsGiven().jaccardAtPosition(discretizer.upscale(threshold_first),
                                                         discretizer.upscale(threshold_second),
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
