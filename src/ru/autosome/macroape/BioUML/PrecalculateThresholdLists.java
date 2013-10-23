package ru.autosome.macroape.BioUML;

import ru.autosome.macroape.BackgroundModel;
import ru.autosome.macroape.Calculations.*;
import ru.autosome.macroape.PWM;

import java.util.HashMap;
import java.util.Map;

public class PrecalculateThresholdLists extends Task<Map<PWM, CanFindPvalue>> {
  public static class Parameters {
    public double discretization;
    public BackgroundModel background;
    public String pvalue_boundary;
    public Integer max_hash_size;
    public double[] pvalues;
    public PWM[] pwmCollection;

    public Parameters() {}
    public Parameters(PWM[] pwmCollection, double[] pvalues, double discretization, BackgroundModel background, String pvalue_boundary, Integer max_hash_size) {
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

  Integer getTotalTicks() {
    return parameters.pwmCollection.length;
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
        results.put(pwm, new ru.autosome.macroape.Calculations.FindPvalueBsearch(pwm,
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

  ru.autosome.macroape.Calculations.PrecalculateThresholdList calculator() {
    return new ru.autosome.macroape.Calculations.PrecalculateThresholdList(parameters.pvalues,
                                                                           parameters.discretization,
                                                                           parameters.background,
                                                                           parameters.pvalue_boundary,
                                                                           parameters.max_hash_size);
  }

}
