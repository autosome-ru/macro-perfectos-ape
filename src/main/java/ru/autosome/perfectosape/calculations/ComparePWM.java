package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.MotifsAligned;
import ru.autosome.perfectosape.Position;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.calculations.ScoringModelDistributions.CountingPWM;
import ru.autosome.perfectosape.calculations.findPvalue.CanFindPvalue;

import java.util.ArrayList;
import java.util.List;

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

  public final CountingPWM firstPWMCounting;
  public final CountingPWM secondPWMCounting;
  public final CanFindPvalue firstPvalueCalculator;
  public final CanFindPvalue secondPvalueCalculator;
  public final Double discretization;
  public Integer maxPairHashSize;

  public ComparePWM(CountingPWM firstPWMCounting, CountingPWM secondPWMCounting,
                    CanFindPvalue firstPvalueCalculator,
                    CanFindPvalue secondPvalueCalculator,
                    Double discretization, Integer maxPairHashSize) {

    this.firstPvalueCalculator = firstPvalueCalculator;
    this.secondPvalueCalculator = secondPvalueCalculator;
    this.discretization = discretization;
    this.maxPairHashSize = maxPairHashSize;
    this.firstPWMCounting = firstPWMCounting.discrete(discretization);
    this.secondPWMCounting = secondPWMCounting.discrete(discretization);
  }

  private ComparePWMCountsGiven calculatorWithCountsGiven() {
    return new ComparePWMCountsGiven(firstPWMCounting, secondPWMCounting,
                                     discretization, maxPairHashSize);
  }

  public SimilarityInfo jaccard(double threshold_first, double threshold_second) throws HashOverflowException {
    double firstCount = firstPvalueCalculator
                         .pvalueByThreshold(threshold_first)
                         .numberOfRecognizedWords(firstPWMCounting.background, firstPWMCounting.length());
    double secondCount = secondPvalueCalculator
                          .pvalueByThreshold(threshold_second)
                          .numberOfRecognizedWords(secondPWMCounting.background, secondPWMCounting.length());

    return calculatorWithCountsGiven().jaccard(threshold_first, threshold_second,
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
    public final CountingPWM firstPWMCounting;
    public final CountingPWM secondPWMCounting;

    public final Double discretization; // PWMs are already stored discreted, disretization needed in order to upscale thresholds
    public Integer maxPairHashSize;

    public ComparePWMCountsGiven(CountingPWM firstPWMCounting, CountingPWM secondPWMCounting,
                                Double discretization, Integer maxPairHashSize) {
      this.discretization = discretization;
      this.maxPairHashSize = maxPairHashSize;

      this.firstPWMCounting = firstPWMCounting;
      this.secondPWMCounting = secondPWMCounting;
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
      for(int shift = -secondPWMCounting.length(); shift <= firstPWMCounting.length(); ++shift) {
        result.add(new Position(shift, true));
        result.add(new Position(shift, false));
      }
      return result;
    }

    double firstCountRenormMultiplier(MotifsAligned alignment) {
      return Math.pow(firstPWMCounting.background.volume(), alignment.length() - firstPWMCounting.length());
    }
    double secondCountRenormMultiplier(MotifsAligned alignment) {
      return Math.pow(secondPWMCounting.background.volume(), alignment.length() - secondPWMCounting.length());
    }

    public SimilarityInfo jaccard(double thresholdFirst, double thresholdSecond,
                                  double firstCount, double secondCount) throws HashOverflowException {
      MotifsAligned<CountingPWM> bestAlignment = null;
      double bestSimilarity = -1;
      double bestIntersection = 0;
      for (Position position: relative_alignments()) {
        MotifsAligned<CountingPWM> alignment = new MotifsAligned<CountingPWM>(firstPWMCounting, secondPWMCounting, position);
        AlignedPWMIntersection calculator = new AlignedPWMIntersection(alignment);
        calculator.maxPairHashSize = maxPairHashSize;
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

      MotifsAligned<CountingPWM> alignment = new MotifsAligned<CountingPWM>(firstPWMCounting, secondPWMCounting, position);
      AlignedPWMIntersection calculator = new AlignedPWMIntersection(alignment);
      calculator.maxPairHashSize = maxPairHashSize;
      double intersection = calculator.count_in_intersection(upscaleThreshold(thresholdFirst), upscaleThreshold(thresholdSecond));

      double firstCountRenormed = firstCount * firstCountRenormMultiplier(alignment);
      double secondCountRenormed = secondCount * secondCountRenormMultiplier(alignment);

      return new SimilarityInfo(alignment, intersection, firstCountRenormed, secondCountRenormed);
    }

  }
}
