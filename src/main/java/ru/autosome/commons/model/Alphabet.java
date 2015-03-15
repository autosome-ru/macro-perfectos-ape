package ru.autosome.commons.model;

import java.util.HashMap;
import java.util.Map;

public class Alphabet {
  protected final int codeLength;
  protected final Map<String, Byte> letterIndices;
  protected final Map<Byte, Byte> reverseComplements;

  // Private constructor for safety reasons (to easy to create invalid alphabet). Use Alphabet.byLetters instead
  private Alphabet(int codeLength, Map<String, Byte> letterIndices, Map<Byte, Byte> reverseComplements) {
    if (letterIndices.size() != reverseComplements.size()) {
      throw new IllegalArgumentException("letters and complements sizes are not compatible");
    }
    for (String k: letterIndices.keySet()) {
      if (k.length() != codeLength) {
        throw new IllegalArgumentException("Key size should be equal to code size");
      }
    }
    for (Map.Entry<Byte,Byte> entry: reverseComplements.entrySet()) {
      if (entry.getKey() != reverseComplements.get(entry.getValue())) {
        throw new IllegalArgumentException("Reverse of reverse should be equal to itself");
      }
    }
    this.codeLength = codeLength;
    this.letterIndices = letterIndices;
    this.reverseComplements = reverseComplements;
  }

  public int size() {
    return letterIndices.size();
  }

  // Converts string, using overlapping (if codeLength>1) positions, uses shift of 1 nt each time.
  public byte[] convertString(String seq) {
    if (seq.length() < codeLength) {
      throw new IllegalArgumentException("Sequence '" + seq + "' is not compatible with alphabet of length " + codeLength);
    }
    final String normalizedSeq = seq.toUpperCase();
    byte[] result = new byte[normalizedSeq.length() + 1 - codeLength];
    for (int i = 0; i < result.length; ++i) {
      result[i] = letterIndices.get( normalizedSeq.substring(i, i + codeLength) );
    }
    return result;
  }

  public byte[] reverseComplement(byte[] seq) {
    int len = seq.length;
    byte[] result = new byte[len];
    for (int i = 0; i < len; ++i) {
      result[i] = reverseComplements.get( seq[len - 1 - i] );
    }
    return result;
  }

  private static String stringByIndex(int ind, String letters, int codeLength) {
    StringBuilder str = new StringBuilder(codeLength);
    int indRemainder = ind;
    for (int letterIndex = 0; letterIndex < codeLength; ++letterIndex) {
      int letterPosition = indRemainder % letters.length();
      str.append(letters.charAt(letterPosition));
      indRemainder /= letters.length();
    }
    return str.reverse().toString();
  }

  public static Alphabet byLetters(int codeLength, String letters, String complementLetters) {
    if (letters.length() != complementLetters.length()) {
      throw new RuntimeException("Error in program: number of letters and complement letters in alphabet definition differs");
    }

    Map<String, Byte> letterIndices = new HashMap<String, Byte>();
    Map<Byte, Byte> complements = new HashMap<Byte, Byte>();

    letters = letters.toUpperCase();
    complementLetters = complementLetters.toUpperCase();
    for (byte ind = 0; ind < Math.pow(letters.length(), codeLength); ++ind) {
      String directSeq = stringByIndex(ind, letters, codeLength);
      letterIndices.put(directSeq, ind);
    }

    for (byte ind = 0; ind < Math.pow(letters.length(), codeLength); ++ind) {
      String complementSeq = stringByIndex(ind, complementLetters, codeLength);
      String revcompSeq = new StringBuilder(complementSeq).reverse().toString();
      complements.put(ind, letterIndices.get(revcompSeq));
    }

    return new Alphabet(codeLength, letterIndices, complements);
  }

  public static Alphabet monoACGT = byLetters(1, "ACGT", "TGCA");
  public static Alphabet monoACGTN = byLetters(1, "ACGTN", "TGCAN");
  public static Alphabet diACGT = byLetters(2, "ACGT", "TGCA");
  public static Alphabet diACGTN = byLetters(2, "ACGTN", "TGCAN");
}
