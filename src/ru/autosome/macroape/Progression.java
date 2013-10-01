package ru.autosome.macroape;


import java.util.StringTokenizer;

abstract public class Progression {
  public abstract double[] values();

  public static Progression fromString(String s) {
    StringTokenizer parser = new StringTokenizer(s);
    double min = Double.valueOf(parser.nextToken(","));
    double max = Double.valueOf(parser.nextToken(","));
    double step = Double.valueOf(parser.nextToken(","));
    String progression_method = parser.nextToken();

    if (progression_method.equals("mul")) {
      return new GeometricProgression(min, max, step);
    } else if (progression_method.equals("add")) {
      return new ArithmeticProgression(min, max, step);
    } else {
      throw new IllegalArgumentException("Progression method for pvalue-list is either add or mul, but you specified " + progression_method);
    }
  }
}
