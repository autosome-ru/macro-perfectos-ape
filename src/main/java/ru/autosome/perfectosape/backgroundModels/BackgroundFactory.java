package ru.autosome.perfectosape.backgroundModels;

public class BackgroundFactory implements AbstractBackgroundFactory<BackgroundModel> {
  @Override
  public WordwiseBackground wordwiseModel() {
    return new WordwiseBackground();
  }

  @Override
  public BackgroundModel fromString(String str) {
    return Background.fromString(str);
  }
}
