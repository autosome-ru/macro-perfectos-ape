package ru.autosome.macroape.BioUML.Calculations;

import ru.autosome.macroape.*;

import java.util.ArrayList;

public class PrecalculateThresholdList {
  public static class Parameters {
    private double discretization;
    private BackgroundModel background;
    private String pvalue_boundary;
    private int max_hash_size;
    private double[] pvalues;
    PWM pwm;
    public Parameters() {}
    public Parameters(PWM pwm, double[] pvalues, double discretization, BackgroundModel background, String pvalue_boundary, int max_hash_size) {
      this.pwm = pwm;
      this.pvalues = pvalues;
      this.discretization = discretization;
      this.background = background;
      this.pvalue_boundary = pvalue_boundary;
      this.max_hash_size = max_hash_size;
    }
  }
  Parameters parameters;
  public PrecalculateThresholdList(Parameters parameters) {
    this.parameters = parameters;
  }

  ru.autosome.macroape.Calculations.PrecalculateThresholdList.Parameters calculator_parameters() {
    return new ru.autosome.macroape.Calculations.PrecalculateThresholdList.Parameters(parameters.pvalues,
                                                                                     parameters.discretization,
                                                                                     parameters.background,
                                                                                     parameters.pvalue_boundary,
                                                                                     parameters.max_hash_size);
  }

  ru.autosome.macroape.Calculations.PrecalculateThresholdList calculator() {
    return new ru.autosome.macroape.Calculations.PrecalculateThresholdList(calculator_parameters());
  }


  public PvalueBsearchList launch() {
    return new PvalueBsearchList(calculator().calculate_thresholds_for_pwm(parameters.pwm));
  }

}