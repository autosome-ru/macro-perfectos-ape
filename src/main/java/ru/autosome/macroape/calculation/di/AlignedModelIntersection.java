package ru.autosome.macroape.calculation.di;

import gnu.trove.iterator.TDoubleDoubleIterator;
import gnu.trove.iterator.TDoubleObjectIterator;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import gnu.trove.map.hash.TDoubleObjectHashMap;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.motifModel.di.DiPWM;
import ru.autosome.macroape.model.PairAligned;

import java.util.ArrayList;
import java.util.List;

public class AlignedModelIntersection extends ru.autosome.macroape.calculation.generalized.AlignedModelIntersection<DiPWM, DiBackgroundModel> {

  public AlignedModelIntersection(PairAligned<DiPWM> alignment, DiBackgroundModel firstBackground, DiBackgroundModel secondBackground) {
    super(alignment, firstBackground, secondBackground);
  }

  public AlignedModelIntersection(DiPWM firstPWM, DiPWM secondPWM, DiBackgroundModel firstBackground, DiBackgroundModel secondBackground, Position relativePosition) {
    super(firstPWM, secondPWM, firstBackground, secondBackground,relativePosition);
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

  @Override
  protected double get_counts(double threshold_first, double threshold_second, DiBackgroundModel background) {
    // last letter, scores_on_first_pwm, scores_on_second_pwm --> count
    List<TDoubleObjectHashMap<TDoubleDoubleHashMap>> scores = initialScoreHash(background);

    for (int pos = 0; pos < alignment.length() - 1; ++pos) {
      double[] firstColumn = alignment.firstModelAligned.getMatrix()[pos];
      double[] secondColumn = alignment.secondModelAligned.getMatrix()[pos];

      double[] leastSufficientScoresFirst = leastSufficientScoresFirst(threshold_first, pos);
      double[] leastSufficientScoresSecond = leastSufficientScoresSecond(threshold_second, pos);

      scores = recalc_score_hash(scores,
                                 firstColumn, secondColumn,
                                 leastSufficientScoresFirst, leastSufficientScoresSecond,
                                 background);
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
    return new_scores;
  }
}
