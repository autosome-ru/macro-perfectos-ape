package ru.autosome.perfectosape.calculations;

import gnu.trove.TDoubleCollection;
import gnu.trove.iterator.TDoubleDoubleIterator;
import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.iterator.TDoubleObjectIterator;
import gnu.trove.map.TDoubleDoubleMap;
import gnu.trove.map.TDoubleObjectMap;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import gnu.trove.map.hash.TDoubleObjectHashMap;
import ru.autosome.perfectosape.*;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThreshold;
import ru.autosome.perfectosape.motifModels.PWM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CountingPWM {
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

  private Integer maxHashSize;

  final PWM pwm;
  final BackgroundModel background;

  public CountingPWM(PWM pwm, BackgroundModel background, Integer maxHashSize) {
    this.pwm = pwm;
    this.background = background;
    this.maxHashSize = maxHashSize;
  }

  private double score_mean() {
    double result = 0.0;
    for (double[] pos : pwm.matrix) {
      result += background.mean_value(pos);
    }
    return result;
  }

  private double score_variance() {
    double variance = 0.0;
    for (double[] pos : pwm.matrix) {
      double mean_square = background.mean_square_value(pos);
      double mean = background.mean_value(pos);
      double squared_mean = mean * mean;
      variance += mean_square - squared_mean;
    }
    return variance;
  }

  private double threshold_gauss_estimation(double pvalue) {
    double sigma = Math.sqrt(score_variance());
    double n_ = MathExtensions.inverf(1 - 2 * pvalue) * Math.sqrt(2);
    return score_mean() + n_ * sigma;
  }

  private double sum(TDoubleCollection vals) {
    TDoubleIterator iterator = vals.iterator();
    double result = 0;
    while(iterator.hasNext()) {
      result += iterator.next();
    }
    return result;
  }

  private TDoubleDoubleMap count_distribution_under_pvalue(double max_pvalue) throws HashOverflowException {
    TDoubleDoubleMap cnt_distribution = new TDoubleDoubleHashMap();
    double look_for_count = max_pvalue * vocabularyVolume();

    while (!(sum(cnt_distribution.valueCollection()) >= look_for_count)) {
      double approximate_threshold;
      try {
        approximate_threshold = threshold_gauss_estimation(max_pvalue);
      } catch (ArithmeticException e) {
        approximate_threshold = pwm.worst_score();
      }
      cnt_distribution = count_distribution_after_threshold(approximate_threshold);
      max_pvalue *= 2; // if estimation counted too small amount of words - try to lower threshold estimation by doubling pvalue
    }

    return cnt_distribution;
  }

  private TDoubleDoubleMap count_distribution_after_threshold(double threshold) throws HashOverflowException {
    TDoubleDoubleMap scores = new TDoubleDoubleHashMap();
    scores.put(0.0, 1.0);
    for (int column = 0; column < pwm.length(); ++column) {
      scores = recalc_score_hash(scores, pwm.matrix[column], threshold - pwm.best_suffix(column + 1));
      if (maxHashSize != null && scores.size() > maxHashSize) {
        throw new HashOverflowException("Hash overflow in PWM::ThresholdByPvalue#count_distribution_after_threshold");
      }
    }
    return scores;
  }

  private TDoubleDoubleMap recalc_score_hash(TDoubleDoubleMap scores, double[] column, double least_sufficient) {
    TDoubleDoubleMap new_scores = new TDoubleDoubleHashMap(scores.size());
    TDoubleDoubleIterator iterator = scores.iterator();
    while(iterator.hasNext()) {
      iterator.advance();
      double score = iterator.key();
      double count = iterator.value();
      for (int letter = 0; letter < 4; ++letter) {
        double new_score = score + column[letter];
        if (new_score >= least_sufficient) {
          double add = count * background.count(letter);
          new_scores.adjustOrPutValue(new_score, add, add);
        }
      }
    }
    return new_scores;
  }

  public TDoubleDoubleMap counts_by_thresholds(double[] thresholds) throws HashOverflowException {
    TDoubleDoubleMap scores = count_distribution_after_threshold(ArrayExtensions.min(thresholds));
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

  private double[] descending_sorted_hash_keys(TDoubleDoubleMap hsh) {
    double[] keys = hsh.keys();
    Arrays.sort(keys);
    return ArrayExtensions.reverse(keys);
  }


  // [ind_1, ind_2] such as value in [value_1, value_2]
  int[] indices_of_range(List<Double> list, double value) {
    int ind = java.util.Collections.binarySearch(list, value);
    if (ind >= 0) {
      return new int[] {ind, ind};
    } else {
      int insertion_point = -ind - 1;
      if (insertion_point == 0) {
        return new int[] {-1, -1};
      } else if (insertion_point < list.size()) {
        return new int[] {insertion_point - 1, insertion_point};
      } else {
        return new int[] {list.size(), list.size()};
      }
    }
  }

  TDoubleObjectMap<ThresholdsRange> thresholds_by_pvalues(double[] pvalues) throws HashOverflowException {
    TDoubleDoubleMap scores_hash = count_distribution_under_pvalue(ArrayExtensions.max(pvalues));
    double[] scores = descending_sorted_hash_keys(scores_hash);

    double counts[] = new double[scores.length];
    for (int i = 0; i < scores.length; ++i) {
      counts[i] = scores_hash.get(scores[i]);
    }
    ArrayList<Double> partial_sums = ArrayExtensions.partial_sums(counts, 0.0);
    TDoubleObjectMap<ThresholdsRange> results = new TDoubleObjectHashMap<ThresholdsRange>();

    for (double pvalue : pvalues) {
      double look_for_count = pvalue * vocabularyVolume();
      int[] range_indices = indices_of_range(partial_sums, look_for_count);
      if (range_indices[0] == -1) {
        results.put(pvalue, new ThresholdsRange(scores[0], pwm.best_score() + 1,
                                                partial_sums.get(0), 0));
      } else if (range_indices[0] == partial_sums.size()) {
        results.put(pvalue, new ThresholdsRange(pwm.worst_score() - 1, scores[scores.length - 1],
                                                vocabularyVolume(), partial_sums.get(scores.length - 1)));
      } else {
        results.put(pvalue, new ThresholdsRange(scores[range_indices[1]], scores[range_indices[0]],
                                                partial_sums.get(range_indices[1]), partial_sums.get(range_indices[0])));
      }
    }
    return results;
  }

  public double vocabularyVolume() {
    return Math.pow(background.volume(), pwm.length());
  }
}
