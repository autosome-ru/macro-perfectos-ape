package ru.autosome.ape.calculation.findThreshold;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.perfectosape.calculation.ScoringModelDistributions.ScoringModelDistibutions;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.ScoreDistribution;

public class FindThresholdAPE<ModelType extends Discretable<ModelType> & ScoreDistribution<BackgroundType>,
                              BackgroundType extends GeneralizedBackgroundModel> extends FindThresholdByDiscretization {
  final ModelType motif;
  final Integer maxHashSize; // if maxHashSize is null - it's not applied
  final BackgroundType background;

  public FindThresholdAPE(ModelType motif, BackgroundType background,
                    Double discretization, Integer max_hash_size) {
    super(discretization);
    this.motif = motif;
    this.background = background;
    this.maxHashSize = max_hash_size;
  }

  @Override
  ScoringModelDistibutions discretedScoringModel() {
    return motif.discrete(discretization).scoringModelDistibutions(background, maxHashSize);
  }
}
