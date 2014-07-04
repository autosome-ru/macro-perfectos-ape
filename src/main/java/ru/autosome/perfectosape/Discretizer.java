package ru.autosome.perfectosape;

import static java.lang.Math.ceil;

// if discretization is null, no discretization applied
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

  public double downscale(double value) {
    if (discretization == null) {
      return value;
    } else {
      return value / discretization;
    }
  }

  public double[] upscale(double[] values) {
    double[] result = new double[values.length];
    for (int i = 0; i < values.length; ++i) {
      result[i] = upscale(values[i]);
    }
    return result;
  }

  public double[] downscale(double[] values) {
    double[] result = new double[values.length];
    for (int i = 0; i < values.length; ++i) {
      result[i] = downscale(values[i]);
    }
    return result;
  }

  public double discrete(double value) {
    if (discretization == null) {
      return value;
    } else {
      return ceil(value * discretization);
    }
  }

  public double[][] discrete(double[][] matrix) {
    if (discretization == null) {
      return matrix;
    } else {
      double[][] mat_result = new double[matrix.length][];
      for (int i = 0; i < matrix.length; ++i) {
        mat_result[i] = new double[matrix[i].length];
        for (int j = 0; j < matrix[i].length; ++j) {
          mat_result[i][j] = ceil(matrix[i][j] * discretization);
        }
      }
      return mat_result;
    }
  }

  @Override
  public String toString() {
    return discretization.toString();
  }
}
