package ru.autosome.perfectosape.calculations.findThreshold;

import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.calculations.CountingDiPWM;
import ru.autosome.perfectosape.motifModels.DiPWM;

public class DiPWMFindThresholdAPE extends FindThresholdAPEGeneralized<DiPWM, DiBackgroundModel> {
  public class Builder extends FindThresholdAPEGeneralizedBuilder<DiPWM, DiBackgroundModel> {

    public Builder(DiBackgroundModel background, Double discretization, Integer maxHashSize) {
      super(background, discretization, maxHashSize);
    }

    @Override
    public CanFindThreshold thresholdCalculator() {
      return new DiPWMFindThresholdAPE(motif, background, discretization, maxHashSize);
    }
  }

  public DiPWMFindThresholdAPE(DiPWM dipwm, DiBackgroundModel dibackground,
                          Double discretization, Integer max_hash_size) {
    super(dipwm, dibackground, discretization, max_hash_size);
  }

  @Override
  CountingDiPWM countingPWM(DiPWM dipwm) {
    return new CountingDiPWM(dipwm, background, maxHashSize);
  }
}
