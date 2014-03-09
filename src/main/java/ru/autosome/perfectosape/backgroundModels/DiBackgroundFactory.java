package ru.autosome.perfectosape.backgroundModels;

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
