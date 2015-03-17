package ru.autosome.ape.calculation;

import ru.autosome.ape.calculation.findThreshold.CanFindThreshold;
import ru.autosome.ape.calculation.findThreshold.FindThresholdAPE;
import ru.autosome.ape.model.PvalueBsearchList;
import ru.autosome.ape.model.exception.HashOverflowException;
import ru.autosome.ape.model.progression.GeometricProgression;
import ru.autosome.commons.model.BoundaryType;
import ru.autosome.commons.model.Discretizer;
import ru.autosome.commons.motifModel.Discretable;
import ru.autosome.commons.motifModel.ScoreDistribution;

import java.util.ArrayList;
import java.util.List;

public class PrecalculateThresholdList<ModelType extends  Discretable<ModelType> & ScoreDistribution<BackgroundType>,
                                       BackgroundType> {
  // We expect not to have P-values less than 1e-15 in common case.
  // It's possible only for motifs of length 25 or more.
  // For SNPScan differences in such low P-values actually doesn't matter
  public static final double[] PVALUE_LIST = new GeometricProgression(1.0, 1E-15, 1.05).values();

  final double[] pvalues;
  final BoundaryType pvalue_boundary;

  final Discretizer discretizer;
  final BackgroundType background;
  final Integer max_hash_size;

  public PrecalculateThresholdList(double[] pvalues, Discretizer discretizer, BackgroundType background, BoundaryType pvalue_boundary, Integer max_hash_size) {
    this.pvalues = pvalues;
    this.discretizer = discretizer;
    this.background = background;
    this.pvalue_boundary = pvalue_boundary;
    this.max_hash_size = max_hash_size;
  }

  protected CanFindThreshold find_threshold_calculator(ModelType motif) {
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
}
