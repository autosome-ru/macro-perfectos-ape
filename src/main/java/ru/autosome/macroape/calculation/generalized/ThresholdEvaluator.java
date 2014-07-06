package ru.autosome.macroape.calculation.generalized;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;

public class ThresholdEvaluator <ModelType> {
  public final ModelType pwm;
  public final CanFindThreshold roughThresholdCalculator;
  public final CanFindThreshold preciseThresholdCalculator;

  public final CanFindPvalue roughPvalueCalculator;
  public final CanFindPvalue precisePvalueCalculator;

  public ThresholdEvaluator(ModelType pwm,
                            CanFindThreshold roughThresholdCalculator, CanFindThreshold preciseThresholdCalculator,
                            CanFindPvalue roughPvalueCalculator, CanFindPvalue precisePvalueCalculator) {
    this.pwm = pwm;
    this.roughThresholdCalculator = roughThresholdCalculator;
    this.preciseThresholdCalculator = preciseThresholdCalculator;
    this.roughPvalueCalculator = roughPvalueCalculator;
    this.precisePvalueCalculator = precisePvalueCalculator;
  }
}
