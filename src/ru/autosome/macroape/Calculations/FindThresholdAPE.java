package ru.autosome.macroape.Calculations;

import ru.autosome.macroape.BackgroundModel;
import ru.autosome.macroape.CountingPWM;
import ru.autosome.macroape.PWM;

import java.util.ArrayList;

public class FindThresholdAPE {
  public static class Parameters {
    public BackgroundModel background;
    public Double discretization; // if discretization is null - it's not applied
    public String pvalue_boundary;
    public Integer max_hash_size; // if max_hash_size is null - it's not applied
    public PWM pwm;

    public Parameters() { }
    public Parameters(PWM pwm, BackgroundModel background,
                      Double discretization, String pvalue_boundary, Integer max_hash_size) {
      this.pwm = pwm;
      this.background = background;
      this.discretization = discretization;
      this.pvalue_boundary = pvalue_boundary;
      this.max_hash_size = max_hash_size;
    }
  }

  Parameters parameters;

  public FindThresholdAPE(Parameters parameters) {
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

  public ArrayList<CountingPWM.ThresholdInfo> threshold_infos(PWM pwm, double[] pvalues) {
    if (parameters.pvalue_boundary.equals("lower")) {
      return countingPWM(pwm).thresholds(pvalues);
    } else {
      return countingPWM(pwm).weak_thresholds(pvalues);
    }
  }

  public ArrayList<CountingPWM.ThresholdInfo> downscale_thresholds(ArrayList<CountingPWM.ThresholdInfo> threshold_infos) {
    ArrayList<CountingPWM.ThresholdInfo> downscaled_infos = new ArrayList<CountingPWM.ThresholdInfo>();
    for (CountingPWM.ThresholdInfo info : threshold_infos) {
      downscaled_infos.add(info.downscale(parameters.discretization));
    }
    return downscaled_infos;
  }

  public ArrayList<CountingPWM.ThresholdInfo> find_thresholds_by_pvalues(double[] pvalues) {
    return downscale_thresholds(threshold_infos(upscaled_pwm(), pvalues));
  }
}
