package ru.autosome.ape.calculation.findThreshold;

import ru.autosome.commons.model.BoundaryType;
import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.perfectosape.calculation.ScoringModelDistributions.ScoringModelDistibutions;

public abstract class FindThresholdByDiscretization implements CanFindThreshold {
  final Discretizer discretizer;

  public FindThresholdByDiscretization(Discretizer discretizer) {
    this.discretizer = discretizer;
  }

  abstract ScoringModelDistibutions discretedScoringModel();

  @Override
  public CanFindThreshold.ThresholdInfo weakThresholdByPvalue(double pvalue) throws HashOverflowException {
    return discretedScoringModel().weak_threshold(pvalue).downscale(discretizer);
  }

  @Override
  public CanFindThreshold.ThresholdInfo strongThresholdByPvalue(double pvalue) throws HashOverflowException {
    return discretedScoringModel().strong_threshold(pvalue).downscale(discretizer);
  }

  @Override
  public CanFindThreshold.ThresholdInfo thresholdByPvalue(double pvalue, BoundaryType boundaryType) throws HashOverflowException {
    return discretedScoringModel().threshold(pvalue, boundaryType).downscale(discretizer);
  }

  @Override
  public CanFindThreshold.ThresholdInfo[] weakThresholdsByPvalues(double[] pvalues) throws HashOverflowException {
    return downscale_all(discretedScoringModel().weak_thresholds(pvalues));
  }

  @Override
  public CanFindThreshold.ThresholdInfo[] strongThresholsdByPvalues(double[] pvalues) throws HashOverflowException {
    return downscale_all(discretedScoringModel().strong_thresholds(pvalues));
  }

  @Override
  public CanFindThreshold.ThresholdInfo[] thresholdsByPvalues(double[] pvalues, BoundaryType boundaryType) throws HashOverflowException {
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
