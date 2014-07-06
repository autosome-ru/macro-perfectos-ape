package ru.autosome.ape.model.exception;

public class HashOverflowException extends Exception {
  public HashOverflowException() {super();}
  public HashOverflowException(String message) {super(message);}
  public HashOverflowException(String message,
                   Throwable cause) {super(message,cause);}
  public HashOverflowException(Throwable cause) {super(cause);}
}
