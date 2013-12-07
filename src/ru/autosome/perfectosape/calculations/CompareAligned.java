package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.BackgroundModel;
import ru.autosome.perfectosape.PWM;
import ru.autosome.perfectosape.PWMAligned;
import ru.autosome.perfectosape.Position;

import java.util.HashMap;

public class CompareAligned {
  public static class SimilarityInfo {
    public final double recognizedByBoth;
    public final double recognizedByFirst;
    public final double recognizedBySecond;

    public Double firstVocabularyVolume;
    public Double secondVocabularyVolume;

    public SimilarityInfo(double recognizedByBoth, double recognizedByFirst, double recognizedBySecond,
                          Double firstVocabularyVolume, Double secondVocabularyVolume) {
      this.recognizedByFirst = recognizedByFirst;
      this.recognizedBySecond = recognizedBySecond;
      this.recognizedByBoth = recognizedByBoth;

      this.firstVocabularyVolume = firstVocabularyVolume;
      this.secondVocabularyVolume = secondVocabularyVolume;
    }

    public Double similarity() {
      if (recognizedByFirst == 0 || recognizedBySecond == 0) {
        return null;
      }
      double union = recognizedByFirst + recognizedBySecond - recognizedByBoth;
      return recognizedByBoth / union;
    }

    public Double distance() {
      Double similarity = similarity();
      if (similarity == null) {
        return null;
      } else {
        return similarity;
      }
    }

    public Double realPvalueFirst() {
      if (firstVocabularyVolume == null) {
        return null;
      } else {
        return recognizedByFirst / firstVocabularyVolume;
      }
    }
    public Double realPvalueSecond() {
      if (secondVocabularyVolume == null) {
        return null;
      } else {
        return recognizedBySecond / secondVocabularyVolume;
      }
    }
  }

  public final CountingPWM firstPWM;
  public final CountingPWM secondPWM;
  public final Position relativePosition;

  public Double max_pair_hash_size;

  private PWMAligned cache_alignment;

  public CompareAligned(CountingPWM firstPWM, CountingPWM secondPWM, Position relativePosition) {
    this.firstPWM = firstPWM;
    this.secondPWM = secondPWM;
    this.relativePosition = relativePosition;
  }

  public SimilarityInfo jaccard(double first_threshold, double second_threshold) throws Exception {
    double f = new CountingPWM(alignment().first_pwm, firstPWM.background, null).count_by_threshold(first_threshold);
    double s = new CountingPWM(alignment().second_pwm, secondPWM.background, null).count_by_threshold(second_threshold);

    double[] intersections = counts_for_two_matrices(first_threshold, second_threshold);
    double intersect = Math.sqrt(intersections[0] * intersections[1]);

    double firstPWMVocabularyVolume = new CountingPWM(alignment().first_pwm, firstPWM.background, null).vocabularyVolume();
    double secondPWMVocabularyVolume = new CountingPWM(alignment().second_pwm, secondPWM.background, null).vocabularyVolume();

    return new SimilarityInfo(intersect, f, s,
                              firstPWMVocabularyVolume,
                              secondPWMVocabularyVolume);
  }

  public SimilarityInfo jaccard_by_pvalue(double pvalue) throws Exception {
    double threshold_first = firstPWM.threshold(pvalue).threshold;
    double threshold_second = secondPWM.threshold(pvalue).threshold;
    return jaccard(threshold_first, threshold_second);
  }

  public SimilarityInfo jaccard_by_weak_pvalue(double pvalue) throws Exception {
    double threshold_first = firstPWM.weak_threshold(pvalue).threshold;
    double threshold_second = secondPWM.weak_threshold(pvalue).threshold;
    return jaccard(threshold_first, threshold_second);
  }

  static public interface RecalculateScore {
    public double recalculateScore(double score, int letter) throws Exception;
  }

  // unoptimized version of this and related methods
  private double[] counts_for_two_matrices(double threshold_first, double threshold_second) throws Exception {
    // just not to call method each time
    final BackgroundModel first_background = firstPWM.background;
    final BackgroundModel second_background = secondPWM.background;
    if (first_background.equals(second_background)) {
      if (firstPWM.background.is_wordwise()) {
        double result = get_counts(threshold_first, threshold_second,  new RecalculateScore() {
          public double recalculateScore(double score, int letter) { return score; }
        });

        return new double[] {result, result};
      } else {
        final BackgroundModel background = first_background;
        double result = get_counts(threshold_first, threshold_second, new RecalculateScore() {
          public double recalculateScore(double score, int letter) { return background.count(letter) * score; }
        });

        return new double[] {result, result};
      }
    } else {
      double first_result = get_counts(threshold_first, threshold_second, new RecalculateScore() {
        public double recalculateScore(double score, int letter) { return first_background.count(letter) * score; }
      });

      double second_result = get_counts(threshold_first, threshold_second, new RecalculateScore() {
        public double recalculateScore(double score, int letter) { return second_background.count(letter) * score; }
      });

      return new double[] {first_result, second_result};
    }
  }


  private int summarySize(HashMap<Double, HashMap<Double,Double> > scores) {
    int sum = 0;
    for (Double key: scores.keySet()) {
      sum += scores.get(key).size();
    }
    return sum;
  }

  public PWMAligned alignment() {
    if (cache_alignment == null) {
      cache_alignment = new PWMAligned(firstPWM.pwm, secondPWM.pwm, relativePosition);
    }
    return cache_alignment;
  }

  // block has form: {|score,letter| contribution to count by `letter` with `score` }
  private double get_counts(double threshold_first, double threshold_second, RecalculateScore count_contribution_block) throws Exception {
    // scores_on_first_pwm, scores_on_second_pwm --> count
    HashMap<Double, HashMap<Double,Double> > scores = new HashMap<Double, HashMap<Double,Double> >();
    scores.put(0.0, new HashMap<Double, Double>());
    scores.get(0.0).put(0.0, 1.0);

    for (int pos = 0; pos < alignment().length(); ++pos) {
      scores = recalc_score_hash(scores,
                                 alignment().first_pwm.matrix[pos], alignment().second_pwm.matrix[pos],
                                 threshold_first - alignment().first_pwm.best_suffix(pos + 1),
                                 threshold_second - alignment().second_pwm.best_suffix(pos + 1),
                                 count_contribution_block);
      if (max_pair_hash_size != null && summarySize(scores) > max_pair_hash_size) {
        throw new Exception("Hash overflow in Macroape::AlignedPairIntersection#counts_for_two_matrices_with_different_probabilities");
      }
    }

    return combine_scores(scores);
  }

  double combine_scores(HashMap<Double, HashMap<Double,Double> > scores) {
    double sum = 0;
    for (Double score_first: scores.keySet()) {
      HashMap<Double,Double> hsh = scores.get(score_first);
      for (Double score_second: hsh.keySet()) {
        double count = hsh.get(score_second);
        sum += count;
      }
    }
    return sum;
  }

  // wouldn't work without count_contribution_block
  HashMap<Double, HashMap<Double,Double> > recalc_score_hash(HashMap<Double, HashMap<Double,Double> > scores,
                                                             double[] first_column, double[] second_column,
                                                             double least_sufficient_first, double least_sufficient_second,
                                                             RecalculateScore count_contribution_block) throws Exception {
    HashMap<Double, HashMap<Double,Double> > new_scores = new HashMap<Double, HashMap<Double,Double> >();  /*** Hash.new{|h,k| h[k] = Hash.new(0)} ***/
    for (double score_first: scores.keySet()) {
      HashMap<Double,Double> second_scores = scores.get(score_first);
      for (double score_second: second_scores.keySet()) {
        double count = second_scores.get(score_second);
        for (int letter = 0; letter < PWM.ALPHABET_SIZE; ++letter) {
          double new_score_first = score_first + first_column[letter];
          if (new_score_first >= least_sufficient_first) {
            double new_score_second = score_second + second_column[letter];
            if (new_score_second >= least_sufficient_second) {
              if (!new_scores.containsKey(new_score_first)) {
                new_scores.put(new_score_first, new HashMap<Double,Double>());
              }
              if (!new_scores.get(new_score_first).containsKey(new_score_second)) {
                new_scores.get(new_score_first).put(new_score_second, 0.0);
              }
              double val = new_scores.get(new_score_first).get(new_score_second);
              new_scores.get(new_score_first).put(new_score_second,
                                                  val + count_contribution_block.recalculateScore(count, letter));
            }
          }
        }
      }
    }
    return new_scores;
  }

}
