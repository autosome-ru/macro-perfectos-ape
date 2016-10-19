package ru.autosome.commons.model.indexingScheme;

// A,C,G,T or N nucleotides
public class DiIndexingSchemeIUPAC {
  public static final int N_index = 4;

  public static int diIndex(int firstLetterIndex, int secondLetterIndex) {
    return 5 * firstLetterIndex + secondLetterIndex;
  }

  public static int firstLetterIndex(int diIndex) {
    return diIndex / 5;
  }

  public static int secondLetterIndex(int diIndex) {
    return diIndex % 5;
  }

  private static int complementMononucleotideIndex(int monoIndex) {
    if (monoIndex == N_index) return N_index;
    return 3 - monoIndex;
  }

  public static int complementDinucleotideIndex(int diIndex) {
    return diIndex(
        complementMononucleotideIndex(secondLetterIndex(diIndex)),
        complementMononucleotideIndex(firstLetterIndex(diIndex))
    );
  }
}
