package ru.autosome.perfectosape.calculations.findThreshold;

import ru.autosome.perfectosape.motifModels.Named;

public abstract class FindThresholdBuilder<ModelType extends Named> {
  ModelType motif;
  public abstract CanFindThreshold thresholdCalculator();

  public FindThresholdBuilder<ModelType> applyMotif(ModelType motif) {
    this.motif = motif;
    return this;
  }

  public CanFindThreshold build() {
    if (motif != null) {
      return thresholdCalculator();
    } else {
      return null;
    }
  }
}