package ru.autosome.commons.model;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.ceil;

public class Discretizer {
  public final Double discretization;
  // if discretization is null - it's not applied
  public Discretizer(Double discretization) {
    this.discretization = discretization;
  }

  public static Discretizer fromString(String s){
    return new Discretizer(Double.valueOf(s));
  }

  public double upscale(double value) {
    if (discretization == null) {
      return value;
    } else {
      return value * discretization;
    }
  }

  public List<Double> upscale(List<Double> values) {
    List<Double> result = new ArrayList<>();
    for (Double value: values) {
      result.add(upscale(value));
    }
    return result;
  }

  public double downscale(double value) {
    if (discretization == null) {
      return value;
    } else {
      return value / discretization;
    }
  }

  public double discrete(double value) {
    if (discretization == null) {
      return value;
    } else {
      return ceil(value * discretization);
    }
  }

  @Override
  public String toString() {
    return discretization.toString();
  }
}
