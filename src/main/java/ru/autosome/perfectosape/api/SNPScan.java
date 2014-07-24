package ru.autosome.perfectosape.api;

import ru.autosome.ape.calculation.findPvalue.CanFindPvalue;
import ru.autosome.commons.api.Task;
import ru.autosome.commons.motifModel.mono.PWM;
import ru.autosome.perfectosape.calculation.SingleSNPScan;
import ru.autosome.perfectosape.model.SequenceWithSNP;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SNPScan extends Task< Map<PWM, Map<SequenceWithSNP, SingleSNPScan.RegionAffinityInfos>> > {
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
  public SNPScan(Parameters parameters) {
    super();
    this.parameters = parameters;
  }

  SingleSNPScan calculator(PWM pwm, SequenceWithSNP sequenceWithSNP, CanFindPvalue pvalueCalculator) {
    return new SingleSNPScan(pwm,
                                                      sequenceWithSNP,
                                                      pvalueCalculator);
  }
  public Map<PWM, Map<SequenceWithSNP, SingleSNPScan.RegionAffinityInfos>> call() {
    Map<PWM, Map<SequenceWithSNP, SingleSNPScan.RegionAffinityInfos>> result;
    setStatus(Status.RUNNING);
    try {
      result = new HashMap<PWM, Map<SequenceWithSNP, SingleSNPScan.RegionAffinityInfos>>();
      for (PWM pwm: parameters.pvalueCalculator.keySet()) {
        CanFindPvalue pvalueCalculator = parameters.pvalueCalculator.get(pwm);

        Map<SequenceWithSNP,SingleSNPScan.RegionAffinityInfos> result_part = new HashMap<SequenceWithSNP, SingleSNPScan.RegionAffinityInfos>();

        for (SequenceWithSNP sequenceWithSNP: parameters.sequencesWithSNP) {
          if (interrupted()) {
            result.put(pwm, result_part);
            return result; // Return partial results
          }

          if (sequenceWithSNP.length() >= pwm.length()) {
            result_part.put(sequenceWithSNP,
                            calculator(pwm, sequenceWithSNP, pvalueCalculator).affinityInfos());
          } else {
            message("Can't scan sequence '" + sequenceWithSNP + "' (length " + sequenceWithSNP.length() + ") with motif of length " + pwm.length());
          }

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
