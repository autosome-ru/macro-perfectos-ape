package ru.autosome.perfectosape.calculations;

import ru.autosome.perfectosape.ArrayExtensions;
import ru.autosome.perfectosape.BoundaryType;
import ru.autosome.perfectosape.Discretizer;
import ru.autosome.perfectosape.PvalueBsearchList;
import ru.autosome.perfectosape.backgroundModels.BackgroundModel;
import ru.autosome.perfectosape.backgroundModels.GeneralizedBackgroundModel;
import ru.autosome.perfectosape.calculations.findThreshold.CanFindThreshold;
import ru.autosome.perfectosape.calculations.findThreshold.FindThresholdAPE;
import ru.autosome.perfectosape.motifModels.Discretable;
import ru.autosome.perfectosape.motifModels.PWM;
import ru.autosome.perfectosape.motifModels.ScoreDistribution;
import ru.autosome.perfectosape.motifModels.ScoringModel;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class PrecalculateThresholdList<ModelType extends ScoringModel & Discretable<ModelType> & ScoreDistribution<BackgroundType>,
                                                  BackgroundType extends GeneralizedBackgroundModel> {
  // We expect not to have P-values less than 1e-15 in common case.
  // It's possible only for motifs of length 25 or more.
  // For SNPScan differences in such low P-values actually doesn't matter
  public static final double[] PVALUE_LIST = new GeometricProgression(1.0, 1E-15, 1.05).values();

  private final double[] pvalues;
  private final BoundaryType pvalue_boundary;

  private final Discretizer discretizer;
  private final BackgroundType background;
  private final Integer max_hash_size;

  public PrecalculateThresholdList(double[] pvalues, Discretizer discretizer, BackgroundType background, BoundaryType pvalue_boundary, Integer max_hash_size) {
    this.pvalues = pvalues;
    this.discretizer = discretizer;
    this.background = background;
    this.pvalue_boundary = pvalue_boundary;
    this.max_hash_size = max_hash_size;
  }

  CanFindThreshold find_threshold_calculator(ModelType motif) {
    return new FindThresholdAPE<ModelType, BackgroundType>(motif, background, discretizer, max_hash_size);
  }

  public PvalueBsearchList bsearch_list_for_pwm(ModelType motif) throws HashOverflowException {
    CanFindThreshold.ThresholdInfo[] infos = find_threshold_calculator(motif).thresholdsByPvalues(pvalues, pvalue_boundary);

    List<PvalueBsearchList.ThresholdPvaluePair> pairs = new ArrayList<PvalueBsearchList.ThresholdPvaluePair>();
    for (CanFindThreshold.ThresholdInfo info: infos) {
      pairs.add(new PvalueBsearchList.ThresholdPvaluePair(info));
    }

    return new PvalueBsearchList(pairs);
  }

  abstract static public class Progression {
    public abstract double[] values();

    public static Progression fromString(String s) {
      StringTokenizer parser = new StringTokenizer(s);
      double from = Double.valueOf(parser.nextToken(","));
      double to = Double.valueOf(parser.nextToken(","));
      double step = Double.valueOf(parser.nextToken(","));
      String progression_method = parser.nextToken();

      if (progression_method.equals("mul")) {
        return new GeometricProgression(from, to, step);
      } else if (progression_method.equals("add")) {
        return new ArithmeticProgression(from, to, step);
      } else {
        throw new IllegalArgumentException("Progression method for pvalue-list is either add or mul, but you specified " + progression_method);
      }
    }
  }

  public static class GeometricProgression extends Progression {
    final double from;
    final double to;
    final double step;

    @Override
    public double[] values() {
      ArrayList<Double> results = new ArrayList<Double>();
      if (to >= from) {
        for (double x = from; x <= to; x *= step) {
          results.add(x);
        }
      } else {
        for (double x = from; x >= to; x *= step) {
          results.add(x);
        }
      }
      return ArrayExtensions.toPrimitiveArray(results);
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

  public static class ArithmeticProgression extends Progression {
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

    ArithmeticProgression(double from, double to, double step) {
      this.from = from;
      this.to = to;
      if (to >= from) {
        this.step = (step > 0.0) ? step : -step;
      } else {
        this.step = (step < 0.0) ? step : -step;
      }
    }
  }

}
