package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.BackgroundModel;
import ru.autosome.perfectosape.PWM;
import ru.autosome.perfectosape.PvalueBsearchList;

import java.util.ArrayList;

public class PrecalculateThresholdList {
  double discretization;
  BackgroundModel background;
  String pvalue_boundary;
  Integer max_hash_size;
  double[] pvalues;
  public PrecalculateThresholdList(double[] pvalues, double discretization, BackgroundModel background, String pvalue_boundary, Integer max_hash_size) {
    this.pvalues = pvalues;
    this.discretization = discretization;
    this.background = background;
    this.pvalue_boundary = pvalue_boundary;
    this.max_hash_size = max_hash_size;
  }

  private ru.autosome.perfectosape.calculations.FindThresholdAPE find_threshold_calculator(PWM pwm) {
    return new ru.autosome.perfectosape.calculations.FindThresholdAPE(pwm,
                                                                  background,
                                                                  discretization,
                                                                  pvalue_boundary,
                                                                  max_hash_size);
  }

  public PvalueBsearchList bsearch_list_for_pwm(PWM pwm) {
    ArrayList<PvalueBsearchList.ThresholdPvaluePair> pairs = new ArrayList<PvalueBsearchList.ThresholdPvaluePair>();
    for (CountingPWM.ThresholdInfo info : find_threshold_calculator(pwm).find_thresholds_by_pvalues(pvalues)) {
      pairs.add(new PvalueBsearchList.ThresholdPvaluePair(info));
    }
    return new PvalueBsearchList(pairs);
  }


}
