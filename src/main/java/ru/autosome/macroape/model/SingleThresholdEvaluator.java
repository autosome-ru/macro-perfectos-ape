package ru.autosome.macroape.model;

import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;

public class SingleThresholdEvaluator<ModelType> {
  public final ModelType pwm;
  public final CanFindThreshold thresholdCalculator;

  public SingleThresholdEvaluator(ModelType pwm, CanFindThreshold thresholdCalculator) {
    this.pwm = pwm;
    this.thresholdCalculator = thresholdCalculator;
  }
}
