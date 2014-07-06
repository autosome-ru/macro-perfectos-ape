package ru.autosome.commons.backgroundModel.mono;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;

public interface BackgroundModel extends GeneralizedBackgroundModel {
  public double count(int index); // Is it necessary?

  static final int ALPHABET_SIZE = 4;
}
