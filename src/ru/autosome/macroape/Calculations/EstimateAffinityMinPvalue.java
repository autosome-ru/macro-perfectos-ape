package ru.autosome.macroape.Calculations;

import ru.autosome.macroape.PWM;
import ru.autosome.macroape.Position;
import ru.autosome.macroape.Sequence;

import java.util.ArrayList;

public class EstimateAffinityMinPvalue implements EstimateAffinity {
  PWM pwm;
  Sequence sequence;
  CanFindPvalue pvalueCalculator;
  ArrayList<Position> positions_to_check;
  public EstimateAffinityMinPvalue(PWM pwm, Sequence sequence, CanFindPvalue pvalueCalculator, ArrayList<Position> positions_to_check) {
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
    double score = scanSequence().best_score();
    return pvalueCalculator.pvalue_by_threshold(score).pvalue;
  }
  public Position bestPosition() {
    return scanSequence().best_position();
  }
}