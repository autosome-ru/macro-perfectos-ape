package ru.autosome.perfectosape.calculations.findThreshold;

import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.calculations.CountingPWM;
import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.motifModels.PWM;

public class FindThresholdAPE implements CanFindThreshold {
  public static class Builder implements CanFindThreshold.Builder<PWM> {
    Double discretization;
    BackgroundModel background;
    Integer maxHashSize;
    PWM pwm;

    public Builder(BackgroundModel background, Double discretization, Integer maxHashSize) {
      this.background = background;
      this.discretization = discretization;
      this.maxHashSize = maxHashSize;
    }

    @Override
    public CanFindThreshold.Builder applyMotif(PWM pwm) {
      this.pwm = pwm;
      return this;
    }

    @Override
    public CanFindThreshold build() {
      if (pwm != null) {
        return new FindThresholdAPE(pwm, background, discretization, maxHashSize);
      } else {
        return null;
      }
    }
  }

  BackgroundModel background;
  Double discretization; // if discretization is null - it's not applied
  Integer maxHashSize; // if maxHashSize is null - it's not applied
  PWM pwm;

  public FindThresholdAPE(PWM pwm, BackgroundModel background,
                    Double discretization, Integer max_hash_size) {
    this.pwm = pwm;
    this.background = background;
    this.discretization = discretization;
    this.maxHashSize = max_hash_size;
  }

  CountingPWM countingPWM(PWM pwm) {
    return new CountingPWM(pwm, background, maxHashSize);
  }

  @Override
  public ThresholdInfo weakThresholdByPvalue(double pvalue) throws HashOverflowException {
    return countingPWM(pwm.discrete(discretization)).weak_threshold(pvalue).downscale(discretization);
  }

  @Override
  public ThresholdInfo strongThresholdByPvalue(double pvalue) throws HashOverflowException {
    return countingPWM(pwm.discrete(discretization)).strong_threshold(pvalue).downscale(discretization);
  }

  @Override
  public ThresholdInfo thresholdByPvalue(double pvalue, BoundaryType boundaryType) throws HashOverflowException {
    return countingPWM(pwm.discrete(discretization)).threshold(pvalue, boundaryType).downscale(discretization);
  }

  @Override
  public ThresholdInfo[] weakThresholdsByPvalues(double[] pvalues) throws HashOverflowException {
    ThresholdInfo[] result = new ThresholdInfo[pvalues.length];
    for (int i = 0; i < pvalues.length; ++i) {
      result[i] = weakThresholdByPvalue(pvalues[i]);
    }
    return result;
  }

  @Override
  public ThresholdInfo[] strongThresholsdByPvalues(double[] pvalues) throws HashOverflowException {
    ThresholdInfo[] result = new ThresholdInfo[pvalues.length];
    for (int i = 0; i < pvalues.length; ++i) {
      result[i] = strongThresholdByPvalue(pvalues[i]);
    }
    return result;
  }

  @Override
  public ThresholdInfo[] thresholdsByPvalues(double[] pvalues, BoundaryType boundaryType) throws HashOverflowException {
    ThresholdInfo[] result = new ThresholdInfo[pvalues.length];
    for (int i = 0; i < pvalues.length; ++i) {
      result[i] = thresholdByPvalue(pvalues[i], boundaryType);
    }
    return result;
  }
}
