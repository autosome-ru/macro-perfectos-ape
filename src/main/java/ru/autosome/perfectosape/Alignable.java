package ru.autosome.perfectosape;

public interface Alignable<T> {
  int length();
  T reverseComplement();
  T leftAugment(int shift);
  T rightAugment(int shift);
}
