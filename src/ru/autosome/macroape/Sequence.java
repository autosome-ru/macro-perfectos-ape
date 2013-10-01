package ru.autosome.macroape;

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

  public Sequence substring(int beginIndex, int endIndex) {
    return new Sequence(sequence.substring(beginIndex, endIndex));
  }

  @Override
  public String toString() {
    return sequence;
  }
}
