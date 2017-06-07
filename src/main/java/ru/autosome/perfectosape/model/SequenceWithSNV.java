package ru.autosome.perfectosape.model;

import gnu.trove.set.TCharSet;
import gnu.trove.set.hash.TCharHashSet;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.model.PositionInterval;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SequenceWithSNV {
  // Duplicated in class Sequence
  private static final TCharSet allowedLetters = new TCharHashSet(new char[]{'A','C','G','T','a','c','g','t', 'n', 'N'});
  static Pattern SNVSequencePattern = Pattern.compile("([ACGTN]*)\\[([ACGTN](?:/[ACGTN])+)\\]([ACGTN]*)", Pattern.CASE_INSENSITIVE);

  final public String left;
  final public String right;
  final public char[] mid;
  private Sequence[] cache_sequence_variants;

  // line should finish with sequence (which doesn't have spaces).
  // Example:
  // input:  "GATTCAAAGGTTCTGAATTCCACAAC[a/g]GCTTTCCTGTGTTTTTGCAGCCAGA"
  // possible SNP formats: [a/g]; [ag]; a/g; a/g/c; [agc]; [a/g/c] and so on
  public SequenceWithSNV(String left, char[] mid, String right) {
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

  public static SequenceWithSNV fromString(String seq_w_snp) {

    Matcher matcher = SNVSequencePattern.matcher(seq_w_snp);
    if (matcher.find()) {
      String left = matcher.group(1);
      String mid_str = matcher.group(2);
      char[] mid = mid_str.replaceAll("/", "").toCharArray();
      String right = matcher.group(3);
      return new SequenceWithSNV(left, mid, right);
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
    if (cache_sequence_variants == null) {
      cache_sequence_variants = new Sequence[num_cases()];

      for (int i = 0; i < num_cases(); ++i) {
        cache_sequence_variants[i] = new Sequence(left + mid[i] + right, true);
      }
    }
    return cache_sequence_variants;
  }

  public int length() {
    return left.length() + 1 + right.length();
  }

//  public int left_shift(int motif_length) {
//    return Math.max(0, pos_of_snp() - motif_length + 1);
//  }

  public PositionInterval positionsOverlappingSNV(int subsequence_length) {
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

  private String polyNString(int len) {
    char[] buf = new char[len];
    Arrays.fill(buf, 'N');
    return new String(buf);
  }

  // Expands sequence with poly-N flanks if necessary
  // sequenceRadius includes substitution position
  public SequenceWithSNV expandFlanksUpTo(int sequenceRadius) {
    int leftExpansionLength = Math.max(sequenceRadius - 1 - left.length(), 0);
    int rightExpansionLength = Math.max(sequenceRadius - 1 - right.length(), 0);
    return new SequenceWithSNV(polyNString(leftExpansionLength) + left,
                               mid,
                               right + polyNString(rightExpansionLength));
  }
}
