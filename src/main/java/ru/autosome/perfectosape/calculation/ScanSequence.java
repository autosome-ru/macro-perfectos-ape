package ru.autosome.perfectosape.calculation;

import ru.autosome.commons.model.Orientation;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.model.PositionInterval;
import ru.autosome.commons.motifModel.HasLength;
import ru.autosome.commons.motifModel.ScoringModel;

public class ScanSequence<SequenceType extends HasLength> {
  private final SequenceType sequence;
  private final ScoringModel<SequenceType> pwm;
  final PositionInterval positions_to_check;
  Boolean cacheBest;
  Position cache_bestPosition;
  double cache_bestScore;

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

  public static class BestPositionWithScore {
    private int position;
    private Orientation orientation;
    private double score;

    public BestPositionWithScore() {
      score = Double.NEGATIVE_INFINITY;
    }
    public void updateBestScore(int newPosition, Orientation newOrientation, double newScore) {
      if (newScore > score) {
        score = newScore;
        position = newPosition;
        orientation = newOrientation;
      }
    }
    public double getScore() {
      return score;
    }
    public Position getPosition() {
      return new Position(position, orientation);
    }
  }
}
