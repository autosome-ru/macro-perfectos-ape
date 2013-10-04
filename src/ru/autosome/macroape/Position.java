package ru.autosome.macroape;

public class Position {
  final int position;
  final boolean directStrand;
  Position(int position, boolean directStrand) {
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
  String strand() {
    return directStrand ? "direct" : "revcomp";
  }
}
