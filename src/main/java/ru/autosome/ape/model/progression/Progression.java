package ru.autosome.ape.model.progression;

import java.util.List;
import java.util.StringTokenizer;

abstract public class Progression {
  public abstract List<Double> values();

  public static Progression fromString(String s) {
    StringTokenizer parser = new StringTokenizer(s);
    double from = Double.valueOf(parser.nextToken(","));
    double to = Double.valueOf(parser.nextToken(","));
    double step = Double.valueOf(parser.nextToken(","));
    String progression_method = parser.nextToken().toLowerCase();

    if (progression_method.equals("mul")) {
      return new GeometricProgression(from, to, step);
    } else if (progression_method.equals("add")) {
      return new ArithmeticProgression(from, to, step);
    } else {
      throw new IllegalArgumentException("Progression method for pvalue-list is either add or mul, but you specified " + progression_method);
    }
  }
}
