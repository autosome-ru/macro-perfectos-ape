package ru.autosome.jMacroape;

import java.util.HashMap;

public class ScanSequence {
  private static HashMap<Character,Character> complements_cache;
  String sequence;
  PWM pwm;
  double[] scores_on_direct_strand_cache;
  double[] scores_on_reverse_strand_cache;
  ScanSequence(String sequence, PWM pwm) {
    this.sequence = sequence;
    this.pwm = pwm;
  }

  public static HashMap<Character,Character> complements(){
    if (complements_cache == null) {
      HashMap<Character,Character> complements = new HashMap<Character,Character>();
      complements.put('a','t');complements.put('c','g');complements.put('g','c');complements.put('t','a');
      complements.put('A','T');complements.put('C','G');complements.put('G','C');complements.put('T','A');
      complements_cache = complements;
    }
    return complements_cache;
  }

  public static String complement(String seq){
    HashMap<Character,Character> complements = complements();
    String result = "";
    for (int i = 0; i < seq.length(); ++i) {
      result += complements.get(seq.charAt(i));
    }
    return result;
  }

  public static String reverse(String seq){
    String result = "";
    for (int i = 0; i < seq.length(); ++i) {
      result += seq.charAt(seq.length() - i - 1);
    }
    return result;
  }

  public double[] scores_on_direct_strand() {
    if (scores_on_direct_strand_cache == null) {
      scores_on_direct_strand_cache = pwm.scores_on_sequence(sequence);
    }
    return scores_on_direct_strand_cache;
  }

  public double[] scores_on_reverse_strand() {
    if (scores_on_reverse_strand_cache == null) {
      scores_on_reverse_strand_cache = pwm.scores_on_sequence(reverse(complement(sequence)));
    }
    return scores_on_reverse_strand_cache;
  }

  public double best_score_on_direct_strand() {
    return ArrayExtensions.max(scores_on_direct_strand());
  }

  public double best_score_on_reverse_strand() {
    return ArrayExtensions.max(scores_on_reverse_strand());
  }

  public String best_score_strand() {
    if (best_score_on_direct_strand() >= best_score_on_reverse_strand()) {
      return "direct";
    } else {
      return "revcomp";
    }
  }

  public double best_score_on_sequence() {
    return ArrayExtensions.max(best_score_on_direct_strand(), best_score_on_reverse_strand());
  }

  // position of motif start (most upstream position, not the leftmost)
  public int best_score_position() {
    if (best_score_strand().equals("direct")) {
      return ArrayExtensions.indexOf(best_score_on_direct_strand(), scores_on_direct_strand());
    } else {
      return sequence.length() - 1 - ArrayExtensions.indexOf(best_score_on_reverse_strand(), scores_on_reverse_strand());
    }
  }

  // params are number of letters before position where snp starts overlap with pwm
  public String best_match_info_string(int shift) {
    String strand = best_score_strand();
    int pos = best_score_position();
    int pos_original = pos + shift; // pos in non-trimmed sequence
    if (strand.equals("revcomp")) pos_original += 1-pwm.length(); // leftmost but not most upstream
    String word = get_word(strand, pos);
    return "" + pos_original + "\t" + strand + "\t" + word;
  }
  public String get_word(String strand, int pos) {
    if (strand.equals("direct")) {
      return sequence.substring(pos, pos + pwm.length());
    } else {
      pos = sequence.length() - 1 - pos;
      return reverse(complement(sequence)).substring(pos, pos + pwm.length());
    }
  }
}
