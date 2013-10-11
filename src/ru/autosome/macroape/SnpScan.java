package ru.autosome.macroape;

import java.util.ArrayList;

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

    if (sequenceWithSNP.num_cases() != 2)
      return null; // Unable to process more than two variants(which fractions to return)

    ArrayList<Position> positions_to_check = sequenceWithSNP.positions_subsequence_overlaps_snp(pwm.length());
    Sequence seq_1 = sequenceWithSNP.sequence_variants()[0];
    Character allele_1 = sequenceWithSNP.mid[0];
    EstimateAffinityMinPvalue seq_1_affinity_calculator =
     new EstimateAffinityMinPvalue(pwm, seq_1, pvalueCalculator, positions_to_check);
    Position pos_1 = seq_1_affinity_calculator.bestPosition();
    double pvalue_1 = seq_1_affinity_calculator.affinity();
    Sequence word_1 = seq_1.substring(pos_1, pwm.length());

    Sequence seq_2 = sequenceWithSNP.sequence_variants()[1];
    Character allele_2 = sequenceWithSNP.mid[1];
    EstimateAffinityMinPvalue seq_2_affinity_calculator =
     new EstimateAffinityMinPvalue(pwm, seq_2, pvalueCalculator, positions_to_check);
    Position pos_2 = seq_2_affinity_calculator.bestPosition();
    double pvalue_2 = seq_2_affinity_calculator.affinity();
    Sequence word_2 = seq_2.substring(pos_2, pwm.length());

    StringBuilder result = new StringBuilder();
    result.append(pos_1.toString()).append("\t").append(word_1).append("\t");
    result.append(pos_2.toString()).append("\t").append(word_2).append("\t");
    result.append(allele_1).append("/").append(allele_2).append("\t");
    result.append(pvalue_1).append("\t").append(pvalue_2).append("\t");
    result.append(pvalue_2 / pvalue_1);
    return result.toString();
  }

}
