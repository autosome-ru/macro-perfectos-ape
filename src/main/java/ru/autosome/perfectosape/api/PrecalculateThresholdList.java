package ru.autosome.perfectosape.api;

import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.PvalueBsearchList;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.motifModels.PWM;

public class PrecalculateThresholdList extends SingleTask<PvalueBsearchList> {
  public static class Parameters {
    public double discretization;
    public BackgroundModel background;
    public BoundaryType pvalue_boundary;
    public Integer max_hash_size;
    public double[] pvalues;
    public PWM pwm;

    public Parameters() {}
    public Parameters(PWM pwm, double[] pvalues, double discretization, BackgroundModel background, BoundaryType pvalue_boundary, Integer max_hash_size) {
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


  @Override
  public PvalueBsearchList launchSingleTask() {
    try {
      return calculator().bsearch_list_for_pwm(parameters.pwm);
    } catch (HashOverflowException e) {
      e.printStackTrace();
      return null;
    }
  }

}