package ru.autosome.ape.calculation.findThreshold;

import ru.autosome.commons.motifModel.ScoreStatistics;
import ru.autosome.commons.support.MathExtensions;

public class GaussianThresholdEstimator<ModelType extends ScoreStatistics> implements CanFindThresholdApproximation {
  final ModelType scoringModel;

  public GaussianThresholdEstimator(ModelType scoringModel) {
    this.scoringModel = scoringModel;
  }

  @Override
  public double thresholdByPvalue(double pvalue) {
    double sigma = Math.sqrt(scoringModel.score_variance());
    double n_ = MathExtensions.inverf(1 - 2 * pvalue) * Math.sqrt(2);
    return scoringModel.score_mean() + n_ * sigma;
  }
}
