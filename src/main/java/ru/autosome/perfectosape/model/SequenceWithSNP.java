package ru.autosome.perfectosape.model;

import gnu.trove.set.TCharSet;
import gnu.trove.set.hash.TCharHashSet;
import ru.autosome.commons.model.Position;

import java.util.ArrayList;

public class SequenceWithSNP {
  // Duplicated in class Sequence
  private static final TCharSet allowedLetters = new TCharHashSet(new char[]{'A','C','G','T','a','c','g','t', 'n', 'N'});

  final public String left;
  final public String right;
  final public char[] mid;

  // line should finish with sequence (which doesn't have spaces).
  // Example:
  // input:  "GATTCAAAGGTTCTGAATTCCACAAC[a/g]GCTTTCCTGTGTTTTTGCAGCCAGA"
  // possible SNP formats: [a/g]; [ag]; a/g; a/g/c; [agc]; [a/g/c] and so on
  public SequenceWithSNP(String left, char[] mid, String right) {
    if ( !allowedLetters.containsAll(left.toCharArray()) ) {
      throw new IllegalArgumentException("Sequence '" + left + "' (left part of SNP) contains unallowed character (only A,C,G,T,N letters are allowed).");
    }
    if ( !allowedLetters.containsAll(right.toCharArray()) ) {
      throw new IllegalArgumentException("Sequence '" + right + "' (right part of SNP) contains unallowed character (only A,C,G,T,N letters are allowed).");
    }

    if ( !allowedLetters.containsAll(mid) ) {
      throw new IllegalArgumentException("SNP variants: '" + new String(mid) + "' contain unallowed character (only A,C,G,T,N letters are allowed).");
    }

    this.left = left.toLowerCase();
    char[] mid_upcased = new char[mid.length];
    for (int i = 0; i < mid.length; ++i) {
      mid_upcased[i] = Character.toUpperCase(mid[i]);
    }
    this.mid = mid_upcased;
    this.right = right.toLowerCase();
  }

  public static SequenceWithSNP fromString(String seq_w_snp) {
    String[] seq_parts = seq_w_snp.split("\\[|\\]");  // split by [ or ]
    if (seq_parts.length == 3) { // acc[T/A]cca  or acc[TA]cca or  acc[T/A/G]cca  or acc[TAG]cca
      String left = seq_parts[0];
      char[] mid = seq_parts[1].replaceAll("/", "").toCharArray();
      String right = seq_parts[2];

      return new SequenceWithSNP(left, mid, right);
    }
    else if (seq_parts.length == 1) { //   accT/Acca  or  accT/A/Gcca
      int left_separator = seq_w_snp.indexOf("/");
      int right_separator = seq_w_snp.lastIndexOf("/");

      String left = seq_w_snp.substring(0, left_separator - 1);
      String right = seq_w_snp.substring(right_separator + 2, seq_w_snp.length());
      char[] mid = seq_w_snp.substring(left_separator - 1, right_separator + 2).replaceAll("/", "").toCharArray();

      return new SequenceWithSNP(left, mid, right);
    } else {
      throw new IllegalArgumentException("Can't parse sequence with SNPs: " + seq_w_snp);
    }
  }

  int pos_of_snp() {
    return left.length();
  }

  public int num_cases() {
    return mid.length;
  }

  // output: ["GATTCAAAGGTTCTGAATTCCACAACaGCTTTCCTGTGTTTTTGCAGCCAGA",
  //          "GATTCAAAGGTTCTGAATTCCACAACgGCTTTCCTGTGTTTTTGCAGCCAGA"]
  public Sequence[] sequence_variants() {
    Sequence[] result = new Sequence[num_cases()];
    for (int i = 0; i < num_cases(); ++i) {
      result[i] = new Sequence(left + mid[i] + right);
    }
    return result;
  }

  public int length() {
    return left.length() + 1 + right.length();
  }

  public int left_shift(int motif_length) {
    return Math.max(0, pos_of_snp() - motif_length + 1);
  }

  // position
  public ArrayList<Position> positions_subsequence_overlaps_snp(int subsequence_length) {
    int left_pos = Math.max(0, left.length() - subsequence_length + 1);
    int right_pos = Math.min(length(), left.length() + subsequence_length);
    return Position.positions_between(left_pos, right_pos, subsequence_length);
  }

  @Override
  public String toString() {
    String mid_variants = "" + mid[0];
    for (int i = 1; i < num_cases(); ++i) {
      mid_variants += "/" + mid[i];
    }
    return left + "[" + mid_variants + "]" + right;
  }

}
