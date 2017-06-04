package ru.autosome.macroape.calculation.generalized;

import ru.autosome.ape.calculation.findPvalue.FoundedPvalueInfo;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.motifModel.Alignable;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.macroape.model.ComparisonSimilarityInfo;
import ru.autosome.macroape.model.PairAligned;

import java.util.function.Function;

public class CompareModels<ModelType extends Alignable<ModelType> & Discretable<ModelType>> {

  private final Discretizer discretizer;
  private final CompareModelsExact<ModelType> evaluator;

  public CompareModels(ModelType firstPWM, ModelType secondPWM,
                       int backgroundVolume,
                       Discretizer discretizer,
                       Function<PairAligned<ModelType>, ? extends AlignedModelIntersection> calculatorOfAligned) {
    this.discretizer = discretizer;
    this.evaluator = new CompareModelsExact<>(firstPWM.discrete(discretizer),
                                              secondPWM.discrete(discretizer),
                                              backgroundVolume,
                                              calculatorOfAligned);
  }

  public ComparisonSimilarityInfo jaccard(FoundedPvalueInfo first, FoundedPvalueInfo second) {
    return evaluator.jaccard(first.upscale(discretizer), second.upscale(discretizer));
  }

  public ComparisonSimilarityInfo jaccardAtPosition(FoundedPvalueInfo first, FoundedPvalueInfo second, Position position) {
    return evaluator.jaccardAtPosition(first.upscale(discretizer), second.upscale(discretizer), position);
  }
}
