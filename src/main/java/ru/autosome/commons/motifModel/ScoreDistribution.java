package ru.autosome.commons.motifModel;

import ru.autosome.ape.calculation.ScoringModelDistributions.ScoringDistributionGenerator;

public interface ScoreDistribution<BackgroundType> {
  ScoringDistributionGenerator scoringModel(BackgroundType background);
}
