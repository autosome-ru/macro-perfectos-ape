package ru.autosome.commons.motifModel;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;

public interface ScoreStatistics<BackgroundType extends GeneralizedBackgroundModel> {
  public double score_mean(BackgroundType background);
  public double score_variance(BackgroundType background);
}
