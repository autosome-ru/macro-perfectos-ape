package ru.autosome.perfectosape.calculations;

import gnu.trove.iterator.TDoubleDoubleIterator;
import gnu.trove.map.TDoubleDoubleMap;
import gnu.trove.map.TDoubleObjectMap;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import gnu.trove.map.hash.TDoubleObjectHashMap;
import ru.autosome.perfectosape.*;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThresholdApproximation;
import ru.autosome.perfectosape.motifModels.PWM;

import java.util.Arrays;
import java.util.List;

public class CountingPWM extends ScoringModelDistibutions {

  private Integer maxHashSize;

  final PWM pwm;
  final BackgroundModel background;

  public CountingPWM(PWM pwm, BackgroundModel background, Integer maxHashSize) {
    this.pwm = pwm;
    this.background = background;
    this.maxHashSize = maxHashSize;
  }

  @Override
  CanFindThresholdApproximation gaussianThresholdEstimator() {
    return new GaussianThresholdEstimator(pwm, background);
  }

  @Override
  double worst_score() {
    return pwm.worst_score();
  }
  @Override
  double best_score() {
    return pwm.best_score();
  }


  @Override
  protected TDoubleDoubleMap count_distribution_above_threshold(double threshold) throws HashOverflowException {
    TDoubleDoubleMap scores = new TDoubleDoubleHashMap();
    scores.put(0.0, 1.0);
    for (int pos = 0; pos < pwm.length(); ++pos) {
      scores = recalc_score_hash(scores, pwm.matrix[pos], threshold - pwm.best_suffix(pos + 1));
      if (maxHashSize != null && scores.size() > maxHashSize) {
        throw new HashOverflowException("Hash overflow in PWM::ThresholdByPvalue#count_distribution_above_threshold");
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

  @Override
  public double vocabularyVolume() {
    return Math.pow(background.volume(), pwm.length());
  }
}
