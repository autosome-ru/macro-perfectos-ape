package ru.autosome.macroape;

public class SnpScan {
  final PWM pwm;
  final SequenceWithSNP sequenceWithSNP;
  final CanFindPvalue pvalueCalculator;
  public SnpScan(PWM pwm, SequenceWithSNP sequenceWithSNP, CanFindPvalue pvalueCalculator) {
    this.pwm = pwm;
    this.sequenceWithSNP = sequenceWithSNP;
    this.pvalueCalculator = pvalueCalculator;
  }

  public String pwm_influence_infos() {
    Sequence[] trimmed_sequence_variants = sequenceWithSNP.trimmed_sequence_variants(pwm);

    if (sequenceWithSNP.num_cases() != 2)
      return null; // Unable to process more than two variants(which fractions to return)

    ScanSequence scan_seq_1 = new ScanSequence(trimmed_sequence_variants[0], pwm);
    double score_1 = scan_seq_1.best_score_on_sequence();
    double pvalue_1 = pvalueCalculator.pvalue_by_threshold(score_1).pvalue;

    ScanSequence scan_seq_2 = new ScanSequence(trimmed_sequence_variants[1], pwm);
    double score_2 = scan_seq_2.best_score_on_sequence();
    double pvalue_2 = pvalueCalculator.pvalue_by_threshold(score_2).pvalue;

    // We print position from the start of seq, not from the start of overlapping region, thus should calculate the shift
    int left_shift = sequenceWithSNP.left_shift(pwm.length());

    StringBuilder result = new StringBuilder();
    result.append(scan_seq_1.best_match_info_string(left_shift)).append("\t").append(pvalue_1).append("\t");
    result.append(scan_seq_2.best_match_info_string(left_shift)).append("\t").append(pvalue_2).append("\t");
    result.append(pvalue_2 / pvalue_1);
    return result.toString();
  }

}
