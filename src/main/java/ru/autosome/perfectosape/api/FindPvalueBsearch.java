package ru.autosome.perfectosape.api;

import ru.autosome.perfectosape.PvalueBsearchList;
import ru.autosome.perfectosape.calculations.findPvalue.CanFindPvalue;
import ru.autosome.perfectosape.motifModels.PWM;

public class FindPvalueBsearch extends SingleTask<CanFindPvalue.PvalueInfo[]> {
  public static class Parameters {
    public PWM pwm;
    public PvalueBsearchList bsearchList;
    public double[] thresholds;

    public Parameters() { }
    public Parameters(PWM pwm, double[] thresholds, PvalueBsearchList bsearchList) {
      this.pwm = pwm;
      this.thresholds = thresholds;
      this.bsearchList = bsearchList;
    }
  }

  Parameters parameters;
  public FindPvalueBsearch(Parameters parameters) {
    super();
    this.parameters = parameters;
  }

  @Override
  public CanFindPvalue.PvalueInfo[] launchSingleTask() {
    return new ru.autosome.perfectosape.calculations.findPvalue.FindPvalueBsearch(parameters.bsearchList)
            .pvaluesByThresholds(parameters.thresholds);
  }

}
