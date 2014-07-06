package ru.autosome.perfectosape.api;

import ru.autosome.commons.api.Task;
import ru.autosome.perfectosape.model.SequenceWithSNP;
import ru.autosome.perfectosape.calculation.SNPScan;
import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.motifModel.mono.PWM;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiSNPScan extends Task< Map<PWM, Map<SequenceWithSNP, SNPScan.RegionAffinityInfos>> > {
  static public class Parameters {
    public List<SequenceWithSNP> sequencesWithSNP;
    Map<PWM, CanFindPvalue> pvalueCalculator;
    public Parameters() { }
    public Parameters(List<SequenceWithSNP> sequencesWithSNP, Map<PWM, CanFindPvalue> pvalueCalculator) {
      this.sequencesWithSNP = sequencesWithSNP;
      this.pvalueCalculator = pvalueCalculator;
    }
  }

  Parameters parameters;
  public MultiSNPScan(Parameters parameters) {
    super();
    this.parameters = parameters;
  }

  ru.autosome.perfectosape.calculation.SNPScan calculator(PWM pwm, SequenceWithSNP sequenceWithSNP, CanFindPvalue pvalueCalculator) {
    return new ru.autosome.perfectosape.calculation.SNPScan(pwm,
                                                      sequenceWithSNP,
                                                      pvalueCalculator);
  }
  public Map<PWM, Map<SequenceWithSNP, SNPScan.RegionAffinityInfos>> call() {
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


  @Override
  public Integer getTotalTicks() {
    return parameters.sequencesWithSNP.size() * parameters.pvalueCalculator.size();
  }

}
