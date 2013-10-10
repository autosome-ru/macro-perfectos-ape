package ru.autosome.macroape;

import java.util.ArrayList;
import java.util.Map;

public class ScanSequence {
  private final Sequence sequence;
  private final PWM pwm;
  ArrayList<Position> positions_to_check;
  private Map<Position, Double> cache_score_by_position;

  public ScanSequence(Sequence sequence, PWM pwm, ArrayList<Position> positions_to_check) {
    this.sequence = sequence;
    this.pwm = pwm;
    this.positions_to_check = positions_to_check;
  }

  public ScanSequence(Sequence sequence, PWM pwm) {
    this(sequence, pwm, sequence.subsequence_positions(pwm.length()));
  }

  Map<Position, Double> scores_by_position() {
    if (cache_score_by_position == null) {
      for (Position position: positions_to_check) {
        Sequence subsequence = sequence.substring(position, pwm.length());
        cache_score_by_position.put(position, pwm.score(subsequence));
      }
    }
    return cache_score_by_position;
  }

  Position best_score_full_position() {
    Double max_score = Double.NEGATIVE_INFINITY;
    Position best_position = null;
    for (Position position : scores_by_position().keySet()) {
      if (scores_by_position().get(position) > max_score) {
        best_position = position;
        max_score = scores_by_position().get(position);
      }
    }
    return best_position;
  }

  String best_score_strand() {
    return best_score_full_position().strand();
  }

  public double best_score_on_sequence() {
    return scores_by_position().get(best_score_full_position());
  }

  int best_score_position() {
    return best_score_full_position().position;
  }

  // params are number of letters before position where snp starts overlap with pwm
  public String best_match_info_string(int shift) {
    String strand = best_score_strand();
    int pos = best_score_position();
    int pos_original = pos + shift; // pos in non-trimmed sequence
    if (strand.equals("revcomp")) pos_original += 1 - pwm.length(); // leftmost but not most upstream
    Sequence word = get_word(strand, pos);
    return "" + pos_original + "\t" + strand + "\t" + word;
  }

  Sequence get_word(String strand, int pos) {
    if (strand.equals("direct")) {
      return sequence.substring(pos, pos + pwm.length());
    } else {
      pos = sequence.length() - 1 - pos;
      return sequence.reverse().complement().substring(pos, pos + pwm.length());
    }
  }
}
