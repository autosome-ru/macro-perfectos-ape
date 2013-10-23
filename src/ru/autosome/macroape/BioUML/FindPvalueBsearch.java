package ru.autosome.macroape.BioUML;

import ru.autosome.macroape.BackgroundModel;
import ru.autosome.macroape.Calculations.CanFindPvalue;
import ru.autosome.macroape.PWM;
import ru.autosome.macroape.PvalueBsearchList;

public class FindPvalueBsearch extends SingleTask<CanFindPvalue.PvalueInfo[]> {
  public static class Parameters {
    public PWM pwm;
    public BackgroundModel background;
    public PvalueBsearchList bsearchList;
    public double[] thresholds;

    public Parameters() { }
    public Parameters(PWM pwm, double[] thresholds, BackgroundModel background, PvalueBsearchList bsearchList) {
      this.pwm = pwm;
      this.thresholds = thresholds;
      this.background = background;
      this.bsearchList = bsearchList;
    }
  }

  Parameters parameters;
  public FindPvalueBsearch(Parameters parameters) {
    super();
    this.parameters = parameters;
  }

  public CanFindPvalue.PvalueInfo[] launchSingleTask() {
    return new ru.autosome.macroape.Calculations.FindPvalueBsearch(parameters.pwm,
                                                                   parameters.background,
                                                                   parameters.bsearchList)
            .pvalues_by_thresholds(parameters.thresholds);
  }

}
