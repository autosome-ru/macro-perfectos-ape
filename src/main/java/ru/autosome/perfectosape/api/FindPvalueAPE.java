package ru.autosome.perfectosape.api;


import ru.autosome.perfectosape.Discretizer;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.motifModels.PWM;

import static ru.autosome.perfectosape.calculations.findPvalue.FindPvalueAPE.PvalueInfo;

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

  private final Parameters parameters;

  public FindPvalueAPE(Parameters parameters) {
    super();
    this.parameters = parameters;
  }

  @Override
  public PvalueInfo[] launchSingleTask() throws HashOverflowException {
    ru.autosome.perfectosape.calculations.findPvalue.FindPvalueAPE calculator =
     new ru.autosome.perfectosape.calculations.findPvalue.FindPvalueAPE<PWM, BackgroundModel>(parameters.pwm,
                                                                                              parameters.background,
                                                                                              parameters.discretizer,
                                                                                              parameters.max_hash_size);
    return calculator.pvaluesByThresholds(parameters.thresholds);
  }
}

