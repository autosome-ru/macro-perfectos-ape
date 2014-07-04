package ru.autosome.perfectosape.calculations.findPvalue;

import ru.autosome.perfectosape.backgroundModels.GeneralizedBackgroundModel;
import ru.autosome.perfectosape.calculations.ScoringModelDistributions.ScoringModelDistibutions;
import ru.autosome.perfectosape.motifModels.Discretable;
import ru.autosome.perfectosape.motifModels.ScoreDistribution;
import ru.autosome.perfectosape.motifModels.ScoringModel;

public class FindPvalueAPE<ModelType extends ScoringModel & Discretable<ModelType> & ScoreDistribution<BackgroundType>,
                           BackgroundType extends GeneralizedBackgroundModel> extends FindPvalueByDiscretization<ModelType, BackgroundType> {
  Integer maxHashSize;

  public FindPvalueAPE(ModelType pwm, BackgroundType background, Double discretization, Integer maxHashSize) {
    super(pwm, background, discretization);
    this.maxHashSize = maxHashSize;
  }

  @Override
  ScoringModelDistibutions discretedScoringModel() {
    return motif.discrete(discretizer.discretization).scoringModelDistibutions(background, maxHashSize);
  }
}
