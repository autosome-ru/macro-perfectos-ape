package ru.autosome.jMacroape;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: MSI
 * Date: 7/25/13
 * Time: 6:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class ArrayExtensions {
  public static double max(double[] array) throws IllegalArgumentException {
    if (array.length == 0) {
      throw new IllegalArgumentException("Can't calculate maximum of empty array");
    }
    double result = array[0];
    for (double pos: array) {
      result = Math.max(result, pos);
    }
    return result;
  }
  public static double min(double[] array) throws IllegalArgumentException {
    if (array.length == 0) {
      throw new IllegalArgumentException("Can't calculate minimum of empty array");
    }
    double result = array[0];
    for (double pos: array) {
      result = Math.min(result, pos);
    }
    return result;
  }

  public static double sum(double[] array) {
    double result = 0;
    for(double el: array) {
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

  public static double[] partial_sums(double array[], double initial) {
    double result[] = new double[array.length];
    double sums = initial;
    for (int i = 0; i < array.length; ++i) {
      sums += array[i];
      result[i] = sums;
    }
    return result;
  }
}
