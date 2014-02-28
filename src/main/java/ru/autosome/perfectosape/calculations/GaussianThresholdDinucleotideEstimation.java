package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.MathExtensions;
import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.motifModels.DiPWM;

// ToDo: join with GaussianThresholdEstimation because it's absolutely the same
// but it's hard to create common ScoringModel/Background hierarchy
public class GaussianThresholdDinucleotideEstimation {
  final DiPWM dipwm;
  final DiBackgroundModel dibackground;

  public GaussianThresholdDinucleotideEstimation(DiPWM dipwm, DiBackgroundModel dibackground) {
    this.dipwm = dipwm;
    this.dibackground = dibackground;
  }

  public double thresholdByPvalue(double pvalue) {
    double sigma = Math.sqrt(dipwm.score_variance(dibackground));
    double n_ = MathExtensions.inverf(1 - 2 * pvalue) * Math.sqrt(2);
    return dipwm.score_mean(dibackground) + n_ * sigma;
  }
}
