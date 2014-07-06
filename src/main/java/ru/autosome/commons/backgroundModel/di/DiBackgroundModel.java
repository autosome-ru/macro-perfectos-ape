package ru.autosome.commons.backgroundModel.di;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;

public interface DiBackgroundModel extends GeneralizedBackgroundModel {
  static final int ALPHABET_SIZE = 16;

  // probabillity of letter under condition that previous letter was previous_letter
  public double conditionalCount(int previous_letter, int letter);
  double countAnyFirstLetter(int secondLetter);
  double countAnySecondLetter(int firstLetter);
}