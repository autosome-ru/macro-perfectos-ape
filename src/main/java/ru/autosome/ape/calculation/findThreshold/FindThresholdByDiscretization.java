package ru.autosome.ape.calculation.findThreshold;

import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.perfectosape.calculation.ScoringModelDistributions.ScoringModelDistributions;

public abstract class FindThresholdByDiscretization implements CanFindThreshold {
  final Discretizer discretizer;

  public FindThresholdByDiscretization(Discretizer discretizer) {
    this.discretizer = discretizer;
  }

  abstract ScoringModelDistributions discretedScoringModel();

  @Override
  public CanFindThreshold.ThresholdInfo weakThresholdByPvalue(double pvalue) {
    return discretedScoringModel().weak_threshold(pvalue).downscale(discretizer);
  }

  @Override
  public CanFindThreshold.ThresholdInfo strongThresholdByPvalue(double pvalue) {
    return discretedScoringModel().strong_threshold(pvalue).downscale(discretizer);
  }

  @Override
  public CanFindThreshold.ThresholdInfo thresholdByPvalue(double pvalue, BoundaryType boundaryType) {
    return discretedScoringModel().threshold(pvalue, boundaryType).downscale(discretizer);
  }

  @Override
  public CanFindThreshold.ThresholdInfo[] weakThresholdsByPvalues(double[] pvalues) {
    return downscale_all(discretedScoringModel().weak_thresholds(pvalues));
  }

  @Override
  public CanFindThreshold.ThresholdInfo[] strongThresholsdByPvalues(double[] pvalues) {
    return downscale_all(discretedScoringModel().strong_thresholds(pvalues));
  }

  @Override
  public CanFindThreshold.ThresholdInfo[] thresholdsByPvalues(double[] pvalues, BoundaryType boundaryType) {
    return downscale_all(discretedScoringModel().thresholds(pvalues, boundaryType));
  }

  private CanFindThreshold.ThresholdInfo[] downscale_all(CanFindThreshold.ThresholdInfo[] thresholdInfos) {
    CanFindThreshold.ThresholdInfo[] result = new CanFindThreshold.ThresholdInfo[thresholdInfos.length];
    for (int i = 0; i < thresholdInfos.length; ++i) {
      result[i] = thresholdInfos[i].downscale(discretizer);
    }
    return result;
  }

}
