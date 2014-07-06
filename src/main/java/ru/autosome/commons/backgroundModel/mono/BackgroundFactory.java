package ru.autosome.commons.backgroundModel.mono;

import ru.autosome.commons.backgroundModel.AbstractBackgroundFactory;

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
