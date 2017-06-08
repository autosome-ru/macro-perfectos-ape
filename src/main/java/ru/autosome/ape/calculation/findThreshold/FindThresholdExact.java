package ru.autosome.ape.calculation.findThreshold;

import ru.autosome.ape.calculation.ScoringModelDistributions.ScoringDistributionGenerator;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.motifModel.ScoreDistribution;

import java.util.List;

public class FindThresholdExact<ModelType extends ScoreDistribution<BackgroundType>,
                               BackgroundType> implements CanFindThreshold {
  final ModelType motif;
  final BackgroundType background;

  public FindThresholdExact(ModelType motif, BackgroundType background) {
    this.motif = motif;
    this.background = background;
  }

  @Override
  public FoundedThresholdInfo thresholdByPvalue(double pvalue, BoundaryType boundaryType) {
    ScoringDistributionGenerator scoringModel = motif.scoringModel(background);
    return scoringModel.threshold(pvalue, boundaryType);
  }

  @Override
  public List<FoundedThresholdInfo> thresholdsByPvalues(List<Double> pvalues, BoundaryType boundaryType) {
    ScoringDistributionGenerator scoringModel = motif.scoringModel(background);
    return scoringModel.thresholds(pvalues, boundaryType);
  }
}
