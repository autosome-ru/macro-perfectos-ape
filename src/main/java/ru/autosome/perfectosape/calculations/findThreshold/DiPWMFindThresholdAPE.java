package ru.autosome.perfectosape.calculations.findThreshold;

import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.calculations.CountingDiPWM;
import ru.autosome.perfectosape.calculations.ScoringModelDistibutions;
import ru.autosome.perfectosape.motifModels.DiPWM;

public class DiPWMFindThresholdAPE extends FindThresholdByDiscretization {
  public static class Builder extends FindThresholdBuilder<DiPWM> {
    Double discretization;
    DiBackgroundModel background;
    Integer maxHashSize;

    public Builder(DiBackgroundModel background, Double discretization, Integer maxHashSize) {
      this.background = background;
      this.discretization = discretization;
      this.maxHashSize = maxHashSize;
    }

    @Override
    public CanFindThreshold thresholdCalculator() {
      return new DiPWMFindThresholdAPE(motif, background, discretization, maxHashSize);
    }
  }

  DiPWM motif;
  Integer maxHashSize; // if maxHashSize is null - it's not applied
  DiBackgroundModel background;

  public DiPWMFindThresholdAPE(DiPWM dipwm, DiBackgroundModel dibackground,
                          Double discretization, Integer max_hash_size) {
    super(discretization);
    this.motif = dipwm;
    this.maxHashSize = max_hash_size;
    this.background = dibackground;
  }

  @Override
  ScoringModelDistibutions discretedModel() {
    return new CountingDiPWM(motif.discrete(discretization), background, maxHashSize);
  }
}
