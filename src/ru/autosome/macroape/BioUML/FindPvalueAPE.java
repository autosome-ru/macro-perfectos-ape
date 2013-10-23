package ru.autosome.macroape.BioUML;


import ru.autosome.macroape.BackgroundModel;
import ru.autosome.macroape.PWM;

import static ru.autosome.macroape.Calculations.FindPvalueAPE.PvalueInfo;

public class FindPvalueAPE {
  public static class Parameters {
    public PWM pwm;
    public Double discretization;
    public BackgroundModel background;
    public Integer max_hash_size;
    double[] thresholds;

    public Parameters() { }
    public Parameters(PWM pwm, double[] thresholds, Double discretization, BackgroundModel background, Integer max_hash_size) {
      this.pwm = pwm;
      this.thresholds = thresholds;
      this.discretization = discretization;
      this.background = background;
      this.max_hash_size = max_hash_size;
    }
  }

  Parameters parameters;
  private Status status;
  private Integer currentTicks;

  public FindPvalueAPE(Parameters parameters) {
    this.parameters = parameters;
    status = Status.INITIALIZED;
    currentTicks = 0;
  }

  public PvalueInfo[] launch() {
    ru.autosome.macroape.Calculations.FindPvalueAPE calculator =
     new ru.autosome.macroape.Calculations.FindPvalueAPE(parameters.pwm,
                                                         parameters.discretization,
                                                         parameters.background,
                                                         parameters.max_hash_size);
    PvalueInfo[] result;
    setStatus(Status.RUNNING);
    try {
      result = calculator.pvalues_by_thresholds(parameters.thresholds);
      tick();
    } catch (Exception err) {
      setStatus(Status.FAIL);
      return null;
    }
    setStatus(Status.SUCCESS);
    return result;
  }

  Integer getTotalTicks() {
    return 1;
  }

  public Status getStatus() {
    synchronized (status) {
      return status;
    }
  }

  public boolean setStatus(Status newStatus) {
    synchronized (status) {
      if (status != Status.INTERRUPTED) {
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

  public static enum Status {
    INITIALIZED, RUNNING, SUCCESS, FAIL, INTERRUPTED
  }

}

