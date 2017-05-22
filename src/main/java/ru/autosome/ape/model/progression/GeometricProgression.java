package ru.autosome.ape.model.progression;

import java.util.ArrayList;
import java.util.List;

public class GeometricProgression extends Progression {
  final double from;
  final double to;
  final double step;

  @Override
  public List<Double> values() {
    ArrayList<Double> results = new ArrayList<>();
    if (to >= from) {
      for (double x = from; x <= to; x *= step) {
        results.add(x);
      }
    } else {
      for (double x = from; x >= to; x *= step) {
        results.add(x);
      }
    }
    return results;
  }

  public GeometricProgression(double from, double to, double step) {
    this.from = from;
    this.to = to;
    if (step <= 0) {
      throw new IllegalArgumentException("Step should be positive");
    }
    if (to >= from) {
      this.step = (step > 1.0) ? step : 1.0 / step;
    } else {
      this.step = (step < 1.0) ? step : 1.0 / step;
    }
  }
}
