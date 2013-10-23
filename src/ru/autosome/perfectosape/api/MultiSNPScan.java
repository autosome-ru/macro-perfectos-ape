package ru.autosome.perfectosape.api;

import ru.autosome.perfectosape.calculations.CanFindPvalue;
import ru.autosome.perfectosape.calculations.SNPScan;
import ru.autosome.perfectosape.PWM;
import ru.autosome.perfectosape.SequenceWithSNP;

import java.util.HashMap;
import java.util.Map;

public class MultiSNPScan extends Task< Map<PWM, Map<SequenceWithSNP, SNPScan.RegionAffinityInfos>> > {
  static public class Parameters {
    public SequenceWithSNP[] sequencesWithSNP;
    Map<PWM, CanFindPvalue> pvalueCalculator;
    public Parameters() { }
    public Parameters(SequenceWithSNP[] sequencesWithSNP, Map<PWM, CanFindPvalue> pvalueCalculator) {
      this.sequencesWithSNP = sequencesWithSNP;
      this.pvalueCalculator = pvalueCalculator;
    }
  }

  Parameters parameters;
  public MultiSNPScan(Parameters parameters) {
    super();
    this.parameters = parameters;
  }

  ru.autosome.perfectosape.calculations.SNPScan calculator(PWM pwm, SequenceWithSNP sequenceWithSNP, CanFindPvalue pvalueCalculator) {
    return new ru.autosome.perfectosape.calculations.SNPScan(pwm,
                                                      sequenceWithSNP,
                                                      pvalueCalculator);
  }
  public Map<PWM, Map<SequenceWithSNP, SNPScan.RegionAffinityInfos>> launch() {
    Map<PWM, Map<SequenceWithSNP, SNPScan.RegionAffinityInfos>> result;
    setStatus(Status.RUNNING);
    try {
      result = new HashMap<PWM, Map<SequenceWithSNP, SNPScan.RegionAffinityInfos>>();
      for (PWM pwm: parameters.pvalueCalculator.keySet()) {
        CanFindPvalue pvalueCalculator = parameters.pvalueCalculator.get(pwm);

        Map<SequenceWithSNP,SNPScan.RegionAffinityInfos> result_part = new HashMap<SequenceWithSNP, SNPScan.RegionAffinityInfos>();

        for (SequenceWithSNP sequenceWithSNP: parameters.sequencesWithSNP) {
          if (interrupted()) {
            result.put(pwm, result_part);
            return result; // Return partial results
          }

          result_part.put(sequenceWithSNP,
                             calculator(pwm, sequenceWithSNP, pvalueCalculator).affinityInfos());
          tick();
        }

        result.put(pwm, result_part);
      }
    } catch (Exception err) {
      setStatus(Status.FAIL);
      return null;
    }
    setStatus(Status.SUCCESS);
    return result;
  }


  Integer getTotalTicks() {
    return parameters.sequencesWithSNP.length * parameters.pvalueCalculator.size();
  }

}