package ru.autosome.perfectosape.calculations;

import gnu.trove.iterator.TDoubleDoubleIterator;
import gnu.trove.iterator.TDoubleObjectIterator;
import gnu.trove.map.TDoubleDoubleMap;
import gnu.trove.map.TDoubleObjectMap;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import gnu.trove.map.hash.TDoubleObjectHashMap;
import ru.autosome.perfectosape.ArrayExtensions;
import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThreshold;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThresholdApproximation;

import java.util.ArrayList;
import java.util.List;

abstract public class ScoringModelDistibutions {
  abstract CanFindThresholdApproximation gaussianThresholdEstimator();
  protected abstract TDoubleDoubleMap count_distribution_above_threshold(double threshold) throws HashOverflowException;
  abstract double worst_score();
  abstract double best_score();
  abstract double vocabularyVolume();

  private TDoubleDoubleMap count_distribution() throws HashOverflowException {
    return count_distribution_above_threshold(worst_score());
  }

  private TDoubleDoubleMap count_distribution_under_pvalue(double max_pvalue) throws HashOverflowException {
    TDoubleDoubleMap cnt_distribution = new TDoubleDoubleHashMap();
    double look_for_count = max_pvalue * vocabularyVolume();
    CanFindThresholdApproximation gaussianThresholdEstimation = gaussianThresholdEstimator();

    while (!(ArrayExtensions.sum(cnt_distribution.valueCollection()) >= look_for_count)) {
      double approximate_threshold;
      try {
        approximate_threshold = gaussianThresholdEstimation.thresholdByPvalue(max_pvalue);
      } catch (ArithmeticException e) {
        return count_distribution();
      }
      cnt_distribution = count_distribution_above_threshold(approximate_threshold);
      max_pvalue *= 2; // if estimation counted too small amount of words - try to lower threshold estimation by doubling pvalue
    }

    return cnt_distribution;
  }

  public TDoubleDoubleMap counts_by_thresholds(double[] thresholds) throws HashOverflowException {
    TDoubleDoubleMap scores = count_distribution_above_threshold(ArrayExtensions.min(thresholds));
    TDoubleDoubleMap result = new TDoubleDoubleHashMap();
    for (double threshold : thresholds) {
      double accum = 0.0;
      TDoubleDoubleIterator iterator = scores.iterator();
      while(iterator.hasNext()) {
        iterator.advance();
        double score = iterator.key();
        double count = iterator.value();
        if (score >= threshold) {
          accum += count;
        }
      }
      result.put(threshold, accum);
    }
    return result;
  }

  TDoubleObjectMap<ThresholdsRange> thresholds_by_pvalues(double[] pvalues) throws HashOverflowException {
    TDoubleDoubleMap scores_hash = count_distribution_under_pvalue(ArrayExtensions.max(pvalues));
    double[] scores = ArrayExtensions.descending_sorted_hash_keys(scores_hash);

    double counts[] = new double[scores.length];
    for (int i = 0; i < scores.length; ++i) {
      counts[i] = scores_hash.get(scores[i]);
    }
    List<Double> partial_sums = ArrayExtensions.partial_sums(counts, 0.0);
    TDoubleObjectMap<ThresholdsRange> results = new TDoubleObjectHashMap<ThresholdsRange>();

    for (double pvalue : pvalues) {
      double look_for_count = pvalue * vocabularyVolume();

      int[] range_indices = ArrayExtensions.indices_of_range(partial_sums, look_for_count);
      ThresholdsRange thresholdsRange;
      if (range_indices[0] == -1) {
        thresholdsRange = new ThresholdsRange(scores[0], best_score() + 1,
                                              partial_sums.get(0), 0);
      } else if (range_indices[0] == partial_sums.size()) {
        thresholdsRange = new ThresholdsRange(worst_score() - 1, scores[scores.length - 1],
                                              vocabularyVolume(), partial_sums.get(scores.length - 1));
      } else {
        thresholdsRange = new ThresholdsRange(scores[range_indices[1]], scores[range_indices[0]],
                                              partial_sums.get(range_indices[1]), partial_sums.get(range_indices[0]));
      }
      results.put(pvalue, thresholdsRange);
    }
    return results;
  }

  public Double count_by_threshold(double threshold) throws HashOverflowException {
    return counts_by_thresholds(new double[]{threshold}).get(threshold);
  }

  // "strong" means that threshold has real pvalue not more than requested one
  public CanFindThreshold.ThresholdInfo[] strong_thresholds(double[] pvalues) throws HashOverflowException {
    return thresholds(pvalues, BoundaryType.LOWER);
  }

  public CanFindThreshold.ThresholdInfo strong_threshold(double pvalue) throws HashOverflowException {
    return strong_thresholds(new double[]{pvalue})[0];
  }

  // "strong" means that threshold has real pvalue not less than requested one
  public CanFindThreshold.ThresholdInfo[] weak_thresholds(double[] pvalues) throws HashOverflowException {
    return thresholds(pvalues, BoundaryType.UPPER);
  }

  public CanFindThreshold.ThresholdInfo weak_threshold(double pvalue) throws HashOverflowException {
    return weak_thresholds(new double[]{pvalue})[0];
  }

  public CanFindThreshold.ThresholdInfo[] thresholds(double[] pvalues, BoundaryType pvalueBoundary) throws HashOverflowException {
    ArrayList<CanFindThreshold.ThresholdInfo> results = new ArrayList<CanFindThreshold.ThresholdInfo>();
    TDoubleObjectMap<ThresholdsRange> thresholds_by_pvalues = thresholds_by_pvalues(pvalues);
    TDoubleObjectIterator<ThresholdsRange> iterator = thresholds_by_pvalues.iterator();
    while (iterator.hasNext()) {
      iterator.advance();
      double pvalue = iterator.key();
      ThresholdsRange range = iterator.value();
      double threshold, real_pvalue;
      if (pvalueBoundary == BoundaryType.LOWER) { // strong threshold
        threshold = range.first_threshold + 0.1 * (range.second_threshold - range.first_threshold);
        real_pvalue = range.second_count / vocabularyVolume();
      } else { // weak threshold
        threshold = range.first_threshold;
        real_pvalue = range.first_count / vocabularyVolume();
      }
      results.add(new CanFindThreshold.ThresholdInfo(threshold, real_pvalue, pvalue));
    }
    return results.toArray(new CanFindThreshold.ThresholdInfo[results.size()]);
  }

  public CanFindThreshold.ThresholdInfo threshold(double pvalue, BoundaryType pvalueBoundary) throws HashOverflowException {
    return thresholds(new double[]{pvalue}, pvalueBoundary)[0];
  }

  // Container for a range of thresholds and appropriate counts.
  // Following inequations are assumed
  // first threshold < second threshold
  // first count > second count
  public static class ThresholdsRange {
    double first_threshold, second_threshold;
    double first_count, second_count;
    ThresholdsRange(double first_threshold, double second_threshold, double first_count, double second_count) {
      this.first_threshold = first_threshold;
      this.second_threshold = second_threshold;
      this.first_count = first_count;
      this.second_count = second_count;
    }
  }
}
