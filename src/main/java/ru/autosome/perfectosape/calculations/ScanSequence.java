package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.motifModels.PWM;
import ru.autosome.perfectosape.Position;
import ru.autosome.perfectosape.Sequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ScanSequence {
  private final Sequence sequence;
  private final PWM pwm;
  ArrayList<Position> positions_to_check;
  private Map<Position, Double> cache_score_by_position;

  public ScanSequence(Sequence sequence, PWM pwm, ArrayList<Position> positions_to_check) {
    this.sequence = sequence;
    this.pwm = pwm;
    this.positions_to_check = positions_to_check;
  }

  public ScanSequence(Sequence sequence, PWM pwm) {
    this(sequence, pwm, sequence.subsequence_positions(pwm.length()));
  }

  // TODO: here we create lots of temporary objects(substrings), may be we should define a method which uses parent object and shift?
  // TODO: reverse-complement matrix, not the sequence
  Map<Position, Double> scores_by_position() {
    if (cache_score_by_position == null) {
      cache_score_by_position = new HashMap<Position,Double>();
      for (Position position: positions_to_check) {
        Sequence subsequence = sequence.substring(position, pwm.length());
        cache_score_by_position.put(position, pwm.score(subsequence));
      }
    }
    return cache_score_by_position;
  }

  public Position best_position() {
    Double max_score = Double.NEGATIVE_INFINITY;
    Position best_position = null;
    for (Position position : scores_by_position().keySet()) {
      if (scores_by_position().get(position) > max_score) {
        best_position = position;
        max_score = scores_by_position().get(position);
      }
    }
    return best_position;
  }

  public double best_score() {
    return scores_by_position().get(best_position());
  }

}
