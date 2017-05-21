package ru.autosome.ape.calculation.findThreshold;

import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.ScoreDistribution;
import ru.autosome.perfectosape.calculation.ScoringModelDistributions.ScoringModelDistributions;

public class FindThresholdAPE<ModelType extends Discretable<ModelType> & ScoreDistribution<BackgroundType>,
                              BackgroundType> extends FindThresholdByDiscretization {
  final ModelType motif;
  final BackgroundType background;

  public FindThresholdAPE(ModelType motif, BackgroundType background, Discretizer discretizer) {
    super(discretizer);
    this.motif = motif;
    this.background = background;
  }

  @Override
  ScoringModelDistributions discretedScoringModel() {
    return motif.discrete(discretizer).scoringModel(background);
  }
}
