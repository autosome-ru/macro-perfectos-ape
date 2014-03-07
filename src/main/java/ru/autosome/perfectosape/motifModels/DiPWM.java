package ru.autosome.perfectosape.motifModels;

import ru.autosome.perfectosape.Sequence;
import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.backgroundModels.DiWordwiseBackground;
import ru.autosome.perfectosape.calculations.ScoringModelDistributions.CountingDiPWM;
import ru.autosome.perfectosape.calculations.ScoringModelDistributions.ScoringModelDistibutions;
import ru.autosome.perfectosape.importers.PMParser;

import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Math.*;

public class DiPWM implements Named,ScoringModel,Discretable<DiPWM>,ScoreStatistics<DiBackgroundModel>,ScoreDistribution<DiBackgroundModel> {
  static final int ALPHABET_SIZE = 16;
  public final double[][] matrix;
  public String name;

  private double[][] cache_best_suffices;
  private double[][] cache_worst_suffices;

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

  @Override
  public String getName() {
    return name;
  }
  @Override
  public void setName(String name) {
    this.name = name;
  }

  public DiPWM(double[][] matrix, String name) {
    for (double[] pos : matrix) {
      if (pos.length != ALPHABET_SIZE) {
        throw new IllegalArgumentException("Matrix must have " + ALPHABET_SIZE + " elements in each position");
      }
    }
    this.matrix = matrix;
    this.name = name;
  }

  public static DiPWM fromPWM(PWM pwm) {
    double[][] matrix = new double[pwm.matrix.length - 1][];
    for (int i = 0; i < matrix.length; ++i) {
      matrix[i] = new double[16];
      for (int letter = 0; letter < ALPHABET_SIZE; ++letter) {
        matrix[i][letter] = pwm.matrix[i][letter/4];
      }
    }
    for (int letter = 0; letter < ALPHABET_SIZE; ++letter) {
      matrix[matrix.length - 1][letter] += pwm.matrix[matrix.length][letter % 4];
    }
    return new DiPWM(matrix, pwm.name);
  }

  // length of TFBS, not of a matrix representation
  @Override
  public int length() {
    return matrix.length + 1;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(name).append("\n");
    for (double[] pos : matrix) {
      for (int letter_index = 0; letter_index < ALPHABET_SIZE; ++letter_index) {
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

  double score(String word, DiBackgroundModel background) throws IllegalArgumentException {
    word = word.toUpperCase();
    if (word.length() != length()) {
      throw new IllegalArgumentException("word in PWM#score(word) should have the same length as matrix");
    }
    double sum = 0.0;
    for (int pos_index = 0; pos_index < matrix.length; ++pos_index) {
      String dinucleotide = word.substring(pos_index, pos_index + 2);
      Integer superletter_index = indexByLetter.get(dinucleotide);
      if (superletter_index != null) {
        sum += matrix[pos_index][superletter_index];
      } /*else if (letter == 'N') {    //  alphabet should include letters such AN, CN, GN, TN, NA, NC, NG, NT, NN
        sum += background.mean_value(matrix[pos_index]);
      } */ else {
        throw new IllegalArgumentException("word in PWM#score(#{word}) should have only {ACGT}^2 dinucleotides , but has '" + dinucleotide + "' dinucleotide");
      }
    }
    return sum;
  }

  public double score(Sequence word, DiBackgroundModel background) throws IllegalArgumentException {
    return score(word.sequence, background);
  }

  @Override
  public double score(Sequence word) throws IllegalArgumentException {
    return score(word, new DiWordwiseBackground());
  }

  public double best_score() {
    double best_score = Double.NEGATIVE_INFINITY;
    for (int letter = 0; letter < 4; ++letter) {
      best_score = max(best_score,
                       best_suffix(0, letter));
    }
    return best_score;
  }

  public double worst_score() {
    double worst_score = Double.POSITIVE_INFINITY;
    for (int letter = 0; letter < 4; ++letter) {
      worst_score = min(worst_score,
                        worst_suffix(0, letter));
    }
    return worst_score;
  }

  // result is an array of best suffices, such that best_suffices()[pos][letter]
  // is the best score of suffix seq[pos:end] of word such that seq[pos] == letter
  // suffix of length 1 has no score (because it's dinculeotide model)
  // so such elements are equal zero
  public double best_suffix(int pos, int letter) {
    return best_suffices()[pos][letter];
  }
  public double worst_suffix(int pos, int letter) {
    return worst_suffices()[pos][letter];
  }

  private double[][] best_suffices() {
    if (cache_best_suffices == null) {
      cache_best_suffices = calculate_best_suffices();
    }
    return cache_best_suffices;
  }

  private double[][] worst_suffices() {
    if (cache_worst_suffices == null) {
      cache_worst_suffices = calculate_worst_suffices();
    }
    return cache_worst_suffices;
  }

  // This pair of methods is alphabet-dependent!
  private double[][] calculate_best_suffices() {
    double[][] result = new double[matrix.length + 1][];
    for(int letter = 0; letter < 4; ++letter) {
      result[matrix.length] = new double[ALPHABET_SIZE];
      result[matrix.length][letter] = 0;
    }
    for(int i = matrix.length - 1; i >= 0; --i) {
      result[i] = new double[ALPHABET_SIZE];
      for (int letter = 0; letter < 4; ++letter) {
        double best_score = Double.NEGATIVE_INFINITY;
        for(int next_letter = 0; next_letter < 4; ++next_letter) {
          best_score = max(best_score, matrix[i][4*letter + next_letter] + result[i+1][next_letter]);
        }
        result[i][letter] = best_score;
      }
    }
    return result;
  }

  private double[][] calculate_worst_suffices() {
    double[][] result = new double[matrix.length + 1][];
    for(int letter = 0; letter < 4; ++letter) {
      result[matrix.length] = new double[ALPHABET_SIZE];
      result[matrix.length][letter] = 0;
    }
    for(int i = matrix.length - 1; i >= 0; --i) {
      result[i] = new double[ALPHABET_SIZE];
      for (int letter = 0; letter < 4; ++letter) {
        double worst_score = Double.POSITIVE_INFINITY;
        for(int next_letter = 0; next_letter < 4; ++next_letter) {
          worst_score = min(worst_score, matrix[i][4*letter + next_letter] + result[i+1][next_letter]);
        }
        result[i][letter] = worst_score;
      }
    }
    return result;
  }

  @Override
  public DiPWM discrete(Double rate) {
    if (rate == null) {
      return this;
    }
    double[][] mat_result;
    mat_result = new double[matrix.length][];
    for (int i = 0; i < matrix.length; ++i) {
      mat_result[i] = new double[ALPHABET_SIZE];
      for (int j = 0; j < ALPHABET_SIZE; ++j) {
        mat_result[i][j] = ceil(matrix[i][j] * rate);
      }
    }
    return new DiPWM(mat_result, name);
  }

  @Override
  public double score_mean(DiBackgroundModel background) {
    double result = 0.0;
    for (double[] pos : matrix) {
      result += background.mean_value(pos);
    }
    return result;
  }

  @Override
  public double score_variance(DiBackgroundModel background) {
    double variance = 0.0;
    for (double[] pos : matrix) {
      double mean_square = background.mean_square_value(pos);
      double mean = background.mean_value(pos);
      double squared_mean = mean * mean;
      variance += mean_square - squared_mean;
    }
    return variance;
  }

  @Override
  public ScoringModelDistibutions scoringModelDistibutions(DiBackgroundModel background, Integer maxHashSize) {
    return new CountingDiPWM(this, background, maxHashSize);
  }
}
