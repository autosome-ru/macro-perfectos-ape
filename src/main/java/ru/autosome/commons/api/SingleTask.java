package ru.autosome.commons.api;

import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.commons.api.Task;

abstract public class SingleTask<ResultType> extends Task<ResultType> {
  @Override
  public Integer getTotalTicks() {
    return 1;
  }

  public abstract ResultType launchSingleTask() throws HashOverflowException;

  @Override
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
