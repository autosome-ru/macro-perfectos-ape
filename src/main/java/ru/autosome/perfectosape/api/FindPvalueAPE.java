package ru.autosome.perfectosape.api;


import ru.autosome.perfectosape.BackgroundModel;
import ru.autosome.perfectosape.PWM;

import static ru.autosome.perfectosape.calculations.FindPvalueAPE.PvalueInfo;

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

  public PvalueInfo[] launchSingleTask() {
    ru.autosome.perfectosape.calculations.FindPvalueAPE calculator =
     new ru.autosome.perfectosape.calculations.FindPvalueAPE(parameters.pwm,
                                                         parameters.discretization,
                                                         parameters.background,
                                                         parameters.max_hash_size);
    return calculator.pvalues_by_thresholds(parameters.thresholds);
  }
}

