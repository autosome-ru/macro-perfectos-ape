package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.PairAligned;
import ru.autosome.perfectosape.Position;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.calculations.findPvalue.CanFindPvalue;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThreshold;
import ru.autosome.perfectosape.calculations.findThreshold.FindThresholdAPE;
import ru.autosome.perfectosape.motifModels.DiPWM;
import ru.autosome.perfectosape.motifModels.PWM;

import java.util.ArrayList;
import java.util.List;

public class CompareDiPWM {
  public final DiPWM firstPWM;
  public final DiPWM secondPWM;
  public final DiBackgroundModel firstBackground;
  public final DiBackgroundModel secondBackground;
  public final CanFindPvalue firstPvalueCalculator;
  public final CanFindPvalue secondPvalueCalculator;
  public final Double discretization;
  public final Integer maxPairHashSize;

  public CompareDiPWM(DiPWM firstPWM, DiPWM secondPWM,
                    DiBackgroundModel firstBackground,
                    DiBackgroundModel secondBackground,
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

  private CompareDiPWMCountsGiven calculatorWithCountsGiven() {
    return new CompareDiPWMCountsGiven(firstPWM, secondPWM,
                                     firstBackground, secondBackground,
                                     discretization, maxPairHashSize);
  }

  double firstCount(double threshold_first) throws HashOverflowException {
    return firstPvalueCalculator
            .pvalueByThreshold(threshold_first)
            .numberOfRecognizedWords(firstBackground, firstPWM.length());
  }

  double secondCount(double threshold_second) throws HashOverflowException {
    return secondPvalueCalculator
            .pvalueByThreshold(threshold_second)
            .numberOfRecognizedWords(secondBackground, secondPWM.length());
  }


  public CompareModels.SimilarityInfo jaccard(double threshold_first, double threshold_second) throws HashOverflowException {
    return calculatorWithCountsGiven().jaccard(threshold_first, threshold_second,
                                               firstCount(threshold_first),
                                               secondCount(threshold_second));
  }

  public CompareModels.SimilarityInfo jaccardAtPosition(double threshold_first, double threshold_second,
                                                        Position position) throws HashOverflowException {

    return calculatorWithCountsGiven().jaccardAtPosition(threshold_first, threshold_second,
                                                         firstCount(threshold_first),
                                                         secondCount(threshold_second),
                                                         position);
  }

  public CompareModels.SimilarityInfo jaccard_by_pvalue(double pvalue) throws HashOverflowException {
    CanFindThreshold canFindThresholdFirst = new FindThresholdAPE<DiPWM,DiBackgroundModel>(firstPWM, firstBackground, discretization, null);
    CanFindThreshold canFindThresholdSecond = new FindThresholdAPE<DiPWM,DiBackgroundModel>(secondPWM, secondBackground, discretization, null);

    double threshold_first = canFindThresholdFirst.strongThresholdByPvalue(pvalue).threshold;
    double threshold_second = canFindThresholdSecond.strongThresholdByPvalue(pvalue).threshold;
    return jaccard(threshold_first, threshold_second);
  }

  public CompareModels.SimilarityInfo jaccard_by_weak_pvalue(double pvalue) throws HashOverflowException {
    CanFindThreshold canFindThresholdFirst = new FindThresholdAPE<DiPWM,DiBackgroundModel>(firstPWM, firstBackground, discretization, null);
    CanFindThreshold canFindThresholdSecond = new FindThresholdAPE<DiPWM,DiBackgroundModel>(secondPWM, secondBackground, discretization, null);

    double threshold_first = canFindThresholdFirst.weakThresholdByPvalue(pvalue).threshold;
    double threshold_second = canFindThresholdSecond.weakThresholdByPvalue(pvalue).threshold;
    return jaccard(threshold_first, threshold_second);
  }

  public static class CompareDiPWMCountsGiven {
    public final DiPWM firstPWM; // here we store discreted PWMs
    public final DiPWM secondPWM;
    public final DiBackgroundModel firstBackground;
    public final DiBackgroundModel secondBackground;
    public final Double discretization; // PWMs are already stored discreted, disretization needed in order to upscale thresholds
    public final Integer maxPairHashSize;

    public CompareDiPWMCountsGiven(DiPWM firstPWM, DiPWM secondPWM,
                                 DiBackgroundModel firstBackground,
                                 DiBackgroundModel secondBackground,
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

    double firstCountRenormMultiplier(PairAligned alignment) {
      return Math.pow(firstBackground.volume(), alignment.length() - firstPWM.length());
    }
    double secondCountRenormMultiplier(PairAligned alignment) {
      return Math.pow(secondBackground.volume(), alignment.length() - secondPWM.length());
    }

    public CompareModels.SimilarityInfo jaccard(double thresholdFirst, double thresholdSecond,
                                                double firstCount, double secondCount) throws HashOverflowException {
      double bestSimilarity = -1;
      CompareModels.SimilarityInfo bestSimilarityInfo = null;
      for (Position position: relative_alignments()) {
        CompareModels.SimilarityInfo similarityInfo;
        similarityInfo = jaccardAtPosition(thresholdFirst, thresholdSecond, firstCount, secondCount, position);
        double similarity = similarityInfo.similarity();
        if (similarity > bestSimilarity) {
          bestSimilarity = similarity;
          bestSimilarityInfo = similarityInfo;
        }
      }
      return bestSimilarityInfo;
    }

    public CompareModels.SimilarityInfo jaccardAtPosition(double thresholdFirst, double thresholdSecond,
                                                          double firstCount, double secondCount,
                                                          Position position) throws HashOverflowException {

      PairAligned<DiPWM> alignment = new PairAligned<DiPWM>(firstPWM, secondPWM, position);
      AlignedDiPWMIntersection calculator = new AlignedDiPWMIntersection(alignment, firstBackground, secondBackground);
      double intersection = calculator.count_in_intersection(upscaleThreshold(thresholdFirst), upscaleThreshold(thresholdSecond));

      double firstCountRenormed = firstCount * firstCountRenormMultiplier(alignment);
      double secondCountRenormed = secondCount * secondCountRenormMultiplier(alignment);

      return new CompareModels.SimilarityInfo(alignment, intersection, firstCountRenormed, secondCountRenormed);
    }

  }
}
