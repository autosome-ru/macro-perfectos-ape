package ru.autosome.macroape;

public class FindPvalueAPEParameters {
  public PWM pwm;
  public Double discretization;
  public BackgroundModel background;
  public Integer max_hash_size;
  double[] thresholds;

  public FindPvalueAPEParameters() { }
  public FindPvalueAPEParameters(PWM pwm, double[] thresholds, Double discretization, BackgroundModel background, Integer max_hash_size) {
    this.pwm = pwm;
    this.thresholds = thresholds;
    this.discretization = discretization;
    this.background = background;
    this.max_hash_size = max_hash_size;
  }
}
