package ru.autosome.perfectosape.calculations.findThreshold;

import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.Discretizer;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.calculations.ScoringModelDistributions.ScoringModelDistibutions;

public abstract class FindThresholdByDiscretization implements CanFindThreshold {
  Discretizer discretizer;

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
    return downscale_all(discretedScoringModel().weak_thresholds(pvalues), discretizer);
  }

  @Override
  public CanFindThreshold.ThresholdInfo[] strongThresholsdByPvalues(double[] pvalues) throws HashOverflowException {
    return downscale_all(discretedScoringModel().strong_thresholds(pvalues), discretizer);
  }

  @Override
  public CanFindThreshold.ThresholdInfo[] thresholdsByPvalues(double[] pvalues, BoundaryType boundaryType) throws HashOverflowException {
    return downscale_all(discretedScoringModel().thresholds(pvalues, boundaryType), discretizer);
  }

  private CanFindThreshold.ThresholdInfo[] downscale_all(CanFindThreshold.ThresholdInfo[] thresholdInfos, Discretizer discretizer) {
    CanFindThreshold.ThresholdInfo[] result = new CanFindThreshold.ThresholdInfo[thresholdInfos.length];
    for (int i = 0; i < thresholdInfos.length; ++i) {
      result[i] = thresholdInfos[i].downscale(discretizer);
    }
    return result;
  }

}
