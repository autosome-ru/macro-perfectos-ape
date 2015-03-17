package ru.autosome.commons.model;

// ToDo: make use of Orientation class
public class Position {
  final public int position;
  final public boolean directStrand;
  public Position(int position, boolean directStrand) {
    this.position = position;
    this.directStrand = directStrand;
  }

  public Position(int position, Orientation orientation) {
    this.position = position;
    this.directStrand = (orientation == Orientation.direct);
  }

  public Position(int position, String strand) {
    if (strand.equals("direct")) {
      this.directStrand = true;
    } else if (strand.equals("revcomp")) {
      this.directStrand = false;
    } else {
      throw new IllegalArgumentException("Strand orientation can be either direct or revcomp, but was " + strand);
    }
    this.position = position;
  }
  public String strand() {
    return directStrand ? "direct" : "revcomp";
  }

  // all positions where subsequence of given length can start on the semiinterval [pos_left; pos_right)
  static public PositionInterval positions_between(int pos_left, int pos_right, int subseq_length) {
    return new PositionInterval(pos_left, pos_right - subseq_length);
  }

  @Override
  public String toString() {
    return String.valueOf(position) + "\t" + strand();
  }

}
