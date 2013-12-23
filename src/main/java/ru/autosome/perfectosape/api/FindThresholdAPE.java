package ru.autosome.perfectosape.api;

import ru.autosome.perfectosape.BackgroundModel;
import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.PWM;
import ru.autosome.perfectosape.calculations.CanFindThreshold;

public class FindThresholdAPE extends SingleTask<CanFindThreshold.ThresholdInfo[]> {
  public static class Parameters {
    public BackgroundModel background;
    public Double discretization; // if discretization is null - it's not applied
    public BoundaryType pvalue_boundary;
    public Integer max_hash_size; // if max_hash_size is null - it's not applied
    public PWM pwm;
    public double[] pvalues;

    public Parameters() { }
    public Parameters(PWM pwm, double[] pvalues, BackgroundModel background,
                      Double discretization, BoundaryType pvalue_boundary, Integer max_hash_size) {
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

  public CanFindThreshold.ThresholdInfo[] launchSingleTask() {
    return new ru.autosome.perfectosape.calculations.FindThresholdAPE(parameters.pwm,
                                                                  parameters.background,
                                                                  parameters.discretization,
                                                                  parameters.pvalue_boundary,
                                                                  parameters.max_hash_size)
            .find_thresholds_by_pvalues(parameters.pvalues);
  }
}
