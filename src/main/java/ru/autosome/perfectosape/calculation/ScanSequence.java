package ru.autosome.perfectosape.calculation;

import ru.autosome.commons.model.Position;
import ru.autosome.commons.motifModel.ScoringModel;
import ru.autosome.perfectosape.model.Sequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ScanSequence {
  private final Sequence sequence;
  private final ScoringModel pwm;
  final ArrayList<Position> positions_to_check;
  private Map<Position, Double> cache_score_by_position;

  public ScanSequence(Sequence sequence, ScoringModel pwm, ArrayList<Position> positions_to_check) {
    if (sequence.length() < pwm.length()) {
      throw new IllegalArgumentException("Can't scan sequence '" + sequence + "' (length " + sequence.length() + ") with motif of length " + pwm.length());
    }
    this.sequence = sequence;
    this.pwm = pwm;
    this.positions_to_check = positions_to_check;
  }

  public ScanSequence(Sequence sequence, ScoringModel pwm) {
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
