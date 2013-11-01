package ru.autosome.perfectosape.api;

abstract public class SingleTask<ResultType extends Object> extends Task<ResultType> {
  public Integer getTotalTicks() {
    return 1;
  }

  abstract ResultType launchSingleTask();

  public ResultType call() {
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
