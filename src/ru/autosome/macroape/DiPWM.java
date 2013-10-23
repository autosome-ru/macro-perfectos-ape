package ru.autosome.macroape;

import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Math.ceil;

public class DiPWM {
  static final int ALPHABET_SIZE = 16;
  public final double[][] matrix;
  String name;
  static HashMap<String, Integer> indexByLetter;
  static {
    indexByLetter = new HashMap<String, Integer>();
    indexByLetter.put("AA", 0);
    indexByLetter.put("AC", 1);
    indexByLetter.put("AG", 2);
    indexByLetter.put("AT", 3);

    indexByLetter.put("CA", 4);
    indexByLetter.put("CC", 5);
    indexByLetter.put("CG", 6);
    indexByLetter.put("CT", 7);

    indexByLetter.put("GA", 8);
    indexByLetter.put("GC", 9);
    indexByLetter.put("GG", 10);
    indexByLetter.put("GT", 11);

    indexByLetter.put("TA", 12);
    indexByLetter.put("TC", 13);
    indexByLetter.put("TG", 14);
    indexByLetter.put("TT", 15);
  }

  public DiPWM(double[][] matrix, String name) {
    for (double[] pos : matrix) {
      if (pos.length != ALPHABET_SIZE) {
        throw new IllegalArgumentException("Matrix must have " + ALPHABET_SIZE + " elements in each position");
      }
    }
    this.matrix = matrix;
    this.name = name;
  };

  public int length() {
    return matrix.length + 1;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(name).append("\n");
    for (double[] pos : matrix) {
      for (int letter_index = 0; letter_index < ALPHABET_SIZE; ++ letter_index) {
        if (letter_index != 0) {
          result.append("\t");
        }
        result.append(pos[letter_index]);
      }
      result.append("\n");
    }
    return result.toString();
  }



  public static DiPWM fromParser(PMParser parser) {
    double[][] matrix = parser.matrix();
    String name = parser.name();
    return new DiPWM(matrix, name);
  }

  private static DiPWM new_from_text(ArrayList<String> input_lines) {
    return fromParser(new PMParser(input_lines));
  }

  double score(String word) throws IllegalArgumentException {
    return score(word, new WordwiseBackground());
  }

  double score(String word, BackgroundModel background) throws IllegalArgumentException {
    word = word.toUpperCase();
    if (word.length() != length()) {
      throw new IllegalArgumentException("word in PWM#score(word) should have the same length as matrix");
    }
    double sum = 0.0;
    for (int pos_index = 0; pos_index < matrix.length; ++pos_index) {
      String dinucleotide = word.substring(pos_index, pos_index + 2);
      Integer letter_index = indexByLetter.get(dinucleotide);
      if (letter_index != null) {
        sum += matrix[pos_index][letter_index];
      } /*else if (letter == 'N') {    //  alphabet should include letters such AN, CN, GN, TN, NA, NC, NG, NT, NN
        sum += background.mean_value(matrix[pos_index]);
      } */ else {
        throw new IllegalArgumentException("word in PWM#score(#{word}) should have only {ACGT}^2 dinucleotides , but have '" + dinucleotide + "' dinucleotide");
      }
    }
    return sum;
  }

  public double score(Sequence word, BackgroundModel background) throws IllegalArgumentException {
    return score(word.sequence, background);
  }

  public double score(Sequence word) throws IllegalArgumentException {
    return score(word.sequence);
  }

  public double best_score() {
    return best_suffix(0);
  }

  public double worst_score() {
    return worst_suffix(0);
  }

  // best score of suffix s[i..l]
  public double best_suffix(int i) {
    double result = 0.0;
    for (int pos_index = i; pos_index < matrix.length; ++pos_index) {
      result += ArrayExtensions.max(matrix[pos_index]);
    }
    return result;
  }

  double worst_suffix(int i) {
    double result = 0.0;
    for (int pos_index = i; pos_index < matrix.length; ++pos_index) {
      result += ArrayExtensions.min(matrix[pos_index]);
    }
    return result;
  }

  public DiPWM discrete(Double rate) {
    if (rate == null) {
      return this;
    }
    double[][] mat_result;
    mat_result = new double[length()][];
    for (int i = 0; i < matrix.length; ++i) {
      mat_result[i] = new double[4];
      for (int j = 0; j < 4; ++j) {
        mat_result[i][j] = ceil(matrix[i][j] * rate);
      }
    }
    return new DiPWM(mat_result, name);
  }

}
