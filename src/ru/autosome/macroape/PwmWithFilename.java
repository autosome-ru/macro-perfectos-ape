package ru.autosome.macroape;

public class PwmWithFilename {
  public final PWM pwm;
  public final String filename;
  //public CanFindPvalue pvalue_calculation;
  public PvalueBsearchList bsearchList;

  public PwmWithFilename(PWM pwm, String filename) {
    this.pwm = pwm;
    this.filename = filename;
    //this.pvalue_calculation = null; // One should specify this after creation
    this.bsearchList = null;
  }
}
