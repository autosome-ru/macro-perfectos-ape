package ru.autosome.perfectosape.backgroundModels;

public interface AbstractBackgroundFactory<BackgroundType extends GeneralizedBackgroundModel> {
  BackgroundType wordwiseModel();
  BackgroundType fromString(String str);
}
