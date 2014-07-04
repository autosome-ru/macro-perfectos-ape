package ru.autosome.perfectosape.api;

import java.util.EventListener;
import java.util.concurrent.Callable;

public abstract class Task <ResultType> implements Callable {
  public static enum Status {
    INITIALIZED, RUNNING, SUCCESS, FAIL, INTERRUPTED
  }
  public static enum Event {
    TICK, STATUS_CHANGED
  }
  interface Listener extends EventListener {
    void eventOccured(Task with_task, Event event);
  }
  private Status status;
  private Integer currentTicks;
  private java.io.PrintStream outputStream;
  private boolean silent;
  private Listener listener;
  private final Object lock;

  public void setEventLister(Listener listener) {
    this.listener = listener;
  }

  Task() {
    lock = new Object();
    status = Status.INITIALIZED;
    currentTicks = 0;
    outputStream = System.err;
    silent = false;
  }

  protected abstract Integer getTotalTicks();

  void tick() {
    synchronized (lock) {
      currentTicks += 1;
      if (listener != null) {
        listener.eventOccured(this, Event.TICK);
      }
    }
  }

  public double completionPercent() {
    return Math.floor((100 * currentTicks)/getTotalTicks());
  }

  public int getCurrentTicks() {
    synchronized (lock) {
      return currentTicks;
    }
  }

  boolean interrupted() {
    synchronized (lock) {
      return status == Status.INTERRUPTED;
    }
  }

  public Status getStatus() {
    synchronized (lock) {
      return status;
    }
  }

  boolean setStatus(Status newStatus) {
    synchronized (lock) {
      if (status != Status.INTERRUPTED) {
        status = newStatus;
        if (listener != null) {
          listener.eventOccured(this, Event.STATUS_CHANGED);
        }
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
