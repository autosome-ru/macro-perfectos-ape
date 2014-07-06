package ru.autosome.ape.api;


import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.commons.api.SingleTask;

import static ru.autosome.ape.calculation.findPvalue.FindPvalueAPE.PvalueInfo;

public class FindPvalueAPE extends SingleTask<PvalueInfo[]> {
  public static class Parameters {
    public PWM pwm;
    public Double discretization;
    public BackgroundModel background;
    public Integer max_hash_size;
    double[] thresholds;

    public Parameters() { }
    public Parameters(PWM pwm, double[] thresholds, Double discretization, BackgroundModel background, Integer max_hash_size) {
      this.pwm = pwm;
      this.thresholds = thresholds;
      this.discretization = discretization;
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
                                                                                              parameters.discretization,
                                                                                              parameters.max_hash_size);
    return calculator.pvaluesByThresholds(parameters.thresholds);
  }
}

