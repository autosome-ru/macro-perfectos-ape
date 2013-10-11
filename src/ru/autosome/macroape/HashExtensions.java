package ru.autosome.macroape;

import java.util.Map;

public class HashExtensions {
  public static double sum_values(Map<Double, Double> hsh) {
    double result = 0;
    for (Map.Entry<Double, Double> entry : hsh.entrySet()) {
      result += entry.getValue();
    }
    return result;
  }

}
