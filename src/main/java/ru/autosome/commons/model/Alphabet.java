package ru.autosome.commons.model;

import java.util.HashMap;
import java.util.Map;

public class Alphabet {
  protected final int codeLength;
  protected final int alphabetSize;
  protected final String[] letters; // AA, AC,... NN codes indexed
  protected final Map<String, Byte> letterIndices;
  protected final Map<Byte, Byte> reverseComplements;

  // Private constructor for safety reasons (to easy to create invalid alphabet). Use Alphabet.byLetters instead
  private Alphabet(int codeLength, int alphabetSize, String[] letters, Map<String, Byte> letterIndices, Map<Byte, Byte> reverseComplements) {
    if (Math.pow(alphabetSize, codeLength) != letters.length) {
      throw new IllegalArgumentException("letters array size should be equal to alphabetSize ** codeLength");
    }
    if (letterIndices.size() != letters.length || letterIndices.size() != reverseComplements.size()) {
      throw new IllegalArgumentException("letters and complements sizes are not compatible");
    }
    for (String k: letterIndices.keySet()) {
      if (k.length() != codeLength) {
        throw new IllegalArgumentException("Key size should be equal to code size");
      }
      if (!k.equals(letters[letterIndices.get(k)])) {
        throw new IllegalArgumentException("Letters should be compatible with letter indices");
      }
    }
    for (Map.Entry<Byte,Byte> entry: reverseComplements.entrySet()) {
      if (!entry.getKey().equals(reverseComplements.get(entry.getValue()))) {
        throw new IllegalArgumentException("Reverse of reverse should be equal to itself");
      }
    }
    this.codeLength = codeLength;
    this.alphabetSize = alphabetSize;
    this.letters = letters;
    this.letterIndices = letterIndices;
    this.reverseComplements = reverseComplements;
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
    String[] separateLetters = new String[(int)Math.pow(letters.length(), codeLength)];

    letters = letters.toUpperCase();
    complementLetters = complementLetters.toUpperCase();
    for (byte ind = 0; ind < Math.pow(letters.length(), codeLength); ++ind) {
      String directSeq = stringByIndex(ind, letters, codeLength);
      letterIndices.put(directSeq, ind);
      separateLetters[ind] = directSeq;
    }

    for (byte ind = 0; ind < Math.pow(letters.length(), codeLength); ++ind) {
      String complementSeq = stringByIndex(ind, complementLetters, codeLength);
      String revcompSeq = new StringBuilder(complementSeq).reverse().toString();
      complements.put(ind, letterIndices.get(revcompSeq));
    }

    return new Alphabet(codeLength, letters.length(), separateLetters, letterIndices, complements);
  }

  public int getCodeLength() {
    return codeLength;
  }

  public String decodeString(byte[] seq) {
    StringBuilder str = new StringBuilder();
    for (int i = 0; i < seq.length - 1; ++i) {
      str.append(letters[seq[i]].charAt(0)); // all except last byte add up the only letter
    }
    str.append(letters[seq[seq.length - 1]]); // the last byte adds up whole the string
    return str.toString();
  }

  // checks whether byte stream represents valid Sequence
  // (for codeLength > 1 they must overlap by coinciding parts)
  public boolean isConsistent(byte[] seq) {
    for (int i = 1; i < seq.length; ++i) {
      if (seq[i - 1] % (int)Math.pow(alphabetSize, codeLength - 1) != seq[i] / codeLength)
        return false;
    }
    return true;
  }

//  public static Alphabet monoACGT = byLetters(1, "ACGT", "TGCA");
  public static final Alphabet monoACGTN = byLetters(1, "ACGTN", "TGCAN");
//  public static Alphabet diACGT = byLetters(2, "ACGT", "TGCA");
  public static final Alphabet diACGTN = byLetters(2, "ACGTN", "TGCAN");
}
