package ru.autosome.macroape.model;

import ru.autosome.macroape.cli.generalized.ScanCollection;

public class ThresholdEvaluator<ModelType> {
  public final ModelType pwm;
  public final String name;
  public final ScanCollection.SingleThresholdEvaluator rough;
  public final ScanCollection.SingleThresholdEvaluator precise;
  //    List<SingleThresholdEvaluator> consequentEvaluators;

  public ThresholdEvaluator(ModelType pwm, String name,
                            ScanCollection.SingleThresholdEvaluator rough,
                            ScanCollection.SingleThresholdEvaluator precise) {
    this.pwm = pwm;
    this.name = name;
    this.rough = rough;
    this.precise = precise;
//      consequentEvaluators = new ArrayList<SingleThresholdEvaluator>();
//      consequentEvaluators.add(this.rough);
//      consequentEvaluators.add(this.precise);
  }
}
