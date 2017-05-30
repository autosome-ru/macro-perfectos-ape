package ru.autosome.macroape.calculation.generalized;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.motifModel.Alignable;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.macroape.model.ComparisonSimilarityInfo;
import ru.autosome.macroape.model.PairAligned;

import java.util.function.Function;

public class CompareModels<ModelType extends Alignable<ModelType> & Discretable<ModelType>,
                           BackgroundType extends GeneralizedBackgroundModel> {

  private final Discretizer discretizer;
  private final CompareModelsExact<ModelType, BackgroundType> evaluator;

  public CompareModels(ModelType firstPWM, ModelType secondPWM,
                       BackgroundType background,
                       Discretizer discretizer,
                       Function<PairAligned<ModelType>, ? extends AlignedModelIntersection> calculatorOfAligned) {
    this.discretizer = discretizer;
    this.evaluator = new CompareModelsExact<>(firstPWM.discrete(discretizer),
                                                            secondPWM.discrete(discretizer),
                                                            background,
                                                            calculatorOfAligned);
  }


  public ComparisonSimilarityInfo jaccard(double thresholdFirst, double thresholdSecond,
                                          double firstCount, double secondCount) {
    return evaluator.jaccard(discretizer.upscale(thresholdFirst),
                              discretizer.upscale(thresholdSecond),
                              firstCount, secondCount);
  }

  public ComparisonSimilarityInfo jaccardAtPosition(double thresholdFirst, double thresholdSecond,
                                                    double firstCount, double secondCount,
                                                    Position position) {
    return evaluator.jaccardAtPosition(discretizer.upscale(thresholdFirst),
                                        discretizer.upscale(thresholdSecond),
                                        firstCount, secondCount, position);
  }
}
