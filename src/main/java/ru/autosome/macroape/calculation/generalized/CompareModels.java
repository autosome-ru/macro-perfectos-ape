package ru.autosome.macroape.calculation.generalized;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findPvalue.FindPvalueAPE;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.motifModel.Alignable;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.ScoreDistribution;
import ru.autosome.macroape.model.ComparisonSimilarityInfo;

public class CompareModels<ModelType extends Alignable<ModelType> & Discretable<ModelType> & ScoreDistribution<BackgroundType>,
                           BackgroundType extends GeneralizedBackgroundModel> {

  public final ModelType firstPWM;
  public final ModelType secondPWM;
  public final BackgroundType background;
  public final CanFindPvalue firstPvalueCalculator;
  public final CanFindPvalue secondPvalueCalculator;
  public final Discretizer discretizer;
  final CompareModelsCountsGiven calculatorWithCountsGiven;

  public CompareModels(ModelType firstPWM, ModelType secondPWM,
                       BackgroundType background,
                       CanFindPvalue firstPvalueCalculator,
                       CanFindPvalue secondPvalueCalculator,
                       Discretizer discretizer,
                       CompareModelsCountsGiven calculatorWithCountsGiven) {
    this.firstPWM = firstPWM;
    this.secondPWM = secondPWM;
    this.background = background;
    this.firstPvalueCalculator = firstPvalueCalculator;
    this.secondPvalueCalculator = secondPvalueCalculator;
    this.discretizer = discretizer;
    this.calculatorWithCountsGiven = calculatorWithCountsGiven;

  }

  public CompareModels(ModelType firstPWM, ModelType secondPWM,
                       BackgroundType background,
                       Discretizer discretizer,
                       CompareModelsCountsGiven calculatorWithCountsGiven) {
    this.firstPWM = firstPWM;
    this.secondPWM = secondPWM;
    this.background = background;
    this.firstPvalueCalculator = new FindPvalueAPE<>(firstPWM, background, discretizer);
    this.secondPvalueCalculator = new FindPvalueAPE<>(secondPWM, background, discretizer);
    this.discretizer = discretizer;
    this.calculatorWithCountsGiven = calculatorWithCountsGiven;
  }


  double firstCount(double threshold_first) {
    return firstPvalueCalculator
            .pvalueByThreshold(threshold_first)
            .numberOfRecognizedWords(background, firstPWM.length());
  }

  double secondCount(double threshold_second) {
    return secondPvalueCalculator
            .pvalueByThreshold(threshold_second)
            .numberOfRecognizedWords(background, secondPWM.length());
  }

  public ComparisonSimilarityInfo jaccard(double threshold_first, double threshold_second) {
    return calculatorWithCountsGiven
            .jaccard(threshold_first, threshold_second,
                     firstCount(threshold_first),
                     secondCount(threshold_second));
  }

  public ComparisonSimilarityInfo jaccardAtPosition(double threshold_first, double threshold_second,
                                                               Position position) {
    return calculatorWithCountsGiven
            .jaccardAtPosition(threshold_first, threshold_second,
                               firstCount(threshold_first),
                               secondCount(threshold_second),
                               position);
  }
}
