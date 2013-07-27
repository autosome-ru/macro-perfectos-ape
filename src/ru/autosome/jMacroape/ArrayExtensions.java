package ru.autosome.jMacroape;

import java.util.ArrayList;

public class ArrayExtensions {
  public static Double max(Double[] array) throws IllegalArgumentException {
    if (array.length == 0) {
      throw new IllegalArgumentException("Can't calculate maximum of empty array");
    }
    Double result = array[0];
    for (Double pos: array) {
      result = Math.max(result, pos);
    }
    return result;
  }
  public static Double min(Double[] array) throws IllegalArgumentException {
    if (array.length == 0) {
      throw new IllegalArgumentException("Can't calculate minimum of empty array");
    }
    Double result = array[0];
    for (Double pos: array) {
      result = Math.min(result, pos);
    }
    return result;
  }

  public static Double sum(Double[] array) {
    Double result = 0.0;
    for(Double el: array) {
      result += el;
    }
    return result;
  }

  public static <T> T[] reverse(T[] array){
    T result[] = array.clone();
    for (int i = 0; i < array.length; ++i) {
      result[i] = array[array.length - 1 - i];
    }
    return result;
  }

  public static Double[] partial_sums(Double array[], Double initial) {
    Double result[] = new Double[array.length];
    Double sums = initial;
    for (int i = 0; i < array.length; ++i) {
      sums += array[i];
      result[i] = sums;
    }
    return result;
  }
}
