package ru.autosome.commons.motifModel.di;

import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.di.DiWordwiseBackground;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.*;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.commons.motifModel.types.PositionWeightModel;
import ru.autosome.commons.scoringModel.DiPWMOnBackground;
import ru.autosome.perfectosape.calculation.ScoringModelDistributions.CountingDiPWM;
import ru.autosome.perfectosape.calculation.ScoringModelDistributions.ScoringModelDistibutions;
import ru.autosome.perfectosape.model.Sequence;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class DiPWM extends DiPM implements ScoringModel,
                                            Discretable<DiPWM>,
                                            ScoreStatistics<DiBackgroundModel>,
                                            ScoreDistribution<DiBackgroundModel>,
                                            PositionWeightModel, Alignable<DiPWM> {

  private double[][] cache_best_suffices;
  private double[][] cache_worst_suffices;

  public DiPWM(double[][] matrix, String name) {
    super(matrix, name);
  }

  public static DiPWM fromPWM(PWM pwm) {
    double[][] matrix = new double[pwm.getMatrix().length - 1][];
    for (int i = 0; i < matrix.length; ++i) {
      matrix[i] = new double[16];
      for (int letter = 0; letter < ALPHABET_SIZE; ++letter) {
        matrix[i][letter] = pwm.getMatrix()[i][letter/4];
      }
    }
    for (int letter = 0; letter < ALPHABET_SIZE; ++letter) {
      matrix[matrix.length - 1][letter] += pwm.getMatrix()[matrix.length][letter % 4];
    }
    return new DiPWM(matrix, pwm.name);
  }

  public double score(Sequence word, DiBackgroundModel background) throws IllegalArgumentException {
    return new DiPWMOnBackground(this, background).score(word);
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
    return discrete(new Discretizer(rate));
  }

  @Override
  public DiPWM discrete(Discretizer discretizer) {
    double[][] mat_result;
    mat_result = new double[matrix.length][];
    for (int i = 0; i < matrix.length; ++i) {
      mat_result[i] = new double[ALPHABET_SIZE];
      for (int j = 0; j < ALPHABET_SIZE; ++j) {
        mat_result[i][j] = discretizer.discrete(matrix[i][j]);
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

  @Override
  public DiPWM reverseComplement() {
    double[][] matrix_revcomp = new double[matrix.length][];
    for (int i = 0; i < matrix.length; ++i) {
      matrix_revcomp[i] = new double[16];
      for (int first_letter_index = 0; first_letter_index < 4; ++first_letter_index) {
        for (int second_letter_index = 0; second_letter_index < 4; ++second_letter_index) {
          matrix_revcomp[i][4*first_letter_index + second_letter_index] = matrix[matrix.length - 1 - i][4*second_letter_index + first_letter_index];
        }
      }
    }

    return new DiPWM(matrix_revcomp, name);
  }

  @Override
  public DiPWM leftAugment(int n) {
    double[][] aligned_matrix = new double[matrix.length + n][];
    for(int i = 0; i < n; ++i) {
      aligned_matrix[i] = new double[]{0,0,0,0,
                                       0,0,0,0,
                                       0,0,0,0,
                                       0,0,0,0};
    }
    System.arraycopy(matrix, 0, aligned_matrix, n, matrix.length);
    return new DiPWM(aligned_matrix, name);
  }

  @Override
  public DiPWM rightAugment(int n) {
    double[][] aligned_matrix = new double[matrix.length + n][];
    System.arraycopy(matrix, 0, aligned_matrix, 0, matrix.length);
    for(int i = 0; i < n; ++i) {
      aligned_matrix[matrix.length + i] = new double[]{0,0,0,0,
                                                  0,0,0,0,
                                                  0,0,0,0,
                                                  0,0,0,0};
    }
    return new DiPWM(aligned_matrix, name);
  }
}
