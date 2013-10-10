package ru.autosome.macroape;

import java.util.ArrayList;

class EstimateAffinityMinPvalue implements EstimateAffinity {
  PWM pwm;
  Sequence sequence;
  CanFindPvalue pvalueCalculator;
  ArrayList<Position> positions_to_check;
  EstimateAffinityMinPvalue(PWM pwm, Sequence sequence, CanFindPvalue pvalueCalculator, ArrayList<Position> positions_to_check) {
    this.pwm = pwm;
    this.sequence = sequence;
    this.pvalueCalculator = pvalueCalculator;
    this.positions_to_check = positions_to_check;
  }
  EstimateAffinityMinPvalue(PWM pwm, Sequence sequence, CanFindPvalue pvalueCalculator) {
    this(pwm, sequence, pvalueCalculator, sequence.subsequence_positions(pwm.length()));
  }

  ScanSequence scanSequence() {
    return new ScanSequence(sequence, pwm, positions_to_check);
  }

  public double affinity() {
    double score = scanSequence().best_score_on_sequence();
    return pvalueCalculator.pvalue_by_threshold(score).pvalue;
  }
  public Position bestPosition() {
    return scanSequence().best_score_full_position();
  }
}