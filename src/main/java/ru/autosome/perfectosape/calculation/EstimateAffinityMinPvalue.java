package ru.autosome.perfectosape.calculation;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.model.PositionInterval;
import ru.autosome.commons.motifModel.HasLength;
import ru.autosome.commons.scoringModel.ScoringModel;

public class EstimateAffinityMinPvalue<SequenceType extends HasLength,
                                       ModelType extends ScoringModel<SequenceType>> {
  final ModelType pwm;
  final SequenceType sequence;
  final CanFindPvalue pvalueCalculator;
  final PositionInterval positions_to_check;
  private ScanSequence<SequenceType> cache_scanSequence;

  public EstimateAffinityMinPvalue(ModelType pwm, SequenceType sequence, CanFindPvalue pvalueCalculator, PositionInterval positions_to_check) {
    if (sequence.length() < pwm.length()) {
      throw new IllegalArgumentException("Can't estimate affinity to sequence '" + sequence + "' (length " + sequence.length() + ") of motif of length " + pwm.length());
    }
    this.pwm = pwm;
    this.sequence = sequence;
    this.pvalueCalculator = pvalueCalculator;
    this.positions_to_check = positions_to_check;
  }

  ScanSequence scanSequence() {
    if (cache_scanSequence == null) {
      cache_scanSequence = new ScanSequence<>(sequence, pwm, positions_to_check);
    }
    return cache_scanSequence;
  }

  public double affinity() {
    double score = scanSequence().best_score();
    return pvalueCalculator.pvalueByThreshold(score).pvalue;
  }
  public Position bestPosition() {
    return scanSequence().best_position();
  }
}