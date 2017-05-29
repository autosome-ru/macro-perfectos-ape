package ru.autosome.macroape.model;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;

public class SingleThresholdEvaluator<ModelType> {
  public final ModelType pwm;
  public final String name;
  public final CanFindThreshold thresholdCalculator;
  public final CanFindPvalue pvalueCalculator;

  public SingleThresholdEvaluator(ModelType pwm, String name,
                                  CanFindThreshold thresholdCalculator,
                                  CanFindPvalue pvalueCalculator) {
    this.pwm = pwm;
    this.name = name;
    this.thresholdCalculator = thresholdCalculator;
    this.pvalueCalculator = pvalueCalculator;
  }
}
