package ru.autosome.commons.motifModel;

public interface Alignable<ModelType> {
  ModelType reverseComplement();
  ModelType leftAugment(int n);
  ModelType rightAugment(int n);
  int length();
}
