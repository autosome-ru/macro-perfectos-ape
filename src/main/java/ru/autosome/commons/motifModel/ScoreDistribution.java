package ru.autosome.commons.motifModel;

import ru.autosome.perfectosape.calculation.ScoringModelDistributions.ScoringModelDistributions;

public interface ScoreDistribution<BackgroundType> {
  ScoringModelDistributions scoringModelDistibutions(BackgroundType background, Integer maxHashSize);
}
