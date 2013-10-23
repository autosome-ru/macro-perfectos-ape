package ru.autosome.macroape.Calculations;

import ru.autosome.macroape.BackgroundModel;
import ru.autosome.macroape.PWM;

import java.util.ArrayList;

public class FindThresholdAPE {
  BackgroundModel background;
  Double discretization; // if discretization is null - it's not applied
  String pvalue_boundary;
  Integer max_hash_size; // if max_hash_size is null - it's not applied
  PWM pwm;

  public FindThresholdAPE(PWM pwm, BackgroundModel background,
                    Double discretization, String pvalue_boundary, Integer max_hash_size) {
    this.pwm = pwm;
    this.background = background;
    this.discretization = discretization;
    this.pvalue_boundary = pvalue_boundary;
    this.max_hash_size = max_hash_size;
  }

  PWM upscaled_pwm() {
    return pwm.discrete(discretization);
  }

  CountingPWM countingPWM(PWM pwm) {
    return new CountingPWM(pwm, background, max_hash_size);
  }

  public CountingPWM.ThresholdInfo[] threshold_infos(PWM pwm, double[] pvalues) {
    if (pvalue_boundary.equals("lower")) {
      return countingPWM(pwm).thresholds(pvalues);
    } else {
      return countingPWM(pwm).weak_thresholds(pvalues);
    }
  }

  public CountingPWM.ThresholdInfo[] downscale_thresholds(CountingPWM.ThresholdInfo[] threshold_infos) {
    CountingPWM.ThresholdInfo[] downscaled_infos = new CountingPWM.ThresholdInfo[threshold_infos.length];
    for (int i = 0; i < threshold_infos.length; ++i) {
      downscaled_infos[i] = threshold_infos[i].downscale(discretization);
    }
    return downscaled_infos;
  }

  public CountingPWM.ThresholdInfo[] find_thresholds_by_pvalues(double[] pvalues) {
    return downscale_thresholds(threshold_infos(upscaled_pwm(), pvalues));
  }

  public CountingPWM.ThresholdInfo find_threshold_by_pvalue(double pvalue) {
    return find_thresholds_by_pvalues( new double[]{pvalue} )[0];
  }
}
