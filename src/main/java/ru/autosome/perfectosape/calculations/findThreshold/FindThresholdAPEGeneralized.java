package ru.autosome.perfectosape.calculations.findThreshold;

import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.calculations.ScoringModelDistibutions;
import ru.autosome.perfectosape.motifModels.Discretable;
import ru.autosome.perfectosape.motifModels.ScoringModel;

public abstract class FindThresholdAPEGeneralized<ModelType extends Discretable<ModelType> & ScoringModel> implements CanFindThreshold {
  Double discretization; // if discretization is null - it's not applied
  ModelType motif;

  public FindThresholdAPEGeneralized(ModelType motif,
                                    Double discretization) {
    this.motif = motif;
    this.discretization = discretization;
  }

  abstract ScoringModelDistibutions countingPWM(ModelType pwm);

  @Override
  public CanFindThreshold.ThresholdInfo weakThresholdByPvalue(double pvalue) throws HashOverflowException {
    return countingPWM(motif.discrete(discretization)).weak_threshold(pvalue).downscale(discretization);
  }

  @Override
  public CanFindThreshold.ThresholdInfo strongThresholdByPvalue(double pvalue) throws HashOverflowException {
    return countingPWM(motif.discrete(discretization)).strong_threshold(pvalue).downscale(discretization);
  }

  @Override
  public CanFindThreshold.ThresholdInfo thresholdByPvalue(double pvalue, BoundaryType boundaryType) throws HashOverflowException {
    return countingPWM(motif.discrete(discretization)).threshold(pvalue, boundaryType).downscale(discretization);
  }

  @Override
  public CanFindThreshold.ThresholdInfo[] weakThresholdsByPvalues(double[] pvalues) throws HashOverflowException {
    return downscale_all(countingPWM(motif.discrete(discretization)).weak_thresholds(pvalues), discretization);
  }

  @Override
  public CanFindThreshold.ThresholdInfo[] strongThresholsdByPvalues(double[] pvalues) throws HashOverflowException {
    return downscale_all( countingPWM(motif.discrete(discretization)).strong_thresholds(pvalues), discretization);
  }

  @Override
  public CanFindThreshold.ThresholdInfo[] thresholdsByPvalues(double[] pvalues, BoundaryType boundaryType) throws HashOverflowException {
    return downscale_all( countingPWM(motif.discrete(discretization)).thresholds(pvalues, boundaryType), discretization);
  }

  private CanFindThreshold.ThresholdInfo[] downscale_all(CanFindThreshold.ThresholdInfo[] thresholdInfos, double discretization) {
    CanFindThreshold.ThresholdInfo[] result = new CanFindThreshold.ThresholdInfo[thresholdInfos.length];
    for (int i = 0; i < thresholdInfos.length; ++i) {
      result[i] = thresholdInfos[i].downscale(discretization);
    }
    return result;
  }

}
