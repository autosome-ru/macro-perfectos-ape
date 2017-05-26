package ru.autosome.commons.motifModel;

import ru.autosome.ape.calculation.ScoringModelDistributions.ScoringModelDistributions;

public interface ScoreDistribution<BackgroundType> {
  ScoringModelDistributions scoringModel(BackgroundType background);
}
