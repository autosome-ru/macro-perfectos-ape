package ru.autosome.ape.calculation.findPvalue;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.perfectosape.calculation.ScoringModelDistributions.ScoringModelDistibutions;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.ScoreDistribution;
import ru.autosome.commons.motifModel.ScoringModel;

public class FindPvalueAPE<ModelType extends ScoringModel & Discretable<ModelType> & ScoreDistribution<BackgroundType>,
                           BackgroundType extends GeneralizedBackgroundModel> extends FindPvalueByDiscretization<ModelType, BackgroundType> {
  final Integer maxHashSize;

  public FindPvalueAPE(ModelType pwm, BackgroundType background, Discretizer discretizer, Integer maxHashSize) {
    super(pwm, background, discretizer);
    this.maxHashSize = maxHashSize;
  }

  @Override
  ScoringModelDistibutions discretedScoringModel() {
    return motif.discrete(discretizer).scoringModelDistibutions(background, maxHashSize);
  }
}
