package ru.autosome.commons.model;

public class Position {
  final protected int position;
  final protected Orientation orientation;

  public Position(int position, Orientation orientation) {
    this.position = position;
    this.orientation = orientation;
  }

  public Position(int position, String strand) {
    this.position = position;
    this.orientation = Orientation.valueOf(strand);
  }

  // all positions where subsequence of given length can start on the semiinterval [pos_left; pos_right)
  static public PositionInterval positions_between(int pos_left, int pos_right, int subseq_length) {
    return new PositionInterval(pos_left, pos_right - subseq_length);
  }

  public int position() {
    return position;
  }

  public Orientation orientation() {
    return orientation;
  }

  public boolean isDirect() {
    return orientation.isDirect();
  }
  public boolean isReverseComplement() {
    return orientation.isReverseComplement();
  }

  @Override
  public String toString() {
    return String.valueOf(position) + "\t" + orientation;
  }

}
