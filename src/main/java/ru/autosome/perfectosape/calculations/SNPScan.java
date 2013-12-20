package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.PWM;
import ru.autosome.perfectosape.Position;
import ru.autosome.perfectosape.Sequence;
import ru.autosome.perfectosape.SequenceWithSNP;

import java.util.ArrayList;

public class SNPScan {
  final PWM pwm;
  final SequenceWithSNP sequenceWithSNP;
  final CanFindPvalue pvalueCalculator;

  public SNPScan(PWM pwm, SequenceWithSNP sequenceWithSNP, CanFindPvalue pvalueCalculator) {
    this.pwm = pwm;
    this.sequenceWithSNP = sequenceWithSNP;
    this.pvalueCalculator = pvalueCalculator;
    if (sequenceWithSNP.num_cases() != 2) {
      throw new IllegalArgumentException("Unable to process more than two variants of nucleotide for SNP " + sequenceWithSNP);
    }
  }

  public static class RegionAffinityVariantInfo {
    final Position position;
    final Sequence word;
    final Character allele;
    final double pvalue;

    public Position getPosition() {
      return position;
    }
    public Sequence getWord() {
      return word;
    }
    public Character getAllele() {
      return allele;
    }
    public double getPvalue() {
      return pvalue;
    }

    RegionAffinityVariantInfo(Position position, Character allele, double pvalue, Sequence word) {
      this.position = position;
      this.allele = allele;
      this.pvalue = pvalue;
      this.word = word;
    }
  }

  public static class RegionAffinityInfos {
    final RegionAffinityVariantInfo info_1;
    final RegionAffinityVariantInfo info_2;

    public RegionAffinityVariantInfo getInfo_1() {
     return info_1;
    }
    public RegionAffinityVariantInfo getInfo_2() {
      return info_2;
    }

    RegionAffinityInfos(RegionAffinityVariantInfo info_1, RegionAffinityVariantInfo info_2) {
      this.info_1 = info_1;
      this.info_2 = info_2;
    }

    public double foldChange() {
      return info_1.pvalue / info_2.pvalue;
    }

    @Override
    public String toString() {
      StringBuilder result = new StringBuilder();
      result.append(info_1.position.toString()).append("\t").append(info_1.word).append("\t");
      result.append(info_2.position.toString()).append("\t").append(info_2.word).append("\t");

      result.append(info_1.allele).append("/").append(info_2.allele).append("\t");
      result.append(info_1.pvalue).append("\t").append(info_2.pvalue).append("\t");

      result.append(foldChange());
      return result.toString();
    }
  }

  ArrayList<Position> positionsToCheck() {
    return sequenceWithSNP.positions_subsequence_overlaps_snp(pwm.length());
  }

  public RegionAffinityVariantInfo affinityVariantInfo(int allele_number) {
      Sequence sequence = sequenceWithSNP.sequence_variants()[allele_number];
      Character allele = sequenceWithSNP.mid[allele_number];
      EstimateAffinityMinPvalue affinity_calculator =  new EstimateAffinityMinPvalue(pwm,
                                                                                     sequence,
                                                                                     pvalueCalculator,
                                                                                     positionsToCheck());
      Position pos = affinity_calculator.bestPosition();
      double pvalue = affinity_calculator.affinity();
      Sequence word = sequence.substring(pos, pwm.length());

      Position pos_centered = new Position(pos.position - sequenceWithSNP.left.length(), pos.directStrand);
      return new RegionAffinityVariantInfo(pos_centered, allele, pvalue, word);
  }

  public RegionAffinityInfos affinityInfos(){
    return new RegionAffinityInfos(affinityVariantInfo(0), affinityVariantInfo(1));
  }
}
