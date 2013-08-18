package ru.autosome.jMacroape;

import java.util.HashMap;

public class Sequence {
  private static HashMap<Character,Character> complements_cache;
  public static HashMap<Character,Character> complements(){
    if (complements_cache == null) {
      HashMap<Character,Character> complements = new HashMap<Character,Character>();
      complements.put('a','t');complements.put('c','g');complements.put('g','c');complements.put('t','a');
      complements.put('A','T');complements.put('C','G');complements.put('G','C');complements.put('T','A');
      complements_cache = complements;
    }
    return complements_cache;
  }


  public String sequence;
  public Sequence(String sequence) {
    this.sequence = sequence;
  }
  public int length() {
    return sequence.length();
  }
  public Sequence reverse(){
    String result = "";
    for (int i = 0; i < sequence.length(); ++i) {
      result += sequence.charAt(sequence.length() - i - 1);
    }
    return new Sequence(result);
  }
  public Sequence complement(){
    HashMap<Character,Character> complements = complements();
    String result = "";
    for (int i = 0; i < sequence.length(); ++i) {
      result += complements.get(sequence.charAt(i));
    }
    return new Sequence(result);
  }
  public Sequence substring(int beginIndex, int endIndex) {
    return new Sequence(sequence.substring(beginIndex, endIndex));
  }
  @Override
  public String toString() {
    return sequence;
  }

}
