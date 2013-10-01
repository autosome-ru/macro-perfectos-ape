package ru.autosome.macroape;

public class ScanSequence {
  private final Sequence sequence;
  private final PWM pwm;
  private double[] scores_on_direct_strand_cache;
  private double[] scores_on_reverse_strand_cache;

  public ScanSequence(Sequence sequence, PWM pwm) {
    this.sequence = sequence;
    this.pwm = pwm;
  }

  double[] scores_on_direct_strand() {
    if (scores_on_direct_strand_cache == null) {
      scores_on_direct_strand_cache = pwm.scores_on_sequence(sequence);
    }
    return scores_on_direct_strand_cache;
  }

  double[] scores_on_reverse_strand() {
    if (scores_on_reverse_strand_cache == null) {
      scores_on_reverse_strand_cache = pwm.scores_on_sequence(sequence.reverse().complement());
    }
    return scores_on_reverse_strand_cache;
  }

  double best_score_on_direct_strand() {
    return ArrayExtensions.max(scores_on_direct_strand());
  }

  double best_score_on_reverse_strand() {
    return ArrayExtensions.max(scores_on_reverse_strand());
  }

  String best_score_strand() {
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
  int best_score_position() {
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
