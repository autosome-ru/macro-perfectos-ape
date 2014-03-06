package ru.autosome.perfectosape.motifModels;

import ru.autosome.perfectosape.backgroundModels.GeneralizedBackgroundModel;

public interface ScoreStatistics<BackgroundType extends GeneralizedBackgroundModel> {
  public double score_mean(BackgroundType background);
  public double score_variance(BackgroundType background);
}
