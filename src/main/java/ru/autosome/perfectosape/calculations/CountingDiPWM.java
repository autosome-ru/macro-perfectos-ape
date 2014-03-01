package ru.autosome.perfectosape.calculations;


import gnu.trove.iterator.TDoubleDoubleIterator;
import gnu.trove.map.TDoubleDoubleMap;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThresholdApproximation;
import ru.autosome.perfectosape.motifModels.DiPWM;

public class CountingDiPWM extends ScoringModelDistibutions {
  private Integer max_hash_size;

  private final DiPWM dipwm;
  private final DiBackgroundModel dibackground;

  public CountingDiPWM(DiPWM dipwm, DiBackgroundModel dibackground, Integer max_hash_size) {
    this.dipwm = dipwm;
    this.dibackground = dibackground;
    this.max_hash_size = max_hash_size;
  }

  @Override
  double worst_score() {
    return dipwm.worst_score();
  }
  @Override
  double best_score() {
    return dipwm.best_score();
  }


  @Override
  CanFindThresholdApproximation gaussianThresholdEstimator() {
    return new GaussianThresholdDinucleotideEstimator(dipwm, dibackground);
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
  protected TDoubleDoubleMap count_distribution_above_threshold(double threshold) {
    // scores[index_of_letter 'A'] are scores of words of specific (current) length ending with A
    TDoubleDoubleMap[] scores = initialCountDistribution();
    for (int column = 0; column < dipwm.matrix.length; ++column) {
      scores = recalc_score_hash(scores, dipwm.matrix[column], threshold - dipwm.best_suffix(column + 1));
      if (max_hash_size != null && scores[0].size() + scores[1].size() + scores[2].size() + scores[3].size() > max_hash_size) {
        throw new IllegalArgumentException("Hash overflow in DiPWM::ThresholdByPvalue#count_distribution_above_threshold");
      }
    }

    return combine_scores(scores);
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

  private TDoubleDoubleMap[] recalc_score_hash(TDoubleDoubleMap[] scores, double[] column, double least_sufficient) {
    TDoubleDoubleMap[] new_scores = new TDoubleDoubleMap[4];
    for(int i = 0; i < 4; ++i) {
      new_scores[i] = new TDoubleDoubleHashMap();

      TDoubleDoubleIterator iterator = scores[i].iterator();
      while(iterator.hasNext()) {
        iterator.advance();
        double score = iterator.key();
        double count = iterator.value();

        for (int letter = 0; letter < 4; ++letter) {
          double new_score = score + column[i*4 + letter];
          if (new_score >= least_sufficient) {
            double add = count * dibackground.count(letter);
            new_scores[letter].adjustOrPutValue(new_score, add, add);
          }
        }
      }
    }
    return new_scores;
  }

  @Override
  public double vocabularyVolume() {
    return Math.pow(dibackground.volume(), dipwm.length());
  }

}
