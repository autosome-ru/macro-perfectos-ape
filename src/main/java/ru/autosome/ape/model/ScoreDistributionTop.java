package ru.autosome.ape.model;

import gnu.trove.iterator.TDoubleDoubleIterator;
import gnu.trove.map.TDoubleDoubleMap;
import gnu.trove.map.TDoubleObjectMap;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import gnu.trove.map.hash.TDoubleObjectHashMap;
import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.support.ArrayExtensions;

import java.util.ArrayList;
import java.util.List;

// Top part of score distribution
public class ScoreDistributionTop {
  public static class NotRepresentativeDistribution extends Exception {
    public NotRepresentativeDistribution() { super(); }
    public NotRepresentativeDistribution(String msg) { super(msg); }
  }

  private final double left_score_boundary; // score distribution left boundary. `-INF` if distribution is full.
                                      // or `threshold` if distribution if above threshold
  private final TDoubleDoubleMap score_count_hash; // score --> count mapping
  private final double total_count; // sum of all counts under score distribution (not only under top part)

  private Double cache_best_score;  // best score and worst score are used to estimate score when it is
  private Double cache_worst_score;


  public double getWorstScore() {
    if (cache_worst_score != null) {
      return cache_worst_score;
    } else {
      return Double.NEGATIVE_INFINITY;
    }
  }
  public void setWorstScore(double value) { cache_worst_score = value; }

  public double getBestScore() {
    if (cache_best_score == null) { // cache
      double max_score = Double.NEGATIVE_INFINITY;
      TDoubleDoubleIterator iterator = score_count_hash.iterator();
      while (iterator.hasNext()) {
        iterator.advance();
        double score = iterator.key();
        max_score = Math.max(score, max_score);
      }
      cache_best_score = max_score;
    }
    return cache_best_score;
  }
  public void setBestScore(double value) { cache_best_score = value; }

  public ScoreDistributionTop(TDoubleDoubleMap score_count_hash, double total_count, double left_score_boundary) {
    this.score_count_hash = score_count_hash;
    this.total_count = total_count;
    this.left_score_boundary = left_score_boundary;
  }

  // returns map threshold --> count
  public TDoubleDoubleMap counts_above_thresholds(List<Double> thresholds) throws NotRepresentativeDistribution {
    TDoubleDoubleMap result = new TDoubleDoubleHashMap();
    for (double threshold : thresholds) {
      result.put(threshold, count_above_threshold(threshold));
    }
    return result;
  }

  public double count_above_threshold(double threshold) throws NotRepresentativeDistribution {
    if (threshold < left_score_boundary) {
      throw new NotRepresentativeDistribution("Score distribution left boundary " + left_score_boundary + " is greater than requested threshold " + threshold);
    }

    double accum = 0.0;
    TDoubleDoubleIterator iterator = score_count_hash.iterator();
    while(iterator.hasNext()) {
      iterator.advance();
      double score = iterator.key();
      double count = iterator.value();
      if (score >= threshold) {
        accum += count;
      }
    }
    return accum;
  }

  ThresholdsRange thresholdsRangeByCount(double[] scores, List<Double> partial_sums, double look_for_count) {
    int[] range_indices = ArrayExtensions.indices_of_range(partial_sums, look_for_count);
    if (range_indices[0] == -1) {
      return new ThresholdsRange(scores[0], getBestScore() + 1,
                                 partial_sums.get(0), 0);
    } else if (range_indices[0] == partial_sums.size()) {
      return new ThresholdsRange(getWorstScore() - 1, scores[scores.length - 1],
                                 total_count, partial_sums.get(scores.length - 1));
    } else {
      return new ThresholdsRange(scores[range_indices[1]], scores[range_indices[0]],
                                 partial_sums.get(range_indices[1]), partial_sums.get(range_indices[0]));
    }
  }

  // count under given part of distribution
  public double top_part_count() {
    double accum = 0.0;
    TDoubleDoubleIterator iterator = score_count_hash.iterator();
    while(iterator.hasNext()) {
      iterator.advance();
      double count = iterator.value();
      accum += count;
    }
    return accum;
  }

  // pvalue of given part of distribution
  public double top_part_pvalue() {
    return top_part_count() / total_count;
  }

  public TDoubleObjectMap<ThresholdsRange> thresholds_by_pvalues(List<Double> pvalues) throws NotRepresentativeDistribution {
    double eps = 1e-10; // allowable discrepancy
    if (top_part_pvalue() + eps < ArrayExtensions.max(pvalues)) {
      throw new NotRepresentativeDistribution("Score distribution covers values up to pvalue " + top_part_pvalue() +
                                               " but pvalue " + ArrayExtensions.max(pvalues)  + " was requested");
    }

    double[] scores = ArrayExtensions.descending_sorted_hash_keys(score_count_hash);

    double counts[] = new double[scores.length];
    for (int i = 0; i < scores.length; ++i) {
      counts[i] = score_count_hash.get(scores[i]);
    }
    List<Double> partial_sums = ArrayExtensions.partial_sums(counts, 0.0);

    TDoubleObjectMap<ThresholdsRange> results = new TDoubleObjectHashMap<>();
    for (double pvalue : pvalues) {
      double look_for_count = pvalue * total_count;
      results.put(pvalue, thresholdsRangeByCount(scores, partial_sums, look_for_count));
    }
    return results;
  }


  // "strong" means that threshold has real pvalue not more than requested one
  public List<CanFindThreshold.ThresholdInfo> strong_thresholds(List<Double> pvalues) throws NotRepresentativeDistribution {
    return thresholds(pvalues, BoundaryType.LOWER);
  }

  public CanFindThreshold.ThresholdInfo strong_threshold(double pvalue) throws NotRepresentativeDistribution {
    List<Double> pvalues = new ArrayList<>();
    pvalues.add(pvalue);
    return strong_thresholds(pvalues).get(0);
  }

  // "strong" means that threshold has real pvalue not less than requested one
  public List<CanFindThreshold.ThresholdInfo> weak_thresholds(List<Double> pvalues) throws NotRepresentativeDistribution {
    return thresholds(pvalues, BoundaryType.UPPER);
  }

  public CanFindThreshold.ThresholdInfo weak_threshold(double pvalue) throws NotRepresentativeDistribution {
    List<Double> pvalues = new ArrayList<>();
    pvalues.add(pvalue);
    return weak_thresholds(pvalues).get(0);
  }

  public List<CanFindThreshold.ThresholdInfo> thresholds(List<Double> pvalues, BoundaryType pvalueBoundary) throws NotRepresentativeDistribution {
    ArrayList<CanFindThreshold.ThresholdInfo> results = new ArrayList<>();
    TDoubleObjectMap<ThresholdsRange> thresholds_by_pvalues = thresholds_by_pvalues(pvalues);
    for (double pvalue: pvalues) {
      ThresholdsRange range = thresholds_by_pvalues.get(pvalue);
      double threshold, real_pvalue;
      if (pvalueBoundary == BoundaryType.LOWER) { // strong threshold
        threshold = range.first_threshold + 0.1 * (range.second_threshold - range.first_threshold);
        real_pvalue = range.second_count / total_count;
      } else { // weak threshold
        threshold = range.first_threshold;
        real_pvalue = range.first_count / total_count;
      }
      results.add(new CanFindThreshold.ThresholdInfo(threshold, real_pvalue, pvalue));
    }
    return results;
  }

  public CanFindThreshold.ThresholdInfo threshold(double pvalue, BoundaryType pvalueBoundary) throws NotRepresentativeDistribution {
    List<Double> pvalues = new ArrayList<>();
    pvalues.add(pvalue);
    return thresholds(pvalues, pvalueBoundary).get(0);
  }


  // Container for a range of thresholds and appropriate counts.
  // Following inequations are assumed
  // first threshold < second threshold
  // first count > second count
  public static class ThresholdsRange {
    final double first_threshold;
    final double second_threshold;
    final double first_count;
    final double second_count;
    ThresholdsRange(double first_threshold, double second_threshold, double first_count, double second_count) {
      this.first_threshold = first_threshold;
      this.second_threshold = second_threshold;
      this.first_count = first_count;
      this.second_count = second_count;
    }
  }
}
