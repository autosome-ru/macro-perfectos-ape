package ru.autosome.commons.motifModel;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.perfectosape.calculation.ScoringModelDistributions.ScoringModelDistibutions;

public interface ScoreDistribution<BackgroundType extends GeneralizedBackgroundModel> {
  ScoringModelDistibutions scoringModelDistibutions(BackgroundType background, Integer maxHashSize);
}
