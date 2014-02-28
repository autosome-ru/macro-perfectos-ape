package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.MathExtensions;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.motifModels.PWM;

public class GaussianThresholdEstimation {
  final PWM pwm;
  final BackgroundModel background;

  public GaussianThresholdEstimation(PWM pwm, BackgroundModel background) {
    this.pwm = pwm;
    this.background = background;
  }

  private double score_mean() {
    double result = 0.0;
    for (double[] pos : pwm.matrix) {
      result += background.mean_value(pos);
    }
    return result;
  }

  private double score_variance() {
    double variance = 0.0;
    for (double[] pos : pwm.matrix) {
      double mean_square = background.mean_square_value(pos);
      double mean = background.mean_value(pos);
      double squared_mean = mean * mean;
      variance += mean_square - squared_mean;
    }
    return variance;
  }

  public double thresholdByPvalue(double pvalue) {
    double sigma = Math.sqrt(score_variance());
    double n_ = MathExtensions.inverf(1 - 2 * pvalue) * Math.sqrt(2);
    return score_mean() + n_ * sigma;
  }
}
