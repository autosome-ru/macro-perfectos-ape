package ru.autosome.perfectosape.calculations.findPvalue;

import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.calculations.CountingPWM;
import ru.autosome.perfectosape.calculations.ScoringModelDistibutions;
import ru.autosome.perfectosape.motifModels.PWM;

public class FindPvalueAPE extends FindPvalueByDiscretization<PWM, BackgroundModel> {
  Integer maxHashSize;

  public FindPvalueAPE(PWM pwm, BackgroundModel background, Double discretization, Integer maxHashSize) {
    super(pwm, background, discretization);
    this.maxHashSize = maxHashSize;
  }

  @Override
  ScoringModelDistibutions countingPWM() {
    return new CountingPWM(motif.discrete(discretization), background, maxHashSize);
  }
}
