package ru.autosome.perfectosape.api;


import ru.autosome.perfectosape.calculations.HashOverflowException;
import ru.autosome.perfectosape.motifModels.PWM;
import ru.autosome.perfectosape.SequenceWithSNP;
import ru.autosome.perfectosape.calculations.findPvalue.CanFindPvalue;
import ru.autosome.perfectosape.calculations.SNPScan.RegionAffinityInfos;

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

  ru.autosome.perfectosape.calculations.SNPScan calculator() {
    return new ru.autosome.perfectosape.calculations.SNPScan(parameters.pwm,
                                                         parameters.sequenceWithSNP,
                                                         parameters.pvalueCalculator);
  }
  @Override
  public RegionAffinityInfos launchSingleTask() throws HashOverflowException {
    return calculator().affinityInfos();
  }
}
