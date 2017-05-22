package ru.autosome.perfectosape.calculation;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.model.PositionInterval;
import ru.autosome.commons.motifModel.Encodable;
import ru.autosome.commons.motifModel.ScoringModel;
import ru.autosome.perfectosape.model.Sequence;
import ru.autosome.perfectosape.model.SequenceWithSNP;
import ru.autosome.perfectosape.model.encoded.EncodedSequenceType;
import ru.autosome.perfectosape.model.encoded.EncodedSequenceWithSNVType;

public class SingleSNPScan<SequenceType extends EncodedSequenceType,
                           SequenceWithSNVType extends EncodedSequenceWithSNVType<SequenceType>,
                           ModelType extends ScoringModel<SequenceType> & Encodable<SequenceType, SequenceWithSNVType>> {
  final ModelType pwm;
  final SequenceWithSNP sequenceWithSNP;
  final SequenceWithSNVType encodedSequenceWithSNP;
  final CanFindPvalue pvalueCalculator;
  final int expandRegionLength;

  public SingleSNPScan(ModelType pwm, SequenceWithSNP sequenceWithSNP, SequenceWithSNVType encodedSequenceWithSNP, CanFindPvalue pvalueCalculator, int expandRegionLength) {
    if (sequenceWithSNP.length() < pwm.length()) {
      throw new IllegalArgumentException("Can't scan sequence '" + sequenceWithSNP + "' (length " + sequenceWithSNP.length() + ") with motif of length " + pwm.length());
    }
    this.pwm = pwm;
    this.sequenceWithSNP = sequenceWithSNP;
    this.encodedSequenceWithSNP = encodedSequenceWithSNP; // Another representation of the same sequence with SNP (not checked they are in accordance due to performance reasons
    this.pvalueCalculator = pvalueCalculator;
    this.expandRegionLength = expandRegionLength;
    if (sequenceWithSNP.num_cases() != 2) {
      throw new IllegalArgumentException("Unable to process more than two variants of nucleotide for SNP " + sequenceWithSNP);
    }
  }

  // More slow constructor than the full one: it encodes sequence in a constructor
  public SingleSNPScan(ModelType pwm, SequenceWithSNP sequenceWithSNP, CanFindPvalue pvalueCalculator, int expandRegionLength) {
    if (sequenceWithSNP.length() < pwm.length()) {
      throw new IllegalArgumentException("Can't scan sequence '" + sequenceWithSNP + "' (length " + sequenceWithSNP.length() + ") with motif of length " + pwm.length());
    }
    this.pwm = pwm;
    this.sequenceWithSNP = sequenceWithSNP;
    this.encodedSequenceWithSNP = pwm.encodeSequenceWithSNP(sequenceWithSNP); // Another representation of the same sequence with SNP (not checked they are in accordance due to performance reasons
    this.pvalueCalculator = pvalueCalculator;
    this.expandRegionLength = expandRegionLength;
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

    public double logFoldChange() {
      return Math.log(info_1.pvalue / info_2.pvalue) / Math.log(2);
    }

    @Override

    public String toString() {
      return toString(false);
    }
    public String toString(boolean useLogFoldChange) {
      StringBuilder result = new StringBuilder();
      result.append(info_1.position.toString()).append("\t").append(info_1.word).append("\t");
      result.append(info_2.position.toString()).append("\t").append(info_2.word).append("\t");

      result.append(info_1.allele).append("/").append(info_2.allele).append("\t");
      result.append(info_1.pvalue).append("\t").append(info_2.pvalue).append("\t");

      if (useLogFoldChange) {
        result.append(logFoldChange());
      } else {
        result.append(foldChange());
      }
      return result.toString();
    }

    public String toStringShort() {
      String result = String.format("%.2e", info_1.pvalue) + "\t" +
                          String.format("%.2e", info_2.pvalue) + "\t" +
                          info_1.position.toStringShort() + "\t" +
                          info_2.position.toStringShort();

      return result;
    }
  }

  PositionInterval positionsToCheck() {
    return sequenceWithSNP.positionsOverlappingSNV(pwm.length()).expand(expandRegionLength);
  }

  public RegionAffinityVariantInfo affinityVariantInfo(int allele_number) {
    EstimateAffinityMinPvalue affinity_calculator;
    affinity_calculator = new EstimateAffinityMinPvalue<SequenceType, ModelType>(pwm,
                                                          encodedSequenceWithSNP.sequenceVariant(allele_number),
                                                          pvalueCalculator,
                                                          positionsToCheck());
    Position pos = affinity_calculator.bestPosition();
    Position pos_centered = new Position(pos.position() - sequenceWithSNP.left.length(), pos.orientation());

    double pvalue = affinity_calculator.affinity();
    Character allele = sequenceWithSNP.mid[allele_number];
    Sequence sequence = sequenceWithSNP.sequence_variants()[allele_number];
    Sequence word = sequence.substring(pos, pwm.length());
    return new RegionAffinityVariantInfo(pos_centered, allele, pvalue, word);
  }

  public RegionAffinityInfos affinityInfos() {
    return new RegionAffinityInfos(affinityVariantInfo(0), affinityVariantInfo(1));
  }
}
