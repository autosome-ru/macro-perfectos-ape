package ru.autosome.commons.backgroundModel.di;

import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;

public interface DiBackgroundModel extends GeneralizedBackgroundModel {
  int ALPHABET_SIZE = 16;

  // probabillity of letter under condition that previous letter was previous_letter
  double conditionalCount(int previous_letter, int letter);
  double countAnyFirstLetter(int secondLetter);
  double countAnySecondLetter(int firstLetter);

  double average_by_first_letter(double[] values, int secondLetterIndex);
  double average_by_second_letter(double[] values, int firstLetterIndex);
}
