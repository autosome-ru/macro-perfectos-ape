package ru.autosome.perfectosape.calculations.findThreshold;

import ru.autosome.perfectosape.backgroundModels.GeneralizedBackgroundModel;
import ru.autosome.perfectosape.motifModels.ScoringModel;

public abstract class FindThresholdAPEGeneralizedBuilder<ModelType extends ScoringModel, BackgroundType extends GeneralizedBackgroundModel> implements CanFindThreshold.Builder<ModelType> {
  Double discretization;
  BackgroundType background;
  Integer maxHashSize;
  ModelType motif;

  public abstract CanFindThreshold thresholdCalculator();

  public FindThresholdAPEGeneralizedBuilder(BackgroundType background, Double discretization, Integer maxHashSize) {
    this.background = background;
    this.discretization = discretization;
    this.maxHashSize = maxHashSize;
  }

  @Override
  public CanFindThreshold.Builder applyMotif(ModelType motif) {
    this.motif = motif;
    return this;
  }

  @Override
  public CanFindThreshold build() {
    if (motif != null) {
      return thresholdCalculator();
    } else {
      return null;
    }
  }
}