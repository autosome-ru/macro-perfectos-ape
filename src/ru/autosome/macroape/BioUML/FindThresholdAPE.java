package ru.autosome.macroape.BioUML;

import ru.autosome.macroape.BackgroundModel;
import ru.autosome.macroape.Calculations.CountingPWM;
import ru.autosome.macroape.PWM;

public class FindThresholdAPE extends SingleTask<CountingPWM.ThresholdInfo[]> {
  public static class Parameters {
    public BackgroundModel background;
    public Double discretization; // if discretization is null - it's not applied
    public String pvalue_boundary;
    public Integer max_hash_size; // if max_hash_size is null - it's not applied
    public PWM pwm;
    public double[] pvalues;

    public Parameters() { }
    public Parameters(PWM pwm, double[] pvalues, BackgroundModel background,
                      Double discretization, String pvalue_boundary, Integer max_hash_size) {
      this.pwm = pwm;
      this.pvalues = pvalues;
      this.background = background;
      this.discretization = discretization;
      this.pvalue_boundary = pvalue_boundary;
      this.max_hash_size = max_hash_size;
    }
  }

  Parameters parameters;
  public FindThresholdAPE(Parameters parameters) {
    super();
    this.parameters = parameters;
  }

  public CountingPWM.ThresholdInfo[] launchSingleTask() {
    return new ru.autosome.macroape.Calculations.FindThresholdAPE(parameters.pwm,
                                                                  parameters.background,
                                                                  parameters.discretization,
                                                                  parameters.pvalue_boundary,
                                                                  parameters.max_hash_size)
            .find_thresholds_by_pvalues(parameters.pvalues);
  }
}
