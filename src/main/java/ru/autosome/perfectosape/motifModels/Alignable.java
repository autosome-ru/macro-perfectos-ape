package ru.autosome.perfectosape.motifModels;

public interface Alignable<ModelType> {
  ModelType reverseComplement();
  ModelType leftAugment(int n);
  ModelType rightAugment(int n);
  int length();
}
