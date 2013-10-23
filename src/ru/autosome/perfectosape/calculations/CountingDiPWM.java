package ru.autosome.perfectosape.calculations;


import ru.autosome.perfectosape.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CountingDiPWM {

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

  private final DiPWM dipwm;
  private final BackgroundModel dibackground;

  public CountingDiPWM(DiPWM dipwm, BackgroundModel dibackground, Integer max_hash_size) {
    this.dipwm = dipwm;
    this.dibackground = dibackground;
    this.max_hash_size = max_hash_size;
  }

  private double score_mean() {
    double result = 0.0;
    for (double[] pos : dipwm.matrix) {
      result += dibackground.mean_value(pos);
    }
    return result;
  }

  private double score_variance() {
    double variance = 0.0;
    for (double[] pos : dipwm.matrix) {
      double mean_square = dibackground.mean_square_value(pos);
      double mean = dibackground.mean_value(pos);
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


  private HashMap<Double, Double> count_distribution_under_pvalue(double max_pvalue) {
    HashMap<Double, Double> cnt_distribution = new HashMap<Double, Double>();
    double look_for_count = max_pvalue * vocabularyVolume();

    while (!(HashExtensions.sum_values(cnt_distribution) >= look_for_count)) {
      double approximate_threshold;
      try {
        approximate_threshold = threshold_gauss_estimation(max_pvalue);
      } catch (ArithmeticException e) {
        approximate_threshold = dipwm.worst_score();
      }
      cnt_distribution = count_distribution_after_threshold(approximate_threshold);
      max_pvalue *= 2; // if estimation counted too small amount of words - try to lower threshold estimation by doubling pvalue
    }

    return cnt_distribution;
  }

  private HashMap<Double, Double> count_distribution_after_threshold(double threshold) {
    // scores[index_of_letter 'A'] are scores of words of specific (current) length ending with A
    HashMap<Double, Double>[] scores = new HashMap[4];
    for(int i = 0; i < 4; ++i) {
      scores[i] = new HashMap<Double, Double>();
      scores[i].put(0.0, 1.0);
    }

    for (int column = 0; column < dipwm.matrix.length; ++column) {
      scores = recalc_score_hash(scores, dipwm.matrix[column], threshold - dipwm.best_suffix(column + 1));
      if (max_hash_size != null && scores[0].size() + scores[1].size() + scores[2].size() + scores[3].size() > max_hash_size) {
        throw new IllegalArgumentException("Hash overflow in DiPWM::ThresholdByPvalue#count_distribution_after_threshold");
      }
    }

    HashMap<Double, Double> result = combine_scores(scores);
    return result;
  }

  HashMap<Double,Double> combine_scores(HashMap<Double,Double>[] scores) {
    HashMap<Double,Double> combined_scores = new HashMap<Double, Double>();
    for (int i = 0; i < 4; ++i) {
      for (Double score : scores[i].keySet()) {
        if (!combined_scores.containsKey(score)) {
          combined_scores.put(score, 0.0);
        }
        combined_scores.put(score, combined_scores.get(score) + scores[i].get(score));
      }
    }
    return combined_scores;
  }

  private HashMap<Double, Double>[] recalc_score_hash(HashMap<Double, Double>[] scores, double[] column, double least_sufficient) {
    HashMap<Double, Double>[] new_scores = new HashMap[4];
    for(int i = 0; i < 4; ++i) {
      new_scores[i] = new HashMap<Double, Double>();
      for (Map.Entry<Double, Double> entry : scores[i].entrySet()) {
        double score = entry.getKey();
        double count = entry.getValue();
        for (int letter = 0; letter < 4; ++letter) {
          double new_score = score + column[i*4 + letter];
          if (new_score >= least_sufficient) {
            if (!new_scores[letter].containsKey(new_score)) {
              new_scores[letter].put(new_score, 0.0);
            }
            new_scores[letter].put(new_score, new_scores[letter].get(new_score) + count * dibackground.count(letter));
          }
        }
      }
    }
    return new_scores;
  }

  public HashMap<Double, Double> counts_by_thresholds(double... thresholds) {
    HashMap<Double, Double> scores = count_distribution_after_threshold(ArrayExtensions.min(thresholds));
    HashMap<Double, Double> result = new HashMap<Double, Double>();
    for (double threshold : thresholds) {
      double accum = 0.0;
      for (Map.Entry<Double, Double> entry : scores.entrySet()) {
        double score = entry.getKey();
        double count = entry.getValue();
        if (score >= threshold) {
          accum += count;
        }
      }
      result.put(threshold, accum);
    }
    return result;
  }

  public ArrayList<ThresholdInfo> thresholds(double... pvalues) {
    ArrayList<ThresholdInfo> results = new ArrayList<ThresholdInfo>();
    HashMap<Double, double[][]> thresholds_by_pvalues = thresholds_by_pvalues(pvalues);
    for (double pvalue : thresholds_by_pvalues.keySet()) {
      double thresholds[] = thresholds_by_pvalues.get(pvalue)[0];
      double counts[] = thresholds_by_pvalues.get(pvalue)[1];
      double threshold = thresholds[0] + 0.1 * (thresholds[1] - thresholds[0]);
      double real_pvalue = counts[1] / vocabularyVolume();
      results.add(new ThresholdInfo(threshold, real_pvalue, pvalue, (int) counts[1]));
    }
    return results;
  }

  // "weak" means that threshold has real pvalue not less than given pvalue, while usual threshold not greater
  public ArrayList<ThresholdInfo> weak_thresholds(double... pvalues) {
    ArrayList<ThresholdInfo> results = new ArrayList<ThresholdInfo>();
    HashMap<Double, double[][]> thresholds_by_pvalues = thresholds_by_pvalues(pvalues);
    for (double pvalue : thresholds_by_pvalues.keySet()) {
      double thresholds[] = thresholds_by_pvalues.get(pvalue)[0];
      double counts[] = thresholds_by_pvalues.get(pvalue)[1];
      double threshold = thresholds[0];
      double real_pvalue = counts[0] / vocabularyVolume();
      results.add(new ThresholdInfo(threshold, real_pvalue, pvalue, (int) counts[0]));
    }
    return results;
  }

  private double[] descending_sorted_hash_keys(Map<Double,?> hsh) {
    Double[] keys = new Double[hsh.size()];
    hsh.keySet().toArray(keys);
    java.util.Arrays.sort(keys);
    Double[] descending_keys = ArrayExtensions.reverse(keys);
    return ArrayExtensions.toPrimitiveArray(descending_keys);
    //return ArrayExtensions.toPrimitiveArray(keys);
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

  HashMap<Double, double[][]> thresholds_by_pvalues(double... pvalues) {
    HashMap<Double, Double> scores_hash = count_distribution_under_pvalue(ArrayExtensions.max(pvalues));
    double[] scores = descending_sorted_hash_keys(scores_hash);

    double counts[] = new double[scores.length];
    for (int i = 0; i < scores.length; ++i) {
      counts[i] = scores_hash.get(scores[i]);
    }
    ArrayList<Double> partial_sums = ArrayExtensions.partial_sums(counts, 0.0);
    HashMap<Double, double[][]> results = new HashMap<Double, double[][]>();

    for (double pvalue : pvalues) {
      double look_for_count = pvalue * vocabularyVolume();
      int[] range_indices = indices_of_range(partial_sums, look_for_count);
      if (range_indices[0] == -1) {
        results.put(pvalue, new double[][] { {scores[0], dipwm.best_score() + 1},
                                            {partial_sums.get(0), 0} });
      } else if (range_indices[0] == partial_sums.size()) {
        results.put(pvalue, new double[][] { {dipwm.worst_score() - 1, scores[scores.length - 1]},
                                            {vocabularyVolume(), partial_sums.get(scores.length - 1)} });
      } else {
        results.put(pvalue, new double[][] { {scores[range_indices[1]], scores[range_indices[0]]},
                                            {partial_sums.get(range_indices[1]), partial_sums.get(range_indices[0])} });
      }
    }
    return results;
  }

  public double vocabularyVolume() {
    return Math.pow(dibackground.volume(), dipwm.length());
  }

}
