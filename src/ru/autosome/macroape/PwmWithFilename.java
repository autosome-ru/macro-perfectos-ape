package ru.autosome.macroape;

public class PwmWithFilename {
  public PWM pwm;
  public String filename;
  public CanFindPvalue pvalue_calculation;
  public PwmWithFilename(PWM pwm, String filename) {
    this.pwm = pwm;
    this.filename = filename;
    this.pvalue_calculation = null; // One should specify this after creation
  }
}
