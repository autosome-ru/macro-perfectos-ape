package ru.autosome.macroape;

class EstimateAffinityMinPvalue implements EstimateAffinity {
  PWM pwm;
  Sequence sequence;
  CanFindPvalue pvalueCalculator;
  EstimateAffinityMinPvalue(PWM pwm, Sequence sequence, CanFindPvalue pvalueCalculator) {
    this.pwm = pwm;
    this.sequence = sequence;
    this.pvalueCalculator = pvalueCalculator;
  }

  public double affinity() {
    ScanSequence scanSequence = new ScanSequence(sequence, pwm);
    double score = scanSequence.best_score_on_sequence();
    return pvalueCalculator.pvalue_by_threshold(score).pvalue;
  }
}