package ru.autosome.macroape.model;

public class ThresholdEvaluator<ModelType> {
  public final ModelType pwm;
  public final String name;
  public final SingleThresholdEvaluator<ModelType> rough;
  public final SingleThresholdEvaluator<ModelType> precise;
//  List<SingleThresholdEvaluator<ModelType>> consequentEvaluators;

  public ThresholdEvaluator(ModelType pwm, String name,
                            SingleThresholdEvaluator<ModelType> rough,
                            SingleThresholdEvaluator<ModelType> precise) {
    this.pwm = pwm;
    this.name = name;
    this.rough = rough;
    this.precise = precise;
//    consequentEvaluators = new ArrayList<>();
//    consequentEvaluators.add(this.rough);
//    consequentEvaluators.add(this.precise);
  }
}
