package ru.autosome.macroape;

import java.util.Collection;

public class ArrayExtensions {
  public static double max(double... array) throws IllegalArgumentException {
    if (array.length == 0) {
      throw new IllegalArgumentException("Can't calculate maximum of empty array");
    }
    double result = array[0];
    for (double pos : array) {
      result = Math.max(result, pos);
    }
    return result;
  }

  public static double min(double... array) throws IllegalArgumentException {
    if (array.length == 0) {
      throw new IllegalArgumentException("Can't calculate minimum of empty array");
    }
    double result = array[0];
    for (double pos : array) {
      result = Math.min(result, pos);
    }
    return result;
  }

  public static double sum(double... array) {
    double result = 0.0;
    for (double el : array) {
      result += el;
    }
    return result;
  }

  public static <T> T[] reverse(T[] array) {
    T[] result = array.clone();
    for (int i = 0; i < array.length; ++i) {
      result[i] = array[array.length - 1 - i];
    }
    return result;
  }

  public static double[] reverse(double[] array) {
    double[] result = array.clone();
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

  public static boolean contain(Iterable<String> list, String obj) {
    for (String el : list) {
      if (el.equals(obj)) return true;
    }
    return false;
  }

  public static Integer indexOf(double el, double[] list) {
    for (int i = 0; i < list.length; ++i) {
      if (list[i] == el) return i;
    }
    return null;
  }

  public static double[] toPrimitiveArray(Double[] wrappedArray) {
    double[] array = new double[wrappedArray.length];
    for (int i = 0; i < wrappedArray.length; i++)
      array[i] = wrappedArray[i];
    return array;
  }

  public static double[] toPrimitiveArray(Collection<Double> wrappedArray) {
    double[] array = new double[wrappedArray.size()];
    int i = 0;
    for(Double element : wrappedArray) {
      array[i] = element;
      i += 1;
    }
    return array;
  }
}
