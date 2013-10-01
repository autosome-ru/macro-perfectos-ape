package ru.autosome.macroape;

import java.util.ArrayList;

public class ArithmeticProgression extends Progression {
  double from;
  double to;
  double step ;
  public double[] values() {
    ArrayList<Double> results = new ArrayList<Double>();
    for (double x = from; x <= to; x += step) {
      results.add(x);
    }
    return ArrayExtensions.toPrimitiveArray(results);
  }

  ArithmeticProgression(double from, double to, double step) {
    this.from = from;
    this.to = to;
    this.step = step;
  }
}
