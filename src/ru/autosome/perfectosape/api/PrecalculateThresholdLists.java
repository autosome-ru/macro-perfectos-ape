package ru.autosome.perfectosape.api;

import ru.autosome.perfectosape.BackgroundModel;
import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.PWM;
import ru.autosome.perfectosape.calculations.CanFindPvalue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrecalculateThresholdLists extends Task<Map<PWM, CanFindPvalue>> {
  public static class Parameters {
    public double discretization;
    public BackgroundModel background;
    public BoundaryType pvalue_boundary;
    public Integer max_hash_size;
    public double[] pvalues;
    public List<PWM> pwmCollection;

    public Parameters() {}
    public Parameters(List<PWM> pwmCollection, double[] pvalues, double discretization, BackgroundModel background, BoundaryType pvalue_boundary, Integer max_hash_size) {
      this.pwmCollection = pwmCollection;
      this.pvalues = pvalues;
      this.discretization = discretization;
      this.background = background;
      this.pvalue_boundary = pvalue_boundary;
      this.max_hash_size = max_hash_size;
    }
  }
  Parameters parameters;
  public PrecalculateThresholdLists(Parameters parameters) {
    super();
    this.parameters = parameters;
  }

  public Integer getTotalTicks() {
    return parameters.pwmCollection.size();
  }


  public Map<PWM, CanFindPvalue> launch() {
    Map<PWM, CanFindPvalue> results;
    setStatus(Status.RUNNING);
    try {
      results = new HashMap<PWM, CanFindPvalue>();
      for (PWM pwm: parameters.pwmCollection) {
        if (interrupted()) {
          return results;
        }
        results.put(pwm, new ru.autosome.perfectosape.calculations.FindPvalueBsearch(pwm,
                                                                                 parameters.background,
                                                                                 calculator().bsearch_list_for_pwm(pwm)) );
        tick();
      }
    } catch (Exception err) {
      setStatus(Status.FAIL);
      return null;
    }
    setStatus(Status.SUCCESS);
    return results;
  }

  ru.autosome.perfectosape.calculations.PrecalculateThresholdList calculator() {
    return new ru.autosome.perfectosape.calculations.PrecalculateThresholdList(parameters.pvalues,
                                                                           parameters.discretization,
                                                                           parameters.background,
                                                                           parameters.pvalue_boundary,
                                                                           parameters.max_hash_size);
  }

}
