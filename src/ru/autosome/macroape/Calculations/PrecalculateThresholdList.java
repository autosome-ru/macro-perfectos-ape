package ru.autosome.macroape.Calculations;

import ru.autosome.macroape.BackgroundModel;
import ru.autosome.macroape.PWM;
import ru.autosome.macroape.PvalueBsearchList;

import java.util.ArrayList;

public class PrecalculateThresholdList {
  double discretization;
  BackgroundModel background;
  String pvalue_boundary;
  int max_hash_size;
  double[] pvalues;
  public PrecalculateThresholdList(double[] pvalues, double discretization, BackgroundModel background, String pvalue_boundary, int max_hash_size) {
    this.pvalues = pvalues;
    this.discretization = discretization;
    this.background = background;
    this.pvalue_boundary = pvalue_boundary;
    this.max_hash_size = max_hash_size;
  }

  private ru.autosome.macroape.Calculations.FindThresholdAPE find_threshold_calculator(PWM pwm) {
    return new ru.autosome.macroape.Calculations.FindThresholdAPE(pwm,
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
