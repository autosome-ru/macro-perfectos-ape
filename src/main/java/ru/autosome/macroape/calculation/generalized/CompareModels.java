package ru.autosome.macroape.calculation.generalized;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findPvalue.FindPvalueAPE;
import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.ape.calculation.findThreshold.FindThresholdAPE;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.motifModel.Alignable;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.ScoreDistribution;

abstract public class CompareModels<ModelType extends Alignable<ModelType> &Discretable<ModelType> &ScoreDistribution<BackgroundType> ,
                                    BackgroundType extends GeneralizedBackgroundModel> {

  public final ModelType firstPWM;
  public final ModelType secondPWM;
  public final BackgroundType firstBackground;
  public final BackgroundType secondBackground;
  public final CanFindPvalue firstPvalueCalculator;
  public final CanFindPvalue secondPvalueCalculator;
  public final Discretizer discretizer;

  public CompareModels(ModelType firstPWM, ModelType secondPWM,
                       BackgroundType firstBackground,
                       BackgroundType secondBackground,
                       CanFindPvalue firstPvalueCalculator,
                       CanFindPvalue secondPvalueCalculator,
                       Discretizer discretizer) {
    this.firstPWM = firstPWM;
    this.secondPWM = secondPWM;
    this.firstBackground = firstBackground;
    this.secondBackground = secondBackground;
    this.firstPvalueCalculator = firstPvalueCalculator;
    this.secondPvalueCalculator = secondPvalueCalculator;
    this.discretizer = discretizer;
  }

  public CompareModels(ModelType firstPWM, ModelType secondPWM,
                       BackgroundType firstBackground,
                       BackgroundType secondBackground,
                       Discretizer discretizer) {
    this.firstPWM = firstPWM;
    this.secondPWM = secondPWM;
    this.firstBackground = firstBackground;
    this.secondBackground = secondBackground;
    this.firstPvalueCalculator = new FindPvalueAPE<ModelType, BackgroundType>(firstPWM, firstBackground, discretizer);
    this.secondPvalueCalculator = new FindPvalueAPE<ModelType, BackgroundType>(secondPWM, secondBackground, discretizer);
    this.discretizer = discretizer;
  }

  abstract protected CompareModelsCountsGiven<ModelType,BackgroundType> calculatorWithCountsGiven();

  double firstCount(double threshold_first) {
    return firstPvalueCalculator
            .pvalueByThreshold(threshold_first)
            .numberOfRecognizedWords(firstBackground, firstPWM.length());
  }

  double secondCount(double threshold_second) {
    return secondPvalueCalculator
            .pvalueByThreshold(threshold_second)
            .numberOfRecognizedWords(secondBackground, secondPWM.length());
  }

  public CompareModelsCountsGiven.SimilarityInfo<ModelType> jaccard(double threshold_first, double threshold_second) {
    return calculatorWithCountsGiven()
            .jaccard(threshold_first, threshold_second,
                     firstCount(threshold_first),
                     secondCount(threshold_second));
  }

  public CompareModelsCountsGiven.SimilarityInfo<ModelType> jaccardAtPosition(double threshold_first, double threshold_second,
                                          Position position) {
    return calculatorWithCountsGiven()
            .jaccardAtPosition(threshold_first, threshold_second,
                               firstCount(threshold_first),
                               secondCount(threshold_second),
                               position);
  }

  public CompareModelsCountsGiven.SimilarityInfo jaccard_by_pvalue(double pvalue) {
    CanFindThreshold canFindThresholdFirst = new FindThresholdAPE<ModelType, BackgroundType>(firstPWM, firstBackground, discretizer);
    CanFindThreshold canFindThresholdSecond = new FindThresholdAPE<ModelType, BackgroundType>(secondPWM, secondBackground, discretizer);

    double threshold_first = canFindThresholdFirst.strongThresholdByPvalue(pvalue).threshold;
    double threshold_second = canFindThresholdSecond.strongThresholdByPvalue(pvalue).threshold;
    return jaccard(threshold_first, threshold_second);
  }

  public CompareModelsCountsGiven.SimilarityInfo jaccard_by_weak_pvalue(double pvalue) {
    CanFindThreshold canFindThresholdFirst = new FindThresholdAPE<ModelType, BackgroundType>(firstPWM, firstBackground, discretizer);
    CanFindThreshold canFindThresholdSecond = new FindThresholdAPE<ModelType, BackgroundType>(secondPWM, secondBackground, discretizer);

    double threshold_first = canFindThresholdFirst.weakThresholdByPvalue(pvalue).threshold;
    double threshold_second = canFindThresholdSecond.weakThresholdByPvalue(pvalue).threshold;
    return jaccard(threshold_first, threshold_second);
  }
}
