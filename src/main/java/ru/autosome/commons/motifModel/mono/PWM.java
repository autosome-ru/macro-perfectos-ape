package ru.autosome.commons.motifModel.mono;

import ru.autosome.ape.calculation.ScoringModelDistributions.PWMScoresGenerator;
import ru.autosome.ape.calculation.ScoringModelDistributions.ScoringDistributionGenerator;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.*;
import ru.autosome.commons.motifModel.types.PositionWeightModel;
import ru.autosome.commons.scoringModel.PWMSequenceScoring;
import ru.autosome.commons.support.ArrayExtensions;

public class PWM extends PM implements  BackgroundAppliable<BackgroundModel, PWMSequenceScoring>,
                                        Discretable<PWM>,
                                        ScoreDistribution<BackgroundModel>,
                                        PositionWeightModel, Alignable<PWM>,
                                        ScoreBoundaries {
  private double[] cache_best_suffices;
  private double[] cache_worst_suffices;

  public PWM(double[][] matrix) throws IllegalArgumentException {
    super(matrix);
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
  public PWM discrete(Discretizer discretizer) {
    return new PWM(discretedMatrix(discretizer));
  }

  @Override
  public PWM reverseComplement() {
    double[][] matrix_revcomp;
    matrix_revcomp = ArrayExtensions.reverse(matrix);
    for (int i = 0; i < matrix_revcomp.length; ++i) {
      matrix_revcomp[i] = ArrayExtensions.reverse(matrix_revcomp[i]);
    }
    return new PWM(matrix_revcomp);
  }

  @Override
  public PWM leftAugment(int n) {
    double[][] aligned_matrix = new double[length() + n][];
    for(int i = 0; i < n; ++i) {
      aligned_matrix[i] = new double[]{0,0,0,0};
    }
    System.arraycopy(matrix, 0, aligned_matrix, n, length());
    return new PWM(aligned_matrix);
  }

  @Override
  public PWM rightAugment(int n) {
    double[][] aligned_matrix = new double[length() + n][];
    System.arraycopy(matrix, 0, aligned_matrix, 0, length());
    for(int i = 0; i < n; ++i) {
      aligned_matrix[length() + i] = new double[]{0,0,0,0};
    }
    return new PWM(aligned_matrix);
  }

  @Override
  public ScoringDistributionGenerator scoringModel(BackgroundModel background) {
    return new PWMScoresGenerator(this, background);
  }

  @Override
  public PWMSequenceScoring onBackground(BackgroundModel background) {
    return new PWMSequenceScoring(this, background);
  }
}
