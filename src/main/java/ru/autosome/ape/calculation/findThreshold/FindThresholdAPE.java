package ru.autosome.ape.calculation.findThreshold;

import ru.autosome.ape.calculation.ScoringModelDistributions.ScoringModelDistributions;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.ScoreDistribution;

import java.util.ArrayList;
import java.util.List;

public class FindThresholdAPE<ModelType extends Discretable<ModelType> & ScoreDistribution<BackgroundType>,
                              BackgroundType> implements CanFindThreshold {
  final ModelType motif;
  final BackgroundType background;
  final Discretizer discretizer;

  public FindThresholdAPE(ModelType motif, BackgroundType background, Discretizer discretizer) {
    this.discretizer = discretizer;
    this.motif = motif;
    this.background = background;
  }

  ScoringModelDistributions discretedScoringModel() {
    return motif.discrete(discretizer).scoringModel(background);
  }

  @Override
  public FoundedThresholdInfo thresholdByPvalue(double pvalue, BoundaryType boundaryType) {
    return discretedScoringModel().threshold(pvalue, boundaryType).downscale(discretizer);
  }

  @Override
  public List<FoundedThresholdInfo> thresholdsByPvalues(List<Double> pvalues, BoundaryType boundaryType) {
    return downscale_all(discretedScoringModel().thresholds(pvalues, boundaryType));
  }

  private List<FoundedThresholdInfo> downscale_all(List<FoundedThresholdInfo> thresholdInfos) {
    List<FoundedThresholdInfo> result = new ArrayList<>();
    for (FoundedThresholdInfo thresholdInfo: thresholdInfos) {
      result.add(thresholdInfo.downscale(discretizer));
    }
    return result;
  }
}
