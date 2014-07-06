package ru.autosome.commons.model;

import java.util.ArrayList;

public class Position {
  final public int position;
  final public boolean directStrand;
  public Position(int position, boolean directStrand) {
    this.position = position;
    this.directStrand = directStrand;
  }
  Position(int position, String strand) {
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
  static public ArrayList<Position> positions_between(int pos_left, int pos_right, int subseq_length) {
    ArrayList<Position> positions = new ArrayList<Position>();
    for (int pos = pos_left; pos <= pos_right - subseq_length; ++pos) {
      positions.add(new Position(pos, true));
      positions.add(new Position(pos, false));
    }
    return positions;
  }

  @Override
  public String toString() {
    return String.valueOf(position) + "\t" + strand();
  }
}
