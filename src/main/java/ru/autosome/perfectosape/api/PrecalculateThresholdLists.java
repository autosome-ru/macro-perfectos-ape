package ru.autosome.perfectosape.api;

import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.Discretizer;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.calculations.PrecalculateThresholdList;
import ru.autosome.perfectosape.calculations.findPvalue.CanFindPvalue;
import ru.autosome.perfectosape.motifModels.PWM;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrecalculateThresholdLists extends Task<Map<PWM, CanFindPvalue>> {
  public static class Parameters {
    public Discretizer discretizer;
    public BackgroundModel background;
    public BoundaryType pvalue_boundary;
    public Integer max_hash_size;
    public double[] pvalues;
    public List<PWM> pwmCollection;

    public Parameters() {}
    public Parameters(List<PWM> pwmCollection, double[] pvalues, Discretizer discretizer, BackgroundModel background, BoundaryType pvalue_boundary, Integer max_hash_size) {
      this.pwmCollection = pwmCollection;
      this.pvalues = pvalues;
      this.discretizer = discretizer;
      this.background = background;
      this.pvalue_boundary = pvalue_boundary;
      this.max_hash_size = max_hash_size;
    }
  }
  private final Parameters parameters;
  public PrecalculateThresholdLists(Parameters parameters) {
    super();
    this.parameters = parameters;
  }

  public Integer getTotalTicks() {
    return parameters.pwmCollection.size();
  }


  public Map<PWM, CanFindPvalue> call() {
    Map<PWM, CanFindPvalue> results;
    setStatus(Status.RUNNING);
    try {
      results = new HashMap<PWM, CanFindPvalue>();
      for (PWM pwm: parameters.pwmCollection) {
        if (interrupted()) {
          return results;
        }
        results.put(pwm, new ru.autosome.perfectosape.calculations.findPvalue.FindPvalueBsearch(calculator().bsearch_list_for_pwm(pwm)) );
        tick();
      }
    } catch (Exception err) {
      setStatus(Status.FAIL);
      return null;
    }
    setStatus(Status.SUCCESS);
    return results;
  }

  PrecalculateThresholdList<PWM, BackgroundModel> calculator() {
    return new PrecalculateThresholdList<PWM, BackgroundModel>( parameters.pvalues,
                                                                parameters.discretizer,
                                                                parameters.background,
                                                                parameters.pvalue_boundary,
                                                                parameters.max_hash_size);
  }

}
