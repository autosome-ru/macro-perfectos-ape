package ru.autosome.ape.calculation.ScoringModelDistributions;

import gnu.trove.iterator.TDoubleDoubleIterator;
import gnu.trove.map.TDoubleDoubleMap;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import ru.autosome.ape.calculation.findThreshold.GaussianThresholdEstimator;
import ru.autosome.ape.model.ScoreDistributionTop;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.commons.scoringModel.PWMSequenceScoring;

public class PWMScoresGenerator extends ScoringDistributionGenerator {

  final PWM pwm;
  final BackgroundModel background;

  public PWMScoresGenerator(PWM pwm, BackgroundModel background) {
    this.pwm = pwm;
    this.background = background;
  }

  @Override
  GaussianThresholdEstimator<PWMSequenceScoring> gaussianThresholdEstimator() {
    return new GaussianThresholdEstimator<>(pwm.onBackground(background));
  }

  protected TDoubleDoubleMap initialCountDistribution() {
    TDoubleDoubleMap scores = new TDoubleDoubleHashMap();
    scores.put(0.0, 1.0);
    return scores;
  }

  @Override
  protected ScoreDistributionTop score_distribution_above_threshold(double threshold) {
    TDoubleDoubleMap scores = initialCountDistribution();
    for (int pos = 0; pos < pwm.length(); ++pos) {
      scores = recalc_score_hash(scores, pwm.getMatrix()[pos], threshold - pwm.best_suffix(pos + 1));
    }
    ScoreDistributionTop result = new ScoreDistributionTop(scores, vocabularyVolume(), threshold);
    result.setWorstScore(pwm.worst_score());
    result.setBestScore(pwm.best_score());
    return result;
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

  private double vocabularyVolume() {
    return Math.pow(background.volume(), pwm.length());
  }
}
