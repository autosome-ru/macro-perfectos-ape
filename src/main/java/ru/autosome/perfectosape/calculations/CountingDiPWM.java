package ru.autosome.perfectosape.calculations;


import ru.autosome.perfectosape.backgroundModels.DiBackgroundModel;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThresholdApproximation;
import ru.autosome.perfectosape.motifModels.DiPWM;

import java.util.HashMap;
import java.util.Map;

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

  private HashMap<Double, Double> count_distribution_above_threshold(double threshold) {
    // scores[index_of_letter 'A'] are scores of words of specific (current) length ending with A
    HashMap<Double, Double>[] scores = new HashMap[4];
    for(int i = 0; i < 4; ++i) {
      scores[i] = new HashMap<Double, Double>();
      scores[i].put(0.0, 1.0);
    }

    for (int column = 0; column < dipwm.matrix.length; ++column) {
      scores = recalc_score_hash(scores, dipwm.matrix[column], threshold - dipwm.best_suffix(column + 1));
      if (max_hash_size != null && scores[0].size() + scores[1].size() + scores[2].size() + scores[3].size() > max_hash_size) {
        throw new IllegalArgumentException("Hash overflow in DiPWM::ThresholdByPvalue#count_distribution_above_threshold");
      }
    }

    return combine_scores(scores);
  }

  HashMap<Double,Double> combine_scores(HashMap<Double,Double>[] scores) {
    HashMap<Double,Double> combined_scores = new HashMap<Double, Double>();
    for (int i = 0; i < 4; ++i) {
      for (Double score : scores[i].keySet()) {
        if (!combined_scores.containsKey(score)) {
          combined_scores.put(score, 0.0);
        }
        combined_scores.put(score, combined_scores.get(score) + scores[i].get(score));
      }
    }
    return combined_scores;
  }

  private HashMap<Double, Double>[] recalc_score_hash(HashMap<Double, Double>[] scores, double[] column, double least_sufficient) {
    HashMap<Double, Double>[] new_scores = new HashMap[4];
    for(int i = 0; i < 4; ++i) {
      new_scores[i] = new HashMap<Double, Double>();
      for (Map.Entry<Double, Double> entry : scores[i].entrySet()) {
        double score = entry.getKey();
        double count = entry.getValue();
        for (int letter = 0; letter < 4; ++letter) {
          double new_score = score + column[i*4 + letter];
          if (new_score >= least_sufficient) {
            if (!new_scores[letter].containsKey(new_score)) {
              new_scores[letter].put(new_score, 0.0);
            }
            new_scores[letter].put(new_score, new_scores[letter].get(new_score) + count * dibackground.count(letter));
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
