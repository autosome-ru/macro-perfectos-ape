package ru.autosome.perfectosape.api;

import ru.autosome.perfectosape.BackgroundModel;
import ru.autosome.perfectosape.PWM;
import ru.autosome.perfectosape.PvalueBsearchList;

public class PrecalculateThresholdList extends SingleTask<PvalueBsearchList> {
  public static class Parameters {
    public double discretization;
    public BackgroundModel background;
    public String pvalue_boundary;
    public Integer max_hash_size;
    public double[] pvalues;
    public PWM pwm;

    public Parameters() {}
    public Parameters(PWM pwm, double[] pvalues, double discretization, BackgroundModel background, String pvalue_boundary, Integer max_hash_size) {
      this.pwm = pwm;
      this.pvalues = pvalues;
      this.discretization = discretization;
      this.background = background;
      this.pvalue_boundary = pvalue_boundary;
      this.max_hash_size = max_hash_size;
    }
  }
  Parameters parameters;
  public PrecalculateThresholdList(Parameters parameters) {
    super();
    this.parameters = parameters;
  }

  ru.autosome.perfectosape.calculations.PrecalculateThresholdList calculator() {
    return new ru.autosome.perfectosape.calculations.PrecalculateThresholdList(parameters.pvalues,
                                                                           parameters.discretization,
                                                                           parameters.background,
                                                                           parameters.pvalue_boundary,
                                                                           parameters.max_hash_size);
  }


  public PvalueBsearchList launchSingleTask() {
    return calculator().bsearch_list_for_pwm(parameters.pwm);
  }

}