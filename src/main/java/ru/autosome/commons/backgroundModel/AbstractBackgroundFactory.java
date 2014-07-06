package ru.autosome.commons.backgroundModel;

public interface AbstractBackgroundFactory<BackgroundType extends GeneralizedBackgroundModel> {
  BackgroundType wordwiseModel();
  BackgroundType fromString(String str);
}
