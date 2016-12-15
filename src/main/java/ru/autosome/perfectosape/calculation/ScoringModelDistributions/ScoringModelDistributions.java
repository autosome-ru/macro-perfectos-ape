package ru.autosome.perfectosape.calculation.ScoringModelDistributions;

import gnu.trove.map.TDoubleDoubleMap;
import gnu.trove.map.TDoubleObjectMap;
import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.ape.calculation.findThreshold.CanFindThresholdApproximation;
import ru.autosome.ape.model.ScoreDistributionTop;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.support.ArrayExtensions;

import java.util.List;

abstract public class ScoringModelDistributions {
  abstract CanFindThresholdApproximation gaussianThresholdEstimator();
  protected abstract ScoreDistributionTop score_distribution_above_threshold(double threshold);

  private ScoreDistributionTop score_distribution() {
    return score_distribution_above_threshold(Double.NEGATIVE_INFINITY);
  }

  private ScoreDistributionTop score_distribution_under_pvalue(double pvalue) {
    final int maxNumberOfAttempts = 2;
    int numberOfAttempts = 0;
    ScoreDistributionTop scoreDistribution;
    CanFindThresholdApproximation gaussianThresholdEstimation = gaussianThresholdEstimator();
    double pvalue_to_estimate_threshold = pvalue;
    try {
      do {
        numberOfAttempts += 1;
        if (numberOfAttempts > maxNumberOfAttempts) {
          return score_distribution(); // calculate whole distribution
        }
        // calculate only top part of distribution cause it's faster
        double approximate_threshold = gaussianThresholdEstimation.thresholdByPvalue(pvalue_to_estimate_threshold);
        scoreDistribution = score_distribution_above_threshold(approximate_threshold);
        pvalue_to_estimate_threshold *= 2;
      } while (scoreDistribution.top_part_pvalue() < pvalue);
      return scoreDistribution;

    } catch (ArithmeticException e) {
      return score_distribution();
    }
  }

  public TDoubleDoubleMap counts_above_thresholds(List<Double> thresholds) {
    ScoreDistributionTop scoreDistribution = score_distribution_above_threshold(ArrayExtensions.min(thresholds));
    try {
      return scoreDistribution.counts_above_thresholds(thresholds);
    } catch (ScoreDistributionTop.NotRepresentativeDistribution exception) {
      throw new RuntimeException("Should never be here", exception);
    }
  }

  public Double count_above_threshold(double threshold) {
    ScoreDistributionTop scoreDistribution = score_distribution_above_threshold(threshold);
    try {
      return scoreDistribution.count_above_threshold(threshold);
    } catch (ScoreDistributionTop.NotRepresentativeDistribution exception) {
      throw new RuntimeException("Should never be here", exception);
    }
  }

  TDoubleObjectMap<ScoreDistributionTop.ThresholdsRange> thresholds_by_pvalues(List<Double> pvalues) {
    ScoreDistributionTop scores_hash = score_distribution_under_pvalue(ArrayExtensions.max(pvalues));
    try {
      return scores_hash.thresholds_by_pvalues(pvalues);
    } catch (ScoreDistributionTop.NotRepresentativeDistribution exception) {
      throw new RuntimeException("Should never be here", exception);
    }
  }

  // "strong" means that threshold has real pvalue not more than requested one
  public List<CanFindThreshold.ThresholdInfo> strong_thresholds(List<Double> pvalues) {
    ScoreDistributionTop scores_hash = score_distribution_under_pvalue(ArrayExtensions.max(pvalues));
    try {
      return scores_hash.strong_thresholds(pvalues);
    } catch (ScoreDistributionTop.NotRepresentativeDistribution exception) {
      throw new RuntimeException("Should never be here", exception);
    }
  }

  public CanFindThreshold.ThresholdInfo strong_threshold(double pvalue) {
    ScoreDistributionTop scores_hash = score_distribution_under_pvalue(pvalue);
    try {
      return scores_hash.strong_threshold(pvalue);
    } catch (ScoreDistributionTop.NotRepresentativeDistribution exception) {
      throw new RuntimeException("Should never be here", exception);
    }
  }

  // "strong" means that threshold has real pvalue not less than requested one
  public List<CanFindThreshold.ThresholdInfo> weak_thresholds(List<Double> pvalues) {
    ScoreDistributionTop scores_hash = score_distribution_under_pvalue(ArrayExtensions.max(pvalues));
    try {
      return scores_hash.weak_thresholds(pvalues);
    } catch (ScoreDistributionTop.NotRepresentativeDistribution exception) {
      throw new RuntimeException("Should never be here", exception);
    }
  }

  public CanFindThreshold.ThresholdInfo weak_threshold(double pvalue) {
    ScoreDistributionTop scores_hash = score_distribution_under_pvalue(pvalue);
    try {
      return scores_hash.weak_threshold(pvalue);
    } catch (ScoreDistributionTop.NotRepresentativeDistribution exception) {
      throw new RuntimeException("Should never be here", exception);
    }
  }

  public List<CanFindThreshold.ThresholdInfo> thresholds(List<Double> pvalues, BoundaryType pvalueBoundary) {
    ScoreDistributionTop scores_hash = score_distribution_under_pvalue(ArrayExtensions.max(pvalues));
    try {
      return scores_hash.thresholds(pvalues, pvalueBoundary);
    } catch (ScoreDistributionTop.NotRepresentativeDistribution exception) {
      throw new RuntimeException("Should never be here", exception);
    }
  }

  public CanFindThreshold.ThresholdInfo threshold(double pvalue, BoundaryType pvalueBoundary) {
    ScoreDistributionTop scores_hash = score_distribution_under_pvalue(pvalue);
    try {
      return scores_hash.threshold(pvalue, pvalueBoundary);
    } catch (ScoreDistributionTop.NotRepresentativeDistribution exception) {
      throw new RuntimeException("Should never be here", exception);
    }
  }
}
