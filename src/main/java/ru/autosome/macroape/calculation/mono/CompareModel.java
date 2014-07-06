package ru.autosome.macroape.calculation.mono;

import ru.autosome.macroape.model.PairAligned;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.ape.calculation.findThreshold.FindThresholdAPE;
import ru.autosome.commons.motifModel.mono.PWM;

import java.util.ArrayList;
import java.util.List;

public class CompareModel {

  public final PWM firstPWM;
  public final PWM secondPWM;
  public final BackgroundModel firstBackground;
  public final BackgroundModel secondBackground;
  public final CanFindPvalue firstPvalueCalculator;
  public final CanFindPvalue secondPvalueCalculator;
  public final Double discretization;
  public final Integer maxPairHashSize;

  public CompareModel(PWM firstPWM, PWM secondPWM,
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


  public ru.autosome.macroape.calculation.generalized.CompareModel.SimilarityInfo jaccard(double threshold_first, double threshold_second) throws HashOverflowException {
    return calculatorWithCountsGiven().jaccard(threshold_first, threshold_second,
                                               firstCount(threshold_first),
                                               secondCount(threshold_second));
  }

  public ru.autosome.macroape.calculation.generalized.CompareModel.SimilarityInfo jaccardAtPosition(double threshold_first, double threshold_second,
                                          Position position) throws HashOverflowException {

    return calculatorWithCountsGiven().jaccardAtPosition(threshold_first, threshold_second,
                                                         firstCount(threshold_first),
                                                         secondCount(threshold_second),
                                                         position);
  }


  public ru.autosome.macroape.calculation.generalized.CompareModel.SimilarityInfo jaccard_by_pvalue(double pvalue) throws HashOverflowException {
    CanFindThreshold canFindThresholdFirst = new FindThresholdAPE<PWM,BackgroundModel>(firstPWM, firstBackground, discretization, null);
    CanFindThreshold canFindThresholdSecond = new FindThresholdAPE<PWM,BackgroundModel>(secondPWM, secondBackground, discretization, null);

    double threshold_first = canFindThresholdFirst.strongThresholdByPvalue(pvalue).threshold;
    double threshold_second = canFindThresholdSecond.strongThresholdByPvalue(pvalue).threshold;
    return jaccard(threshold_first, threshold_second);
  }

  public ru.autosome.macroape.calculation.generalized.CompareModel.SimilarityInfo jaccard_by_weak_pvalue(double pvalue) throws HashOverflowException {
    CanFindThreshold canFindThresholdFirst = new FindThresholdAPE<PWM,BackgroundModel>(firstPWM, firstBackground, discretization, null);
    CanFindThreshold canFindThresholdSecond = new FindThresholdAPE<PWM,BackgroundModel>(secondPWM, secondBackground, discretization, null);

    double threshold_first = canFindThresholdFirst.weakThresholdByPvalue(pvalue).threshold;
    double threshold_second = canFindThresholdSecond.weakThresholdByPvalue(pvalue).threshold;
    return jaccard(threshold_first, threshold_second);
  }


  public static class ComparePWMCountsGiven {
    public final PWM firstPWM; // here we store discreted PWMs
    public final PWM secondPWM;
    public final BackgroundModel firstBackground;
    public final BackgroundModel secondBackground;
    public final Double discretization; // PWMs are already stored discreted, disretization needed in order to upscale thresholds
    public final Integer maxPairHashSize;

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

    double firstCountRenormMultiplier(PairAligned alignment) {
      return Math.pow(firstBackground.volume(), alignment.length() - firstPWM.length());
    }
    double secondCountRenormMultiplier(PairAligned alignment) {
      return Math.pow(secondBackground.volume(), alignment.length() - secondPWM.length());
    }

    public ru.autosome.macroape.calculation.generalized.CompareModel.SimilarityInfo jaccard(double thresholdFirst, double thresholdSecond,
                                  double firstCount, double secondCount) throws HashOverflowException {
      double bestSimilarity = -1;
      ru.autosome.macroape.calculation.generalized.CompareModel.SimilarityInfo bestSimilarityInfo = null;
      for (Position position: relative_alignments()) {
        ru.autosome.macroape.calculation.generalized.CompareModel.SimilarityInfo similarityInfo;
        similarityInfo = jaccardAtPosition(thresholdFirst, thresholdSecond, firstCount, secondCount, position);
        double similarity = similarityInfo.similarity();
        if (similarity > bestSimilarity) {
          bestSimilarity = similarity;
          bestSimilarityInfo = similarityInfo;
        }
      }
      return bestSimilarityInfo;
    }

    public ru.autosome.macroape.calculation.generalized.CompareModel.SimilarityInfo jaccardAtPosition(double thresholdFirst, double thresholdSecond,
                                                       double firstCount, double secondCount,
                                                       Position position) throws HashOverflowException {

      PairAligned<PWM> alignment = new PairAligned<PWM>(firstPWM, secondPWM, position);
      AlignedModelIntersection calculator = new AlignedModelIntersection(alignment, firstBackground, secondBackground);
      double intersection = calculator.count_in_intersection(upscaleThreshold(thresholdFirst), upscaleThreshold(thresholdSecond));

      double firstCountRenormed = firstCount * firstCountRenormMultiplier(alignment);
      double secondCountRenormed = secondCount * secondCountRenormMultiplier(alignment);

      return new ru.autosome.macroape.calculation.generalized.CompareModel.SimilarityInfo(alignment, intersection, firstCountRenormed, secondCountRenormed);
    }

  }
}
