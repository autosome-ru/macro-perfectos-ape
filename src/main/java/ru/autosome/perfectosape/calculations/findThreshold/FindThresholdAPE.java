package ru.autosome.perfectosape.calculations.findThreshold;

import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.calculations.CountingPWM;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.motifModels.DiPWM;
import ru.autosome.perfectosape.motifModels.PWM;

public class FindThresholdAPE extends FindThresholdAPEGeneralized<PWM, BackgroundModel> {
  public static class Builder extends FindThresholdAPEGeneralizedBuilder<PWM, BackgroundModel> {

    public Builder(BackgroundModel background, Double discretization, Integer maxHashSize) {
      super(background, discretization, maxHashSize);
    }

    @Override
    public CanFindThreshold thresholdCalculator() {
      return new FindThresholdAPE(motif, background, discretization, maxHashSize);
    }

  }

  public FindThresholdAPE(PWM pwm, BackgroundModel background,
                    Double discretization, Integer max_hash_size) {
    super(pwm, background, discretization, max_hash_size);
  }

  @Override
  CountingPWM countingPWM(PWM pwm) {
    return new CountingPWM(pwm, background, maxHashSize);
  }
}
