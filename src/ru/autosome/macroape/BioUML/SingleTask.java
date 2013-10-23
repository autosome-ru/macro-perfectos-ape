package ru.autosome.macroape.BioUML;

abstract public class SingleTask<ResultType extends Object> extends Task<ResultType> {
  Integer getTotalTicks() {
    return 1;
  }

  abstract ResultType launchSingleTask();

  public ResultType launch() {
    ResultType result;
    setStatus(Status.RUNNING);
    try {
      result = launchSingleTask();
      tick();
    } catch (Exception err) {
      setStatus(Status.FAIL);
      return null;
    }
    setStatus(Status.SUCCESS);
    return result;
  }
}
