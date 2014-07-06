package ru.autosome.commons.motifModel.mono;

import ru.autosome.commons.support.ArrayExtensions;
import ru.autosome.commons.motifModel.*;
import ru.autosome.commons.motifModel.types.PositionWeightModel;
import ru.autosome.perfectosape.model.Sequence;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.backgroundModel.mono.WordwiseBackground;
import ru.autosome.perfectosape.calculation.ScoringModelDistributions.CountingPWM;
import ru.autosome.perfectosape.calculation.ScoringModelDistributions.ScoringModelDistibutions;
import ru.autosome.commons.importer.PMParser;

import java.util.ArrayList;

import static java.lang.Math.ceil;

public class PWM extends PM implements ScoringModel,Discretable<PWM>,
                                        ScoreStatistics<BackgroundModel>,
                                        ScoreDistribution<BackgroundModel>,
                                        PositionWeightModel, Alignable<PWM> {
  private double[] cache_best_suffices;
  private double[] cache_worst_suffices;

  public PWM(double[][] matrix, String name) throws IllegalArgumentException {
    super(matrix, name);
  }

  public static PWM fromParser(PMParser parser) {
    double[][] matrix = parser.matrix();
    String name = parser.name();
    return new PWM(matrix, name);
  }

  private static PWM new_from_text(ArrayList<String> input_lines) {
    return fromParser(new PMParser(input_lines));
  }

  double score(String word, BackgroundModel background) throws IllegalArgumentException {
    word = word.toUpperCase();
    if (word.length() != length()) {
      throw new IllegalArgumentException("word in PWM#score(word) should have the same length as matrix");
    }
    double sum = 0.0;
    for (int pos_index = 0; pos_index < length(); ++pos_index) {
      char letter = word.charAt(pos_index);
      if (indexByLetter.containsKey(letter)) {
        int letter_index = indexByLetter.get(letter);
        sum += matrix[pos_index][letter_index];
      } else if (letter == 'N') {
        sum += background.mean_value(matrix[pos_index]);
      } else {
        throw new IllegalArgumentException("word in PWM#score(#{word}) should have only ACGT or N letters, but have '" + letter + "' letter");
      }
    }
    return sum;
  }

  public double score(Sequence word, BackgroundModel background) throws IllegalArgumentException {
    return score(word.sequence, background);
  }

  @Override
  public double score(Sequence word) throws IllegalArgumentException {
    return score(word, new WordwiseBackground());
  }

  public double best_score() {
    return best_suffix(0);
  }

  public double worst_score() {
    return worst_suffix(0);
  }

  // best score of suffix s[i..l]
  public double best_suffix(int i) {
    return best_suffices()[i];
  }

  double worst_suffix(int i) {
    return worst_suffices()[i];
  }

  double[] worst_suffices() {
    if (cache_worst_suffices == null) {
      double[] result = new double[length() + 1];
      result[length()] = 0;
      for (int pos_index = length() - 1; pos_index >= 0; --pos_index) {
        result[pos_index] = ArrayExtensions.min(matrix[pos_index]) + result[pos_index + 1];
      }
      cache_worst_suffices = result;
    }
    return cache_worst_suffices;
  }

  double[] best_suffices() {
    if (cache_best_suffices == null) {
      double[] result = new double[length() + 1];
      result[length()] = 0;
      for (int pos_index = length() - 1; pos_index >= 0; --pos_index) {
        result[pos_index] = ArrayExtensions.max(matrix[pos_index]) + result[pos_index + 1];
      }
      cache_best_suffices = result;
    }
    return cache_best_suffices;
  }

  @Override
  public PWM discrete(Double rate) {
    if (rate == null) {
      return this;
    }
    double[][] mat_result;
    mat_result = new double[matrix.length][];
    for (int i = 0; i < matrix.length; ++i) {
      mat_result[i] = new double[4];
      for (int j = 0; j < 4; ++j) {
        mat_result[i][j] = ceil(matrix[i][j] * rate);
      }
    }
    return new PWM(mat_result, name);
  }

  @Override
  public PWM reverseComplement() {
    double[][] matrix_revcomp;
    matrix_revcomp = ArrayExtensions.reverse(matrix);
    for (int i = 0; i < matrix_revcomp.length; ++i) {
      matrix_revcomp[i] = ArrayExtensions.reverse(matrix_revcomp[i]);
    }
    return new PWM(matrix_revcomp, name);
  }

  @Override
  public PWM leftAugment(int n) {
    double[][] aligned_matrix = new double[length() + n][];
    for(int i = 0; i < n; ++i) {
      aligned_matrix[i] = new double[]{0,0,0,0};
    }
    System.arraycopy(matrix, 0, aligned_matrix, n, length());
    return new PWM(aligned_matrix, name);
  }

  @Override
  public PWM rightAugment(int n) {
    double[][] aligned_matrix = new double[length() + n][];
    System.arraycopy(matrix, 0, aligned_matrix, 0, length());
    for(int i = 0; i < n; ++i) {
      aligned_matrix[length() + i] = new double[]{0,0,0,0};
    }
    return new PWM(aligned_matrix, name);
  }

  @Override
  public double score_mean(BackgroundModel background) {
    double result = 0.0;
    for (double[] pos : matrix) {
      result += background.mean_value(pos);
    }
    return result;
  }

  @Override
  public double score_variance(BackgroundModel background) {
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
  public ScoringModelDistibutions scoringModelDistibutions(BackgroundModel background, Integer maxHashSize) {
    return new CountingPWM(this, background, maxHashSize);
  }
}