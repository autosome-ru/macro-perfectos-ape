package ru.autosome.macroape.BioUML.Calculations;

import ru.autosome.macroape.BackgroundModel;
import ru.autosome.macroape.PWM;
import ru.autosome.macroape.PvalueInfo;
import ru.autosome.macroape.ThresholdInfo;

import java.util.ArrayList;

public class FindThresholdAPE {
  public static class Parameters {
    public BackgroundModel background;
    public Double discretization; // if discretization is null - it's not applied
    public String pvalue_boundary;
    public Integer max_hash_size; // if max_hash_size is null - it's not applied
    public PWM pwm;
    public double[] pvalues;

    public Parameters() { }
    public Parameters(PWM pwm, double[] pvalues, BackgroundModel background,
                      Double discretization, String pvalue_boundary, Integer max_hash_size) {
      this.pwm = pwm;
      this.pvalues = pvalues;
      this.background = background;
      this.discretization = discretization;
      this.pvalue_boundary = pvalue_boundary;
      this.max_hash_size = max_hash_size;
    }
  }

  Parameters parameters;
  FindThresholdAPE(Parameters parameters) {
    this.parameters = parameters;
  }

  ru.autosome.macroape.Calculations.FindThresholdAPE.Parameters calculation_parameters() {
    return new ru.autosome.macroape.Calculations.FindThresholdAPE.Parameters(parameters.pwm,
                                                                             parameters.background,
                                                                             parameters.discretization,
                                                                             parameters.pvalue_boundary,
                                                                             parameters.max_hash_size);
  }

  ArrayList<ThresholdInfo> launch() {
    return new ru.autosome.macroape.Calculations.FindThresholdAPE(calculation_parameters())
            .find_thresholds_by_pvalues(parameters.pvalues);
  }
}
