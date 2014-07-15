package ru.autosome.ape.api;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.model.PvalueBsearchList;
import ru.autosome.commons.api.SingleTask;
import ru.autosome.commons.motifModel.mono.PWM;

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
    return new ru.autosome.ape.calculation.findPvalue.FindPvalueBsearch(parameters.bsearchList)
            .pvaluesByThresholds(parameters.thresholds);
  }

}
