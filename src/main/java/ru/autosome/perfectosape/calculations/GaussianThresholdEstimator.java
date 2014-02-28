package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.MathExtensions;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThresholdApproximation;
import ru.autosome.perfectosape.motifModels.PWM;

public class GaussianThresholdEstimator implements CanFindThresholdApproximation {
  final PWM pwm;
  final BackgroundModel background;

  public GaussianThresholdEstimator(PWM pwm, BackgroundModel background) {
    this.pwm = pwm;
    this.background = background;
  }
  @Override
  public double thresholdByPvalue(double pvalue) {
    double sigma = Math.sqrt(pwm.score_variance(background));
    double n_ = MathExtensions.inverf(1 - 2 * pvalue) * Math.sqrt(2);
    return pwm.score_mean(background) + n_ * sigma;
  }
}
