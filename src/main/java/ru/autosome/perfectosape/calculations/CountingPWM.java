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

import java.util.ArrayList;
import java.util.Arrays;

public class CountingPWM {

  public static class ThresholdInfo extends ResultInfo {
    public final double threshold;
    public final double real_pvalue;
    public final double expected_pvalue;
    public final int recognized_words;

    public ThresholdInfo(double threshold, double real_pvalue, double expected_pvalue, int recognized_words) {
      this.threshold = threshold;
      this.real_pvalue = real_pvalue;
      this.expected_pvalue = expected_pvalue;
      this.recognized_words = recognized_words;
    }

    // generate infos for non-disreeted matrix from infos for discreeted matrix
    public ThresholdInfo downscale(Double discretization) {
      if (discretization == null) {
        return this;
      } else {
        return new ThresholdInfo(threshold / discretization, real_pvalue, expected_pvalue, recognized_words);
      }
    }
  }


  private Integer max_hash_size;

  final PWM pwm;
  final BackgroundModel background;

  public CountingPWM(PWM pwm, BackgroundModel background, Integer max_hash_size) {
    this.pwm = pwm;
    this.background = background;
    this.max_hash_size = max_hash_size;
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

  private TDoubleDoubleMap count_distribution_under_pvalue(double max_pvalue) {
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

  private TDoubleDoubleMap count_distribution_after_threshold(double threshold) {
    TDoubleDoubleMap scores = new TDoubleDoubleHashMap();
    scores.put(0.0, 1.0);
    for (int column = 0; column < pwm.length(); ++column) {
      scores = recalc_score_hash(scores, pwm.matrix[column], threshold - pwm.best_suffix(column + 1));
      if (max_hash_size != null && scores.size() > max_hash_size) {
        throw new IllegalArgumentException("Hash overflow in PWM::ThresholdByPvalue#count_distribution_after_threshold");
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

  public TDoubleDoubleMap counts_by_thresholds(double... thresholds) {
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

  public Double count_by_threshold(double threshold) {
    return counts_by_thresholds(new double[]{threshold}).get(threshold);
  }

  public ThresholdInfo[] thresholds(double... pvalues) {
    ArrayList<ThresholdInfo> results = new ArrayList<ThresholdInfo>();
    TDoubleObjectMap<double[][]> thresholds_by_pvalues = thresholds_by_pvalues(pvalues);
    TDoubleObjectIterator<double[][]> iterator = thresholds_by_pvalues.iterator();
    while (iterator.hasNext()) {
      iterator.advance();
      double pvalue = iterator.key();
      double thresholds[] = iterator.value()[0];
      double counts[] = iterator.value()[1];
      double threshold = thresholds[0] + 0.1 * (thresholds[1] - thresholds[0]);
      double real_pvalue = counts[1] / vocabularyVolume();
      results.add(new ThresholdInfo(threshold, real_pvalue, pvalue, (int) counts[1]));
    }
    return results.toArray(new ThresholdInfo[results.size()]);
  }

  public ThresholdInfo threshold(double pvalue) {
    return thresholds(new double[]{pvalue})[0];
  }

  // "weak" means that threshold has real pvalue not less than given pvalue, while usual threshold not greater
  public ThresholdInfo[] weak_thresholds(double... pvalues) {
    ArrayList<ThresholdInfo> results = new ArrayList<ThresholdInfo>();
    TDoubleObjectMap<double[][]> thresholds_by_pvalues = thresholds_by_pvalues(pvalues);
    TDoubleObjectIterator<double[][]> iterator = thresholds_by_pvalues.iterator();
    while (iterator.hasNext()) {
      iterator.advance();
      double pvalue = iterator.key();
      double thresholds[] = iterator.value()[0];
      double counts[] = iterator.value()[1];
      double threshold = thresholds[0];
      double real_pvalue = counts[0] / vocabularyVolume();
      results.add(new ThresholdInfo(threshold, real_pvalue, pvalue, (int) counts[0]));
    }
    return results.toArray(new ThresholdInfo[results.size()]);
  }

  public ThresholdInfo weak_threshold(double pvalue) {
    return weak_thresholds(new double[]{pvalue})[0];
  }

  private double[] descending_sorted_hash_keys(TDoubleDoubleMap hsh) {
    double[] keys = hsh.keys();
    Arrays.sort(keys);
    return ArrayExtensions.reverse(keys);
  }


  // [ind_1, ind_2] such as value in [value_1, value_2]
  int[] indices_of_range(ArrayList<Double> list, double value) {
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

  TDoubleObjectMap<double[][]> thresholds_by_pvalues(double... pvalues) {
    TDoubleDoubleMap scores_hash = count_distribution_under_pvalue(ArrayExtensions.max(pvalues));
    double[] scores = descending_sorted_hash_keys(scores_hash);

    double counts[] = new double[scores.length];
    for (int i = 0; i < scores.length; ++i) {
      counts[i] = scores_hash.get(scores[i]);
    }
    ArrayList<Double> partial_sums = ArrayExtensions.partial_sums(counts, 0.0);
    TDoubleObjectMap<double[][]> results = new TDoubleObjectHashMap<double[][]>();

    for (double pvalue : pvalues) {
      double look_for_count = pvalue * vocabularyVolume();
      int[] range_indices = indices_of_range(partial_sums, look_for_count);
      if (range_indices[0] == -1) {
        results.put(pvalue, new double[][] { {scores[0], pwm.best_score() + 1},
                                             {partial_sums.get(0), 0} });
      } else if (range_indices[0] == partial_sums.size()) {
        results.put(pvalue, new double[][] { {pwm.worst_score() - 1, scores[scores.length - 1]},
                                             {vocabularyVolume(), partial_sums.get(scores.length - 1)} });
      } else {
        results.put(pvalue, new double[][] { {scores[range_indices[1]], scores[range_indices[0]]},
                                             {partial_sums.get(range_indices[1]), partial_sums.get(range_indices[0])} });
      }
    }
    return results;
  }

  public double vocabularyVolume() {
    return Math.pow(background.volume(), pwm.length());
  }
}
