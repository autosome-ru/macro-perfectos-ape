package ru.autosome.perfectosape.api;

public abstract class Task <ResultType extends Object> {
  public static enum Status {
    INITIALIZED, RUNNING, SUCCESS, FAIL, INTERRUPTED
  }
  private Status status;
  private Integer currentTicks;
  public java.io.PrintStream outputStream;
  public boolean silent;

  protected Task() {
    status = Status.INITIALIZED;
    currentTicks = 0;
    outputStream = System.err;
    silent = false;
  }
  public abstract ResultType launch();

  public abstract Integer getTotalTicks();

  void tick() {
    synchronized (currentTicks) {
      currentTicks += 1;
    }
  }

  public double completionPercent() {
    return Math.floor((100 * currentTicks)/getTotalTicks());
  }

  public int getCurrentTicks() {
    synchronized (currentTicks) {
      return currentTicks;
    }
  }

  boolean interrupted() {
    synchronized (status) {
      return status == Status.INTERRUPTED;
    }
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
    if (!silent) {
      outputStream.println(msg);
    }
  }
}
