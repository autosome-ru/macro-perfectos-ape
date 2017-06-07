package ru.autosome.perfectosape.model;

import ru.autosome.commons.model.Orientation;
import ru.autosome.commons.model.Position;

public class PositionWithScore {
  private int position;
  private Orientation orientation;
  private double score;

  public PositionWithScore() {
    score = Double.NEGATIVE_INFINITY;
  }
  public void replaceIfBetter(int newPosition, Orientation newOrientation, double newScore) {
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
