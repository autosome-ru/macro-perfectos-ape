package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.BackgroundModel;
import ru.autosome.perfectosape.PWM;
import ru.autosome.perfectosape.PvalueBsearchList;

public class FindThresholdBsearch implements CanFindThreshold {
  PWM pwm;
  BackgroundModel background;
  PvalueBsearchList bsearchList;

  public FindThresholdBsearch(PWM pwm, BackgroundModel background, PvalueBsearchList bsearchList) {
    this.pwm = pwm;
    this.background = background;
    this.bsearchList = bsearchList;
  }

  @Override
  public ThresholdInfo[] find_thresholds_by_pvalues(double[] pvalues) {
    ThresholdInfo[] result = new ThresholdInfo[pvalues.length];
    for (int i = 0; i < pvalues.length; ++i) {
      result[i] = find_threshold_by_pvalue(pvalues[i]);
    }
    return result;
  }

  @Override
  public ThresholdInfo find_threshold_by_pvalue(double pvalue) {
    double threshold = bsearchList.threshold_by_pvalue(pvalue);
    double real_pvalue = pvalue; // approximation
    return new ThresholdInfo(threshold,
                                         real_pvalue,
                                         pvalue,
                                         pvalue * Math.pow(background.volume(), pwm.length()));
  }
}
