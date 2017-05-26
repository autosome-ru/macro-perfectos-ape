package ru.autosome.ape.calculation.findThreshold;

import ru.autosome.ape.calculation.ScoringModelDistributions.ScoringModelDistributions;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;

import java.util.ArrayList;
import java.util.List;

public abstract class FindThresholdByDiscretization implements CanFindThreshold {
  final Discretizer discretizer;

  public FindThresholdByDiscretization(Discretizer discretizer) {
    this.discretizer = discretizer;
  }

  abstract ScoringModelDistributions discretedScoringModel();

  @Override
  public CanFindThreshold.ThresholdInfo thresholdByPvalue(double pvalue, BoundaryType boundaryType) {
    return discretedScoringModel().threshold(pvalue, boundaryType).downscale(discretizer);
  }

  @Override
  public List<CanFindThreshold.ThresholdInfo> thresholdsByPvalues(List<Double> pvalues, BoundaryType boundaryType) {
    return downscale_all(discretedScoringModel().thresholds(pvalues, boundaryType));
  }

  private List<CanFindThreshold.ThresholdInfo> downscale_all(List<CanFindThreshold.ThresholdInfo> thresholdInfos) {
    List<CanFindThreshold.ThresholdInfo> result = new ArrayList<>();
    for (CanFindThreshold.ThresholdInfo thresholdInfo: thresholdInfos) {
      result.add(thresholdInfo.downscale(discretizer));
    }
    return result;
  }

}
