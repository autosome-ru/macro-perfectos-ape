package ru.autosome.ape.calculation.findThreshold;

import ru.autosome.commons.motifModel.ScoreStatistics;
import ru.autosome.commons.support.MathExtensions;

// It's more simple version of CanFindThreshold: it can find an approximation of threshold
// but makes no guaranties about lower or upper boundary
// and don't know real pvalue of predicted threshold
public class GaussianThresholdEstimator<ModelType extends ScoreStatistics> {
  final ModelType scoringModel;

  public GaussianThresholdEstimator(ModelType scoringModel) {
    this.scoringModel = scoringModel;
  }

  public double thresholdByPvalue(double pvalue) {
    double sigma = Math.sqrt(scoringModel.score_variance());
    double n_ = MathExtensions.inverf(1 - 2 * pvalue) * Math.sqrt(2);
    return scoringModel.score_mean() + n_ * sigma;
  }
}
