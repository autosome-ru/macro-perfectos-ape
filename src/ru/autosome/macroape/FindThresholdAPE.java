package ru.autosome.macroape;

import java.util.ArrayList;

public class FindThresholdAPE {
  FindThresholdAPEParameters parameters;

  public FindThresholdAPE(FindThresholdAPEParameters parameters) {
    this.parameters = parameters;
  }

  PWM upscaled_pwm() {
    return parameters.pwm.discrete(parameters.discretization);
  }

  CountingPWM countingPWM(PWM pwm) {
    CountingPWM result = new CountingPWM(pwm, parameters.background);
    result.max_hash_size = parameters.max_hash_size;
    return result;
  }

  public ArrayList<ThresholdInfo> threshold_infos(PWM pwm) {
    if (parameters.pvalue_boundary.equals("lower")) {
      return countingPWM(pwm).thresholds(parameters.pvalues);
    } else {
      return countingPWM(pwm).weak_thresholds(parameters.pvalues);
    }
  }

  public ArrayList<ThresholdInfo> downscale_thresholds(ArrayList<ThresholdInfo> threshold_infos) {
    ArrayList<ThresholdInfo> downscaled_infos = new ArrayList<ThresholdInfo>();
    for (ThresholdInfo info : threshold_infos) {
      downscaled_infos.add(info.downscale(parameters.discretization));
    }
    return downscaled_infos;
  }

  public ArrayList<ThresholdInfo> find_thresholds_by_pvalues() {
    return downscale_thresholds(threshold_infos(upscaled_pwm()));
  }
}
