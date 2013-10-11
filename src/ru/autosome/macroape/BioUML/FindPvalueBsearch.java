package ru.autosome.macroape.BioUML;

import ru.autosome.macroape.BackgroundModel;
import ru.autosome.macroape.CanFindPvalue;
import ru.autosome.macroape.PWM;
import ru.autosome.macroape.PvalueBsearchList;

import java.util.ArrayList;

public class FindPvalueBsearch {
  public static class Parameters {
    public PWM pwm;
    public BackgroundModel background;
    public PvalueBsearchList bsearchList;
    public double[] thresholds;

    Parameters() { }
    public Parameters(PWM pwm, double[] thresholds, BackgroundModel background, PvalueBsearchList bsearchList) {
      this.pwm = pwm;
      this.thresholds = thresholds;
      this.background = background;
      this.bsearchList = bsearchList;
    }
  }

  Parameters parameters;
  public FindPvalueBsearch(Parameters parameters) {
    this.parameters = parameters;
  }

  ru.autosome.macroape.Calculations.FindPvalueBsearch.Parameters calculation_parameters() {
    return new ru.autosome.macroape.Calculations.FindPvalueBsearch.Parameters(parameters.pwm,
                                                                              parameters.background,
                                                                              parameters.bsearchList);
  }

  ArrayList<CanFindPvalue.PvalueInfo> launch() {
    return new ru.autosome.macroape.Calculations.FindPvalueBsearch(calculation_parameters())
            .pvalues_by_thresholds(parameters.thresholds);
  }

}
