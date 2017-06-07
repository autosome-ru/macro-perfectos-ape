package ru.autosome.perfectosape.calculation;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.model.PositionInterval;
import ru.autosome.commons.scoringModel.ScoringModel;
import ru.autosome.perfectosape.model.PositionWithScore;
import ru.autosome.perfectosape.model.RegionAffinityVariantInfo;
import ru.autosome.perfectosape.model.Sequence;
import ru.autosome.perfectosape.model.SequenceWithSNV;
import ru.autosome.perfectosape.model.encoded.EncodedSequenceType;
import ru.autosome.perfectosape.model.encoded.EncodedSequenceWithSNVType;

public class SingleSNVScan<SequenceType extends EncodedSequenceType,
                           SequenceWithSNVType extends EncodedSequenceWithSNVType<SequenceType>,
                           ModelType extends ScoringModel<SequenceType>> {
  private final ModelType pwm;
  private final SequenceWithSNV sequenceWithSNV;
  private final SequenceWithSNVType encodedSequenceWithSNP;
  private final CanFindPvalue pvalueCalculator;
  private final int expandRegionLength;

  public SingleSNVScan(ModelType pwm, SequenceWithSNV sequenceWithSNV, SequenceWithSNVType encodedSequenceWithSNP, CanFindPvalue pvalueCalculator, int expandRegionLength) {
    if (sequenceWithSNV.length() < pwm.length()) {
      throw new IllegalArgumentException("Can't scan sequence '" + sequenceWithSNV + "' (length " + sequenceWithSNV.length() + ") with motif of length " + pwm.length());
    }
    this.pwm = pwm;
    this.sequenceWithSNV = sequenceWithSNV;
    this.encodedSequenceWithSNP = encodedSequenceWithSNP; // Another representation of the same sequence with SNP (not checked they are in accordance due to performance reasons
    this.pvalueCalculator = pvalueCalculator;
    this.expandRegionLength = expandRegionLength;
    if (sequenceWithSNV.length() < pwm.length()) {
      throw new IllegalArgumentException("Can't estimate affinity to sequence '" + sequenceWithSNV + "' (length " + sequenceWithSNV.length() + ") for motif of length " + pwm.length());
    }
    if (sequenceWithSNV.num_cases() != 2) {
      throw new IllegalArgumentException("Unable to process more than two variants of nucleotide for SNP " + sequenceWithSNV);
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
      return String.format("%.2e", info_1.pvalue) + "\t" +
                          String.format("%.2e", info_2.pvalue) + "\t" +
                          info_1.position.toStringShort() + "\t" +
                          info_2.position.toStringShort();
    }
  }

  PositionInterval positionsToCheck() {
    return sequenceWithSNV.positionsOverlappingSNV(pwm.length()).expand(expandRegionLength);
  }

  public RegionAffinityVariantInfo affinityVariantInfo(int allele_number) {
    SequenceType encodedSequence = encodedSequenceWithSNP.sequenceVariant(allele_number);
    PositionWithScore bestPositionWithScore = positionsToCheck().findBestPosition(encodedSequence, pwm);

    Position pos = bestPositionWithScore.getPosition();
    Position pos_centered = new Position(pos.position() - sequenceWithSNV.left.length(), pos.orientation());

    double score = bestPositionWithScore.getScore();
    double pvalue = pvalueCalculator.pvalueByThreshold(score).pvalue;
    Character allele = sequenceWithSNV.mid[allele_number];
    Sequence sequence = sequenceWithSNV.sequence_variants()[allele_number];
    Sequence word = sequence.substring(pos, pwm.length());
    return new RegionAffinityVariantInfo(pos_centered, allele, pvalue, word);
  }

  public RegionAffinityInfos affinityInfos() {
    return new RegionAffinityInfos(affinityVariantInfo(0), affinityVariantInfo(1));
  }
}
