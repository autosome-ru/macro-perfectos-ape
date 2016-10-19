package ru.autosome.commons.model.indexingScheme;

// A,C,G,T or N nucleotides
public class MonoIndexingSchemeIUPAC {
  public static final int N_index = 4;

  public static int complementIndex(int nucleotideIndex) {
    if (nucleotideIndex == N_index) return N_index;
    return 3 - nucleotideIndex;
  }
}
