package ru.autosome.commons.motifModel;

public interface Alignable<ModelType> extends HasLength {
  ModelType reverseComplement();
  ModelType leftAugment(int n);
  ModelType rightAugment(int n);
}
