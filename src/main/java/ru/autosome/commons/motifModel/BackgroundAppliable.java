package ru.autosome.commons.motifModel;

public interface BackgroundAppliable<BackgroundType, ModelType> {
  ModelType onBackground(BackgroundType background);
}
