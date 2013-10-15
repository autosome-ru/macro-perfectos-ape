package ru.autosome.macroape.BioUML;


import ru.autosome.macroape.Calculations.CanFindPvalue;
import ru.autosome.macroape.PWM;
import ru.autosome.macroape.SequenceWithSNP;

public class SNPScan {
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
    this.parameters = parameters;
  }

  ru.autosome.macroape.Calculations.SNPScan calculator() {
    return new ru.autosome.macroape.Calculations.SNPScan(parameters.pwm,
                                                         parameters.sequenceWithSNP,
                                                         parameters.pvalueCalculator);
  }
  public ru.autosome.macroape.Calculations.SNPScan.RegionAffinityInfos launch() {
    return calculator().affinityInfos();
  }
}
