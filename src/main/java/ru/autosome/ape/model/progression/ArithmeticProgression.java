package ru.autosome.ape.model.progression;

import ru.autosome.commons.support.ArrayExtensions;

import java.util.ArrayList;

public class ArithmeticProgression extends Progression {
  final double from;
  final double to;
  final double step;

  @Override
  public double[] values() {
    ArrayList<Double> results = new ArrayList<Double>();
    if (to >= from) {
      for (double x = from; x <= to; x += step) {
        results.add(x);
      }
    } else {
      for (double x = from; x >= to; x += step) {
        results.add(x);
      }
    }
    return ArrayExtensions.toPrimitiveArray(results);
  }

  public ArithmeticProgression(double from, double to, double step) {
    this.from = from;
    this.to = to;
    if (to >= from) {
      this.step = (step > 0.0) ? step : -step;
    } else {
      this.step = (step < 0.0) ? step : -step;
    }
  }
}
