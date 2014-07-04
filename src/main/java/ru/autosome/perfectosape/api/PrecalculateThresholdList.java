package ru.autosome.perfectosape.api;

import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.Discretizer;
import ru.autosome.perfectosape.PvalueBsearchList;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.motifModels.PWM;

class PrecalculateThresholdList extends SingleTask<PvalueBsearchList> {
  public static class Parameters {
    public Discretizer discretizer;
    public BackgroundModel background;
    public BoundaryType pvalue_boundary;
    public Integer max_hash_size;
    public double[] pvalues;
    public PWM pwm;

    public Parameters() {}
    public Parameters(PWM pwm, double[] pvalues, Discretizer discretizer, BackgroundModel background, BoundaryType pvalue_boundary, Integer max_hash_size) {
      this.pwm = pwm;
      this.pvalues = pvalues;
      this.discretizer = discretizer;
      this.background = background;
      this.pvalue_boundary = pvalue_boundary;
      this.max_hash_size = max_hash_size;
    }
  }
  private final Parameters parameters;
  public PrecalculateThresholdList(Parameters parameters) {
    super();
    this.parameters = parameters;
  }

  ru.autosome.perfectosape.calculations.PrecalculateThresholdList<PWM, BackgroundModel> calculator() {
    return new ru.autosome.perfectosape.calculations.PrecalculateThresholdList<PWM, BackgroundModel>(parameters.pvalues,
                                                                          parameters.discretizer,
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