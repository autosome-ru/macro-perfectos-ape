package ru.autosome.perfectosape.model;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.scoringModel.SequenceScoringModel;

public class ThresholdEvaluator<SequenceType,
                                ModelType extends SequenceScoringModel<SequenceType>> {
  public final ModelType pwm;
  public final CanFindPvalue pvalueCalculator;
  public final String name;

  public ThresholdEvaluator(ModelType pwm, CanFindPvalue pvalueCalculator, String name) {
    this.pwm = pwm;
    this.pvalueCalculator = pvalueCalculator;
    this.name = name;
  }
}
