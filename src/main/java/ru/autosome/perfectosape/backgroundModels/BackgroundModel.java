package ru.autosome.perfectosape.backgroundModels;

public interface BackgroundModel extends GeneralizedBackgroundModel {
  public double count(int index); // Is it necessary?

  static final int ALPHABET_SIZE = 4;
}
