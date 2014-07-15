package ru.autosome.perfectosape.api;


import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.commons.api.SingleTask;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.perfectosape.calculation.SNPScan.RegionAffinityInfos;
import ru.autosome.perfectosape.model.SequenceWithSNP;

public class SNPScan extends SingleTask<RegionAffinityInfos> {
  static public class Parameters {
    public SequenceWithSNP sequenceWithSNP;
    public PWM pwm;
    public CanFindPvalue pvalueCalculator;
    public Parameters() { }
    public Parameters(SequenceWithSNP sequenceWithSNP, PWM pwm, CanFindPvalue pvalueCalculator) {
      this.sequenceWithSNP = sequenceWithSNP;
      this.pwm = pwm;
      this.pvalueCalculator = pvalueCalculator;
    }
  }

  Parameters parameters;
  public SNPScan(Parameters parameters) {
    super();
    this.parameters = parameters;
  }

  ru.autosome.perfectosape.calculation.SNPScan calculator() {
    return new ru.autosome.perfectosape.calculation.SNPScan(parameters.pwm,
                                                         parameters.sequenceWithSNP,
                                                         parameters.pvalueCalculator);
  }
  @Override
  public RegionAffinityInfos launchSingleTask() throws HashOverflowException {
    return calculator().affinityInfos();
  }
}
