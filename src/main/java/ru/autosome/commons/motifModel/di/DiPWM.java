package ru.autosome.commons.motifModel.di;

import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.di.DiWordwiseBackground;
import ru.autosome.commons.model.indexingScheme.DiIndexingScheme;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.*;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.commons.motifModel.types.PositionWeightModel;
import ru.autosome.commons.scoringModel.DiPWMOnBackground;
import ru.autosome.perfectosape.calculation.ScoringModelDistributions.CountingDiPWM;
import ru.autosome.perfectosape.calculation.ScoringModelDistributions.ScoringModelDistributions;
import ru.autosome.perfectosape.model.Sequence;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class DiPWM extends DiPM implements  BackgroundAppliable<DiBackgroundModel, DiPWMOnBackground>,
                                            Discretable<DiPWM>,
                                            ScoreDistribution<DiBackgroundModel>,
                                            PositionWeightModel, Alignable<DiPWM>,
                                            ScoreBoundaries {

  private double[][] cache_best_suffices;
  private double[][] cache_worst_suffices;

  public DiPWM(double[][] matrix) {
    super(matrix);
  }

  public static DiPWM fromPWM(PWM pwm) {
    double[][] matrix = new double[pwm.getMatrix().length - 1][];
    for (int i = 0; i < matrix.length; ++i) {
      matrix[i] = new double[16];
      for (int letter = 0; letter < ALPHABET_SIZE; ++letter) {
        matrix[i][letter] = pwm.getMatrix()[i][DiIndexingScheme.firstLetterIndex(letter)];
      }
    }
    for (int letter = 0; letter < ALPHABET_SIZE; ++letter) {
      matrix[matrix.length - 1][letter] += pwm.getMatrix()[matrix.length][DiIndexingScheme.secondLetterIndex(letter)];
    }
    return new DiPWM(matrix);
  }

  public double score(Sequence word) {
    return new DiPWMOnBackground(this, new DiWordwiseBackground()).score(word.diEncode());
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
          best_score = max(best_score, matrix[i][DiIndexingScheme.diIndex(letter, next_letter)] + result[i+1][next_letter]);
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
          worst_score = min(worst_score, matrix[i][DiIndexingScheme.diIndex(letter, next_letter)] + result[i+1][next_letter]);
        }
        result[i][letter] = worst_score;
      }
    }
    return result;
  }

  @Override
  public DiPWM discrete(Discretizer discretizer) {
    return new DiPWM(discretizedMatrix(discretizer));
  }

  @Override
  public ScoringModelDistributions scoringModelDistibutions(DiBackgroundModel background) {
    return new CountingDiPWM(this, background);
  }

  @Override
  public DiPWM reverseComplement() {
    double[][] matrix_revcomp = new double[matrix.length][];
    for (int i = 0; i < matrix.length; ++i) {
      matrix_revcomp[i] = new double[16];
      for (int di_index = 0; di_index < 16; ++di_index) {
        matrix_revcomp[i][di_index] =
            matrix[matrix.length - 1 - i][DiIndexingScheme.complementDinucleotideIndex(di_index)];
      }
    }
    return new DiPWM(matrix_revcomp);
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
    return new DiPWM(aligned_matrix);
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
    return new DiPWM(aligned_matrix);
  }

  @Override
  public DiPWMOnBackground onBackground(DiBackgroundModel background) {
    return new DiPWMOnBackground(this, background);
  }
}
