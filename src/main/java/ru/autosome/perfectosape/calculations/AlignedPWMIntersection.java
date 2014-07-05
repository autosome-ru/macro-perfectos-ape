package ru.autosome.perfectosape.calculations;

import gnu.trove.iterator.TDoubleDoubleIterator;
import gnu.trove.iterator.TDoubleObjectIterator;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import gnu.trove.map.hash.TDoubleObjectHashMap;
import ru.autosome.perfectosape.PWMAligned;
import ru.autosome.perfectosape.Position;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.formatters.ResultInfo;
import ru.autosome.perfectosape.motifModels.PWM;

public class AlignedPWMIntersection {
  public final BackgroundModel firstBackground;
  public final BackgroundModel secondBackground;
  public final PWMAligned<PWM> alignment;
  public Double maxPairHashSize;

  public AlignedPWMIntersection(PWMAligned alignment, BackgroundModel firstBackground, BackgroundModel secondBackground) {
    this.firstBackground = firstBackground;
    this.secondBackground = secondBackground;
    this.alignment = alignment;
  }

  public AlignedPWMIntersection(PWM firstPWM, PWM secondPWM, BackgroundModel firstBackground, BackgroundModel secondBackground, Position relativePosition) {
    this.firstBackground = firstBackground;
    this.secondBackground = secondBackground;
    this.alignment = new PWMAligned(firstPWM, secondPWM, relativePosition);
  }

  public double count_in_intersection(double first_threshold, double second_threshold) throws HashOverflowException {
    double[] intersections = counts_for_two_matrices(first_threshold, second_threshold);

    return combine_intersection_values(intersections[0], intersections[1]);
  }

  public double combine_intersection_values(double intersection_count_1, double intersection_count_2) {
    return Math.sqrt(intersection_count_1 * intersection_count_2);
  }

  private double[] counts_for_two_matrices(double threshold_first, double threshold_second) throws HashOverflowException {
    if (firstBackground.equals(secondBackground)) {
      final BackgroundModel background = firstBackground;
      double result = get_counts(threshold_first, threshold_second, background);

      return new double[] {result, result};
    } else {
      // unoptimized code (two-pass instead of one) but it's rare case
      double first_result = get_counts(threshold_first, threshold_second, firstBackground);
      double second_result = get_counts(threshold_first, threshold_second, secondBackground);

      return new double[] {first_result, second_result};
    }
  }


  private int summarySize(TDoubleObjectHashMap<TDoubleDoubleHashMap> scores) {
    int sum = 0;
    TDoubleObjectIterator<TDoubleDoubleHashMap> iterator = scores.iterator();
    while (iterator.hasNext()) {
      iterator.advance();
      sum += iterator.value().size();
    }
    return sum;
  }

  // 2d-score hash before first step
  private TDoubleObjectHashMap<TDoubleDoubleHashMap> initialScoreHash() {
    TDoubleObjectHashMap<TDoubleDoubleHashMap> scores = new TDoubleObjectHashMap<TDoubleDoubleHashMap>();
    scores.put(0.0, new TDoubleDoubleHashMap(new double[] {0},
                                             new double[] {1}) );
    return scores;
  }

  private double get_counts(double threshold_first, double threshold_second, BackgroundModel background) throws HashOverflowException {
    // scores_on_first_pwm, scores_on_second_pwm --> count
    TDoubleObjectHashMap<TDoubleDoubleHashMap> scores = initialScoreHash();

    for (int pos = 0; pos < alignment.length(); ++pos) {
      double[] firstColumn = alignment.firstModelAligned.matrix[pos];
      double[] secondColumn = alignment.secondModelAligned.matrix[pos];
      double leastSufficientScoreFirst = threshold_first - alignment.firstModelAligned.best_suffix(pos + 1);
      double leastSufficientScoreSecond = threshold_second - alignment.secondModelAligned.best_suffix(pos + 1);

      if (background.is_wordwise()) {
      scores = recalc_score_hash_wordwise(scores,
                                          firstColumn, secondColumn,
                                          leastSufficientScoreFirst, leastSufficientScoreSecond);
      } else {
        scores = recalc_score_hash(scores,
                                   firstColumn, secondColumn,
                                   leastSufficientScoreFirst, leastSufficientScoreSecond,
                                   background);
      }

      if (maxPairHashSize != null && summarySize(scores) > maxPairHashSize) {
        throw new HashOverflowException("Hash overflow in AlignedPWMIntersection#get_counts");
      }
    }

    return combine_scores(scores);
  }

  double combine_scores(TDoubleObjectHashMap<TDoubleDoubleHashMap> scores) {
    double sum = 0;
    TDoubleObjectIterator<TDoubleDoubleHashMap> iterator = scores.iterator();
    while (iterator.hasNext()){
      iterator.advance();
      TDoubleDoubleIterator second_iterator = iterator.value().iterator();
      while (second_iterator.hasNext()) {
        second_iterator.advance();
        sum += second_iterator.value();
      }
    }

    return sum;
  }

  // 2d-hash which has first level keys initialized, but second level is empty
  TDoubleObjectHashMap<TDoubleDoubleHashMap> feedHashToRecalc(TDoubleObjectHashMap<TDoubleDoubleHashMap> scores,
                                                              double[] firstColumn, double leastSufficientScoreFirst) {
    TDoubleObjectHashMap<TDoubleDoubleHashMap> result = new TDoubleObjectHashMap<TDoubleDoubleHashMap>();

    TDoubleObjectIterator<TDoubleDoubleHashMap> iterator = scores.iterator();
    while (iterator.hasNext()) {
      iterator.advance();
      double score_first = iterator.key();
      for (int letter = 0; letter < PWM.ALPHABET_SIZE; ++letter) {
        double new_score_first = score_first + firstColumn[letter];
        if (new_score_first >= leastSufficientScoreFirst) {
          result.put(new_score_first, new TDoubleDoubleHashMap());
        }
      }
    }
    return result;
  }

  // Step of dynamic programming algorithm which recalculates score distribution `scores`
  // for matrices of length augmented with one column (different for each matrix).
  // leastSufficientScore makes it possible to reject lots of prefixes which start words that can't overcome thresholds
  TDoubleObjectHashMap<TDoubleDoubleHashMap> recalc_score_hash(TDoubleObjectHashMap<TDoubleDoubleHashMap> scores,
                                                               double[] firstColumn, double[] secondColumn,
                                                               double leastSufficientScoreFirst, double leastSufficientScoreSecond,
                                                               BackgroundModel background) {
    TDoubleObjectHashMap<TDoubleDoubleHashMap> new_scores = feedHashToRecalc(scores, firstColumn, leastSufficientScoreFirst);

    TDoubleObjectIterator<TDoubleDoubleHashMap> iterator = scores.iterator();
    while (iterator.hasNext()) {
      iterator.advance();
      double score_first = iterator.key();

      TDoubleDoubleHashMap second_scores = iterator.value();

      TDoubleDoubleIterator second_iterator = second_scores.iterator();
      while (second_iterator.hasNext()) {
        second_iterator.advance();
        double score_second = second_iterator.key();
        double count = second_iterator.value();

        for (int letter = 0; letter < PWM.ALPHABET_SIZE; ++letter) {
          double new_score_first = score_first + firstColumn[letter];

          if (new_score_first >= leastSufficientScoreFirst) {
            double new_score_second = score_second + secondColumn[letter];

            if (new_score_second >= leastSufficientScoreSecond) {
              double add = background.count(letter) * count;
              new_scores.get(new_score_first).adjustOrPutValue(new_score_second, add, add);
            }
          }
        }

      }
    }
    return new_scores;
  }

  // optimized version for a case of wordwise background
  TDoubleObjectHashMap<TDoubleDoubleHashMap> recalc_score_hash_wordwise(TDoubleObjectHashMap<TDoubleDoubleHashMap> scores,
                                                                        double[] firstColumn, double[] secondColumn,
                                                                        double leastSufficientScoreFirst, double leastSufficientScoreSecond) {
    TDoubleObjectHashMap<TDoubleDoubleHashMap> new_scores = feedHashToRecalc(scores, firstColumn, leastSufficientScoreFirst);

    TDoubleObjectIterator<TDoubleDoubleHashMap> iterator = scores.iterator();
    while (iterator.hasNext()) {
      iterator.advance();
      double score_first = iterator.key();

      TDoubleDoubleHashMap second_scores = iterator.value();

      TDoubleDoubleIterator second_iterator = second_scores.iterator();
      while (second_iterator.hasNext()) {
        second_iterator.advance();
        double score_second = second_iterator.key();
        double count = second_iterator.value();

        for (int letter = 0; letter < PWM.ALPHABET_SIZE; ++letter) {
          double new_score_first = score_first + firstColumn[letter];

          if (new_score_first >= leastSufficientScoreFirst) {
            double new_score_second = score_second + secondColumn[letter];

            if (new_score_second >= leastSufficientScoreSecond) {
              new_scores.get(new_score_first).adjustOrPutValue(new_score_second, count, count);
            }
          }
        }

      }
    }
    return new_scores;
  }

  public static class SimilarityInfo extends ResultInfo {
    public final double recognizedByBoth;
    public final double recognizedByFirst;
    public final double recognizedBySecond;

    public SimilarityInfo(double recognizedByBoth, double recognizedByFirst, double recognizedBySecond) {
      this.recognizedByFirst = recognizedByFirst;
      this.recognizedBySecond = recognizedBySecond;
      this.recognizedByBoth = recognizedByBoth;
    }

    public static Double jaccardByCounts(double recognizedByFirst, double recognizedBySecond, double recognizedByBoth) {
      if (recognizedByFirst == 0 || recognizedBySecond == 0) {
        return null;
      }
      double union = recognizedByFirst + recognizedBySecond - recognizedByBoth;
      return recognizedByBoth / union;
    }

    public Double similarity() {
      return jaccardByCounts(recognizedByFirst, recognizedBySecond, recognizedByBoth);
    }

    public Double distance() {
      Double similarity = similarity();
      if (similarity == null) {
        return null;
      } else {
        return 1.0 - similarity;
      }
    }

    public Double realPvalueFirst(BackgroundModel background, int alignmentLength) {
      double vocabularyVolume = Math.pow(background.volume(), alignmentLength);
      return recognizedByFirst / vocabularyVolume;
    }
    public Double realPvalueSecond(BackgroundModel background, int alignmentLength) {
      double vocabularyVolume = Math.pow(background.volume(), alignmentLength);
      return recognizedBySecond / vocabularyVolume;
    }
  }
}
