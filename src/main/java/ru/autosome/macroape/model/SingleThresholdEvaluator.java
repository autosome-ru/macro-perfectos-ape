package ru.autosome.macroape.model;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;

public class SingleThresholdEvaluator<ModelType> {
  public final ModelType pwm;
  public final CanFindThreshold thresholdCalculator;
  public final CanFindPvalue pvalueCalculator;

  public SingleThresholdEvaluator(ModelType pwm,
                                  CanFindThreshold thresholdCalculator,
                                  CanFindPvalue pvalueCalculator) {
    this.pwm = pwm;
    this.thresholdCalculator = thresholdCalculator;
    this.pvalueCalculator = pvalueCalculator;
  }
}
