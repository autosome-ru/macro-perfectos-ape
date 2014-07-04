package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.Position;
import ru.autosome.perfectosape.Sequence;
import ru.autosome.perfectosape.calculations.findPvalue.CanFindPvalue;
import ru.autosome.perfectosape.motifModels.ScoringModel;

import java.util.ArrayList;

public class EstimateAffinityMinPvalue implements EstimateAffinity {
  private final ScoringModel pwm;
  private final Sequence sequence;
  private final CanFindPvalue pvalueCalculator;
  private final ArrayList<Position> positions_to_check;
  public EstimateAffinityMinPvalue(ScoringModel pwm, Sequence sequence, CanFindPvalue pvalueCalculator, ArrayList<Position> positions_to_check) {
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