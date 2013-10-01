package ru.autosome.macroape;

import java.util.ArrayList;
import java.util.Collections;

public class FindPvalueBsearchParameters {
  public PWM pwm;
  public BackgroundModel background;
  public PvalueBsearchList bsearchList;
  public double[] thresholds;

  FindPvalueBsearchParameters() { }
  public FindPvalueBsearchParameters(PWM pwm, double[] thresholds, BackgroundModel background, PvalueBsearchList bsearchList) {
    this.pwm = pwm;
    this.thresholds = thresholds;
    this.background = background;
    this.bsearchList = bsearchList;
  }
}
