package ru.autosome.commons.backgroundModel.mono;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;

public interface BackgroundModel extends GeneralizedBackgroundModel {
  double count(int index); // Is it necessary?

  int ALPHABET_SIZE = 4;
}
