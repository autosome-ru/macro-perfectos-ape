package ru.autosome.perfectosape.calculation;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.commons.model.Position;
import ru.autosome.commons.motifModel.ScoringModel;
import ru.autosome.perfectosape.model.Sequence;

import java.util.ArrayList;

public class EstimateAffinityMinPvalue implements EstimateAffinity {
  final ScoringModel pwm;
  final Sequence sequence;
  final CanFindPvalue pvalueCalculator;
  final ArrayList<Position> positions_to_check;
  public EstimateAffinityMinPvalue(ScoringModel pwm, Sequence sequence, CanFindPvalue pvalueCalculator, ArrayList<Position> positions_to_check) {
    if (sequence.length() < pwm.length()) {
      throw new IllegalArgumentException("Can't estimate affinity to sequence '" + sequence + "' (length " + sequence.length() + ") of motif of length " + pwm.length());
    }
    this.pwm = pwm;
    this.sequence = sequence;
    this.pvalueCalculator = pvalueCalculator;
    this.positions_to_check = positions_to_check;
  }
  EstimateAffinityMinPvalue(ScoringModel pwm, Sequence sequence, CanFindPvalue pvalueCalculator) {
    this(pwm, sequence, pvalueCalculator, sequence.subsequence_positions(pwm.length()));
  }

  ScanSequence scanSequence() {
    return new ScanSequence(sequence, pwm, positions_to_check);
  }

  @Override
  public double affinity() throws HashOverflowException {
    double score = scanSequence().best_score();
    return pvalueCalculator.pvalueByThreshold(score).pvalue;
  }
  public Position bestPosition() {
    return scanSequence().best_position();
  }
}