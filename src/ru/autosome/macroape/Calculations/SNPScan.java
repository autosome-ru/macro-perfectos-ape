package ru.autosome.macroape.Calculations;

import ru.autosome.macroape.PWM;
import ru.autosome.macroape.Position;
import ru.autosome.macroape.Sequence;
import ru.autosome.macroape.SequenceWithSNP;

import java.util.ArrayList;

public class SNPScan {
  final PWM pwm;
  final SequenceWithSNP sequenceWithSNP;
  final CanFindPvalue pvalueCalculator;
  RegionAffinityInfo[] cacheAffinityInfos;

  public SNPScan(PWM pwm, SequenceWithSNP sequenceWithSNP, CanFindPvalue pvalueCalculator) {
    this.pwm = pwm;
    this.sequenceWithSNP = sequenceWithSNP;
    this.pvalueCalculator = pvalueCalculator;
    if (sequenceWithSNP.num_cases() != 2) {
      throw new IllegalArgumentException("Unable to process more than two variants of nucleotide for SNP " + sequenceWithSNP);
    }
    cacheAffinityInfos = new RegionAffinityInfo[2];
  }

  public static class RegionAffinityInfo {
    Position position;
    Sequence word;
    Character allele;
    double pvalue;

    RegionAffinityInfo(Position position, Character allele, double pvalue, Sequence word) {
      this.position = position;
      this.allele = allele;
      this.pvalue = pvalue;
      this.word = word;
    }
  }

  ArrayList<Position> positionsToCheck() {
    return sequenceWithSNP.positions_subsequence_overlaps_snp(pwm.length());
  }

  RegionAffinityInfo affinityInfo(int allele_number) {
    if (cacheAffinityInfos[allele_number] == null) {
      Sequence sequence = sequenceWithSNP.sequence_variants()[allele_number];
      Character allele = sequenceWithSNP.mid[allele_number];
      EstimateAffinityMinPvalue affinity_calculator =  new EstimateAffinityMinPvalue(pwm,
                                                                                     sequence,
                                                                                     pvalueCalculator,
                                                                                     positionsToCheck());
      Position pos = affinity_calculator.bestPosition();
      double pvalue = affinity_calculator.affinity();
      Sequence word = sequence.substring(pos, pwm.length());

      cacheAffinityInfos[allele_number] = new RegionAffinityInfo(pos, allele, pvalue, word);
    }
    return cacheAffinityInfos[allele_number];
  }

  double foldChange() {
    return affinityInfo(1).pvalue / affinityInfo(0).pvalue;
  }

  public String influenceString() {
    RegionAffinityInfo info_1 = affinityInfo(0);
    RegionAffinityInfo info_2 = affinityInfo(1);

    StringBuilder result = new StringBuilder();
    result.append(info_1.position.toString()).append("\t").append(info_1.word).append("\t");
    result.append(info_2.position.toString()).append("\t").append(info_2.word).append("\t");

    result.append(info_1.allele).append("/").append(info_2.allele).append("\t");
    result.append(info_1.pvalue).append("\t").append(info_2.pvalue).append("\t");

    result.append(foldChange());
    return result.toString();
  }

}
