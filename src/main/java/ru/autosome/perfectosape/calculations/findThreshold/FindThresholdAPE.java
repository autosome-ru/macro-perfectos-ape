package ru.autosome.perfectosape.calculations.findThreshold;

import ru.autosome.perfectosape.Discretizer;
import ru.autosome.perfectosape.backgroundModels.GeneralizedBackgroundModel;
import ru.autosome.perfectosape.calculations.ScoringModelDistributions.ScoringModelDistibutions;
import ru.autosome.perfectosape.motifModels.Discretable;
import ru.autosome.perfectosape.motifModels.ScoreDistribution;

public class FindThresholdAPE<ModelType extends Discretable<ModelType> & ScoreDistribution<BackgroundType>,
                              BackgroundType extends GeneralizedBackgroundModel> extends FindThresholdByDiscretization {
  ModelType motif;
  Integer maxHashSize; // if maxHashSize is null - it's not applied
  BackgroundType background;

  public FindThresholdAPE(ModelType motif, BackgroundType background,
                    Discretizer discretizer, Integer max_hash_size) {
    super(discretizer);
    this.motif = motif;
    this.background = background;
    this.maxHashSize = max_hash_size;
  }

  @Override
  ScoringModelDistibutions discretedScoringModel() {
    return motif.discrete(discretizer).scoringModelDistibutions(background, maxHashSize);
  }
}
