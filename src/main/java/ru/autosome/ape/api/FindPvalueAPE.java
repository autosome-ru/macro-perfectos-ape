package ru.autosome.ape.api;


import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.commons.api.SingleTask;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.mono.PWM;

import static ru.autosome.ape.calculation.findPvalue.FindPvalueAPE.PvalueInfo;

public class FindPvalueAPE extends SingleTask<PvalueInfo[]> {
  public static class Parameters {
    public PWM pwm;
    public Discretizer discretizer;
    public BackgroundModel background;
    public Integer max_hash_size;
    double[] thresholds;

    public Parameters() { }
    public Parameters(PWM pwm, double[] thresholds, Discretizer discretizer, BackgroundModel background, Integer max_hash_size) {
      this.pwm = pwm;
      this.thresholds = thresholds;
      this.discretizer = discretizer;
      this.background = background;
      this.max_hash_size = max_hash_size;
    }
  }

  Parameters parameters;

  public FindPvalueAPE(Parameters parameters) {
    super();
    this.parameters = parameters;
  }

  @Override
  public PvalueInfo[] launchSingleTask() throws HashOverflowException {
    ru.autosome.ape.calculation.findPvalue.FindPvalueAPE calculator =
     new ru.autosome.ape.calculation.findPvalue.FindPvalueAPE<PWM, BackgroundModel>(parameters.pwm,
                                                                                              parameters.background,
                                                                                              parameters.discretizer,
                                                                                              parameters.max_hash_size);
    return calculator.pvaluesByThresholds(parameters.thresholds);
  }
}

