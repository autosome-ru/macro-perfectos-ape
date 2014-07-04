package ru.autosome.perfectosape;

public class Discretizer {
  public final Double discretization;

  public Discretizer(Double discretization) {
    this.discretization = discretization;
  }

  public double upscale(double value) {
    if (discretization == null) {
      return value;
    } else {
      return value * discretization;
    }
  }

  public double[] upscale(double[] values) {
    double[] result = new double[values.length];
    for (int i = 0; i < values.length; ++i) {
      result[i] = upscale(values[i]);
    }
    return result;
  }
}
