package ru.autosome.perfectosape.calculation;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.model.PositionInterval;
import ru.autosome.commons.scoringModel.ScoringModel;
import ru.autosome.perfectosape.model.*;
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
