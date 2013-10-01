package ru.autosome.macroape;

import java.util.ArrayList;

public class GeometricProgression extends Progression {
  double from;
  double to;
  double step ;
  public double[] values() {
    ArrayList<Double> results = new ArrayList<Double>();
    for (double x = from; x <= to; x *= step) {
      results.add(x);
    }
    return ArrayExtensions.toPrimitiveArray(results);
  }

  public GeometricProgression(double min, double to, double step) {
    this.from = min;
    this.to = to;
    this.step = step;
  }
}
