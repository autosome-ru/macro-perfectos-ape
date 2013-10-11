package ru.autosome.macroape.BioUML;


import ru.autosome.macroape.BackgroundModel;
import ru.autosome.macroape.PWM;

import java.util.ArrayList;

import static ru.autosome.macroape.Calculations.FindPvalueAPE.PvalueInfo;

public class FindPvalueAPE {
  public static class Parameters {
    public PWM pwm;
    public Double discretization;
    public BackgroundModel background;
    public Integer max_hash_size;
    double[] thresholds;

    public Parameters() { }
    public Parameters(PWM pwm, double[] thresholds, Double discretization, BackgroundModel background, Integer max_hash_size) {
      this.pwm = pwm;
      this.thresholds = thresholds;
      this.discretization = discretization;
      this.background = background;
      this.max_hash_size = max_hash_size;
    }
  }

  Parameters parameters;

  public FindPvalueAPE(Parameters parameters) {
    this.parameters = parameters;
  }

  ru.autosome.macroape.Calculations.FindPvalueAPE.Parameters calculation_parameters() {
    return new ru.autosome.macroape.Calculations.FindPvalueAPE.Parameters(parameters.pwm,
                                                                          parameters.discretization,
                                                                          parameters.background,
                                                                          parameters.max_hash_size);
  }
  ArrayList<PvalueInfo> launch() {
    return new ru.autosome.macroape.Calculations.FindPvalueAPE(calculation_parameters())
            .pvalues_by_thresholds(parameters.thresholds);
  }

}

