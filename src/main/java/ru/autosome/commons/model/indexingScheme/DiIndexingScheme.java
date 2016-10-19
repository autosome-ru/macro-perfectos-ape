package ru.autosome.commons.model.indexingScheme;

public class DiIndexingScheme {
  public static int diIndex(int firstLetterIndex, int secondLetterIndex) {
    return 4 * firstLetterIndex + secondLetterIndex;
  }

  public static int firstLetterIndex(int diIndex) {
    return diIndex / 4;
  }

  public static int secondLetterIndex(int diIndex) {
    return diIndex % 4;
  }

  private static int complementMononucleotideIndex(int monoIndex) {
    return 3 - monoIndex;
  }

  public static int complementDinucleotideIndex(int diIndex) {
    return diIndex(
        complementMononucleotideIndex(secondLetterIndex(diIndex)),
        complementMononucleotideIndex(firstLetterIndex(diIndex))
    );
  }
}
