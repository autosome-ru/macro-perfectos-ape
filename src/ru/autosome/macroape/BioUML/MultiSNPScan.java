package ru.autosome.macroape.BioUML;

import ru.autosome.macroape.Calculations.CanFindPvalue;
import ru.autosome.macroape.Calculations.SNPScan;
import ru.autosome.macroape.PWM;
import ru.autosome.macroape.SequenceWithSNP;

import java.util.HashMap;
import java.util.Map;

public class MultiSNPScan {
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
  private Status status;
  private Integer currentTicks;
  public MultiSNPScan(Parameters parameters) {
    this.parameters = parameters;
    status = Status.INITIALIZED;
    currentTicks = 0;
  }

  ru.autosome.macroape.Calculations.SNPScan calculator(PWM pwm, SequenceWithSNP sequenceWithSNP, CanFindPvalue pvalueCalculator) {
    return new ru.autosome.macroape.Calculations.SNPScan(pwm,
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

  public Status getStatus() {
    synchronized (status) {
      return status;
    }
  }

  public boolean setStatus(Status newStatus) {
    synchronized (status) {
      if (status != Status.INTERRUPTED && status != Status.FAIL && status != Status.SUCCESS) {
        status = newStatus;
        return true;
      } else {
        return false;
      }
    }
  }

  void message(String msg) {
    System.out.println(msg);
  }

  void tick() {
    synchronized (currentTicks) {
      currentTicks += 1;
      double done = Math.floor((100 * currentTicks)/getTotalTicks());
      this.message("overall: " + done + "% complete");
    }
  }

  int getCurrentTicks() {
    synchronized (currentTicks) {
      return currentTicks;
    }
  }

  boolean interrupted() {
    synchronized (status) {
      return status == Status.INTERRUPTED;
    }
  }

  public static enum Status {
    INITIALIZED, RUNNING, SUCCESS, FAIL, INTERRUPTED
  }
}
