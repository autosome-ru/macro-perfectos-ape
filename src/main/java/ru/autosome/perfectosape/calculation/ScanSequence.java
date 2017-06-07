package ru.autosome.perfectosape.calculation;

import ru.autosome.commons.model.Position;
import ru.autosome.commons.model.PositionInterval;
import ru.autosome.commons.motifModel.HasLength;
import ru.autosome.commons.scoringModel.ScoringModel;
import ru.autosome.perfectosape.model.BestPositionWithScore;

public class ScanSequence<SequenceType extends HasLength> {
  private final SequenceType sequence;
  private final ScoringModel<SequenceType> pwm;
  private final PositionInterval positions_to_check;
  private Boolean cacheBest;
  private Position cache_bestPosition;
  private double cache_bestScore;

  public ScanSequence(SequenceType sequence, ScoringModel<SequenceType> pwm, PositionInterval positions_to_check) {
    if (sequence.length() < pwm.length()) {
      throw new IllegalArgumentException("Can't scan sequence '" + sequence + "' (length " + sequence.length() + ") with motif of length " + pwm.length());
    }
    this.sequence = sequence;
    this.pwm = pwm;
    this.positions_to_check = positions_to_check;
  }

  public void findBest() {
    if (cacheBest == null) {
      BestPositionWithScore bestPos = positions_to_check.findBestPosition(sequence, pwm);
      cache_bestScore = bestPos.getScore();
      cache_bestPosition = bestPos.getPosition();
      cacheBest = true;
    }
  }

  public Position best_position() {
    findBest();
    return cache_bestPosition;
  }

  public double best_score() {
    findBest();
    return cache_bestScore;
  }

}
