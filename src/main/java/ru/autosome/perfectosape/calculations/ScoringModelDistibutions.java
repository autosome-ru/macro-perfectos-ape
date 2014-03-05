package ru.autosome.perfectosape.calculations;

import gnu.trove.map.TDoubleDoubleMap;
import gnu.trove.map.TDoubleObjectMap;
import ru.autosome.perfectosape.ArrayExtensions;
import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.ScoreDistributionTop;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThreshold;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThresholdApproximation;

abstract public class ScoringModelDistibutions {
  abstract CanFindThresholdApproximation gaussianThresholdEstimator();
  protected abstract ScoreDistributionTop score_distribution_above_threshold(double threshold) throws HashOverflowException;

  private ScoreDistributionTop score_distribution() throws HashOverflowException {
    return score_distribution_above_threshold(Double.NEGATIVE_INFINITY);
  }

  private ScoreDistributionTop score_distribution_under_pvalue(double pvalue) throws HashOverflowException {
    ScoreDistributionTop scoreDistribution;
    CanFindThresholdApproximation gaussianThresholdEstimation = gaussianThresholdEstimator();
    double pvalue_to_estimate_threshold = pvalue;
    do {
      try {
        double approximate_threshold = gaussianThresholdEstimation.thresholdByPvalue(pvalue_to_estimate_threshold);
        scoreDistribution = score_distribution_above_threshold(approximate_threshold);
      } catch (ArithmeticException e) {
        return score_distribution();
      }
      pvalue_to_estimate_threshold *= 2;
    } while (scoreDistribution.top_part_pvalue() < pvalue);

    return scoreDistribution;
  }

  public TDoubleDoubleMap counts_above_thresholds(double[] thresholds) throws HashOverflowException {
    ScoreDistributionTop scoreDistribution = score_distribution_above_threshold(ArrayExtensions.min(thresholds));
    try {
      return scoreDistribution.counts_above_thresholds(thresholds);
    } catch (ScoreDistributionTop.NotRepresentativeDistribution exception) {
      throw new RuntimeException("Should never be here", exception);
    }
  }

  public Double count_above_threshold(double threshold) throws HashOverflowException {
    ScoreDistributionTop scoreDistribution = score_distribution_above_threshold(threshold);
    try {
      return scoreDistribution.count_above_threshold(threshold);
    } catch (ScoreDistributionTop.NotRepresentativeDistribution exception) {
      throw new RuntimeException("Should never be here", exception);
    }
  }

  TDoubleObjectMap<ScoreDistributionTop.ThresholdsRange> thresholds_by_pvalues(double[] pvalues) throws HashOverflowException {
    ScoreDistributionTop scores_hash = score_distribution_under_pvalue(ArrayExtensions.max(pvalues));
    try {
      return scores_hash.thresholds_by_pvalues(pvalues);
    } catch (ScoreDistributionTop.NotRepresentativeDistribution exception) {
      throw new RuntimeException("Should never be here", exception);
    }
  }

  // "strong" means that threshold has real pvalue not more than requested one
  public CanFindThreshold.ThresholdInfo[] strong_thresholds(double[] pvalues) throws HashOverflowException {
    ScoreDistributionTop scores_hash = score_distribution_under_pvalue(ArrayExtensions.max(pvalues));
    try {
      return scores_hash.strong_thresholds(pvalues);
    } catch (ScoreDistributionTop.NotRepresentativeDistribution exception) {
      throw new RuntimeException("Should never be here", exception);
    }
  }

  public CanFindThreshold.ThresholdInfo strong_threshold(double pvalue) throws HashOverflowException {
    ScoreDistributionTop scores_hash = score_distribution_under_pvalue(pvalue);
    try {
      return scores_hash.strong_threshold(pvalue);
    } catch (ScoreDistributionTop.NotRepresentativeDistribution exception) {
      throw new RuntimeException("Should never be here", exception);
    }
  }

  // "strong" means that threshold has real pvalue not less than requested one
  public CanFindThreshold.ThresholdInfo[] weak_thresholds(double[] pvalues) throws HashOverflowException {
    ScoreDistributionTop scores_hash = score_distribution_under_pvalue(ArrayExtensions.max(pvalues));
    try {
      return scores_hash.weak_thresholds(pvalues);
    } catch (ScoreDistributionTop.NotRepresentativeDistribution exception) {
      throw new RuntimeException("Should never be here", exception);
    }
  }

  public CanFindThreshold.ThresholdInfo weak_threshold(double pvalue) throws HashOverflowException {
    ScoreDistributionTop scores_hash = score_distribution_under_pvalue(pvalue);
    try {
      return scores_hash.weak_threshold(pvalue);
    } catch (ScoreDistributionTop.NotRepresentativeDistribution exception) {
      throw new RuntimeException("Should never be here", exception);
    }
  }

  public CanFindThreshold.ThresholdInfo[] thresholds(double[] pvalues, BoundaryType pvalueBoundary) throws HashOverflowException {
    ScoreDistributionTop scores_hash = score_distribution_under_pvalue(ArrayExtensions.max(pvalues));
    try {
      return scores_hash.thresholds(pvalues, pvalueBoundary);
    } catch (ScoreDistributionTop.NotRepresentativeDistribution exception) {
      throw new RuntimeException("Should never be here", exception);
    }
  }

  public CanFindThreshold.ThresholdInfo threshold(double pvalue, BoundaryType pvalueBoundary) throws HashOverflowException {
    ScoreDistributionTop scores_hash = score_distribution_under_pvalue(pvalue);
    try {
      return scores_hash.threshold(pvalue, pvalueBoundary);
    } catch (ScoreDistributionTop.NotRepresentativeDistribution exception) {
      throw new RuntimeException("Should never be here", exception);
    }
  }
}
