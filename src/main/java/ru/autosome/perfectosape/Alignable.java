package ru.autosome.perfectosape;

public interface Alignable<T extends Object> {
  int length();
  T reverseComplement();
  T leftAugment(int shift);
  T rightAugment(int shift);
}
