package ru.autosome.ape.api;

import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.commons.api.SingleTask;
import ru.autosome.commons.backgroundModel.mono.BackgroundModel;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.mono.PWM;

public class FindThresholdAPE extends SingleTask<CanFindThreshold.ThresholdInfo[]> {
  public static class Parameters {
    public BackgroundModel background;
    public Discretizer discretizer;
    public BoundaryType pvalue_boundary;
    public Integer max_hash_size; // if max_hash_size is null - it's not applied
    public PWM pwm;
    public double[] pvalues;

    public Parameters() { }
    public Parameters(PWM pwm, double[] pvalues, BackgroundModel background,
                      Discretizer discretizer, BoundaryType pvalue_boundary, Integer max_hash_size) {
      this.pwm = pwm;
      this.pvalues = pvalues;
      this.background = background;
      this.discretizer = discretizer;
      this.pvalue_boundary = pvalue_boundary;
      this.max_hash_size = max_hash_size;
    }
  }

  Parameters parameters;
  public FindThresholdAPE(Parameters parameters) {
    super();
    this.parameters = parameters;
  }

  @Override
  public CanFindThreshold.ThresholdInfo[] launchSingleTask() throws HashOverflowException {
    return new ru.autosome.ape.calculation.findThreshold.FindThresholdAPE<PWM, BackgroundModel>(parameters.pwm,
                                                                                                parameters.background,
                                                                                                parameters.discretizer,
                                                                                                parameters.max_hash_size)
     .thresholdsByPvalues(parameters.pvalues, parameters.pvalue_boundary);
  }
}
