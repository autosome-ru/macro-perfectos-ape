package ru.autosome.macroape.model;

import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;

public class ThresholdEvaluator<ModelType> {
  public final String name;
  public final ModelType pwm;
  public final CanFindThreshold rough;
  public final CanFindThreshold precise;
//  List<CanFindThreshold> consequentEvaluators;

  public ThresholdEvaluator(String name,
                            ModelType pwm,
                            CanFindThreshold rough,
                            CanFindThreshold precise) {
    this.name = name;
    this.pwm = pwm;
    this.rough = rough;
    this.precise = precise;
//    consequentEvaluators = new ArrayList<>();
//    consequentEvaluators.add(this.rough);
//    consequentEvaluators.add(this.precise);
  }
}
