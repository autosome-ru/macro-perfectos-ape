package ru.autosome.ape.calculation.findThreshold;

import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.ScoreDistribution;

import java.util.ArrayList;
import java.util.List;

public class FindThresholdAPE<ModelType extends Discretable<ModelType> & ScoreDistribution<BackgroundType>,
                              BackgroundType> implements CanFindThreshold {
  final FindThresholdExact<ModelType, BackgroundType> thresholdCalculator;
  final Discretizer discretizer;

  public FindThresholdAPE(ModelType motif, BackgroundType background, Discretizer discretizer) {
    this.discretizer = discretizer;
    this.thresholdCalculator = new FindThresholdExact<>(motif.discrete(discretizer), background);
  }

  @Override
  public FoundedThresholdInfo thresholdByPvalue(double pvalue, BoundaryType boundaryType) {
    return thresholdCalculator.thresholdByPvalue(pvalue, boundaryType).downscale(discretizer);
  }

  @Override
  public List<FoundedThresholdInfo> thresholdsByPvalues(List<Double> pvalues, BoundaryType boundaryType) {
    List<FoundedThresholdInfo> infos_upscaled = thresholdCalculator.thresholdsByPvalues(pvalues, boundaryType);
    List<FoundedThresholdInfo> result = new ArrayList<>();
    for (FoundedThresholdInfo info_upscaled: infos_upscaled) {
      result.add(info_upscaled.downscale(discretizer));
    }
    return result;
  }
}
