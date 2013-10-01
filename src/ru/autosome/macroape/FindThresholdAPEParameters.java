package ru.autosome.macroape;

public class FindThresholdAPEParameters {
  public BackgroundModel background;
  public Double discretization; // if discretization is null - it's not applied
  public String pvalue_boundary;
  public Integer max_hash_size; // if max_hash_size is null - it's not applied
  public PWM pwm;
  public double[] pvalues;

  public FindThresholdAPEParameters() { }
  public FindThresholdAPEParameters(PWM pwm, double[] pvalues, BackgroundModel background,
                                    Double discretization, String pvalue_boundary, Integer max_hash_size) {
    this.pwm = pwm;
    this.pvalues = pvalues;
    this.background = background;
    this.discretization = discretization;
    this.pvalue_boundary = pvalue_boundary;
    this.max_hash_size = max_hash_size;
  }
}
