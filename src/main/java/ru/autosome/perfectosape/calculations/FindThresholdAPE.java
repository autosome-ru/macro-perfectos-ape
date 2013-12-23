package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.BackgroundModel;
import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.PWM;

public class FindThresholdAPE implements CanFindThreshold {
  BackgroundModel background;
  Double discretization; // if discretization is null - it's not applied
  BoundaryType pvalue_boundary;
  Integer maxHashSize; // if maxHashSize is null - it's not applied
  PWM pwm;

  public FindThresholdAPE(PWM pwm, BackgroundModel background,
                    Double discretization, BoundaryType pvalue_boundary, Integer max_hash_size) {
    this.pwm = pwm;
    this.background = background;
    this.discretization = discretization;
    this.pvalue_boundary = pvalue_boundary;
    this.maxHashSize = max_hash_size;
  }

  PWM upscaled_pwm() {
    return pwm.discrete(discretization);
  }

  CountingPWM countingPWM(PWM pwm) {
    return new CountingPWM(pwm, background, maxHashSize);
  }

  public ThresholdInfo[] threshold_infos(PWM pwm, double[] pvalues) {
    if (pvalue_boundary == BoundaryType.LOWER) {
      return countingPWM(pwm).thresholds(pvalues);
    } else {
      return countingPWM(pwm).weak_thresholds(pvalues);
    }
  }

  public ThresholdInfo[] downscale_thresholds(ThresholdInfo[] threshold_infos) {
    ThresholdInfo[] downscaled_infos = new ThresholdInfo[threshold_infos.length];
    for (int i = 0; i < threshold_infos.length; ++i) {
      downscaled_infos[i] = threshold_infos[i].downscale(discretization);
    }
    return downscaled_infos;
  }

  @Override
  public ThresholdInfo[] find_thresholds_by_pvalues(double[] pvalues) {
    return downscale_thresholds(threshold_infos(upscaled_pwm(), pvalues));
  }

  @Override
  public ThresholdInfo find_threshold_by_pvalue(double pvalue) {
    return find_thresholds_by_pvalues( new double[]{pvalue} )[0];
  }
}
