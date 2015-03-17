package ru.autosome.perfectosape.calculation.ScoringModelDistributions;


import gnu.trove.iterator.TDoubleDoubleIterator;
import gnu.trove.map.TDoubleDoubleMap;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import ru.autosome.ape.calculation.findThreshold.CanFindThresholdApproximation;
import ru.autosome.ape.calculation.findThreshold.GaussianThresholdEstimator;
import ru.autosome.ape.model.ScoreDistributionTop;
import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.commons.backgroundModel.di.DiBackgroundModel;
import ru.autosome.commons.motifModel.di.DiPWM;

public class CountingDiPWM extends ScoringModelDistributions {
  private final Integer maxHashSize;

  private final DiPWM dipwm;
  private final DiBackgroundModel dibackground;

  public CountingDiPWM(DiPWM dipwm, DiBackgroundModel dibackground, Integer maxHashSize) {
    this.dipwm = dipwm;
    this.dibackground = dibackground;
    this.maxHashSize = maxHashSize;
  }

  @Override
  CanFindThresholdApproximation gaussianThresholdEstimator() {
    return new GaussianThresholdEstimator<>(dipwm.onBackground(dibackground));
  }

  protected TDoubleDoubleMap[] initialCountDistribution() {
    TDoubleDoubleMap[] scores = new TDoubleDoubleMap[4];
    for(int i = 0; i < 4; ++i) {
      scores[i] = new TDoubleDoubleHashMap();
      scores[i].put(0.0, dibackground.countAnyFirstLetter(i));
    }
    return scores;
  }

  @Override
  protected ScoreDistributionTop score_distribution_above_threshold(double threshold) throws HashOverflowException {
    // scores[index_of_letter 'A'] are scores of words of specific (current) length ending with A
    TDoubleDoubleMap[] scores = initialCountDistribution();
    for (int column = 0; column < dipwm.getMatrix().length; ++column) {
      double[] least_sufficient = new double[4];
      for (int letter = 0; letter < 4; ++letter) {
        least_sufficient[letter] = threshold - dipwm.best_suffix(column + 1, letter);
      }
      scores = recalc_score_hash(scores, dipwm.getMatrix()[column], least_sufficient);
      if (exceedHashSizeLimit(scores)) {
        throw new HashOverflowException("Hash overflow in DiPWM::ThresholdByPvalue#score_distribution_above_threshold");
      }
    }

    TDoubleDoubleMap score_count_hash = combine_scores(scores);
    ScoreDistributionTop result = new ScoreDistributionTop(score_count_hash, vocabularyVolume(), threshold);
    result.setWorstScore(dipwm.worst_score());
    result.setBestScore(dipwm.best_score());
    return result;
  }

  private TDoubleDoubleMap[] recalc_score_hash(TDoubleDoubleMap[] scores, double[] column, double[] least_sufficient) {
    TDoubleDoubleMap[] new_scores = new TDoubleDoubleMap[4];
    for(int i = 0; i < 4; ++i) {
      new_scores[i] = new TDoubleDoubleHashMap();
    }
    for(int previousLetter = 0; previousLetter < 4; ++previousLetter) {
      TDoubleDoubleIterator iterator = scores[previousLetter].iterator();
      while(iterator.hasNext()) {
        iterator.advance();
        double score = iterator.key();
        double count = iterator.value();

        for (int letter = 0; letter < 4; ++letter) {
          double new_score = score + column[previousLetter*4 + letter];
          if (new_score >= least_sufficient[letter]) {
            double add = count * dibackground.conditionalCount(previousLetter, letter);
            new_scores[letter].adjustOrPutValue(new_score, add, add);
          }
        }
      }
    }
    return new_scores;
  }

  TDoubleDoubleMap combine_scores(TDoubleDoubleMap[] scores) {
    TDoubleDoubleMap combined_scores = new TDoubleDoubleHashMap();
    for (int i = 0; i < 4; ++i) {
      TDoubleDoubleIterator iterator = scores[i].iterator();
      while(iterator.hasNext()) {
        iterator.advance();
        double score = iterator.key();
        double count = iterator.value();
        combined_scores.adjustOrPutValue(score, count, count);
      }
    }
    return combined_scores;
  }

  public double vocabularyVolume() {
    return Math.pow(dibackground.volume(), dipwm.length());
  }

  protected boolean exceedHashSizeLimit(TDoubleDoubleMap[] scores) {
    return maxHashSize != null && (scores[0].size() + scores[1].size() + scores[2].size() + scores[3].size()) > maxHashSize;
  }
}
