package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.*;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class PrecalculateThresholdList {
  abstract static public class Progression {
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

  public static class GeometricProgression extends Progression {
    double from;
    double to;
    double step;

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

  public static class ArithmeticProgression extends Progression {
    double from;
    double to;
    double step;

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

  public static final double[] PVALUE_LIST = new GeometricProgression(1E-15, 0.5, 1.05).values();

  double discretization;
  BackgroundModel background;
  BoundaryType pvalue_boundary;
  Integer max_hash_size;
  double[] pvalues;
  public PrecalculateThresholdList(double[] pvalues, double discretization, BackgroundModel background, BoundaryType pvalue_boundary, Integer max_hash_size) {
    this.pvalues = pvalues;
    this.discretization = discretization;
    this.background = background;
    this.pvalue_boundary = pvalue_boundary;
    this.max_hash_size = max_hash_size;
  }

  private CanFindThreshold find_threshold_calculator(PWM pwm) {
    return new ru.autosome.perfectosape.calculations.FindThresholdAPE(pwm,
                                                                  background,
                                                                  discretization,
                                                                  pvalue_boundary,
                                                                  max_hash_size);
  }

  public PvalueBsearchList bsearch_list_for_pwm(PWM pwm) {
    ArrayList<PvalueBsearchList.ThresholdPvaluePair> pairs = new ArrayList<PvalueBsearchList.ThresholdPvaluePair>();
    for (CanFindThreshold.ThresholdInfo info : find_threshold_calculator(pwm).find_thresholds_by_pvalues(pvalues)) {
      pairs.add(new PvalueBsearchList.ThresholdPvaluePair(info));
    }
    return new PvalueBsearchList(pairs);
  }


}
