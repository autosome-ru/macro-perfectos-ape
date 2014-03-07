package ru.autosome.perfectosape.motifModels;

import ru.autosome.perfectosape.backgroundModels.GeneralizedBackgroundModel;
import ru.autosome.perfectosape.calculations.ScoringModelDistibutions;

public interface ScoreDistribution<BackgroundType extends GeneralizedBackgroundModel> {
  ScoringModelDistibutions scoringModelDistibutions(BackgroundType background, Integer maxHashSize);
}
