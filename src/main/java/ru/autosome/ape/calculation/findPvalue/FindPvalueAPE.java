package ru.autosome.ape.calculation.findPvalue;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.HasLength;
import ru.autosome.commons.motifModel.ScoreDistribution;
import ru.autosome.perfectosape.calculation.ScoringModelDistributions.ScoringModelDistributions;

public class FindPvalueAPE<ModelType extends HasLength & Discretable<ModelType> & ScoreDistribution<BackgroundType>,
                           BackgroundType extends GeneralizedBackgroundModel> extends FindPvalueByDiscretization<ModelType, BackgroundType> {
  final Integer maxHashSize;

  public FindPvalueAPE(ModelType pwm, BackgroundType background, Discretizer discretizer, Integer maxHashSize) {
    super(pwm, background, discretizer);
    this.maxHashSize = maxHashSize;
  }

  @Override
  ScoringModelDistributions discretedScoringModel() {
    return motif.discrete(discretizer).scoringModelDistibutions(background, maxHashSize);
  }
}
