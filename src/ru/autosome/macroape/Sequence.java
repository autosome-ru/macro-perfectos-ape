package ru.autosome.macroape;

import java.util.ArrayList;
import java.util.HashMap;

public class Sequence {
  private static final HashMap<Character, Character> complements;
  static {
    HashMap<Character, Character> hsh = new HashMap<Character, Character>();
    hsh.put('a', 't'); hsh.put('A', 'T'); hsh.put('t', 'a'); hsh.put('T', 'A');
    hsh.put('c', 'g'); hsh.put('C', 'G'); hsh.put('g', 'c'); hsh.put('G', 'C');
    complements = hsh;
  }

  final public String sequence;

  public Sequence(String sequence) {
    this.sequence = sequence;
  }

  public int length() {
    return sequence.length();
  }

  public Sequence reverse() {
    return new Sequence(new StringBuilder(sequence).reverse().toString());
  }

  public Sequence complement() {
    StringBuilder result = new StringBuilder(length());
    for (int i = 0; i < sequence.length(); ++i) {
      result.append(complements.get(sequence.charAt(i)));
    }
    return new Sequence(result.toString());
  }

  // works on direct strand
  public Sequence substring(int beginIndex, int endIndex) {
    return new Sequence(sequence.substring(beginIndex, endIndex));
  }

  // returns subsequence of given length on according strand, starting from given upstream boundary
  public Sequence substring(Position upstream_boundary, int substring_length) {
    if (upstream_boundary.directStrand) {
      return substring(upstream_boundary.position, Math.min(upstream_boundary.position + substring_length, length()));
    } else {
      return substring(Math.max(upstream_boundary.position - substring_length + 1, 0), upstream_boundary.position + 1).reverse().complement();
    }
  }

  // (upstream) positions at which subsequence of given length can start
  public ArrayList<Position> subsequence_positions(int subsequence_length) {
    return Position.positions_between(0, length(), subsequence_length);
  }

  @Override
  public String toString() {
    return sequence;
  }
}
