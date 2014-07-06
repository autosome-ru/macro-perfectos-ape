package ru.autosome.commons.backgroundModel.di;

import ru.autosome.commons.backgroundModel.AbstractBackgroundFactory;

public class DiBackgroundFactory implements AbstractBackgroundFactory<DiBackgroundModel> {
  @Override
  public DiWordwiseBackground wordwiseModel() {
    return new DiWordwiseBackground();
  }

  @Override
  public DiBackgroundModel fromString(String str) {
    return DiBackground.fromString(str);
  }
}
