package ru.autosome.macroape.Calculations;

import ru.autosome.macroape.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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


  public Integer max_hash_size;

  private final PWM pwm;
  private final BackgroundModel background;

  public CountingPWM(PWM pwm, BackgroundModel background) {
    this.pwm = pwm;
    this.background = background;
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


  private HashMap<Double, Double> count_distribution_under_pvalue(double max_pvalue) {
    HashMap<Double, Double> cnt_distribution = new HashMap<Double, Double>();
    double look_for_count = max_pvalue * vocabularyVolume();

    while (!(HashExtensions.sum_values(cnt_distribution) >= look_for_count)) {
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

  private HashMap<Double, Double> count_distribution_after_threshold(double threshold) {
    HashMap<Double, Double> scores = new HashMap<Double, Double>();
    scores.put(0.0, 1.0);
    for (int column = 0; column < pwm.length(); ++column) {
      scores = recalc_score_hash(scores, pwm.matrix[column], threshold - pwm.best_suffix(column + 1));
      if (max_hash_size != null && scores.size() > max_hash_size) {
        throw new IllegalArgumentException("Hash overflow in PWM::ThresholdByPvalue#count_distribution_after_threshold");
      }
    }
    return scores;
  }

  private HashMap<Double, Double> recalc_score_hash(HashMap<Double, Double> scores, double[] column, double least_sufficient) {
    HashMap<Double, Double> new_scores = new HashMap<Double, Double>();
    for (Map.Entry<Double, Double> entry : scores.entrySet()) {
      double score = entry.getKey();
      double count = entry.getValue();
      for (int letter = 0; letter < 4; ++letter) {
        double new_score = score + column[letter];
        if (new_score >= least_sufficient) {
          if (!new_scores.containsKey(new_score)) {
            new_scores.put(new_score, 0.0);
          }
          new_scores.put(new_score, new_scores.get(new_score) + count * background.count(letter));
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

  HashMap<Double, double[][]> thresholds_by_pvalues(double... pvalues) {
    HashMap<Double, Double> scores_hash = count_distribution_under_pvalue(ArrayExtensions.max(pvalues));
    Double[] scores_keys = new Double[scores_hash.size()];
    scores_hash.keySet().toArray(scores_keys);
    java.util.Arrays.sort(scores_keys);
    Double[] sorted_scores_keys = ArrayExtensions.reverse(scores_keys);
    double scores[] = ArrayExtensions.toPrimitiveArray(sorted_scores_keys);

    double counts[] = new double[scores.length];
    for (int i = 0; i < scores.length; ++i) {
      counts[i] = scores_hash.get(scores[i]);
    }

    double partial_sums[] = ArrayExtensions.partial_sums(counts, 0.0);

    HashMap<Double, double[][]> results = new HashMap<Double, double[][]>();

    double sorted_pvalues[] = pvalues.clone();
    Arrays.sort(sorted_pvalues);
    HashMap<Double, Double> pvalue_counts = new HashMap<Double, Double>();
    for (double pvalue : sorted_pvalues) {
      pvalue_counts.put(pvalue, pvalue * vocabularyVolume());
    }
    for (Map.Entry<Double, Double> entry : pvalue_counts.entrySet()) {
      double pvalue = entry.getKey();
      double look_for_count = entry.getValue();
      int ind = 0;
      for (int i = 0; i < partial_sums.length; ++i) {
        if (partial_sums[i] >= look_for_count) {
          ind = i;
          break;
        }
      }
      double minscore = scores[ind];
      double count_at_minscore = partial_sums[ind];
      double maxscore;
      double count_at_maxscore;
      if (ind > 0) {
        maxscore = scores[ind - 1];
        count_at_maxscore = partial_sums[ind - 1];
      } else {
        maxscore = pwm.best_score() + 1.0;
        count_at_maxscore = 0.0;
      }

      double[][] resulting_ranges = {{minscore, maxscore}, {count_at_minscore, count_at_maxscore}};
      results.put(pvalue, resulting_ranges);
    }
    return results;
  }

  public double vocabularyVolume() {
    return Math.pow(background.volume(), pwm.length());
  }
}
