package ru.autosome.perfectosape.calculations;


import gnu.trove.iterator.TDoubleDoubleIterator;
import gnu.trove.map.TDoubleDoubleMap;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import ru.autosome.perfectosape.ScoreDistributionTop;
import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThresholdApproximation;
import ru.autosome.perfectosape.motifModels.DiPWM;

public class CountingDiPWM extends ScoringModelDistibutions {
  private Integer maxHashSize;

  private final DiPWM dipwm;
  private final DiBackgroundModel dibackground;

  public CountingDiPWM(DiPWM dipwm, DiBackgroundModel dibackground, Integer maxHashSize) {
    this.dipwm = dipwm;
    this.dibackground = dibackground;
    this.maxHashSize = maxHashSize;
  }

  @Override
  CanFindThresholdApproximation gaussianThresholdEstimator() {
    return new GaussianThresholdEstimator(dipwm, dibackground);
  }

  protected TDoubleDoubleMap[] initialCountDistribution() {
    TDoubleDoubleMap[] scores = new TDoubleDoubleMap[4];
    for(int i = 0; i < 4; ++i) {
      scores[i] = new TDoubleDoubleHashMap();
      scores[i].put(0.0, 1.0);
    }
    return scores;
  }

  @Override
  protected ScoreDistributionTop score_distribution_above_threshold(double threshold) throws HashOverflowException {
    // scores[index_of_letter 'A'] are scores of words of specific (current) length ending with A
    TDoubleDoubleMap[] scores = initialCountDistribution();
    for (int column = 0; column < dipwm.matrix.length; ++column) {
      double[] least_sufficient = new double[4];
      for (int letter = 0; letter < 4; ++letter) {
        least_sufficient[letter] = threshold - dipwm.best_suffix(column + 1, letter);
      }
      scores = recalc_score_hash(scores, dipwm.matrix[column], least_sufficient);
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
    for(int letter = 0; letter < 4; ++letter) {
      TDoubleDoubleIterator iterator = scores[letter].iterator();
      while(iterator.hasNext()) {
        iterator.advance();
        double score = iterator.key();
        double count = iterator.value();

        for (int next_letter = 0; next_letter < 4; ++next_letter) {
          double new_score = score + column[letter*4 + next_letter];
          if (new_score >= least_sufficient[next_letter]) {
            double add = count * dibackground.count(next_letter);
            new_scores[next_letter].adjustOrPutValue(new_score, add, add);
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
