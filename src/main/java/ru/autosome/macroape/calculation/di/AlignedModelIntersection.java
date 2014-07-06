package ru.autosome.macroape.calculation.di;

import gnu.trove.iterator.TDoubleDoubleIterator;
import gnu.trove.iterator.TDoubleObjectIterator;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import gnu.trove.map.hash.TDoubleObjectHashMap;
import ru.autosome.macroape.model.PairAligned;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.backgroundModel.GeneralizedBackgroundModel;
import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.commons.cli.ResultInfo;
import ru.autosome.commons.motifModel.di.DiPWM;

import java.util.ArrayList;
import java.util.List;

public class AlignedModelIntersection {
  public final DiBackgroundModel firstBackground;
  public final DiBackgroundModel secondBackground;
  public final PairAligned<DiPWM> alignment;
  public Double maxPairHashSize;

  public AlignedModelIntersection(PairAligned<DiPWM> alignment, DiBackgroundModel firstBackground, DiBackgroundModel secondBackground) {
    this.firstBackground = firstBackground;
    this.secondBackground = secondBackground;
    this.alignment = alignment;
  }

  public AlignedModelIntersection(DiPWM firstPWM, DiPWM secondPWM, DiBackgroundModel firstBackground, DiBackgroundModel secondBackground, Position relativePosition) {
    this.firstBackground = firstBackground;
    this.secondBackground = secondBackground;
    this.alignment = new PairAligned<DiPWM>(firstPWM, secondPWM, relativePosition);
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
      final DiBackgroundModel background = firstBackground;
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

  private int summarySize(List<TDoubleObjectHashMap<TDoubleDoubleHashMap>> scores) {
    int sum = 0;
    for (TDoubleObjectHashMap<TDoubleDoubleHashMap> score_part: scores) {
      sum += summarySize(score_part);
    }
    return sum;
  }

  // 2d-score hash before first step
  private List<TDoubleObjectHashMap<TDoubleDoubleHashMap>> initialScoreHash(DiBackgroundModel dibackground) {
    List<TDoubleObjectHashMap<TDoubleDoubleHashMap>> scores;
    scores = new ArrayList<TDoubleObjectHashMap<TDoubleDoubleHashMap>>(4);
    for (int last_letter_index = 0; last_letter_index < 4; ++last_letter_index) {
      TDoubleObjectHashMap<TDoubleDoubleHashMap> scores_by_letter;
      scores_by_letter = new TDoubleObjectHashMap<TDoubleDoubleHashMap>();
      scores_by_letter.put(0.0, new TDoubleDoubleHashMap(new double[] {0.0},
                                                         new double[] {dibackground.countAnyFirstLetter(last_letter_index)}) );
      scores.add(scores_by_letter);
    }
    return scores;
  }

  double[] leastSufficientScoresFirst(double threshold, int last_prefix_position) {
    double[] result = new double[4];
    for (int first_suffix_letter = 0; first_suffix_letter < 4; ++first_suffix_letter) {
      result[first_suffix_letter] = threshold - alignment.firstModelAligned.best_suffix(last_prefix_position + 1, first_suffix_letter);
    }
    return result;
  }

  double[] leastSufficientScoresSecond(double threshold, int last_prefix_position) {
    double[] result = new double[4];
    for (int first_suffix_letter = 0; first_suffix_letter < 4; ++first_suffix_letter) {
      result[first_suffix_letter] = threshold - alignment.secondModelAligned.best_suffix(last_prefix_position + 1, first_suffix_letter);
    }
    return result;
  }

  private double get_counts(double threshold_first, double threshold_second, DiBackgroundModel background) throws HashOverflowException {
    // last letter, scores_on_first_pwm, scores_on_second_pwm --> count
    List<TDoubleObjectHashMap<TDoubleDoubleHashMap>> scores = initialScoreHash(background);

    for (int pos = 0; pos < alignment.length() - 1; ++pos) {
      double[] firstColumn = alignment.firstModelAligned.matrix[pos];
      double[] secondColumn = alignment.secondModelAligned.matrix[pos];
//      double leastSufficientScoreFirst = threshold_first - alignment.firstModelAligned.best_suffix(pos + 1);
//      double leastSufficientScoreSecond = threshold_second - alignment.secondModelAligned.best_suffix(pos + 1);

      double[] leastSufficientScoresFirst = leastSufficientScoresFirst(threshold_first, pos);
      double[] leastSufficientScoresSecond = leastSufficientScoresSecond(threshold_second, pos);


//      if (background.is_wordwise()) {
//        scores = recalc_score_hash_wordwise(scores,
//                                            firstColumn, secondColumn,
//                                            leastSufficientScoreFirst, leastSufficientScoreSecond);
//      } else {
        scores = recalc_score_hash(scores,
                                   firstColumn, secondColumn,
                                   leastSufficientScoresFirst, leastSufficientScoresSecond,
                                   background);
//      }

      if (maxPairHashSize != null && summarySize(scores) > maxPairHashSize) {
        throw new HashOverflowException("Hash overflow in AlignedModelIntersection#get_counts");
      }
    }

    return combine_scores(scores);
  }

  double combine_scores(List<TDoubleObjectHashMap<TDoubleDoubleHashMap>> scores) {
    double sum = 0;
    for (TDoubleObjectHashMap<TDoubleDoubleHashMap> scores_part: scores) {
      sum += combine_scores(scores_part);
    }
    return sum;
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

  // list of 2d-hashes which has first level keys initialized, but second level is empty
  List<TDoubleObjectHashMap<TDoubleDoubleHashMap>> seedHashToRecalc(List<TDoubleObjectHashMap<TDoubleDoubleHashMap>> scores,
                                                                    double[] firstColumn, double[] leastSufficientScoresFirst) {
    List<TDoubleObjectHashMap<TDoubleDoubleHashMap>> result = new ArrayList<TDoubleObjectHashMap<TDoubleDoubleHashMap>>(4);

    for (int last_letter = 0; last_letter < 4; ++last_letter) {
      TDoubleObjectHashMap<TDoubleDoubleHashMap> partiallyRecalculatedSeed = new TDoubleObjectHashMap<TDoubleDoubleHashMap>();
      for (int first_letter = 0; first_letter < 4; ++first_letter) {
        TDoubleObjectIterator<TDoubleDoubleHashMap> iterator = scores.get(first_letter).iterator();

        while (iterator.hasNext()) {
          iterator.advance();
          double score_first = iterator.key();
          double new_score_first = score_first + firstColumn[4*first_letter + last_letter];
          if (new_score_first >= leastSufficientScoresFirst[last_letter]) {
            partiallyRecalculatedSeed.put(new_score_first, new TDoubleDoubleHashMap());
          }
        }
      }
      result.add(partiallyRecalculatedSeed);
    }
    return result;
  }

  // Step of dynamic programming algorithm which recalculates score distribution `scores`
  // for matrices of length augmented with one column (different for each matrix).
  // leastSufficientScore makes it possible to reject lots of prefixes which start words that can't overcome thresholds
  List<TDoubleObjectHashMap<TDoubleDoubleHashMap>> recalc_score_hash(List<TDoubleObjectHashMap<TDoubleDoubleHashMap>> scores,
                                                               double[] firstColumn, double[] secondColumn,
                                                               double[] leastSufficientScoresFirst, double[] leastSufficientScoresSecond,
                                                               DiBackgroundModel background) {
    List<TDoubleObjectHashMap<TDoubleDoubleHashMap>> new_scores = seedHashToRecalc(scores, firstColumn, leastSufficientScoresFirst);


    for (int first_letter = 0; first_letter < 4; ++first_letter) {
      TDoubleObjectIterator<TDoubleDoubleHashMap> iterator = scores.get(first_letter).iterator();
      while (iterator.hasNext()) {
        iterator.advance();
        double score_first = iterator.key();

        TDoubleDoubleHashMap second_scores = iterator.value();

        TDoubleDoubleIterator second_iterator = second_scores.iterator();
        while (second_iterator.hasNext()) {
          second_iterator.advance();
          double score_second = second_iterator.key();
          double count = second_iterator.value();

          for (int last_letter = 0; last_letter < 4; ++last_letter) {
            double new_score_first = score_first + firstColumn[4 * first_letter + last_letter];

            if (new_score_first >= leastSufficientScoresFirst[last_letter]) {
              double new_score_second = score_second + secondColumn[4 * first_letter + last_letter];

              if (new_score_second >= leastSufficientScoresSecond[last_letter]) {
                double add = background.conditionalCount(first_letter, last_letter) * count;
                new_scores.get(last_letter).get(new_score_first).adjustOrPutValue(new_score_second, add, add);
              }
            }
          }

        }
      }
    }
//    TDoubleObjectIterator<TDoubleDoubleHashMap> iterator = scores.iterator();
//    while (iterator.hasNext()) {
//      iterator.advance();
//      double score_first = iterator.key();
//
//      TDoubleDoubleHashMap second_scores = iterator.value();
//
//      TDoubleDoubleIterator second_iterator = second_scores.iterator();
//      while (second_iterator.hasNext()) {
//        second_iterator.advance();
//        double score_second = second_iterator.key();
//        double count = second_iterator.value();
//
//        for (int letter = 0; letter < 4; ++letter) {
//          double new_score_first = score_first + firstColumn[letter];
//
//          if (new_score_first >= leastSufficientScoreFirst) {
//            double new_score_second = score_second + secondColumn[letter];
//
//            if (new_score_second >= leastSufficientScoreSecond) {
//              double add = background.count(letter) * count;
//              new_scores.get(new_score_first).adjustOrPutValue(new_score_second, add, add);
//            }
//          }
//        }
//
//      }
//    }
    return new_scores;
  }

//  // optimized version for a case of wordwise background
//  TDoubleObjectHashMap<TDoubleDoubleHashMap> recalc_score_hash_wordwise(TDoubleObjectHashMap<TDoubleDoubleHashMap> scores,
//                                                                        double[] firstColumn, double[] secondColumn,
//                                                                        double leastSufficientScoreFirst, double leastSufficientScoreSecond) {
//    TDoubleObjectHashMap<TDoubleDoubleHashMap> new_scores = seedHashToRecalc(scores, firstColumn, leastSufficientScoreFirst);
//
//    TDoubleObjectIterator<TDoubleDoubleHashMap> iterator = scores.iterator();
//    while (iterator.hasNext()) {
//      iterator.advance();
//      double score_first = iterator.key();
//
//      TDoubleDoubleHashMap second_scores = iterator.value();
//
//      TDoubleDoubleIterator second_iterator = second_scores.iterator();
//      while (second_iterator.hasNext()) {
//        second_iterator.advance();
//        double score_second = second_iterator.key();
//        double count = second_iterator.value();
//
//        for (int letter = 0; letter < 4; ++letter) {
//          double new_score_first = score_first + firstColumn[letter];
//
//          if (new_score_first >= leastSufficientScoreFirst) {
//            double new_score_second = score_second + secondColumn[letter];
//
//            if (new_score_second >= leastSufficientScoreSecond) {
//              new_scores.get(new_score_first).adjustOrPutValue(new_score_second, count, count);
//            }
//          }
//        }
//
//      }
//    }
//    return new_scores;
//  }

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

    public Double realPvalueFirst(GeneralizedBackgroundModel background, int alignmentLength) {
      double vocabularyVolume = Math.pow(background.volume(), alignmentLength);
      return recognizedByFirst / vocabularyVolume;
    }
    public Double realPvalueSecond(GeneralizedBackgroundModel background, int alignmentLength) {
      double vocabularyVolume = Math.pow(background.volume(), alignmentLength);
      return recognizedBySecond / vocabularyVolume;
    }
  }
}
