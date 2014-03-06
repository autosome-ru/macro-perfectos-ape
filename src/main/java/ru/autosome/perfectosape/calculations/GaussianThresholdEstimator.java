package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.MathExtensions;
import ru.autosome.perfectosape.backgroundModels.GeneralizedBackgroundModel;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThresholdApproximation;
import ru.autosome.perfectosape.motifModels.ScoreStatistics;

public class GaussianThresholdEstimator<ModelType extends ScoreStatistics<BackgroundType>, BackgroundType extends GeneralizedBackgroundModel> implements CanFindThresholdApproximation {
  final ModelType motif;
  final BackgroundType dibackground;

  public GaussianThresholdEstimator(ModelType motif, BackgroundType dibackground) {
    this.motif = motif;
    this.dibackground = dibackground;
  }

  @Override
  public double thresholdByPvalue(double pvalue) {
    double sigma = Math.sqrt(motif.score_variance(dibackground));
    double n_ = MathExtensions.inverf(1 - 2 * pvalue) * Math.sqrt(2);
    return motif.score_mean(dibackground) + n_ * sigma;
  }
}
