package ru.autosome.perfectosape.calculations.findPvalue;

import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.calculations.CountingPWM;
import ru.autosome.perfectosape.calculations.ScoringModelDistibutions;
import ru.autosome.perfectosape.motifModels.PWM;

public class FindPvalueAPE extends FindPvalueByDiscretization<PWM, BackgroundModel> {
  public static class Builder extends FindPvalueBuilder<PWM> {
    Double discretization;
    BackgroundModel background;
    Integer maxHashSize;

    public Builder(Double discretization, BackgroundModel background, Integer maxHashSize) {
      this.discretization = discretization;
      this.background = background;
      this.maxHashSize = maxHashSize;
    }

    @Override
    public CanFindPvalue pvalueCalculator() {
      return new FindPvalueAPE(motif, background, discretization, maxHashSize);
    }
  }

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
